package com.local.qk3jump

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * 启动页：这里就是「目标设备列表」。
 *
 * 打开本应用看到的第一屏就是可以点的设备，点一下就出发：
 *   拉起胖乖生活 → 无障碍自动点「饮水」→ 点那台设备的「立即使用」→ 到设备确认页。
 * 到了确认页就撒手，后面的「立即使用」由用户自己按。
 *
 * 为什么把列表放在启动页而不是二级页：这个应用存在的唯一理由就是把
 * 「开支付宝 → 搜小程序 → 点扫一扫 → 扫码」压缩成「点一下」，
 * 所以第一个可交互的东西必须是那台机器本身。
 */
class JumpActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var emptyView: TextView
    private lateinit var tvStatus: TextView
    private lateinit var progress: ProgressBar
    private lateinit var btnSettings: TextView
    private lateinit var btnAdd: TextView
    private lateinit var btnClear: TextView

    private val handler = Handler(Looper.getMainLooper())

    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jump)

        container = findViewById(R.id.containerTargets)
        emptyView = findViewById(R.id.tvTargetsEmpty)
        tvStatus = findViewById(R.id.tvStatus)
        progress = findViewById(R.id.progress)
        btnSettings = findViewById(R.id.btnSettings)
        btnAdd = findViewById(R.id.btnAddTarget)
        btnClear = findViewById(R.id.btnClearTargets)

        TargetStore.seedIfEmpty(this)

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        btnAdd.setOnClickListener { askAddTarget() }
        btnClear.setOnClickListener { confirmClear() }

        QLog.i("Life", "═══ JumpActivity 启动 ═══")
        QLog.i(
            "Life",
            "调试=${Prefs.dryRun(this)}  步进等待=${Prefs.stepWaitMs(this)}ms  窗口=${Prefs.windowMs(this)}ms  " +
                "胖乖版本=${Qk.versionName(this)}  已安装=${Qk.isInstalled(this)}"
        )

        // 从桌面小部件进来的话，渲染完就立刻出发，不用用户再点一次。
        handleWidgetLaunch(intent)
    }

    /**
     * 桌面小部件点击的入口。
     *
     * 小部件只带一个设备名字过来（见 [DeviceWidget]），这里回 [TargetStore] 查，
     * 查到就当作「用户点了那一行」处理 —— 走完全相同的 [jumpTo]，
     * 不存在任何小部件专属的捷径。
     *
     * extra 用完立刻清掉：本 Activity 是 singleTask，留着的话用户下次从桌面
     * 点本应用图标进来会被这个陈旧的 extra 再自动出发一次。
     */
    private fun handleWidgetLaunch(intent: Intent?) {
        val want = intent?.getStringExtra(EXTRA_TARGET) ?: return
        intent.removeExtra(EXTRA_TARGET)

        val t = TargetStore.all(this).firstOrNull { it.name == want }
        if (t == null) {
            QLog.w("Widget", "小部件要去的「$want」已不在目标簿里，停在本页让用户改")
            toast(getString(R.string.toast_widget_target_gone, want))
            return
        }
        QLog.i("Widget", "从小部件进入，目标「$want」")
        // 等这一帧画完再出发：否则用户点下去只看到胖乖直接盖上来，
        // 没有任何「我点了哪台」的反馈。
        handler.postDelayed({ jumpTo(t) }, 150)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // singleTask：Activity 已经活着时，点小部件不会走 onCreate，走这里。
        handleWidgetLaunch(intent)
    }

    override fun onResume() {
        super.onResume()
        // 冷启时 onCreate 早于 onResume，而掉授权之后必须先把授权补回来，
        // 再去渲染「无障碍是否就绪」那一行，否则会显示过期的判断。
        if (!A11yGuard.isEnabled(this)) {
            QLog.i("Jump", "onResume 发现授权不在位，先尝试自愈")
            A11yGuard.heal(this)
        }
        render()
    }

    private fun render() {
        val targets = TargetStore.all(this)
        container.removeAllViews()
        if (targets.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            btnClear.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            btnClear.visibility = View.VISIBLE
            targets.forEach { t ->
                val row = layoutInflater.inflate(R.layout.item_target, container, false)
                val name = row.findViewById<TextView>(R.id.tvTargetName)
                val more = row.findViewById<TextView>(R.id.btnTargetMore)
                name.text = t.name
                row.setOnClickListener { jumpTo(t) }
                more.setOnClickListener { showRowMenu(t) }
                container.addView(row)
            }
        }

        val a11y = ScanTapService.isConnected(this)
        tvStatus.text = when {
            !Qk.isInstalled(this) -> getString(R.string.status_no_qk)
            !a11y -> getString(R.string.status_no_a11y)
            else -> getString(R.string.status_ready, targets.size)
        }
        if (!a11y) {
            tvStatus.setOnClickListener {
                QLog.i("Jump", "点状态行，跳系统无障碍设置")
                openA11ySettings()
            }
        } else {
            tvStatus.setOnClickListener(null)
        }
        progress.visibility = View.GONE
        QLog.i("Jump", "目标簿共 ${targets.size} 台：${targets.joinToString(", ") { it.name }}")

        // 桌面上那两块小部件显示的是「目标簿里的哪个名字」。清单被改名/删除之后，
        // 它们必须跟着变，否则会挂着一个指向不存在设备的方块。
        // 放在 render() 里而不是每个增删改分支里，是因为 render() 是清单唯一的出口。
        DeviceWidget.refreshAll(this)
    }

    /** 点一行 → 出发。 */
    private fun jumpTo(target: TargetStore.Target) {
        if (running) {
            QLog.i("Jump", "上一次还在进行中，忽略重复点击")
            return
        }
        if (!Qk.isInstalled(this)) {
            toast(getString(R.string.status_no_qk))
            return
        }

        val a11y = ScanTapService.isConnected(this)
        if (!a11y) {
            QLog.w("Jump", "无障碍没就绪，先自愈再看")
            A11yGuard.heal(this)
        }
        val ready = ScanTapService.isConnected(this)

        Prefs.setTargetDevice(this, target.name)
        val intent = Qk.buildLaunchIntent(this)
        if (intent == null) {
            toast(getString(R.string.toast_no_entry))
            QLog.e("Jump", "拿不到拉起胖乖的 Intent")
            return
        }

        QLog.i("Jump", "═══ 准备直达「${target.name}」═══")
        QLog.i("Jump", "启动 Intent = ${Qk.dumpIntent(intent)}")
        QLog.i(
            "Jump",
            "resolveActivity = ${resolveOf(intent)}  " +
                "调试=${Prefs.dryRun(this)}  无障碍已连接=$ready"
        )

        if (Prefs.dryRun(this)) {
            progress.visibility = View.GONE
            tvStatus.text = getString(R.string.status_dry, target.name)
            JumpWindow.arm(this)
            QLog.i("Jump", "调试模式：已进入待命但不会真正点击（无障碍侧会打印将要点的控件）")
            // 调试模式仍然把胖乖拉起来 —— 不拉起来的话无障碍根本看不到任何页面，
            // 也就没法验证「它会认出哪一页、打算点哪个控件」。
            // 调试模式要拦的是「点击」，不是「启动」；启动本身不会产生任何费用。
            handler.postDelayed({
                try {
                    startActivity(intent)
                    QLog.i("Jump", "调试模式：已拉起胖乖，接下来只观察不点击")
                } catch (t: Throwable) {
                    QLog.e("Jump", "调试模式下拉起胖乖失败", t)
                }
            }, 260)
            return
        }

        running = true
        progress.visibility = View.VISIBLE
        tvStatus.text = getString(R.string.status_going, target.name)

        JumpWindow.arm(this)
        handler.postDelayed({
            try {
                startActivity(intent)
                QLog.i("Jump", "✓ 已拉起胖乖，等待无障碍把它领到「${target.name}」")
                // 这里刻意不退到后台。
                //
                // 原来这里有一句 moveTaskToBack(true)，实测会被 MIUI 拦：
                // 应用一旦退到后台，它发起的 startActivity 就被判为「后台启动 Activity」，
                // 系统会插入 com.miui.securitycenter/com.miui.wakepath.ui.ConfirmStartActivity
                // 弹一个「是否允许跳转」的确认框（焦点一闪而过，无障碍还来不及点）。
                // 保持前台就没有这个判定，跳转直接放行。
                //
                // 代价是用户会看到本应用的界面在胖乖起来之前多停一会儿 ——
                // 但那是几百毫秒的事，比弹一个需要人点的框好得多。
                progress.visibility = View.GONE
            } catch (t: Throwable) {
                QLog.e("Jump", "拉起胖乖失败", t)
                toast(getString(R.string.toast_launch_failed))
                jumpingFailed()
            } finally {
                running = false
            }
        }, 260)
    }

    private fun jumpingFailed() {
        progress.visibility = View.GONE
        handler.postDelayed({ if (!isFinishing) render() }, 800)
    }

    private fun resolveOf(i: Intent): String = try {
        packageManager.resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY)
            ?.let { "${it.activityInfo.packageName}/${it.activityInfo.name}" } ?: "null"
    } catch (t: Throwable) {
        "查询失败：${QLog.describe(t)}"
    }

    private fun showRowMenu(t: TargetStore.Target) {
        val items = arrayOf(getString(R.string.target_rename), getString(R.string.target_delete))
        AlertDialog.Builder(this)
            .setTitle(R.string.target_menu_title)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> askRename(t)
                    1 -> {
                        TargetStore.forget(this, t.key)
                        render()
                    }
                }
            }
            .show()
    }

    private fun askAddTarget() {
        val input = EditText(this).apply {
            hint = getString(R.string.dialog_target_hint)
            setSingleLine()
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.target_add)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val v = input.text.toString()
                if (v.isBlank()) {
                    toast(getString(R.string.toast_blank_name))
                    return@setPositiveButton
                }
                TargetStore.remember(this, v)
                render()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun askRename(t: TargetStore.Target) {
        val input = EditText(this).apply {
            setText(t.name)
            setSingleLine()
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.target_rename)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val v = input.text.toString()
                if (v.isBlank()) {
                    toast(getString(R.string.toast_blank_name))
                    return@setPositiveButton
                }
                TargetStore.rename(this, t.key, v)
                render()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun confirmClear() {
        AlertDialog.Builder(this)
            .setTitle(R.string.target_clear)
            .setMessage(getString(R.string.target_clear_confirm, TargetStore.all(this).size))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                TargetStore.clear(this)
                render()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun openA11ySettings() {
        try {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (t: Throwable) {
            QLog.e("Jump", "打开无障碍设置失败", t)
            toast(getString(R.string.toast_a11y_settings_failed))
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        /**
         * 小部件带过来的目标设备名。
         *
         * 只在「点击桌面方块」这一条路上出现；从桌面图标正常打开时没有这个 extra。
         */
        const val EXTRA_TARGET = "com.local.qk3jump.extra.TARGET"
    }
}
