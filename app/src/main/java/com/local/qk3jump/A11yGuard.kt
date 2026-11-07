package com.local.qk3jump

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings

/**
 * 无障碍授权的自愈。
 *
 * 背景：MIUI / ColorOS 都会在「从最近任务划掉 App」或 force-stop 时
 * 把 accessibility_enabled 和 enabled_accessibility_services 一起清空，
 * 清空之后系统不会自己绑回来，用户必须重新去设置里打开一次。
 *
 * 能自己写回的前提是拿到 WRITE_SECURE_SETTINGS。它是 signature|privileged 级，
 * 安装时一律不授予，只能靠：
 *     adb shell pm grant com.local.qk3jump android.permission.WRITE_SECURE_SETTINGS
 * 拿不到时本类所有方法都安全空转，不会崩、也不会假装成功。
 *
 * 两台 ROM 的写入顺序是相反的，所以这里两种都试：
 *   MIUI    ：先 accessibility_enabled=1，再写 services
 *   ColorOS ：先写 services，再 accessibility_enabled=1
 * 顺序错了那次写入会被系统立刻重置回去 —— 实测 realme 上就是这样。
 */
object A11yGuard {

    private const val TAG_AREA = "A11y"

    private fun component(context: Context): String =
        ComponentName(context, ScanTapService::class.java).flattenToString()

    /**
     * 把一条无障碍组件名规整成 `包名/完整类名` 的规范形式。
     *
     * 系统里同一条服务可能有两种写法：
     *   `com.local.qk3jump/.ScanTapService`            ← 简写（相对包名）
     *   `com.local.qk3jump/com.local.qk3jump.ScanTapService`  ← 全写
     * 它们指的是同一个服务。早期版本写回时只用字符串拼接、不查重，
     * 于是在「简短写法已在列表里」的情况下把全写又追加了一遍，实测 logcat：
     *
     *   [A11y]   原有服务列表=com.local.qk3jump/.ScanTapService
     *   [A11y]   写回后=com.local.qk3jump/.ScanTapService:com.local.qk3jump/com.local.qk3jump.ScanTapService
     *
     * 同一条服务在系统设置里出现两次，既难看也可能让某些 ROM 的解析出错。
     * 现在统一先规整再比对 / 写回。
     */
    private fun normalizeEntry(entry: String): String {
        val slash = entry.indexOf('/')
        if (slash <= 0) return entry
        val pkg = entry.substring(0, slash)
        val cls = entry.substring(slash + 1)
        val full = if (cls.startsWith(".")) pkg + cls else cls
        return "$pkg/$full"
    }

