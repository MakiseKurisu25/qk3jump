package com.local.qk3jump

import android.content.Context

/**
 * 设置项。全部存在 "qk3jump_prefs" 一个文件里。
 *
 * 这里故意不存任何 URL、token、订单号 —— 本应用只负责「把用户领到设备确认页」，
 * 到了那一页之后发生什么由胖乖和官方服务端决定，本应用一概不参与。
 */
object Prefs {

    const val FILE = "qk3jump_prefs"

    // 调试模式：只记录将要执行的动作，不真的点、不真的拉。
    private const val KEY_DRY_RUN = "dry_run"

    // 每个步骤之间的等待毫秒。给页面留出渲染时间，太快会点空。
    private const val KEY_STEP_WAIT = "step_wait_ms"
    const val DEFAULT_STEP_WAIT_MS = 1200
    const val MIN_STEP_WAIT_MS = 300
    const val MAX_STEP_WAIT_MS = 5000

    // 待命窗口：从拉起胖乖开始，多久之内允许自动点击。超时就不再动手。
    private const val KEY_WINDOW = "window_ms"
    const val DEFAULT_WINDOW_MS = 25_000
    const val MIN_WINDOW_MS = 5_000
    const val MAX_WINDOW_MS = 90_000

    // 无障碍服务的连接回执。存 pid 是为了作废「上一个进程留下的 true」——
    // force-stop 不会回调 onUnbind，旧回执会一直留在盘上骗人。
    private const val KEY_A11Y_CONNECTED = "a11y_connected"
    private const val KEY_A11Y_PID = "a11y_pid"

    // 要直达的目标设备名（胖乖 App 设备列表里显示的那串字）。
    private const val KEY_TARGET = "target_device"

    // 自动解锁：到了设备确认页之后，是否替用户点「领优惠券使用 / 立即使用」。
    // 默认关闭 —— 这一下意味着开始计费，必须由用户显式打开。
    private const val KEY_AUTO_UNLOCK = "auto_unlock"

    // 自动解锁的等待秒数：从认出确认页起，等满这么久再动手。
    // 给页面留出渲染和用户反悔的时间，也是 V1 一条时钟的延续。
    private const val KEY_UNLOCK_DELAY = "unlock_delay_s"
    const val DEFAULT_UNLOCK_DELAY_S = 3
    const val MIN_UNLOCK_DELAY_S = 0
    const val MAX_UNLOCK_DELAY_S = 30

    // 上次执行到哪一步，用于界面回显。
    private const val KEY_LAST_RESULT = "last_result"

    private fun sp(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /**
     * 给 TargetStore 直接读写原始字符串用。
     * 放在 Prefs 里是为了让「所有 SharedPreferences 都只经由这一个文件」这条规矩不被破掉。
     */
    fun spRaw(c: Context, key: String): String = sp(c).getString(key, "") ?: ""

    fun setSpRaw(c: Context, key: String, v: String) = sp(c).edit().putString(key, v).apply()

    fun dryRun(c: Context) = sp(c).getBoolean(KEY_DRY_RUN, false)
    fun setDryRun(c: Context, v: Boolean) = sp(c).edit().putBoolean(KEY_DRY_RUN, v).apply()

    fun stepWaitMs(c: Context): Int =
        sp(c).getInt(KEY_STEP_WAIT, DEFAULT_STEP_WAIT_MS).coerceIn(MIN_STEP_WAIT_MS, MAX_STEP_WAIT_MS)

    fun setStepWaitMs(c: Context, v: Int) = sp(c).edit().putInt(KEY_STEP_WAIT, v).apply()

    fun windowMs(c: Context): Int =
        sp(c).getInt(KEY_WINDOW, DEFAULT_WINDOW_MS).coerceIn(MIN_WINDOW_MS, MAX_WINDOW_MS)

    fun setWindowMs(c: Context, v: Int) = sp(c).edit().putInt(KEY_WINDOW, v).apply()

    fun targetDevice(c: Context): String = sp(c).getString(KEY_TARGET, "") ?: ""
    fun setTargetDevice(c: Context, v: String) = sp(c).edit().putString(KEY_TARGET, v).apply()

    fun lastResult(c: Context): String = sp(c).getString(KEY_LAST_RESULT, "") ?: ""
    fun setLastResult(c: Context, v: String) = sp(c).edit().putString(KEY_LAST_RESULT, v).apply()

    fun autoUnlock(c: Context) = sp(c).getBoolean(KEY_AUTO_UNLOCK, false)
    fun setAutoUnlock(c: Context, v: Boolean) = sp(c).edit().putBoolean(KEY_AUTO_UNLOCK, v).apply()

    fun unlockDelayS(c: Context): Int =
        sp(c).getInt(KEY_UNLOCK_DELAY, DEFAULT_UNLOCK_DELAY_S).coerceIn(MIN_UNLOCK_DELAY_S, MAX_UNLOCK_DELAY_S)

    fun setUnlockDelayS(c: Context, v: Int) = sp(c).edit().putInt(KEY_UNLOCK_DELAY, v).apply()

    /** 读回执时必须校验 pid：进程换了，回执就不算数。 */
    fun a11yConnected(c: Context): Boolean =
        sp(c).getBoolean(KEY_A11Y_CONNECTED, false) &&
            sp(c).getInt(KEY_A11Y_PID, -1) == android.os.Process.myPid()

    fun setA11yConnected(c: Context, v: Boolean) =
        sp(c).edit()
            .putBoolean(KEY_A11Y_CONNECTED, v)
            .putInt(KEY_A11Y_PID, if (v) android.os.Process.myPid() else -1)
            .apply()
}
