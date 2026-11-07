package com.local.qk3jump

import android.util.Log

/**
 * 全工程唯一的日志入口。
 *
 * TAG 固定为 "Qk3Jump"，这样 `adb logcat -s Qk3Jump:V` 能把整条链路一次捞全。
 * area 用来区分阶段，约定如下：
 *   Life  —— 生命周期
 *   Jump  —— 拉起胖乖、判定前置条件
 *   Tap   —— 无障碍点击的每一步
 *   Match —— 设备名匹配（这一块最容易出错，单独留一个 area）
 *   A11y  —— 无障碍授权本身
 *   Set   —— 设置页
 */
object QLog {

    const val TAG = "Qk3Jump"

    fun i(area: String, msg: String) = Log.i(TAG, "[$area] $msg")

    fun w(area: String, msg: String) = Log.w(TAG, "[$area] $msg")

    fun e(area: String, msg: String) = Log.e(TAG, "[$area] $msg")

    fun e(area: String, msg: String, t: Throwable) = Log.e(TAG, "[$area] $msg：${describe(t)}")

    /**
     * 把异常压成一行，含类型、message、以及抛出点的类.方法:行号。
     * 栈里第一段是 Log 自己的调用方，所以从 StackTraceElement[0] 开始找第一个非 QLog 的帧。
     */
    fun describe(t: Throwable): String {
        val where = t.stackTrace.firstOrNull {
            !it.className.startsWith("com.local.qk3jump.QLog")
        }?.let { "${it.className.substringAfterLast('.')}.${it.methodName}:${it.lineNumber}" } ?: "未知位置"
        return "${t.javaClass.simpleName}: ${t.message ?: "（无 message）"} @$where"
    }
}
