package com.local.qk3jump

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * 目标设备簿。
 *
 * V1 存的是 SN / NQT（那台机器的真实编号）；V3 走的这条路里，胖乖 App
 * 的设备列表上根本没有 SN 可读，只有它自己给设备起的名字（例如「北3栋201」）。
 * 所以这里存的是「名字」——它只是给你认设备用的一串字，不是凭证。
 *
 * 存的东西只有：名字 + 上次使用时间。没有 URL、没有 token、没有账号、
 * 没有订单号。点开设备页之后的一切都由胖乖和官方服务端决定。
 */
object TargetStore {

    const val MAX_TARGETS = 20

    private const val KEY_LIST = "targets"

    data class Target(val name: String, val time: Long) {
        val key: String get() = name
    }

    fun all(context: Context): List<Target> {
        val raw = Prefs.spRaw(context, KEY_LIST)
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            val out = ArrayList<Target>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val name = o.optString("name", "").trim()
                if (name.isEmpty()) continue
                out.add(Target(name, o.optLong("time", 0L)))
            }
            out.sortedByDescending { it.time }
        } catch (t: Throwable) {
            QLog.e("Memory", "目标簿解析失败，当作空", t)
            emptyList()
        }
    }

    fun remember(context: Context, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val list = all(context).toMutableList()
        val existing = list.indexOfFirst { Matcher.rank(trimmed, it.name) == 2 }
        if (existing >= 0) {
            val old = list.removeAt(existing)
            list.add(0, old.copy(time = System.currentTimeMillis()))
            QLog.i("Memory", "目标已存在，更新时间：$trimmed（共 ${list.size} 台）")
        } else {
            list.add(0, Target(trimmed, System.currentTimeMillis()))
            QLog.i("Memory", "记住新目标：$trimmed（共 ${list.size} 台）")
        }
        write(context, list.take(MAX_TARGETS))
    }

    fun forget(context: Context, key: String) {
        val list = all(context).filterNot { it.key == key }
        write(context, list)
        QLog.i("Memory", "已删除目标：$key（剩余 ${list.size} 台）")
    }

    fun rename(context: Context, key: String, alias: String) {
        val list = all(context).map { if (it.key == key) it.copy(name = alias.trim()) else it }
        write(context, list)
        QLog.i("Memory", "目标改名：$key → 「$alias」")
    }

    fun clear(context: Context) {
        write(context, emptyList())
        QLog.i("Memory", "已清空目标簿")
    }

    private fun write(context: Context, list: List<Target>) {
        val arr = JSONArray()
        list.forEach { t ->
            arr.put(JSONObject().apply {
                put("name", t.name)
                put("time", t.time)
            })
        }
        Prefs.setSpRaw(context, KEY_LIST, arr.toString())
    }

    /**
     * 首次启动时的默认值：两台**占位名**，不是真实设备。
     *
     * 这里刻意不写真实宿舍号 —— 一是仓库要公开，二是写死的名字换台机器就用不了。
     * 占位名的作用只是让首屏和小部件有东西可显示、能点进改名流程。
     *
     * 改成你自己的：在 App 首页点那一行右侧「⋯」→ 改名，
     * 名字要和胖乖 App「我的设备」里显示的**一模一样**（例如它显示「北3栋201」，
     * 你就写「北3栋201」；写「北三201」也能匹配，见 [Matcher.rank] 的规整规则）。
     */
    fun seedIfEmpty(context: Context) {
        if (all(context).isNotEmpty()) return
        val now = System.currentTimeMillis()
        write(context, listOf(
            Target("宿舍A", now),
            Target("宿舍B", now - 1000L)
        ))
        QLog.i("Memory", "目标簿为空，写入两台占位设备：宿舍A / 宿舍B（请改成你自己的）")
    }
}
