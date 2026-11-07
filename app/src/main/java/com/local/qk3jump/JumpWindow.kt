package com.local.qk3jump

import android.content.Context
import android.os.SystemClock

/**
 * 待命窗口。
 *
 * 只有在这个窗口内、且本应用刚刚主动拉起过胖乖的时候，无障碍服务才允许动手。
 * 这样即使服务一直开着，用户自己手动打开胖乖的时候也不会被莫名其妙地点一下。
 *
 * 三条约束全部满足才会返回 true：
 *   1. 用户按过一次启动（arm）
 *   2. 还没超过窗口时长（默认 25 秒）
 *   3. 这一轮还没点过（一台机器只点一次，点完就失效）
 */
object JumpWindow {

    @Volatile private var armedAt = 0L

    @Volatile private var windowMs = Prefs.DEFAULT_WINDOW_MS

    /**
     * 这一轮里已经点过的**步骤**。
     *
     * 原来只有一个 fired 布尔量，因为整条链路只有「点设备那一行」一次动作。
     * 现在链路变成一串（设备行 → 确认页按钮 → 会员页「放弃优惠」），
     * 每一步都得各自只放行一次，否则会出现「点完设备行就再也点不动确认页」，
     * 或者「确认页按钮一秒被连点三次」。
     *
     * 用字符串当步骤名，比给每一步加一个字段更好读，也不会随着链路变长而膨胀。
     */
    private val firedStages = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    fun arm(context: Context) {
        windowMs = Prefs.windowMs(context)
        armedAt = SystemClock.elapsedRealtime()
        firedStages.clear()
        knownShown = null
        QLog.i("Tap", "进入待命（窗口 ${windowMs}ms）")
    }

    /**
     * 本轮待命的标识。
     *
     * 无障碍服务用它判断「是不是新的一轮」，从而把属于单轮的状态
     * （比如「MIUI 跳转确认框已经点过了」）清掉。
     * 用时间戳当令牌，不用回调，避免这个纯状态对象反向依赖服务。
     */
    fun armToken(): Long = armedAt

    fun disarm(reason: String) {
        if (armedAt == 0L) return
        armedAt = 0L
        firedStages.clear()
        QLog.i("Tap", "退出待命：$reason")
    }

    fun isArmed(): Boolean {
        if (armedAt == 0L) return false
        val elapsed = SystemClock.elapsedRealtime() - armedAt
        if (elapsed > windowMs) {
            QLog.w("Tap", "待命已超时（${elapsed}ms > ${windowMs}ms），不再动手")
            armedAt = 0L
            firedStages.clear()
            return false
        }
        return true
    }

    /** 是否已经动过手（任意一步）。给「绑定服务时要不要自举」这类判断用。 */
    fun isFired(): Boolean = firedStages.isNotEmpty()

    /** 已经放行过的步骤（内部用，判断「这一步是否已完成」而不打日志）。 */
    fun alreadyDone(stage: String): Boolean = firedStages.contains(stage)

    /**
     * 申请在某一步动手，返回 true 表示放行。
     * 同一步在这一轮里只会放行一次。
     */
    fun claimStage(stage: String): Boolean {
        if (!isArmed()) return false
        if (!firedStages.add(stage)) return false
        return true
    }

    /**
     * 撤销某一步的「已做过」标记，允许下一轮重试。
     * 只在点击动作本身失败（performAction 返回 false、dispatchGesture 提交不上）时调用。
     */
    fun resetStage(stage: String) {
        if (firedStages.remove(stage)) {
            QLog.i("Tap", "「$stage」没生效，撤回标记，允许重试")
        }
    }

    /** 已经点到了哪一步，给日志与界面用。 */
    @Volatile var lastStep: String = ""
        private set

    fun noteStep(step: String) {
        // 600ms 轮询会把同一句进度反复打出来，只有真的变了才记一行。
        if (lastStep == step) return
        lastStep = step
        QLog.i("Tap", "进度：$step")
    }

    /** 记住设备列表里实际显示的名字，界面回显「现在会点哪一行」。 */
    @Volatile var knownShown: String? = null
        private set

    fun noteShown(name: String) {
        if (knownShown == name) return
        knownShown = name
        QLog.i("Match", "设备列表里目标那台显示为「$name」")
    }
}
