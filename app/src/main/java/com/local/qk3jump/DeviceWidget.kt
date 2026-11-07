package com.local.qk3jump

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * 桌面小部件：**一块 2×1，里面并排两台设备，每台各占 1×1 的一格**。
 *
 * ┌───────────────┬───────────────┐
 * │   💧 宿舍A     │   💧 宿舍B     │   ← 一块 2×1 组件（110dp × 40dp）
 * └───────────────┴───────────────┘
 *        点左半边              点右半边
 *
 * ── 左右两格放哪两台设备 ──
 * **不写死名字**，而是每次重画时读一次 [TargetStore.all]：
 * 第 1 台（最近用的）放左格，第 2 台放右格。这样：
 *   · 设备改名、增删、重排，桌面自动跟着变（靠 [refreshAll]）；
 *   · 换人用、换房间用，只要在 App 里改清单，不必重新编译；
 *   · 仓库里不会留下任何真实宿舍号（这是能公开的原因之一）。
 * 如果清单里不足两台，缺的那格显示「加一台」，点进去就是 [JumpActivity] 的加设备流程。
 *
 * ── 为什么走小部件这条路 ──
 * 「把一台机器放到桌面」原本有两条路，在这台 MIUI 机器上**都不通**（2026-11-06 实测）：
 *   1. `requestPinShortcut` 返回 true 却什么都不画（MIUI 屏蔽了展示层）；
 *   2. 旧版 `INSTALL_SHORTCUT` 广播被系统直接丢弃，logcat 原文：
 *      `W/ActivityManager: Broadcast com.android.launcher.action.INSTALL_SHORTCUT
 *       no longer supported. It will not be delivered.`
 * 小部件是 Android 1.5 就有、从未被废弃、MIUI 也完整支持的机制，所以只剩它。
 *
 * ── 为什么是「一个 provider + 内部两个可点区域」 ──
 * 早期版本是「两个 provider，各一块 2×1」，桌面上会占两行、两块的玻璃底板叠在一起，
 * 视觉上不像一件东西。现在改成一块 2×1 的整板，中间一条竖线分开，
 * 左半格点左设备、右半格点右设备 —— 这才和「一个组件、两个 1×1」对得上。
 *
 * 之所以不用 `android:configure` 配置页：`requestPinAppWidget` 在 MIUI 上是假成功
 * （`E/AddItemActivity-PinShortcutRequestUtils: add widget failed, ... has no permission`），
 * 只能长按桌面手动拖；手动拖时若有 configure 属性，拖完还要再选一次设备，
 * 多一步，还容易「拖到桌面才发现它要选」。而这里根本不需要选 —— 清单前两台就是它。
 *
 * ── 安全边界 ──
 * 小部件上只有**设备名字**。点击时把名字交给 [JumpActivity]，
 * 由它回 [TargetStore] 查、再走和手动点击完全相同的整条链路
 * （拉起胖乖 → 无障碍点「饮水」→ 点那一行的「立即使用」）。
 * 小部件不存 URL、不存 token、不存订单号，也不具备「绕过主流程直接动作」的能力。
 */
class DeviceWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        QLog.i("Widget", "小部件收到刷新：${appWidgetIds.size} 个实例")
        val views = build(context)
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    override fun onEnabled(context: Context) {
        QLog.i("Widget", "小部件被放到桌面（${slots(context).joinToString(" / ") { it ?: "空" }}）")
    }

    override fun onDisabled(context: Context) {
        QLog.i("Widget", "小部件被移除")
    }

    companion object {

        /** 桌面一次拉起的实例 ID（同一块 providers 的所有桌面副本）。 */
        private val PROVIDER = DeviceWidget::class.java

        /** 这块板子能放几台：左右各一，就是 2。 */
        private const val SLOT_COUNT = 2

        /**
         * 取「这一格该放哪台设备」：按 [TargetStore.all] 的顺序取前两台。
         *
         * 返回的列表长度永远是 [SLOT_COUNT]，缺的位置用 null 占位
         * （清单里只有 1 台时，右格是 null；一台都没有时两格都是 null）。
         */
        fun slots(context: Context): List<String?> {
            val names = TargetStore.all(context).map { it.name }
            return (0 until SLOT_COUNT).map { i -> names.getOrNull(i) }
        }

        /**
         * 目标簿一变（改名、删除、重排、新增）就调它，把桌面上所有实例重画一遍。
         *
         * 不这么做的话，用户把清单里的设备改名之后，桌面上会一直显示旧名字，
         * 而且点了会走向一台「已经不存在的设备」。宁可显示成「不在清单」。
         */
        fun refreshAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context) ?: return
            val ids = try {
                mgr.getAppWidgetIds(ComponentName(context, PROVIDER))
            } catch (t: Throwable) {
                QLog.w("Widget", "取小部件实例失败：${QLog.describe(t)}")
                return
            }
            if (ids.isEmpty()) return
            QLog.i("Widget", "目标簿变化，刷新 ${ids.size} 个实例")
            val views = build(context)
            ids.forEach { id -> mgr.updateAppWidget(id, views) }
        }

        /**
         * 画一块 2×1：整板玻璃 + 中间竖线 + 左右两个可点区域。
         *
         * 两个区域的点击目标**分别**绑 PendingIntent，requestCode 按设备名取哈希 ——
         * 用固定值的话，后创建的那个会把前一个覆盖掉，点哪半边都去同一台设备
         * （这是 RemoteViews 上最经典的坑）。
         */
        private fun build(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_devices)
            val names = slots(context)

            bindCell(
                context, views,
                cellId = R.id.widgetCellLeft,
                nameId = R.id.widgetNameLeft,
                iconId = R.id.widgetIconLeft,
                targetName = names.getOrNull(0)
            )
            bindCell(
                context, views,
                cellId = R.id.widgetCellRight,
                nameId = R.id.widgetNameRight,
                iconId = R.id.widgetIconRight,
                targetName = names.getOrNull(1)
            )
            return views
        }

        /**
         * 灌一格（1×1 的半边）。
         *
         * 1×1 里放不下「设备名 + 状态副标题」两行，所以状态改用**颜色 + 图标淡化**表达：
         *   · 正常（胖乖在、无障碍开着、名字还在）→ 名字纯白、水滴亮青
         *   · 任何一项不满足                     → 名字降到 55% 白、水滴降到 35% 透明度
         * 具体缺什么，点进去 [JumpActivity] 会在状态行里写清楚。
         */
        private fun bindCell(
            context: Context,
            views: RemoteViews,
            cellId: Int,
            nameId: Int,
            iconId: Int,
            targetName: String?
        ) {
            if (targetName == null) {
                // 清单里还没有这一台：显示「加一台」，点进去就是添加流程。
                views.setTextViewText(nameId, context.getString(R.string.widget_cell_empty))
                views.setTextColor(nameId, 0x8CFFFFFF.toInt())
                views.setInt(iconId, "setAlpha", 60)
                views.setOnClickPendingIntent(cellId, launchPending(context, null))
                views.setContentDescription(
                    cellId,
                    context.getString(R.string.widget_cell_empty_desc)
                )
                return
            }

            val exists = TargetStore.all(context).any { it.name == targetName }
            val ready = Qk.isInstalled(context) && exists && ScanTapService.isConnected(context)

            views.setTextViewText(nameId, if (exists) targetName else context.getString(R.string.widget_name_gone))
            views.setTextColor(nameId, if (ready) 0xFFFFFFFF.toInt() else 0x8CFFFFFF.toInt())
            views.setInt(
                iconId, "setAlpha",
                if (ready) 255 else 90
            )

            // 不管哪种状态都可点：点下去总是进 JumpActivity。
            // 名字不在了 / 胖乖没装 / 没授权，都在那一页里能改、能装、能开，
            // 不会出现「点了没反应」的死方块。
            views.setOnClickPendingIntent(cellId, launchPending(context, targetName))
            views.setContentDescription(
                cellId,
                context.getString(R.string.widget_cell_desc, targetName)
            )
        }

        /**
         * 一格的点击目标。
         *
         * targetName 为 null（空格子）时不带 EXTRA_TARGET —— [JumpActivity] 收到
         * 「没指定设备」的行为就是老老实实停在选设备页，正好是「加一台」想要的。
         *
         * requestCode 用名字哈希而不是固定值：两个 PendingIntent 用同一个 requestCode
         * 时系统认为它们是同一个，后建的覆盖先建的，点哪格都去同一台设备。
         */
        private fun launchPending(context: Context, targetName: String?): PendingIntent {
            val intent = Intent(context, JumpActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                if (targetName != null) putExtra(JumpActivity.EXTRA_TARGET, targetName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            return PendingIntent.getActivity(
                context,
                targetName?.hashCode() ?: 0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
