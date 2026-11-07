package com.local.qk3jump

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * 胖乖生活 App 的常量与探针。
 *
 * 关于「为什么是这条路」——
 * 胖乖的扫一扫页（HomeScanCodeAct）、设备页（PayStateAct）都是 exported=false，
 * 外部应用（包括本应用）无法用 am start 直接打开，被系统以
 *   SecurityException: Permission Denial: ... not exported from uid 10307
 * 拒绝。它也没有声明任何 http/https 的 host，所以系统不会把
 * https://h5.qiekj.com/skip?SN=... 交给它 —— 那条 URL 只会被浏览器接走，
 * 浏览器上是一个「下载 App」的引导页。
 *
 * 唯一走得通的入口是它自己的 MainActivity（exported=true，LAUNCHER），
 * 进去以后靠无障碍替用户点三下：
 *   首页 tab → 「饮水」分类 → 设备列表里那台机器的「立即使用」。
 * 这三次点击点的都是胖乖自己界面上真实存在的控件，等价于用户用手指点，
 * 不注入数据、不构造 Intent、不碰鉴权与计费。
 */
object Qk {

    const val PKG = "com.qiekj.user"

    /** 首页分类横条上的「饮水」。点它进入 MyDeviceAct（我的设备）。 */
    const val TEXT_CATEGORY_WATER = "饮水"

    /** 设备列表里每行的「立即使用」按钮（resource-id 精确匹配）。 */
    const val ID_USE_TEXT = "com.qiekj.user:id/tvUse"

    /** 设备列表里每行显示设备名的 TextView。 */
    const val ID_DEVICE_NAME = "com.qiekj.user:id/tvDeviceName"

    /** 设备列表里每行显示状态的 TextView（空闲 / 使用中）。 */
    const val ID_DEVICE_STATUS = "com.qiekj.user:id/tvStatus"

    /** 设备确认页（AfterPayUseAct）里的机器名。到了这一页就算成功。 */
    const val ID_MACHINE_NAME = "com.qiekj.user:id/tv_machine_name"

    /** 设备列表页右下角悬浮广告的关闭按钮。点它只关广告，不改变页面。 */
    const val ID_FLOAT_AD_CLOSE = "com.qiekj.user:id/channel_banner_close_iv"

    /** 会员营销页上「放弃优惠 原价使用」。这是**不订阅**的正确选择。 */
    const val TEXT_DECLINE_OFFER = "放弃优惠"

    /**
     * 会员营销页上「开通并领取优惠」。
     *
     * 这个按钮会开通自动续费（页面原文：「免费体验3天，到期前一天自动续费9.7元/月」）。
     * 本应用**永远不点它**。留成常量只是为了在日志和判定里显式地把它排除掉，
     * 让后来改代码的人一眼看到这条线在哪。
     */
    const val TEXT_SUBSCRIBE_OFFER = "开通并领取优惠"

    /** 首页的判据：这几个字出现在首页，说明还在首页，需要先点「饮水」。 */
    val HOME_MARKERS = listOf("附近设备", "胖乖福利助手")

    /** 设备列表页的判据。 */
    val DEVICE_LIST_MARKERS = listOf("最近使用设备", "查看附近设备")

    /** 设备确认页的判据。 */
    val CONFIRM_MARKERS = listOf("选择支付方式", "出水口")

    /** 会员营销页（TransparentWebViewAct）的判据。 */
    val MEMBER_OFFER_MARKERS = listOf("放弃优惠", "开通并领取优惠", "会员服务协议")

    /** 已经解锁成功的判据（DeviceStartupAct）。到了这里就撒手。 */
    val UNLOCKED_MARKERS = listOf("解锁成功", "结束后将自动为您计费")

    /**
     * 广告区域的 resource-id 黑名单。
     *
     * 这些容器里的东西**一个都不许点**。实测确认页顶部那条快手联盟广告
     * （「净水器无敌版本…/ 疯狂水世界 / 点击即玩」）的多个子节点都是
     * clickable=true，误点会直接跳去广告落地页，把整个流程带偏。
     * 所以 clickNode() 里有一道统一闸门：只要目标自己或祖先命中这张表，直接拒绝。
     *
     * 注意 rl_float_ad 自己 clickable=false，但它整个是广告位；
     * include_rebate_guide（抖音商城返积分）是首页那条 clickable=true 的推广条。
     */
    val AD_ZONE_IDS = setOf(
        "com.qiekj.user:id/fl_ad_container",
        "com.qiekj.user:id/ksad_container",
        "com.qiekj.user:id/rl_float_ad",
        "com.qiekj.user:id/include_rebate_guide"
    )

    /** 广告文案特征。命中这些字样的节点一律不点。 */
    val AD_TEXT_HINTS = listOf("点击即玩", "左滑点击即玩", "广告")

    fun isInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(PKG, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    } catch (t: Throwable) {
        QLog.e("Jump", "查胖乖是否安装时出错", t)
        false
    }

    fun versionName(context: Context): String = try {
        context.packageManager.getPackageInfo(PKG, 0).versionName ?: "未知"
    } catch (_: Throwable) {
        "未安装"
    }

    /**
     * 构造拉起胖乖的 Intent。
     *
     * 实测结论（2026-11-07，Redmi K40S / Android 14）：
     * 用「隐式 MAIN + CATEGORY_LAUNCHER + setPackage」这条路走不通 ——
     * resolveActivity 能查出 SplashActivity，但真去 startActivity 会抛
     *   ActivityNotFoundException: No Activity found to handle
     *   Intent { act=MAIN cat=[LAUNCHER] pkg=com.qiekj.user }
     * 而显式指定组件（setClassName）两条路都能成。
     * 所以这里直接优先用显式组件，隐式那条只作为极端兜底。
     *
     * 另外注意：不要拉 SplashActivity。它是启动广告/闪屏，会先播几秒再进首页，
     * 我们的待命窗口经不起这个浪费，直接点名 MainActivity 更稳。
     */
    fun buildLaunchIntent(context: Context): Intent? {
        val explicit = Intent().apply {
            setClassName(PKG, "$PKG.MainActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val r = context.packageManager.resolveActivity(explicit, 0)
        if (r != null) {
            QLog.i("Jump", "a) 显式入口可用：${r.activityInfo.packageName}/${r.activityInfo.name}")
            return explicit
        }
        QLog.w("Jump", "a) 显式 MainActivity 不可用，改试隐式 LAUNCHER")

        val probe = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(PKG)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val viaResolve = context.packageManager.resolveActivity(probe, 0)
        if (viaResolve != null) {
            QLog.i("Jump", "b) 隐式入口已解析：${viaResolve.activityInfo.name}（注意这条实测可能抛 ActivityNotFound）")
            return probe
        }
        QLog.e("Jump", "两条入口都不可用，胖乖可能被禁用了")
        return null
    }

    /** 把 Intent 拍平成一行，便于日志里直接看出来「到底要发什么」。 */
    fun dumpIntent(i: Intent): String =
        "action=${i.action} data=${i.data} pkg=${i.`package`} " +
            "component=${i.component?.flattenToShortString()} " +
            "categories=${i.categories} flags=0x${Integer.toHexString(i.flags)}"
}