    fun canWrite(context: Context): Boolean = try {
        context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) ==
            PackageManager.PERMISSION_GRANTED
    } catch (_: Throwable) {
        false
    }

    /**
     * 当前无障碍服务是否已在系统里启用。
     *
     * 有写权限时直接读 secure 设置（最准）；没有时退回 AccessibilityManager 的列表。
     * 注意 Settings.Secure 里那个 key 从 Android 4.2 起对普通应用是读不到的，
     * 所以不能只靠它，两条路都要。
     */
    fun isEnabled(context: Context): Boolean {
        val mine = component(context)

        if (canWrite(context)) {
            val raw = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (!raw.isNullOrBlank()) {
                return raw.split(':').any {
                    it.isNotBlank() &&
                        normalizeEntry(it).equals(normalizeEntry(mine), ignoreCase = true)
                }
            }
        }

        return try {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE)
                as? android.view.accessibility.AccessibilityManager
            val list = am?.getEnabledAccessibilityServiceList(
                android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            )
            list?.any {
                it.resolveInfo.serviceInfo.packageName == context.packageName &&
                    it.resolveInfo.serviceInfo.name == ScanTapService::class.java.name
            } ?: false
        } catch (t: Throwable) {
            QLog.w(TAG_AREA, "查无障碍列表失败：${QLog.describe(t)}")
            false
        }
    }

    /**
     * 尝试把授权写回去。
     * @return true 表示写回并复核成功（服务能否立刻绑上还要看系统的异步节奏）
     */
    fun heal(context: Context): Boolean {
        if (isEnabled(context)) {
            QLog.i(TAG_AREA, "无障碍授权在位，无需自愈")
            return true
        }
        if (!canWrite(context)) {
            QLog.w(
                TAG_AREA,
                "没有 WRITE_SECURE_SETTINGS，无法自愈。一次性执行：" +
                    "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
            )
            return false
        }

        val mine = component(context)
        val cr = context.contentResolver
        val before = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""

        // 先按规范形式判断「是不是其实已经在里面了」—— 简写与全写指同一个服务。
        val alreadyThere = before.split(':').any {
            it.isNotBlank() && normalizeEntry(it).equals(normalizeEntry(mine), ignoreCase = true)
        }
        if (alreadyThere) {
            QLog.i(TAG_AREA, "服务已在列表里（写法不同），只补开关，不重复追加：${before.ifBlank { "(空)" }}")
            writeAll(cr, enabledFirst = true, services = before)
            if (stillEnabled(cr, mine)) {
                QLog.i(TAG_AREA, "✓ 自愈成功（顺序 A：先 enabled 后 services）")
                return true
            }
            writeAll(cr, enabledFirst = false, services = before)
            val ok2 = stillEnabled(cr, mine)
            if (ok2) QLog.i(TAG_AREA, "✓ 自愈成功（顺序 B：先 services 后 enabled）")
            else QLog.e(TAG_AREA, "✗ 两种写序都没生效，系统拦截了第三方写入。需要手动去系统设置里打开一次无障碍")
            return ok2
        }

        val after = if (before.isBlank()) mine else "$before:$mine"

        QLog.i(TAG_AREA, "检测到无障碍授权被清空（多半是从最近任务划掉了 App），开始写回")
        QLog.i(TAG_AREA, "  原有服务列表=${before.ifBlank { "(空)" }}")
        QLog.i(TAG_AREA, "  写回后=$after")

        // 顺序 A（MIUI）
        writeAll(cr, enabledFirst = true, services = after)
        if (stillEnabled(cr, mine)) {
            QLog.i(TAG_AREA, "✓ 自愈成功（顺序 A：先 enabled 后 services）")
            return true
        }

        // 顺序 B（ColorOS）
        QLog.i(TAG_AREA, "顺序 A 没生效，改试顺序 B（先 services 后 enabled）")
        writeAll(cr, enabledFirst = false, services = after)
        val ok = stillEnabled(cr, mine)
        if (ok) {
            QLog.i(TAG_AREA, "✓ 自愈成功（顺序 B：先 services 后 enabled）")
        } else {
            QLog.e(TAG_AREA, "✗ 两种写序都没生效，系统拦截了第三方写入。需要手动去系统设置里打开一次无障碍")
        }
        return ok
    }

    private fun writeAll(cr: android.content.ContentResolver, enabledFirst: Boolean, services: String): Boolean {
        val putEnabled = { Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_ENABLED, 1) }
        val putServices = {
            Settings.Secure.putString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, services)
        }
        val a: Boolean
        val b: Boolean
        if (enabledFirst) {
            a = putEnabled()
            b = putServices()
        } else {
            a = putServices()
            b = putEnabled()
        }
        QLog.i(TAG_AREA, "  write(enabledFirst=$enabledFirst) 两次写入结果=$a/$b")
        return a && b
    }

    private fun stillEnabled(cr: android.content.ContentResolver, mine: String): Boolean {
        val list = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
        val flag = Settings.Secure.getInt(cr, Settings.Secure.ACCESSIBILITY_ENABLED, 0)
        val inList = list.split(':').any {
            it.isNotBlank() && normalizeEntry(it).equals(normalizeEntry(mine), ignoreCase = true)
        }
        val ok = inList && flag == 1
        if (!ok) QLog.i(TAG_AREA, "复核：服务在列表=$inList  开关=$flag")
        return ok
    }

    /** 给设置页显示的一行状态。 */
    fun statusLine(context: Context): String {
        val enabled = isEnabled(context)
        val writable = canWrite(context)
        return when {
            enabled && writable -> "无障碍：已启用，且可自愈"
            enabled && !writable -> "无障碍：已启用；未授予 WRITE_SECURE_SETTINGS，掉授权后需要手动恢复"
            !enabled && writable -> "无障碍：未启用；本应用可以自己写回"
            else -> "无障碍：未启用；本机不允许本应用写回，需要手动去设置里打开"
        }
    }

    /** 本机标识，给日志用。 */
    fun deviceLine(): String = "PID=${Process.myPid()}"
}
