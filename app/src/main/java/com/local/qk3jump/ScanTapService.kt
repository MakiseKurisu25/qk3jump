package com.local.qk3jump

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 替用户在胖乖生活 App 上点三下，把它从首页领到设备确认页。
 *
 * 三下分别是：
 *   1. 首页分类条上的「饮水」          → 进入「我的设备」
 *   2. 设备列表里目标那台的「立即使用」  → 进入设备确认页
 *   （点到确认页就停，确认页上的「立即使用」不归这里管，那是用户自己的决定）
 *
 * 四条锁，缺一条都不动手：
 *   ① 用户按过启动（JumpWindow.isArmed）
 *   ② 前台包名必须是 com.qiekj.user
 *   ③ 当前页面必须认得出来（首页 / 设备列表），不认识的页面一律不动手
 *   ④ 点设备时，必须在同一行里同时找到目标设备名 —— 找不到就跳过，宁可不点
 *
 * 为什么不能只靠事件：胖乖的页面渲染完之后就不再发 TYPE_WINDOW_CONTENT_CHANGED，
 * 所以必须有定时轮询兜底（每 600ms 一次），否则会永远卡在「等事件」上。
 */
class ScanTapService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())

    @Volatile private var polling = false

    /** 上一次点「饮水」的时间，用来做冷却，避免同一动作连点。 */
    @Volatile private var lastCategoryTapAt = 0L

    /** 上一次点「立即使用」的时间。 */
    @Volatile private var lastUseTapAt = 0L

    /** 已经打过一次的提示，避免每秒刷屏。 */
    private val noted = HashSet<String>()

    /**
     * MIUI 的跳转确认框只点一次。
     * 用令牌绑定到具体某一轮待命，新一轮（armToken 变了）就会自动失效，
     * 不需要谁去主动重置它。
     */
    @Volatile private var jumpConfirmToken = 0L

    /**
     * 第一次认出「设备确认页」的时刻（elapsedRealtime）。
     * 自动解锁要「等到页面渲染稳了再点」，所以需要一个起点。
     * 每轮待命换了（armToken 变了）就清零，免得上一轮的计时漏到下一轮。
     */
    @Volatile private var confirmSeenAt = 0L

    /**
     * 会员页「放弃优惠」上一次点下去的时刻与已试次数。
     *
     * 为什么需要这个：`performAction(ACTION_CLICK)` 返回 true 只代表事件被受理了，
     * 不代表页面真的跳了。实测出现过一次「点了、返回 true、页面原地不动」——
     * 那是 WebView 还没把点击接到 JS 上。如果不重试，整条链路会在会员页上
     * 干等到待命窗口超时。所以点完记时间戳，隔 1.5 秒页面还没走就再点一次，
     * 总共给 3 次机会。
     */
    @Volatile private var declineClickedAt = 0L
    @Volatile private var declineAttempts = 0

    /** 确认页按钮同理：点了没跳也要重试，否则同样会干等到超时。 */
    @Volatile private var confirmClickedAt = 0L
    @Volatile private var confirmAttempts = 0

    @Volatile private var confirmSeenToken = 0L

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (!polling) return
            try {
                checkOnce()
            } catch (t: Throwable) {
                QLog.e("Tap", "轮询里出错", t)
            }
            if (polling) handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        QLog.i("Tap", "无障碍服务已连接（canPerformGestures 见配置）")
        Prefs.setA11yConnected(this, true)

        // 重装/重新授权时，胖乖那边可能早就渲染完了，一条事件都不会再发。
        // 如果只等事件，这时的待命会永远空转 —— 所以在绑定时自己先催一次。
        if (JumpWindow.isArmed()) {
            QLog.i("Tap", "绑定后发现仍在待命窗口内，直接启动轮询（不依赖事件）")
            startPolling()
            checkOnce()
        }
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        QLog.i("Tap", "无障碍服务已解绑")
        Prefs.setA11yConnected(this, false)
        stopPolling()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        stopPolling()
        Prefs.setA11yConnected(this, false)
        super.onDestroy()
    }

    /**
     * 系统要求实现。会被调用的场景：用户主动关掉服务、系统回收、
     * 或者本服务被另一个无障碍服务抢占。无论哪种，都意味着我们不该再动手了。
     */
    override fun onInterrupt() {
        QLog.i("Tap", "无障碍服务被中断，停止轮询")
        stopPolling()
        JumpWindow.disarm("无障碍服务被中断")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> Unit
            else -> return
        }
        if (!JumpWindow.isArmed()) return
        // 调试模式也要轮询 —— 它虽然不动手，但需要持续观察页面，
        // 好把「认出了哪一页、打算点哪个控件」打印出来。轮询本身不产生任何动作。
        startPolling()
        checkOnce()
    }

    private fun startPolling() {
        if (polling) return
        polling = true
        QLog.i("Tap", "开始轮询页面状态（每 ${POLL_INTERVAL_MS}ms 一次）")
        handler.post(pollRunnable)
    }

    private fun stopPolling() {
        if (!polling) return
        polling = false
        handler.removeCallbacks(pollRunnable)
        QLog.i("Tap", "停止轮询")
    }

    /** 页面识别的结果，一次遍历同时判断四件事，省得反复遍历大页面。 */
    private class PageScan {
        var onHome = false
        var onDeviceList = false
        var onConfirm = false
        var onMemberOffer = false
        var onUnlocked = false
        var deviceRows: List<DeviceRow> = emptyList()

        /** 确认页底部那个「领优惠券使用 / 立即使用」按钮。 */
        var confirmUseButton: AccessibilityNodeInfo? = null

        /** 设备列表右下角悬浮广告的关闭按钮（有就点掉，没有就算了）。 */
        var floatAdClose: AccessibilityNodeInfo? = null
        var nodeCount = 0
    }

    private class DeviceRow(val shownName: String, val useButton: AccessibilityNodeInfo)

    private fun checkOnce() {
        if (!JumpWindow.isArmed()) {
            stopPolling()
            return
        }

        // 每轮待命开始时把「确认页第一次被认出的时刻」清掉。
        // 用令牌比对，而不是让 JumpActivity 反向调用服务 —— 服务可能不在一进程、
        // 也没启动，用令牌最稳，而且天然对齐 arm() 的生命周期。
        val token = JumpWindow.armToken()
        if (token != confirmSeenToken) {
            confirmSeenToken = token
            confirmSeenAt = 0L
            declineClickedAt = 0L
            declineAttempts = 0
            confirmClickedAt = 0L
            confirmAttempts = 0
            // 新一轮待命要把「这句话已经打过」的记录也清掉，
            // 否则第二次使用时整条链路会一句日志都不出，看起来像没跑。
            noted.clear()
        }

        val root = rootInActiveWindow ?: run {
            note("拿不到当前窗口内容，等下一轮")
            return
        }
        try {
            val pkg = root.packageName?.toString()

            // MIUI 的「应用跳转确认」框会插在中间，包名不是胖乖，
            // 但它恰恰是拦着我们去找胖乖的那道门。先处理它，再谈别的。
            if (pkg == MIUI_SECURITY_PKG) {
                handleJumpConfirm(root)
                return
            }

            if (pkg != Qk.PKG) {
                note("前台不是胖乖（当前=$pkg），不动手")
                return
            }
            val scan = scanPage(root)

            // 判断顺序按「页面叠加的先后」倒着来：
            // 解锁页最靠上、也最该立刻停手；会员页弹在确认页之上；确认页又盖在列表之上。
            when {
                scan.onUnlocked -> onUnlockedPage(root)
                scan.onMemberOffer -> onMemberOfferPage(root)
                scan.onConfirm -> onConfirmPage(root, scan)
                scan.onDeviceList -> onDeviceListPage(scan)
                scan.onHome -> onHomePage()
                else -> note("当前页面既不是首页、设备列表、确认页，也不是会员页/解锁页，等下一轮")
            }
        } catch (t: Throwable) {
            QLog.e("Tap", "识别页面出错", t)
        } finally {
            @Suppress("DEPRECATION")
            runCatching { root.recycle() }
        }
    }

    /**
     * MIUI 的「应用跳转确认」框。
     *
     * 实测原文：
     *     启动应用
     *     胖乖直达 想要打开 胖乖生活，是否允许？
     *     [拒绝]  [始终允许]
     *
     * 这是 MIUI 的链式启动拦截，任何应用往别的应用跳都可能撞上。它不是我们
     * 的东西，但正好挡在去胖乖的路上，所以必须处理。
     *
     * 限定条件收得很死，宁可不动也不能替你点错：
     *   ① 只有「本应用 → 胖乖」这一个组合才点，别的跳转框一律绕开
     *   ② 文案里必须同时出现「胖乖直达」和「胖乖生活」
     *   ③ 点的是「始终允许」—— 点「拒绝」会让整条链路彻底走不通
     *   ④ 一次待命窗口内只点一次，避免弹框反复出现时连点
     */
    private fun handleJumpConfirm(root: AccessibilityNodeInfo) {
        if (jumpConfirmToken == JumpWindow.armToken()) {
            return
        }
        val texts = ArrayList<String>(16)
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0
        while (queue.isNotEmpty() && visited < MAX_NODES) {
            val n = queue.removeFirst()
            visited++
            n.text?.toString()?.let { if (it.isNotBlank()) texts.add(it) }
            for (i in 0 until n.childCount) {
                n.getChild(i)?.let { queue.add(it) }
            }
        }
        val joined = texts.joinToString("\u0001")

        val isOurJump = joined.contains("胖乖直达") && joined.contains("胖乖生活") &&
            (joined.contains("想要打开") || joined.contains("是否允许"))
        if (!isOurJump) {
            note("遇到别的应用跳转确认框，不碰")
            return
        }

        QLog.i("Tap", "遇到 MIUI 的跳转确认框：「胖乖直达 → 胖乖生活」")
        if (Prefs.dryRun(this)) {
            note("调试模式：本应点「始终允许」，未动手")
            return
        }

        val allow = findByText(root, "始终允许")
        if (allow == null) {
            note("跳转确认框里没找到「始终允许」")
            return
        }
        val ok = clickNode(allow)
        QLog.i("Tap", "点「始终允许」结果=$ok")
        if (ok) {
            jumpConfirmToken = JumpWindow.armToken()
            JumpWindow.noteStep("已允许跳转到胖乖")
        }
    }

    /** 首页：点分类条上的「饮水」。 */
    private fun onHomePage() {
        val now = System.currentTimeMillis()
        if (now - lastCategoryTapAt < CATEGORY_COOLDOWN_MS) return
        val target = Prefs.targetDevice(this)
        // 600ms 轮询下一次，这句话会刷满日志，所以走 note()（同一句话只打一次）。
        note("在胖乖首页，点「${Qk.TEXT_CATEGORY_WATER}」进入设备列表（目标设备=$target）")
        if (Prefs.dryRun(this)) {
            note("调试模式：本应点「${Qk.TEXT_CATEGORY_WATER}」，未动手")
            JumpWindow.noteStep("调试：停在首页，未点分类")
            return
        }
        val root = rootInActiveWindow ?: return
        try {
            val node = findByText(root, Qk.TEXT_CATEGORY_WATER)
            if (node == null) {
                note("首页没找到「${Qk.TEXT_CATEGORY_WATER}」这个分类")
                return
            }
            lastCategoryTapAt = now
            val ok = clickNode(node)
            QLog.i("Tap", "点「${Qk.TEXT_CATEGORY_WATER}」结果=$ok")
            JumpWindow.noteStep("已点「${Qk.TEXT_CATEGORY_WATER}」")
        } finally {
            @Suppress("DEPRECATION")
            runCatching { root.recycle() }
        }
    }

    /** 设备列表：找出目标那台，点它同一行的「立即使用」。 */
    private fun onDeviceListPage(scan: PageScan) {
        val target = Prefs.targetDevice(this)
        if (target.isBlank()) {
            note("还没设置目标设备，无法选择")
            Prefs.setLastResult(this, "未设置目标设备")
            JumpWindow.disarm("未设置目标设备")
            stopPolling()
            return
        }

        note("设备列表共 ${scan.deviceRows.size} 行：${scan.deviceRows.joinToString(" | ") { it.shownName }}")

        // 页面认出是设备列表、但一行都还没有 —— 这不是「目标不在列表里」，
        // 而是 recyclerView 的数据还没绑上。实测点「饮水」之后约 830ms 就会
        // 出现这个中间态：顶部「查看附近设备」已经画出来了，列表还空着。
        // 这时候如果按「找不到目标」退出待命，整个链路就断在半路上，
        // 所以必须等下一轮再看，而不是现在下结论。
        if (scan.deviceRows.isEmpty()) {
            note("设备列表已打开但还没渲染出行，等下一轮")
            return
        }

        // 两轮挑选：先找完全相等的（rank=2），一行都没有才退而用包含匹配（rank=1）。
        // 只做一次「取第一个 rank>0」会让目标 201 抢到「北3栋201」那一行，
        // 那样开的是别人的水 —— 这是这条路最危险的失败方式，必须两轮。
        val exact = scan.deviceRows.firstOrNull { Matcher.rank(target, it.shownName) == 2 }
        val hit = exact ?: scan.deviceRows.firstOrNull { Matcher.rank(target, it.shownName) == 1 }

        if (hit == null) {
            // 每行都打一次比对过程，方便排查「为什么没匹配上」。
            scan.deviceRows.forEach { row ->
                QLog.i("Match", "不匹配：${Matcher.describe(target, row.shownName)}  → rank=${Matcher.rank(target, row.shownName)}")
            }
            note("设备列表里没有「$target」这一台")
            Prefs.setLastResult(this, "设备列表里找不到「$target」")
            JumpWindow.disarm("目标设备不在列表里")
            stopPolling()
            return
        }

        val strength = Matcher.rank(target, hit.shownName)
        JumpWindow.noteShown(hit.shownName)
        note("匹配成功（${if (strength == 2) "完全相等" else "包含关系"}）：${Matcher.describe(target, hit.shownName)}")

        val now = System.currentTimeMillis()

        // 先把右下角那个悬浮广告点掉。实测点它的关闭按钮只关广告、页面不动、焦点不变，
        // 而且它虽然压不到「立即使用」，但会在后面的轮次里持续挡视线 —— 顺手清掉。
        // 一轮只点一次（claimStage），避免关了又弹、弹了又关地死循环。
        if (passAdGate(scan.floatAdClose, "悬浮广告关闭按钮")) {
            if (Prefs.dryRun(this)) {
                note("调试模式：本应点掉右下角悬浮广告，未动手")
            } else if (JumpWindow.claimStage("floatAd")) {
                val adOk = clickNode(scan.floatAdClose!!)
                QLog.i("Tap", "点悬浮广告的关闭按钮结果=$adOk")
                if (!adOk) JumpWindow.resetStage("floatAd")
                // 关广告这一轮就不再点设备了，等页面重排完再点，免得点偏。
                return
            }
        }

        if (now - lastUseTapAt < USE_COOLDOWN_MS) return

        if (Prefs.dryRun(this)) {
            note("调试模式：本应点「${hit.shownName}」这一行的「立即使用」，未动手")
            JumpWindow.noteStep("调试：停在设备列表，目标=${hit.shownName}")
            return
        }

        if (!JumpWindow.claimStage(STAGE_DEVICE_ROW)) return
        lastUseTapAt = now
        val ok = clickNode(hit.useButton)
        QLog.i("Tap", "点「${hit.shownName}」的「立即使用」结果=$ok")
        if (ok) {
            JumpWindow.noteStep("已点「${hit.shownName}」的立即使用")
        } else {
            QLog.w("Tap", "点击没成功，把这一步还给待命窗口，下一轮再试")
            JumpWindow.resetStage(STAGE_DEVICE_ROW)
        }
    }

    /**
     * 设备确认页。
     *
     * 分两种走法：
     *   - 自动解锁关着（默认）：到这儿就停，把决定权留给用户。这是原来的行为。
     *   - 自动解锁开着：等满「解锁等待秒数」后点底部的「领优惠券使用」，
     *     一路点到「解锁成功」为止。
     *
     * 为什么要等：确认页是异步渲染的，刚跳过来时底部按钮可能还没绑上事件，
     * 点下去会静默无效。等满设定秒数再点，成功率高得多。
     *
     * 点下这个按钮**不会出水**，只会弹会员营销页或直接解锁；真正出水要靠饮水机
     * 机身上的「出水」实体键，App 管不到，所以这里不算"替你接水"。
     */
    private fun onConfirmPage(root: AccessibilityNodeInfo, scan: PageScan) {
        val machine = findMachineName(root)
        val suffix = if (machine.isNullOrBlank()) "" else "（$machine）"

        if (!Prefs.autoUnlock(this)) {
            note("已到设备确认页$suffix，任务完成（自动解锁未开启，不再往下点）")
            Prefs.setLastResult(this, "已到设备确认页$suffix")
            JumpWindow.disarm("已到设备确认页")
            stopPolling()
            return
        }

        val delayMs = Prefs.unlockDelayS(this) * 1000L
        val seenAt = confirmSeenAt
        if (seenAt == 0L) {
            confirmSeenAt = SystemClock.elapsedRealtime()
            QLog.i("Tap", "自动解锁已开启：已到设备确认页$suffix，等 ${delayMs}ms 后点「领优惠券使用」")
            JumpWindow.noteStep("确认页：等待中")
            return
        }
        val waited = SystemClock.elapsedRealtime() - seenAt
        if (waited < delayMs) {
            // 只在还剩不到 1 秒时才出声，否则一页等 3 秒会刷出四五条一样的日志。
            if (delayMs - waited < 1000L) {
                QLog.i("Tap", "确认页再过 ${delayMs - waited}ms 就点解锁按钮")
            }
            return
        }

        val btn = scan.confirmUseButton
        if (btn == null) {
            note("确认页上没找到底部那个使用按钮（tvUse）")
            return
        }
        val label = btn.text?.toString().orEmpty()
        if (isAdTarget(btn, label)) {
            QLog.w("Tap", "确认页的使用按钮看着像广告位（「$label」），拒绝点击")
            return
        }

        if (Prefs.dryRun(this)) {
            note("调试模式：本应点确认页的「$label」，未动手")
            JumpWindow.noteStep("调试：停在确认页，未解锁")
            return
        }
        // 已经点过、但页面还停在确认页 —— clickNode 返回 true 只代表事件被受理了，
        // 不代表页面真的跳了（WebView / 异步绑事件的按钮会静默吞掉一次点击）。
        // 实测过一次：点下去返回 true，页面原地不动，然后整条链路干等到窗口超时。
        // 所以这里隔 1.5 秒再看一次，还没走就重试，总共给 3 次。
        if (JumpWindow.alreadyDone(STAGE_CONFIRM_USE)) {
            if (confirmClickedAt == 0L) return

            // 特殊情形：会员页那一步已经走过了，说明现在看到的「确认页」很可能是
            // 会员页正在关闭、底下这层短暂露出来的残影。这时候立刻回点确认页按钮，
            // 会又弹回会员页，白跑一圈。所以要求离上一次会员页点击至少 2.5 秒，
            // 让过渡动画走完再说。
            if (JumpWindow.alreadyDone(STAGE_DECLINE) && declineClickedAt > 0L) {
                val sinceDecline = SystemClock.elapsedRealtime() - declineClickedAt
                if (sinceDecline < DECLINE_SETTLE_MS) {
                    // 文案里不带具体毫秒数，这样 note() 能把连续的重复自动去掉。
                    note("会员页刚关，等它彻底退干净再判断")
                    return
                }
            }

            val since = SystemClock.elapsedRealtime() - confirmClickedAt
            if (since < RETRY_AFTER_MS) return
            if (confirmAttempts >= MAX_ATTEMPTS) {
                note("确认页点了 ${confirmAttempts} 次都没跳过去，停手（可以手动点一下那个按钮）")
                JumpWindow.disarm("确认页点击始终无效")
                stopPolling()
                return
            }
            note("确认页还停着（上次点击 ${since}ms 前），重试第 ${confirmAttempts + 1} 次")
            JumpWindow.resetStage(STAGE_CONFIRM_USE)
        }

        if (!JumpWindow.claimStage(STAGE_CONFIRM_USE)) return

        QLog.i("Tap", "确认页点「$label」（自动解锁）")
        confirmClickedAt = SystemClock.elapsedRealtime()
        confirmAttempts++
        val ok = clickNode(btn)
        QLog.i("Tap", "确认页点「$label」结果=$ok")
        if (ok) {
            JumpWindow.noteStep("已点确认页「$label」")
        } else {
            QLog.w("Tap", "确认页点击失败，撤回这一步，下一轮再试")
            JumpWindow.resetStage(STAGE_CONFIRM_USE)
            confirmClickedAt = 0L
        }
    }

    /**
     * 会员营销页（点确认页按钮之后弹出来的那层 WebView）。
     *
     * 这一页上有两个红底/蓝底的大按钮挨得很近（间隔约 220px）：
     *   - 上面「开通并领取优惠」→ **会开通自动续费 9.7 元/月**，绝对不能点
     *   - 下面「放弃优惠 原价使用」→ 不订阅，按原价用，这才是我们要的
     *
     * 所以这里只认「放弃优惠」这四个字，并且要求它在文案里**出现但「开通」二字不出现**，
     * 用整串文本比对而不是模糊包含，宁可认不出来也不冒险点错。
     * 这一页所有节点的 resource-id 都是空的，只能靠文本。
     */
    private fun onMemberOfferPage(root: AccessibilityNodeInfo) {
        if (!Prefs.autoUnlock(this)) {
            note("弹出会员营销页，但自动解锁未开启，停手")
            Prefs.setLastResult(this, "停在会员营销页")
            JumpWindow.disarm("停在会员营销页")
            stopPolling()
            return
        }

        val decline = findByTextContainsOnly(root, Qk.TEXT_DECLINE_OFFER, Qk.TEXT_SUBSCRIBE_OFFER)
        if (decline == null) {
            note("会员页上没找到「${Qk.TEXT_DECLINE_OFFER}」（可能是别的营销样式），等下一轮")
            return
        }
        if (Prefs.dryRun(this)) {
            note("调试模式：本应点「${Qk.TEXT_DECLINE_OFFER}」，未动手")
            JumpWindow.noteStep("调试：停在会员页，未解锁")
            return
        }
        // 和确认页同一套重试：点了返回 true 但页面没走，隔 1.5 秒再点，最多 3 次。
        if (JumpWindow.alreadyDone(STAGE_DECLINE)) {
            if (declineClickedAt == 0L) return
            val since = SystemClock.elapsedRealtime() - declineClickedAt
            if (since < RETRY_AFTER_MS) return
            if (declineAttempts >= MAX_ATTEMPTS) {
                note("会员页点了 ${declineAttempts} 次都没跳过去，停手（可以手动点「${Qk.TEXT_DECLINE_OFFER}」）")
                JumpWindow.disarm("会员页点击始终无效")
                stopPolling()
                return
            }
            note("会员页还停着（上次点击 ${since}ms 前），重试第 ${declineAttempts + 1} 次")
            JumpWindow.resetStage(STAGE_DECLINE)
        }
        if (!JumpWindow.claimStage(STAGE_DECLINE)) return

        QLog.i("Tap", "会员页点「${decline.text}」（不订阅，按原价使用）")
        declineClickedAt = SystemClock.elapsedRealtime()
        declineAttempts++
        val ok = clickNode(decline)
        QLog.i("Tap", "会员页点「${decline.text}」结果=$ok")
        if (ok) {
            JumpWindow.noteStep("已拒绝会员优惠，按原价使用")
        } else {
            QLog.w("Tap", "会员页点击失败，撤回这一步，下一轮再试")
            JumpWindow.resetStage(STAGE_DECLINE)
            declineClickedAt = 0L
        }
    }

    /**
     * 解锁成功页 —— 终点。到这里立刻撒手，一下都不许再点。
     *
     * 这一页意味着「设备已经解锁、开始计费」，也就是已经替用户做了最后那个决定。
     * 页面上的「联系商家」「故障上报」都是出问题才点的，正常流程绝不能碰；
     * 出水要靠机身上的实体键，我们不下任何指令人为出水。
     */
    private fun onUnlockedPage(root: AccessibilityNodeInfo) {
        val machine = findMachineName(root)
        val suffix = if (machine.isNullOrBlank()) "" else "（$machine）"
        val shown = JumpWindow.knownShown
        val namePart = when {
            !shown.isNullOrBlank() -> "（$shown）"
            else -> suffix
        }
        note("已解锁$namePart —— 到这儿就停，出水请按饮水机机身上的「出水」键")
        Prefs.setLastResult(this, "已解锁$namePart，请按机身出水键")
        JumpWindow.disarm("已解锁，任务完成")
        stopPolling()
    }

    /**
     * 判断一个节点是不是广告位里的东西。
     *
     * 三层依据，任一层命中就拒绝点击：
     *   ① 节点自己的 id 在广告容器名单里
     *   ② 往上 8 层祖先里有广告容器（广告的大部分内容都是容器的子节点，自己没 id）
     *   ③ 文案里带「点击即玩」「广告」「左滑点击即玩」这类典型广告词
     *
     * 这是「宁可点不动，也不点错」的那道闸。真正要点的按钮
     * （「立即使用」「领优惠券使用」「放弃优惠 原价使用」）三层都不会命中。
     */
    private fun isAdTarget(node: AccessibilityNodeInfo, text: String): Boolean {
        // 唯一的例外：悬浮广告自己的关闭按钮。它物理上就在广告容器里
        // （channel_banner_close_iv 是 rl_float_ad 的子节点），但它是「关掉广告」
        // 这个动作本身，实测点了只关广告、页面与焦点都不变，所以要放行。
        // 除它以外的任何节点，只要在广告容器里，都不点。
        if (node.viewIdResourceName == Qk.ID_FLOAT_AD_CLOSE) return false

        if (Qk.AD_TEXT_HINTS.any { text.contains(it) }) return true
        var cursor: AccessibilityNodeInfo? = node
        var depth = 0
        while (cursor != null && depth <= MAX_CLICK_UP) {
            val id = cursor.viewIdResourceName
            if (id != null && id in Qk.AD_ZONE_IDS) return true
            cursor = cursor.parent
            depth++
        }
        return false
    }

    /**
     * 找文案里含 [must] 但不含 [mustNot] 的第一个可点节点。
     *
     * 专门给会员页用：「放弃优惠 原价使用」和「开通并领取优惠」两个按钮
     * 在屏幕上只隔 220px，靠坐标或顺序都不保险，只能把负向关键词做成硬条件 ——
     * 只要文案里同时出现「开通」，就当它不是我们要的那个，直接跳过。
     */
    private fun findByTextContainsOnly(
        root: AccessibilityNodeInfo,
        must: String,
        mustNot: String
    ): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0
        while (queue.isNotEmpty() && visited < MAX_NODES) {
            val n = queue.removeFirst()
            visited++
            val t = n.text?.toString()
            if (t != null && t.contains(must) && !t.contains(mustNot) && n.isEnabled) {
                if (n.isClickable || clickableAncestorOf(n) != null) return n
            }
            for (i in 0 until n.childCount) {
                n.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    private fun clickableAncestorOf(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var cursor = node.parent
        var depth = 0
        while (cursor != null && depth <= MAX_CLICK_UP) {
            if (cursor.isClickable && cursor.isEnabled) return cursor
            cursor = cursor.parent
            depth++
        }
        return null
    }

    private fun passAdGate(node: AccessibilityNodeInfo?, what: String): Boolean {
        if (node == null) return false
        if (isAdTarget(node, node.text?.toString().orEmpty())) {
            QLog.w("Tap", "$what 落在广告区里，不点")
            return false
        }
        return true
    }

    /**
     * 一次 BFS 遍历整棵控件树，同时判断：
     *   - 是不是设备确认页（有「选择支付方式」/「出水口」）
     *   - 是不是会员营销页（有「放弃优惠」/「开通并领取优惠」/「会员服务协议」）
     *   - 是不是解锁成功页（有「解锁成功」/「结束后将自动为您计费」）
     *   - 是不是设备列表页（有「最近使用设备」/「查看附近设备」）
     *   - 是不是首页（有「附近设备」/「胖乖福利助手」）
     *   - 设备列表里的每一行（设备名 + 同一行里的「立即使用」）
     *
     * 判断顺序是「越靠后面的页面越优先」，因为确认页 → 会员页 → 解锁页 是
     * 一层压一层弹出来的：会员页弹在确认页上面，解锁页又取代会员页。
     * 解锁页的文案里没有「选择支付方式」，会员页也没有，所以三者不会互相误判；
     * 但把解锁页放最前面判断，是为了尽早停手 —— 到了那一步再点任何东西都是错的。
     */
    private fun scanPage(root: AccessibilityNodeInfo): PageScan {
        val result = PageScan()
        val texts = ArrayList<String>(64)
        val nameNodes = ArrayList<AccessibilityNodeInfo>()
        val adCloseNodes = ArrayList<AccessibilityNodeInfo>()

        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0
        while (queue.isNotEmpty() && visited < MAX_NODES) {
            val node = queue.removeFirst()
            visited++
            val t = node.text?.toString()
            if (!t.isNullOrBlank()) texts.add(t)
            val id = node.viewIdResourceName
            if (id == Qk.ID_DEVICE_NAME) nameNodes.add(node)
            if (id == Qk.ID_FLOAT_AD_CLOSE) adCloseNodes.add(node)
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        result.nodeCount = visited

        val joined = texts.joinToString("\u0001")
        result.onUnlocked = Qk.UNLOCKED_MARKERS.any { joined.contains(it) }
        result.onMemberOffer = Qk.MEMBER_OFFER_MARKERS.any { joined.contains(it) }
        result.onConfirm = Qk.CONFIRM_MARKERS.any { joined.contains(it) }
        result.onDeviceList = Qk.DEVICE_LIST_MARKERS.any { joined.contains(it) }
        result.onHome = Qk.HOME_MARKERS.any { joined.contains(it) }

        if (result.onConfirm && !result.onMemberOffer && !result.onUnlocked) {
            result.confirmUseButton = findByViewId(root, Qk.ID_USE_TEXT)
        }
        result.floatAdClose = adCloseNodes.firstOrNull { it.isEnabled }

        if (result.onDeviceList) {
            result.deviceRows = nameNodes.mapNotNull { nameNode ->
                val shown = nameNode.text?.toString() ?: return@mapNotNull null
                val use = findUseButtonInRow(nameNode) ?: return@mapNotNull null
                DeviceRow(shown, use)
            }
        }
        return result
    }

    /**
     * 从设备名节点往上找最多 4 层祖先行，在这一行的子树里找「立即使用」。
     *
     * 为什么要往上找：tvDeviceName 和 tvUse 在 recyclerView 里是同一行的两个兄弟，
     * 它们没有共同的、能直接用 id 定位的父节点信息，只能靠「共同祖先」这个结构关系。
     * 只在往上 4 层以内找，是为了避免一路爬到 recyclerView 根，
     * 把别行的按钮当成这一行的。
     */
    private fun findUseButtonInRow(nameNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var cursor: AccessibilityNodeInfo? = nameNode
        var depth = 0
        while (cursor != null && depth <= MAX_ROW_UP) {
            val found = findByViewId(cursor, Qk.ID_USE_TEXT)
            if (found != null) return found
            cursor = cursor.parent
            depth++
        }
        return null
    }

    private fun findByViewId(node: AccessibilityNodeInfo, viewId: String): AccessibilityNodeInfo? {
        val byApi = node.findAccessibilityNodeInfosByViewId(viewId)
        if (!byApi.isNullOrEmpty()) return byApi.firstOrNull { it.isClickable } ?: byApi.firstOrNull()

        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(node)
        var visited = 0
        while (queue.isNotEmpty() && visited < MAX_ROW_NODES) {
            val n = queue.removeFirst()
            visited++
            if (n.viewIdResourceName == viewId) return n
            for (i in 0 until n.childCount) {
                n.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    private fun findByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0
        while (queue.isNotEmpty() && visited < MAX_NODES) {
            val n = queue.removeFirst()
            visited++
            if (n.text?.toString() == text && n.isEnabled) return n
            for (i in 0 until n.childCount) {
                n.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    private fun findMachineName(root: AccessibilityNodeInfo): String? {
        val nodes = root.findAccessibilityNodeInfosByViewId(Qk.ID_MACHINE_NAME)
        return nodes?.firstOrNull()?.text?.toString()
    }

    /**
     * 点一个节点。
     *
     * 两条路，按可靠性排序：
     *   1. 自己和祖先里谁 clickable 就点谁 —— 走系统无障碍的 ACTION_CLICK，
     *      不产生真实触摸，最稳。
     *   2. 都不 clickable（WebView 里渲染的内容常见），就取控件中心的坐标，
     *      用 dispatchGesture 做一次 60ms 的按下-抬起。这需要 canPerformGestures。
     *
     * 实测胖乖的「立即使用」是 clickable=true 的 TextView，走第 1 条即可；
     * 第 2 条留着兜底，因为它的设备列表将来可能改成 Compose 或 WebView 渲染。
     */
    private fun clickNode(node: AccessibilityNodeInfo): Boolean {
        // 统一闸门：不管调用方是谁，只要目标落在广告区 / 文案像广告，
        // 一律不点。放在最底层，是因为「上层忘了判」的代价太大 ——
        // 确认页上那个快手广告里的「点击即玩」会把你带到应用商店。
        val label = node.text?.toString().orEmpty()
        if (isAdTarget(node, label)) {
            QLog.w("Tap", "拒绝点击广告节点：「${label.take(24)}」")
            return false
        }

        var cursor: AccessibilityNodeInfo? = node
        var depth = 0
        while (cursor != null && depth <= MAX_CLICK_UP) {
            if (cursor.isClickable && cursor.isEnabled) {
                val ok = cursor.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                QLog.i("Tap", "performAction(ACTION_CLICK) 于第 $depth 层祖先 → $ok")
                if (ok) return true
            }
            cursor = cursor.parent
            depth++
        }

        val rect = Rect()
        node.getBoundsInScreen(rect)
        if (rect.width() <= 0 || rect.height() <= 0) {
            QLog.w("Tap", "节点没有有效尺寸，无法手势点击")
            return false
        }
        val cx = rect.centerX().toFloat()
        val cy = rect.centerY().toFloat()
        val path = android.graphics.Path().apply { moveTo(cx, cy) }
        val gesture = android.accessibilityservice.GestureDescription.Builder()
            .addStroke(
                android.accessibilityservice.GestureDescription.StrokeDescription(
                    path, 0L, TAP_DURATION_MS
                )
            )
            .build()
        val submitted = dispatchGesture(gesture, null, null)
        QLog.i("Tap", "dispatchGesture 提交=$submitted 坐标=($cx, $cy)")
        return submitted
    }

    private fun note(msg: String) {
        if (noted.add(msg)) QLog.i("Tap", msg)
    }

    companion object {
        private const val POLL_INTERVAL_MS = 600L
        private const val MAX_NODES = 900
        private const val MAX_ROW_NODES = 120
        private const val MAX_ROW_UP = 4
        private const val MAX_CLICK_UP = 8
        private const val TAP_DURATION_MS = 60L
        private const val CATEGORY_COOLDOWN_MS = 1200L
        private const val USE_COOLDOWN_MS = 1500L

        /** 点完一步之后，隔多久页面还没走就重试。太短会重复点击，太长会磨掉窗口。 */
        private const val RETRY_AFTER_MS = 1500L

        /** 同一步最多点几次。3 次还不跳就停手，把控制权交回用户。 */
        private const val MAX_ATTEMPTS = 3

        /**
         * 会员页关掉之后，底下那层确认页要过半秒到一秒才会重新成为活动窗口。
         * 这段时间里看到的「确认页」是残影，回点它的按钮会又弹回会员页。
         * 所以要求离上一次会员页点击至少这么久，才允许再判断确认页。
         */
        private const val DECLINE_SETTLE_MS = 2500L

        /**
         * 链路上每一步的名字。给 JumpWindow.claimStage 用，
         * 保证同一轮待命里每一步各自只走一次。
         */
        private const val STAGE_DEVICE_ROW = "设备行-立即使用"
        private const val STAGE_CONFIRM_USE = "确认页-使用按钮"
        private const val STAGE_DECLINE = "会员页-放弃优惠"

        /** MIUI 的安全中心。它的 wakepath ConfirmStartActivity 就是「应用跳转」确认框。 */
        private const val MIUI_SECURITY_PKG = "com.miui.securitycenter"

        /**
         * 无障碍服务此刻是否真的可用。
         *
         * 三个来源按可靠性排序：
         *   1. 服务自己写的连接回执（带 pid，进程变了即作废）—— 最准
         *   2. 系统 secure 设置里是否登记了本服务 —— 能写就能读，最权威
         *   3. AccessibilityManager 的已启用列表 —— 读不到 secure 时的兜底
         * 三者任一为真就算连上，宁可多试一次也不要因为一次读不到就把用户挡在门外。
         */
        fun isConnected(context: android.content.Context): Boolean {
            if (Prefs.a11yConnected(context)) return true
            if (A11yGuard.isEnabled(context)) return true
            return false
        }
    }
}
