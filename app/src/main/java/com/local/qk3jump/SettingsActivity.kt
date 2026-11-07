package com.local.qk3jump

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * 设置页。四块：行为、无障碍、关于、恢复默认。
 *
 * 这里刻意只保留「真的会影响结果」的旋钮：
 *   调试模式  —— 只看不动，验证「会点哪一行」而不真的开出水
 *   步进等待  —— 页面渲染慢的机器上要调大，否则会点空
 *   待命窗口  —— 从拉起胖乖开始多久之内允许动手
 * 其它东西（比如「选第几行」）不做，因为按名字匹配更可靠，见 Matcher。
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var cbDryRun: SwitchMaterial
    private lateinit var tvStepWait: TextView
    private lateinit var sbStepWait: SeekBar
    private lateinit var tvWindow: TextView
    private lateinit var sbWindow: SeekBar
    private lateinit var cbAutoUnlock: SwitchMaterial
    private lateinit var tvUnlockDelay: TextView
    private lateinit var sbUnlockDelay: SeekBar
    private lateinit var tvA11yStatus: TextView
    private lateinit var btnOpenA11y: TextView
    private lateinit var btnHealA11y: TextView
    private lateinit var tvLast: TextView
    private lateinit var btnReset: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        cbDryRun = findViewById(R.id.cbDryRun)
        tvStepWait = findViewById(R.id.tvStepWait)
        sbStepWait = findViewById(R.id.sbStepWait)
        tvWindow = findViewById(R.id.tvWindow)
        sbWindow = findViewById(R.id.sbWindow)
        cbAutoUnlock = findViewById(R.id.cbAutoUnlock)
        tvUnlockDelay = findViewById(R.id.tvUnlockDelay)
        sbUnlockDelay = findViewById(R.id.sbUnlockDelay)
        tvA11yStatus = findViewById(R.id.tvA11yStatus)
        btnOpenA11y = findViewById(R.id.btnOpenA11y)
        btnHealA11y = findViewById(R.id.btnHealA11y)
        tvLast = findViewById(R.id.tvLast)
        btnReset = findViewById(R.id.btnReset)

        cbDryRun.setOnCheckedChangeListener { _, v ->
            Prefs.setDryRun(this, v)
            QLog.i("Set", "调试模式=$v")
        }

        // 自动解锁是唯一一个会真的产生费用的开关，所以它单独一个对话确认，
        // 不能像别的开关那样一拨就生效 —— 手滑一下就是一次真实计费。
        cbAutoUnlock.setOnCheckedChangeListener { _, v ->
            if (v && !Prefs.autoUnlock(this)) {
                showAutoUnlockConfirm()
                return@setOnCheckedChangeListener
            }
            Prefs.setAutoUnlock(this, v)
            QLog.i("Set", "自动解锁=$v")
        }

        sbUnlockDelay.max = Prefs.MAX_UNLOCK_DELAY_S - Prefs.MIN_UNLOCK_DELAY_S
        sbUnlockDelay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val s = Prefs.MIN_UNLOCK_DELAY_S + progress
                tvUnlockDelay.text = getString(R.string.set_unlock_delay, s)
                if (fromUser) Prefs.setUnlockDelayS(this@SettingsActivity, s)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) = Unit
            override fun onStopTrackingTouch(sb: SeekBar?) {
                QLog.i("Set", "解锁等待=${Prefs.unlockDelayS(this@SettingsActivity)}s")
            }
        })

        sbStepWait.max = (Prefs.MAX_STEP_WAIT_MS - Prefs.MIN_STEP_WAIT_MS) / STEP_STEP_MS
        sbStepWait.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val ms = Prefs.MIN_STEP_WAIT_MS + progress * STEP_STEP_MS
                tvStepWait.text = getString(R.string.set_step_wait_value, ms)
                if (fromUser) Prefs.setStepWaitMs(this@SettingsActivity, ms)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) = Unit
            override fun onStopTrackingTouch(sb: SeekBar?) {
                QLog.i("Set", "步进等待=${Prefs.stepWaitMs(this@SettingsActivity)}ms")
            }
        })

        sbWindow.max = (Prefs.MAX_WINDOW_MS - Prefs.MIN_WINDOW_MS) / WINDOW_STEP_MS
        sbWindow.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val ms = Prefs.MIN_WINDOW_MS + progress * WINDOW_STEP_MS
                tvWindow.text = getString(R.string.set_window_value, ms / 1000)
                if (fromUser) Prefs.setWindowMs(this@SettingsActivity, ms)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) = Unit
            override fun onStopTrackingTouch(sb: SeekBar?) {
                QLog.i("Set", "待命窗口=${Prefs.windowMs(this@SettingsActivity)}ms")
            }
        })

        btnOpenA11y.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (t: Throwable) {
                QLog.e("Set", "打开无障碍设置失败", t)
                toast(getString(R.string.toast_a11y_settings_failed))
            }
        }

        btnHealA11y.setOnClickListener {
            val ok = A11yGuard.heal(this)
            refreshA11yStatus()
            val msg = when {
                ok -> getString(R.string.a11y_heal_ok)
                !A11yGuard.canWrite(this) -> getString(R.string.a11y_heal_no_perm)
                else -> getString(R.string.a11y_heal_failed)
            }
            toast(msg)
        }

        btnReset.setOnClickListener { confirmReset() }

        loadIntoViews()
    }

    override fun onResume() {
        super.onResume()
        refreshA11yStatus()
        tvLast.text = Prefs.lastResult(this).ifBlank { getString(R.string.set_last_none) }
    }

    private fun loadIntoViews() {
        cbDryRun.isChecked = Prefs.dryRun(this)
        val step = Prefs.stepWaitMs(this)
        sbStepWait.progress = (step - Prefs.MIN_STEP_WAIT_MS) / STEP_STEP_MS
        tvStepWait.text = getString(R.string.set_step_wait_value, step)
        val win = Prefs.windowMs(this)
        sbWindow.progress = (win - Prefs.MIN_WINDOW_MS) / WINDOW_STEP_MS
        tvWindow.text = getString(R.string.set_window_value, win / 1000)
        cbAutoUnlock.isChecked = Prefs.autoUnlock(this)
        val delay = Prefs.unlockDelayS(this)
        sbUnlockDelay.progress = delay - Prefs.MIN_UNLOCK_DELAY_S
        tvUnlockDelay.text = getString(R.string.set_unlock_delay, delay)
        refreshA11yStatus()
    }

    /**
     * 打开自动解锁前的确认。
     *
     * 这个开关和其它开关不同：它一旦生效，下一次扫码就会真的把设备解锁、
     * 真的开始计费。所以必须在开启前把后果说清楚，并且默认把「取消」当退路。
     * 用户点「取消」时把开关拨回去 —— 拨动开关本身已经改变了 UI 状态，
     * 不回调就会留下一个「看起来开着、其实没生效」的假象。
     */
    private fun showAutoUnlockConfirm() {
        AlertDialog.Builder(this)
            .setTitle(R.string.set_auto_unlock)
            .setMessage(R.string.set_auto_unlock_confirm)
            .setPositiveButton(R.string.set_auto_unlock_ok) { _, _ ->
                Prefs.setAutoUnlock(this, true)
                cbAutoUnlock.isChecked = true
                QLog.i("Set", "自动解锁=true（用户已确认）")
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                cbAutoUnlock.isChecked = false
                QLog.i("Set", "自动解锁=保持关闭（用户取消）")
            }
            .setOnCancelListener {
                cbAutoUnlock.isChecked = false
                QLog.i("Set", "自动解锁=保持关闭（对话被取消）")
            }
            .show()
    }

    private fun refreshA11yStatus() {
        val line = A11yGuard.statusLine(this)
        tvA11yStatus.text = line
        QLog.i("Set", "无障碍状态：$line")
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle(R.string.set_reset)
            .setMessage(R.string.set_reset_confirm)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                Prefs.setDryRun(this, false)
                Prefs.setStepWaitMs(this, Prefs.DEFAULT_STEP_WAIT_MS)
                Prefs.setWindowMs(this, Prefs.DEFAULT_WINDOW_MS)
                Prefs.setAutoUnlock(this, false)
                Prefs.setUnlockDelayS(this, Prefs.DEFAULT_UNLOCK_DELAY_S)
                loadIntoViews()
                QLog.i("Set", "已恢复默认：调试=false 自动解锁=false 步进=${Prefs.DEFAULT_STEP_WAIT_MS}ms 窗口=${Prefs.DEFAULT_WINDOW_MS}ms 解锁等待=${Prefs.DEFAULT_UNLOCK_DELAY_S}s")
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private companion object {
        const val STEP_STEP_MS = 100
        const val WINDOW_STEP_MS = 1000
    }
}
