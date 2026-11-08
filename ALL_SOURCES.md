# 胖乖直达（V3）· 全部源码

一次提交里所有文本文件的完整内容，共 37 个文件。每个文件都从第一行到最后一行，无省略。

| # | 文件 | 行数 |
|---|---|---|
| 1 | `settings.gradle.kts` | 18 |
| 2 | `build.gradle.kts` | 4 |
| 3 | `gradle.properties` | 6 |
| 4 | `app/build.gradle.kts` | 49 |
| 5 | `app/proguard-rules.pro` | 1 |
| 6 | `app/src/main/AndroidManifest.xml` | 112 |
| 7 | `app/src/main/java/com/local/qk3jump/QLog.kt` | 39 |
| 8 | `app/src/main/java/com/local/qk3jump/Prefs.kt` | 99 |
| 9 | `app/src/main/java/com/local/qk3jump/Qk.kt` | 175 |
| 10 | `app/src/main/java/com/local/qk3jump/Matcher.kt` | 88 |
| 11 | `app/src/main/java/com/local/qk3jump/JumpWindow.kt` | 117 |
| 12 | `app/src/main/java/com/local/qk3jump/ScanTapService.kt` | 986 |
| 13 | `app/src/main/java/com/local/qk3jump/A11yGuard.kt` | 211 |
| 14 | `app/src/main/java/com/local/qk3jump/TargetStore.kt` | 109 |
| 15 | `app/src/main/java/com/local/qk3jump/JumpActivity.kt` | 354 |
| 16 | `app/src/main/java/com/local/qk3jump/SettingsActivity.kt` | 217 |
| 17 | `app/src/main/java/com/local/qk3jump/DeviceWidget.kt` | 212 |
| 18 | `app/src/main/res/xml/accessibility_service_config.xml` | 23 |
| 19 | `app/src/main/res/xml/widget_devices_info.xml` | 23 |
| 20 | `app/src/main/res/values/strings.xml` | 87 |
| 21 | `app/src/main/res/values/themes.xml` | 13 |
| 22 | `app/src/main/res/layout/activity_jump.xml` | 124 |
| 23 | `app/src/main/res/layout/item_target.xml` | 47 |
| 24 | `app/src/main/res/layout/activity_settings.xml` | 265 |
| 25 | `app/src/main/res/layout/widget_devices.xml` | 122 |
| 26 | `app/src/main/res/drawable/bg_app.xml` | 13 |
| 27 | `app/src/main/res/drawable/bg_glass.xml` | 6 |
| 28 | `app/src/main/res/drawable/bg_button.xml` | 15 |
| 29 | `app/src/main/res/drawable/bg_button_outline.xml` | 17 |
| 30 | `app/src/main/res/drawable/bg_row.xml` | 17 |
| 31 | `app/src/main/res/drawable/bg_widget_devices.xml` | 24 |
| 32 | `app/src/main/res/drawable/ic_widget_drop.xml` | 23 |
| 33 | `app/src/main/res/drawable/ic_launcher_background.xml` | 10 |
| 34 | `app/src/main/res/drawable/ic_launcher_foreground.xml` | 30 |
| 35 | `gradlew` | 249 |
| 36 | `gradlew.bat` | 92 |
| 37 | `gradle/wrapper/gradle-wrapper.properties` | 7 |

---

## 1. `settings.gradle.kts`（18 行）

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Qk3Jump"
include(":app")
```

## 2. `build.gradle.kts`（4 行）

```kotlin
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
```

## 3. `gradle.properties`（6 行）

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
android.useAndroidX=true
android.nonTransitiveRClass=true
android.defaults.buildfeatures.buildconfig=false
```

## 4. `app/build.gradle.kts`（49 行）

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.local.qk3jump"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.local.qk3jump"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}
```

## 5. `app/proguard-rules.pro`（1 行）

```proguard
-keepattributes SourceFile,LineNumberTable
```

## 6. `app/src/main/AndroidManifest.xml`（112 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<!--
    胖乖直达（V3）。

    红线写在这里，改动前先读一遍：
      1. 只做「入口替换 + 界面辅助点击」。点的是胖乖自己界面上真实存在的控件
         （首页的「饮水」分类、设备列表里的「立即使用」、确认页的「领优惠券使用」、
         会员页的「放弃优惠 原价使用」），等价于用户用手指点。
      2. 不抓包、不逆向私有接口、不构造订单、不模拟支付、不碰账号与登录态。
      3. 自动解锁默认关闭，打开要过一次显式风险确认。关着时到设备确认页就撒手。
      4. 永远不点会员页的「开通并领取优惠」（会开通 9.7 元/月自动续费）。
      5. 不出水。自动解锁最多走到「解锁成功」，出水必须按饮水机机身上的实体键。
      6. 不走支付宝小程序：这条路不需要支付宝登录。
      7. 只存设备名字，不存 URL / token / 订单号。

    权限只要一条：
      WRITE_SECURE_SETTINGS —— 只用于「掉授权后自己写回无障碍开关」，
                                装包时永不授予，不授予时一切照旧只是不能自愈。
-->
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission
        android:name="android.permission.WRITE_SECURE_SETTINGS"
        tools:ignore="ProtectedPermissions" />

    <!--
        Android 11 起的包可见性：不声明就查不到胖乖的安装状态与入口，
        resolveActivity 会恒为 null，导致误判成「没装」。
    -->
    <queries>
        <package android:name="com.qiekj.user" />
        <intent>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent>
    </queries>

    <application
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher"
        android:supportsRtl="true"
        android:theme="@style/Theme.Qk3Jump">

        <!--
            唯一的启动页。它就是目标设备列表 —— 打开即见。
        -->
        <activity
            android:name=".JumpActivity"
            android:exported="true"
            android:launchMode="singleTask"
            android:screenOrientation="portrait"
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:theme="@style/Theme.Qk3Jump">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".SettingsActivity"
            android:exported="false"
            android:label="@string/settings_title"
            android:parentActivityName=".JumpActivity"
            android:theme="@style/Theme.Qk3Jump" />

        <!--
            无障碍服务。exported 必须是 false，并且必须带 BIND_ACCESSIBILITY_SERVICE
            这个系统级权限 —— 只有系统能绑，任何第三方都绑不上。
        -->
        <service
            android:name=".ScanTapService"
            android:exported="false"
            android:label="@string/a11y_label"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>

        <!--
            桌面上唯一一块 2×1 小部件：左边是清单第 1 台、右边是第 2 台，
            每格 1×1，各点各的。

            exported 必须是 true：启动器是别的进程，它要来问「这个 receiver 收不收
            APPWIDGET_UPDATE」，不收就永远画不出来。

            刻意不写 android:configure —— 见 DeviceWidget.kt 顶部注释：
            这台机器只能手动拖，没有配置页时拖出来就是成品，少一次点击。
            两台设备编译期定死，不给用户在拖拽时选。
        -->
        <receiver
            android:name=".DeviceWidget"
            android:exported="true"
            android:label="@string/widget_devices_label">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/widget_devices_info" />
        </receiver>

    </application>

</manifest>
```

## 7. `app/src/main/java/com/local/qk3jump/QLog.kt`（39 行）

```kotlin
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
```

## 8. `app/src/main/java/com/local/qk3jump/Prefs.kt`（99 行）

```kotlin
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
```

## 9. `app/src/main/java/com/local/qk3jump/Qk.kt`（175 行）

```kotlin
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

    /**
     * 设备确认页上「选择支付方式」下面那排可选项（rv_pay_mode 里的 RecyclerView）。
     * 每一项的标题都是 [ID_PAY_NAME]。
     */
    const val ID_PAY_NAME = "com.qiekj.user:id/tvPayName"

    /**
     * 我们希望在确认页上**选中**的支付方式。
     *
     * 为什么必须主动选：实测同一台手机、同一台饮水机，不同设备（不同商户）的
     * 默认支付方式不一样。某台热水机默认是「云闪付免密支付」，而这台手机只装了
     * 云闪付的 SE 组件（com.unionpay.tsmservice.mi），没有云闪付 App 本体，
     * 于是点「立即使用」只会弹一个 Toast「请先下载安装云闪付App」，页面纹丝不动。
     * 而 801 默认就是「支付宝支付」，所以一直没暴露这个问题。
     *
     * 结论：不能依赖默认值，也不能指望胖乖记住上次的选择（实测重进确认页
     * 又变回云闪付）。凡是看到有「支付宝支付」这一项、且它没被选中，就先点它，
     * 点完下一轮再点「立即使用」。
     */
    const val TEXT_PAY_ALIPAY = "支付宝支付"

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
```

## 10. `app/src/main/java/com/local/qk3jump/Matcher.kt`（88 行）

```kotlin
package com.local.qk3jump

/**
 * 设备名匹配。
 *
 * 为什么需要一个专门的文件：胖乖 App 的设备列表里显示的名字，
 * 和你嘴里叫的名字，往往不是一个写法。实测某台机器上它显示的是
 *
 *     北3栋201 / 工楼3-2 / 201
 *
 * 而用户习惯说「北三201」。如果按字符串全等去匹配，永远匹配不上。
 *
 * 做法：把两边都规整成「只剩数字和关键字」的形态再比。
 *   北三201   -> 北3201
 *   北3栋201  -> 北3201     ← 栋 被剥掉
 *   201       -> 201
 *
 * 比较顺序：先找规整后完全相等的，再找互相包含的。
 * 这样「201」不会抢走「北3栋201」的那一行，反过来也一样。
 *
 * 注意这里只做匹配，不做任何模糊到可能点错机器的猜测 ——
 * 点错一台机器会开出别人的水，这是不能接受的，所以宁可不点。
 */
object Matcher {

    /** 中文数字 → 阿拉伯数字。只处理 0~9，设备名里不会出现更大的单字。 */
    private val CN_DIGITS = mapOf(
        '零' to '0', '〇' to '0',
        '一' to '1', '二' to '2', '两' to '2', '三' to '3', '四' to '4',
        '五' to '5', '六' to '6', '七' to '7', '八' to '8', '九' to '9'
    )

    /** 这些字符只是位置描述，各人写法不同，比对前一律剥掉。 */
    private val NOISE = charArrayOf('栋', '楼', '室', '号', '层', '-', '－', '_', ' ', '（', '）', '(', ')')

    /**
     * 规整一串设备名。
     * 返回的串只保留：中文数字转成的数字、阿拉伯数字、以及其它汉字字母。
     */
    fun normalize(raw: String): String {
        if (raw.isBlank()) return ""
        val sb = StringBuilder(raw.length)
        for (ch in raw) {
            when {
                ch in NOISE -> Unit
                CN_DIGITS.containsKey(ch) -> sb.append(CN_DIGITS[ch])
                else -> sb.append(ch)
            }
        }
        return sb.toString().lowercase()
    }

    /**
     * target（用户设置的目标设备）与 shown（设备列表里显示的名字）的匹配强度。
     *
     *   2 = 规整后完全相等      —— 唯一可靠，优先采用
     *   1 = 一方包含另一方      —— 允许「201」匹配「201(热水)」这种带后缀的写法
     *   0 = 不匹配
     *
     * 为什么不能只用「包含」：某台机器上设备列表里同时有
     *     北3栋201  -> 规整后 北3201
     *     201       -> 规整后 201
     * 「201」是「北3201」的子串，纯包含判断会让目标 201 误吃第一行，
     * 结果是点开别人那台机器的水 —— 这是不能接受的。
     * 所以调用方必须先在所有行里找强度 2 的，找不到才退而用强度 1。
     */
    fun rank(target: String, shown: String): Int {
        val t = normalize(target)
        val s = normalize(shown)
        if (t.isEmpty() || s.isEmpty()) return 0
        if (t == s) return 2

        val (shorter, longer) = if (t.length <= s.length) t to s else s to t
        // 太短的串不做包含匹配，否则「8」「01」这类会吃掉整页。
        if (shorter.length < 3) return 0
        return if (longer.contains(shorter)) 1 else 0
    }

    /** 只要「是同一台」这个布尔判断时用这个。 */
    fun sameDevice(target: String, shown: String): Boolean = rank(target, shown) > 0

    /**
     * 给日志用的说明：把两侧的原始值和规整值一起打出来，
     * 排查「为什么没匹配上」时一眼就能看出是哪一步的问题。
     */
    fun describe(target: String, shown: String): String =
        "目标「$target」→规整「${normalize(target)}」  页面「$shown」→规整「${normalize(shown)}」"
}
```

## 11. `app/src/main/java/com/local/qk3jump/JumpWindow.kt`（117 行）

```kotlin
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
```

## 12. `app/src/main/java/com/local/qk3jump/ScanTapService.kt`（986 行）

```kotlin
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

    /**
     * 确认页上「把支付方式改成支付宝」这件事做没做过。
     *
     * 单独一个标记，不复用 STAGE_CONFIRM_USE：改支付方式可能发生多次
     * （万一用户中途手动改回去了），而「点立即使用」每次待命只该发生一次。
     */
    @Volatile private var payPickedAt = 0L

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

        /**
         * 确认页「选择支付方式」里那一行「支付宝支付」。
         *
         * 有它就先点它 —— 不同设备（不同商户）的默认支付方式不一样，某台热水机默认
         * 「云闪付免密支付」，而这台手机没装云闪付 App，点「立即使用」只会弹
         * 「请先下载安装云闪付App」然后原地不动。801 默认就是支付宝，所以从没暴露。
         *
         * 没读到「已选中」这个状态也照点：点一个已经选中的单选行是无害的，
         * 而读不到状态就跳过则会重新掉进 热水机那个坑里。
         */
        var payAlipayRow: AccessibilityNodeInfo? = null

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
            payPickedAt = 0L
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

        // ── 「立即使用」点了没反应时，才去改支付方式 ──
        //
        // 为什么不是一上来就改：实测点「支付宝支付」那一行**即使它已经选中**，
        // 也会弹出「你有3天免费会员待领取 / [放弃优惠] [立即领取]」的营销框。
        // 801 默认就是支付宝，本来一次就能过，先点那一行反而凭空多出一个
        // 需要处理的会员弹框（点错就是自动续费）。所以只在「已经点过使用按钮、
        // 等够了、页面还赖在确认页」这个条件下才动支付方式 —— 那正是热水机的情形。
        //
        // 热水机的原因：它的默认支付方式是被选中的「云闪付免密支付」，而这台手机
        // 只有云闪付的 SE 组件、没有 App 本体，点「立即使用」只弹一个 Toast
        // 「请先下载安装云闪付App」，页面纹丝不动，看起来就像卡死。
        // 也不能指望胖乖记住选择：实测重新进入确认页又变回云闪付。
        val payRow = scan.payAlipayRow
        val useClickStuck = confirmClickedAt > 0L &&
            SystemClock.elapsedRealtime() - confirmClickedAt >= RETRY_AFTER_MS
        if (payRow != null && payPickedAt == 0L && useClickStuck) {
            if (Prefs.dryRun(this)) {
                note("调试模式：本应在点不动时把支付方式改成「${Qk.TEXT_PAY_ALIPAY}」，未动手")
                JumpWindow.noteStep("调试：确认页，未改支付方式")
                return
            }
            QLog.i("Tap", "点「立即使用」没反应，怀疑支付方式不对，先拨到「${Qk.TEXT_PAY_ALIPAY}」")
            val picked = clickNode(payRow)
            QLog.i("Tap", "点「${Qk.TEXT_PAY_ALIPAY}」那一行结果=$picked")
            if (picked) {
                payPickedAt = SystemClock.elapsedRealtime()
                // 改了支付方式，之前那次失败不算数，重给 3 次机会。
                JumpWindow.resetStage(STAGE_CONFIRM_USE)
                confirmClickedAt = 0L
                confirmAttempts = 0
                note("已把支付方式改成「${Qk.TEXT_PAY_ALIPAY}」，重新点使用按钮")
                JumpWindow.noteStep("确认页：已改支付方式")
            } else {
                QLog.w("Tap", "没点中支付方式那一行，下一轮再试")
            }
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
            result.payAlipayRow = findPayRow(root, Qk.TEXT_PAY_ALIPAY)
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

    /**
     * 在确认页的「选择支付方式」里找指定的那一行（例如「支付宝支付」）。
     *
     * 按 resource-id + 文本全等双重匹配，不做模糊包含：
     * 这一排里同时可能有「支付宝支付」「云闪付免密支付」「免费饮水」，
     * 用包含匹配很容易把「云闪付免密支付」也当成目标。
     *
     * 找不到就返回 null —— 有些设备/商户只有一种支付方式，没有可选项，
     * 这时不该挡路，直接往下走点「立即使用」。
     */
    private fun findPayRow(root: AccessibilityNodeInfo, payName: String): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0
        while (queue.isNotEmpty() && visited < MAX_NODES) {
            val n = queue.removeFirst()
            visited++
            if (n.viewIdResourceName == Qk.ID_PAY_NAME &&
                n.text?.toString() == payName &&
                n.isEnabled
            ) {
                return n
            }
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
```

## 13. `app/src/main/java/com/local/qk3jump/A11yGuard.kt`（211 行）

```kotlin
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
```

## 14. `app/src/main/java/com/local/qk3jump/TargetStore.kt`（109 行）

```kotlin
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
```

## 15. `app/src/main/java/com/local/qk3jump/JumpActivity.kt`（354 行）

```kotlin
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
```

## 16. `app/src/main/java/com/local/qk3jump/SettingsActivity.kt`（217 行）

```kotlin
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
```

## 17. `app/src/main/java/com/local/qk3jump/DeviceWidget.kt`（212 行）

```kotlin
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
```

## 18. `app/src/main/res/xml/accessibility_service_config.xml`（23 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<!--
    无障碍服务配置。

    几个刻意的选择：
      · canPerformGestures="true"   —— 设备列表将来若改成 WebView / Compose 渲染，
                                      控件本身不可点击，只能靠坐标手势。现在是
                                      ACTION_CLICK 就能点，但留着这一位不吃亏。
      · 不写 packageNames           —— 把过滤放在代码里做。写在 XML 里的话，
                                      日志里看不出「为什么这个事件被忽略了」；
                                      放代码里就能逐条打出来。
      · settingsActivity            —— 系统设置里点「设置」会回到本应用的设置页。
      · notificationTimeout="100"   —— 事件合并窗口。胖乖首页控件多，太大会丢事件。
-->
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged|typeWindowContentChanged|typeViewScrolled"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagReportViewIds|flagIncludeNotImportantViews"
    android:canPerformGestures="true"
    android:canRetrieveWindowContent="true"
    android:description="@string/a11y_desc"
    android:notificationTimeout="100"
    android:settingsActivity="com.local.qk3jump.SettingsActivity" />
```

## 19. `app/src/main/res/xml/widget_devices_info.xml`（23 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<!--
  2×1 设备小部件的声明：一块板，里面并排两台设备，每台 1×1。

  尺寸换算见 res/layout/widget_devices.xml 顶部注释：
  110dp × 40dp = 2 格宽 × 1 格高。

  updatePeriodMillis=0：不靠系统定时刷新（系统最快也只给 30 分钟一次，没意义）。
  真正需要重画的时候是「目标簿被改名/删除」，那种时候由 DeviceWidget.refreshAll 主动推。

  刻意不写 android:configure —— 没有配置页。
  左右两格取设备清单的前两台（左=第 1 台、右=第 2 台），清单变了会自动重画。
-->
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/widget_devices_desc"
    android:initialLayout="@layout/widget_devices"
    android:minHeight="40dp"
    android:minWidth="110dp"
    android:previewLayout="@layout/widget_devices"
    android:targetCellHeight="1"
    android:targetCellWidth="2"
    android:updatePeriodMillis="0"
    android:widgetCategory="home_screen" />
```

## 20. `app/src/main/res/values/strings.xml`（87 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <string name="app_name">胖乖直达</string>

    <!-- 启动页 -->
    <string name="jump_title">胖乖直达</string>
    <string name="targets_empty">还没有设备。点下面的「加一台」写下设备名。</string>
    <string name="btn_add_target">加一台</string>
    <string name="btn_clear_targets">清空全部</string>

    <!-- 状态行 -->
    <string name="status_ready">点一台机器，直接进它的设备页（共 %1$d 台）</string>
    <string name="status_no_a11y">无障碍服务没开，点了也到不了。点这一行去打开它</string>
    <string name="status_ready_none">点一台机器，直接进它的设备页</string>
    <string name="status_no_qk">这台手机没装胖乖生活 App</string>
    <string name="status_dry">调试模式：本应带你去「%1$s」，但不会真的点</string>
    <string name="status_going">正在带你去「%1$s」…</string>

    <!-- 行菜单 -->
    <string name="target_menu_title">这台设备</string>
    <string name="target_more">⋯</string>
    <string name="target_rename">改个名字</string>
    <string name="target_delete">删除这一台</string>
    <string name="target_add">加一台设备</string>
    <string name="target_clear">清空全部</string>
    <string name="target_clear_confirm">会删掉全部 %1$d 台。只删名字，不碰任何账号数据。</string>
    <string name="dialog_target_hint">照胖乖 App 里显示的名字写，比如 北3栋201</string>

    <!-- Toast -->
    <string name="toast_blank_name">名字不能是空的</string>
    <string name="toast_no_entry">找不到胖乖生活的入口，可能被禁用了</string>
    <string name="toast_launch_failed">拉起胖乖失败，看 logcat 里的 Qk3Jump</string>
    <string name="toast_a11y_settings_failed">打不开系统设置，请手动到「设置 → 无障碍」</string>

    <!-- 设置页 -->
    <string name="settings_title">设置</string>
    <string name="set_section_behavior">行为</string>
    <string name="set_dry_run">调试模式（只看不动）</string>
    <string name="set_dry_run_note">只记录会点哪一行、会拉起什么，不真的点。开着它验证匹配是否正确，不会开出任何水。</string>
    <string name="set_step_wait">每一步之间等 %1$d 毫秒</string>
    <string name="set_step_wait_value">每一步之间等 %1$d 毫秒</string>
    <string name="set_step_wait_note">机器慢、页面渲染慢的时候往右调。太小会点空，太大会觉得卡。</string>
    <string name="set_window">待命窗口 %1$d 秒</string>
    <string name="set_window_value">待命窗口 %1$d 秒</string>
    <string name="set_window_note">从拉起胖乖开始，多久之内允许自动点击。超过就撒手，不会在你手动用 App 时突然点一下。</string>

    <string name="set_auto_unlock">自动解锁（会真的开始计费）</string>
    <string name="set_auto_unlock_note">默认关闭。打开后会在设备确认页替你点下「领优惠券使用」，遇到会员营销页点「放弃优惠 原价使用」，直到出现「解锁成功」为止。到了那一步设备就已经解锁并开始计费 —— 但出水还要按饮水机机身上的「出水」键，本应用不会、也没有能力替你出水。</string>
    <string name="set_unlock_delay">解锁等待 %1$d 秒</string>
    <string name="set_unlock_delay_note">确认页渲染慢的时候往右调。太早点下去会静默无效，太大会觉得卡。</string>
    <string name="set_auto_unlock_confirm">打开后，本应用会在设备确认页替你点下「领优惠券使用」。\n\n遇到会员营销页时，它会点「放弃优惠 原价使用」——不会点「开通并领取优惠」，所以不会给你开通自动续费。\n\n点完就到了「解锁成功」，设备从这一刻起开始计费。出水仍然要按饮水机机身上的「出水」键，本应用不会替你出水，也不会替你结束。\n\n只在你自己的机器上用。</string>
    <string name="set_auto_unlock_ok">我明白，打开</string>

    <string name="set_section_a11y">无障碍</string>
    <string name="set_open_a11y">打开系统的无障碍设置</string>
    <string name="set_heal_a11y">尝试自动恢复授权</string>
    <string name="set_a11y_note">从最近任务划掉本应用或强行停止，系统会清空无障碍授权。拿到 WRITE_SECURE_SETTINGS 后本应用会自己写回来，该权限只能 adb 给：\nadb shell pm grant com.local.qk3jump android.permission.WRITE_SECURE_SETTINGS</string>

    <string name="a11y_heal_ok">已写回，系统会在数秒内绑上</string>
    <string name="a11y_heal_failed">两种写入顺序都没生效，系统拦了。请手动去设置里开一次</string>
    <string name="a11y_heal_no_perm">没有 WRITE_SECURE_SETTINGS，无法自己写回</string>

    <string name="set_section_about">关于</string>
    <string name="set_about">本应用只做一件事：把你领到胖乖的设备确认页。它点的是胖乖界面上真实的「饮水」和设备名旁的「立即使用」，不会点确认页上那个开始出水的按钮，也不碰账号、订单与计费。</string>
    <string name="set_last">上次结果：%1$s</string>
    <string name="set_last_none">（还没有跑过）</string>

    <string name="set_reset">恢复默认</string>
    <string name="set_reset_confirm">调试模式关掉，步进 1200 毫秒，待命窗口 25 秒。设备清单不动。</string>

    <!-- 无障碍服务在系统设置里的展示 -->
    <string name="a11y_label">胖乖直达：自动点「饮水」与设备</string>
    <string name="a11y_desc">在胖乖生活 App 里替你点三下：首页的「饮水」、设备列表里目标那台的「立即使用」。只在你自己按过启动之后、25 秒窗口内生效，且只点你设置的那一台。不会点任何开始出水或支付的按钮。</string>

    <!-- 2×1 桌面小部件：一块板，左右各一格 1×1，每格一台设备 -->
    <string name="widget_devices_label">两台饮水机</string>
    <string name="widget_devices_desc">左边是清单第 1 台、右边是第 2 台，点哪半边就去哪台</string>
    <string name="widget_cell_desc">直达「%1$s」</string>
    <!-- 设备被改名/删掉之后，那一格显示这个代替名字 -->
    <string name="widget_name_gone">不在清单</string>
    <!-- 清单里还没凑够两台时，空着的那一格 -->
    <string name="widget_cell_empty">加一台</string>
    <string name="widget_cell_empty_desc">去添加第二台设备</string>
    <string name="toast_widget_target_gone">小部件要去的「%1$s」已经不在设备清单里了</string>

</resources>
```

## 21. `app/src/main/res/values/themes.xml`（13 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <style name="Theme.Qk3Jump" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">#5B8CFF</item>
        <item name="colorSecondary">#8FD8FF</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowLightStatusBar">false</item>
        <item name="android:windowBackground">@drawable/bg_app</item>
    </style>

</resources>
```

## 22. `app/src/main/res/layout/activity_jump.xml`（124 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<!--
    启动页：目标设备列表。

    整个应用就是这一屏 —— 打开即见，点一下就出发。
    上面一行状态提示，中间是可点的设备行，底部是「加一台 / 清空全部」。
-->
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true"
    android:scrollbars="none">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:paddingStart="24dp"
        android:paddingTop="96dp"
        android:paddingEnd="24dp"
        android:paddingBottom="32dp">

        <!-- 标题行 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="center_vertical"
            android:orientation="horizontal">

            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:fontFamily="sans-serif-medium"
                android:letterSpacing="0.02"
                android:text="@string/jump_title"
                android:textColor="#FFFFFF"
                android:textSize="30sp" />

            <TextView
                android:id="@+id/btnSettings"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:background="@drawable/bg_button_outline"
                android:paddingStart="18dp"
                android:paddingTop="9dp"
                android:paddingEnd="18dp"
                android:paddingBottom="9dp"
                android:text="@string/settings_title"
                android:textColor="#E6FFFFFF"
                android:textSize="13sp" />
        </LinearLayout>

        <!-- 状态行。无障碍没开时它变成可点的入口 -->
        <TextView
            android:id="@+id/tvStatus"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="14dp"
            android:lineSpacingExtra="3dp"
            android:textColor="#B3FFFFFF"
            android:textSize="13sp"
            android:text="@string/status_ready_none" />

        <ProgressBar
            android:id="@+id/progress"
            style="?android:attr/progressBarStyleHorizontal"
            android:layout_width="match_parent"
            android:layout_height="2dp"
            android:layout_marginTop="14dp"
            android:indeterminate="true"
            android:indeterminateTint="#8FD8FF"
            android:visibility="gone" />

        <!-- 设备行容器 -->
        <LinearLayout
            android:id="@+id/containerTargets"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="26dp"
            android:orientation="vertical" />

        <TextView
            android:id="@+id/tvTargetsEmpty"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="26dp"
            android:background="@drawable/bg_glass"
            android:gravity="center"
            android:lineSpacingExtra="4dp"
            android:paddingStart="20dp"
            android:paddingTop="34dp"
            android:paddingEnd="20dp"
            android:paddingBottom="34dp"
            android:text="@string/targets_empty"
            android:textColor="#99FFFFFF"
            android:textSize="14sp"
            android:visibility="gone" />

        <!-- 底部两个动作 -->
        <TextView
            android:id="@+id/btnAddTarget"
            android:layout_width="match_parent"
            android:layout_height="54dp"
            android:layout_marginTop="30dp"
            android:background="@drawable/bg_button"
            android:gravity="center"
            android:text="@string/btn_add_target"
            android:textColor="#FFFFFF"
            android:textSize="16sp" />

        <TextView
            android:id="@+id/btnClearTargets"
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:layout_marginTop="12dp"
            android:background="@drawable/bg_button_outline"
            android:gravity="center"
            android:text="@string/btn_clear_targets"
            android:textColor="#CCFFFFFF"
            android:textSize="14sp" />

    </LinearLayout>
</ScrollView>
```

## 23. `app/src/main/res/layout/item_target.xml`（47 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<!--
    设备列表里的一行。

    只有一个名字和右边一个「⋯」，没有副标题 —— 手机上这行字本来就不多，
    多写一行小字只会让人分不清点哪。名字本身就是你要认的那台机器。
-->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="10dp"
    android:background="@drawable/bg_row"
    android:clickable="true"
    android:focusable="true"
    android:gravity="center_vertical"
    android:orientation="horizontal"
    android:paddingStart="22dp"
    android:paddingTop="20dp"
    android:paddingEnd="14dp"
    android:paddingBottom="20dp">

    <TextView
        android:id="@+id/tvTargetName"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:ellipsize="end"
        android:fontFamily="sans-serif-medium"
        android:maxLines="1"
        android:textColor="#FFFFFF"
        android:textSize="17sp" />

    <TextView
        android:id="@+id/btnTargetMore"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="@drawable/bg_button_outline"
        android:minWidth="0dp"
        android:paddingStart="14dp"
        android:paddingTop="6dp"
        android:paddingEnd="14dp"
        android:paddingBottom="6dp"
        android:text="@string/target_more"
        android:textColor="#CCFFFFFF"
        android:textSize="12sp" />

</LinearLayout>
```

## 24. `app/src/main/res/layout/activity_settings.xml`（265 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true"
    android:scrollbars="none">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:paddingStart="24dp"
        android:paddingTop="96dp"
        android:paddingEnd="24dp"
        android:paddingBottom="36dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:fontFamily="sans-serif-medium"
            android:text="@string/settings_title"
            android:textColor="#FFFFFF"
            android:textSize="28sp" />

        <!-- 行为 -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="30dp"
            android:layout_marginBottom="10dp"
            android:letterSpacing="0.06"
            android:text="@string/set_section_behavior"
            android:textColor="#8FD8FF"
            android:textSize="11sp" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@drawable/bg_glass"
            android:orientation="vertical"
            android:paddingStart="20dp"
            android:paddingTop="18dp"
            android:paddingEnd="20dp"
            android:paddingBottom="18dp">

            <com.google.android.material.switchmaterial.SwitchMaterial
                android:id="@+id/cbDryRun"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="@string/set_dry_run"
                android:textColor="#FFFFFF"
                android:textSize="15sp" />

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                android:lineSpacingExtra="3dp"
                android:text="@string/set_dry_run_note"
                android:textColor="#80FFFFFF"
                android:textSize="11sp" />

            <TextView
                android:id="@+id/tvStepWait"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="20dp"
                android:textColor="#FFFFFF"
                android:textSize="14sp" />

            <SeekBar
                android:id="@+id/sbStepWait"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="6dp" />

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:lineSpacingExtra="3dp"
                android:text="@string/set_step_wait_note"
                android:textColor="#80FFFFFF"
                android:textSize="11sp" />

            <TextView
                android:id="@+id/tvWindow"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="18dp"
                android:textColor="#FFFFFF"
                android:textSize="14sp" />

            <SeekBar
                android:id="@+id/sbWindow"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="6dp" />

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:lineSpacingExtra="3dp"
                android:text="@string/set_window_note"
                android:textColor="#80FFFFFF"
                android:textSize="11sp" />

            <View
                android:layout_width="match_parent"
                android:layout_height="1dp"
                android:layout_marginTop="20dp"
                android:background="#1AFFFFFF" />

            <com.google.android.material.switchmaterial.SwitchMaterial
                android:id="@+id/cbAutoUnlock"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:text="@string/set_auto_unlock"
                android:textColor="#FFFFFF"
                android:textSize="15sp" />

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                android:lineSpacingExtra="3dp"
                android:text="@string/set_auto_unlock_note"
                android:textColor="#FFB4A0"
                android:textSize="11sp" />

            <TextView
                android:id="@+id/tvUnlockDelay"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="14dp"
                android:textColor="#FFFFFF"
                android:textSize="14sp" />

            <SeekBar
                android:id="@+id/sbUnlockDelay"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="6dp" />

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:lineSpacingExtra="3dp"
                android:text="@string/set_unlock_delay_note"
                android:textColor="#80FFFFFF"
                android:textSize="11sp" />
        </LinearLayout>

        <!-- 无障碍 -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="26dp"
            android:layout_marginBottom="10dp"
            android:letterSpacing="0.06"
            android:text="@string/set_section_a11y"
            android:textColor="#8FD8FF"
            android:textSize="11sp" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@drawable/bg_glass"
            android:orientation="vertical"
            android:paddingStart="20dp"
            android:paddingTop="18dp"
            android:paddingEnd="20dp"
            android:paddingBottom="18dp">

            <TextView
                android:id="@+id/tvA11yStatus"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:lineSpacingExtra="3dp"
                android:textColor="#FFFFFF"
                android:textSize="13sp" />

            <TextView
                android:id="@+id/btnOpenA11y"
                android:layout_width="match_parent"
                android:layout_height="48dp"
                android:layout_marginTop="14dp"
                android:background="@drawable/bg_button_outline"
                android:gravity="center"
                android:text="@string/set_open_a11y"
                android:textColor="#E6FFFFFF"
                android:textSize="14sp" />

            <TextView
                android:id="@+id/btnHealA11y"
                android:layout_width="match_parent"
                android:layout_height="48dp"
                android:layout_marginTop="10dp"
                android:background="@drawable/bg_button_outline"
                android:gravity="center"
                android:text="@string/set_heal_a11y"
                android:textColor="#E6FFFFFF"
                android:textSize="14sp" />

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="12dp"
                android:lineSpacingExtra="3dp"
                android:text="@string/set_a11y_note"
                android:textColor="#80FFFFFF"
                android:textSize="11sp" />
        </LinearLayout>

        <!-- 关于 -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="26dp"
            android:layout_marginBottom="10dp"
            android:letterSpacing="0.06"
            android:text="@string/set_section_about"
            android:textColor="#8FD8FF"
            android:textSize="11sp" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@drawable/bg_glass"
            android:orientation="vertical"
            android:paddingStart="20dp"
            android:paddingTop="18dp"
            android:paddingEnd="20dp"
            android:paddingBottom="18dp">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:lineSpacingExtra="4dp"
                android:text="@string/set_about"
                android:textColor="#CCFFFFFF"
                android:textSize="12sp" />

            <TextView
                android:id="@+id/tvLast"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="14dp"
                android:textColor="#80FFFFFF"
                android:textSize="12sp" />
        </LinearLayout>

        <TextView
            android:id="@+id/btnReset"
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:layout_marginTop="26dp"
            android:background="@drawable/bg_button_outline"
            android:gravity="center"
            android:text="@string/set_reset"
            android:textColor="#CCFFFFFF"
            android:textSize="14sp" />

    </LinearLayout>
</ScrollView>
```

## 25. `app/src/main/res/layout/widget_devices.xml`（122 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<!--
  一块 2×1 的设备小部件：左右各一格 1×1，每格一台设备。

  ┌───────────────┬───────────────┐
  │   💧 宿舍A     │   💧 宿舍B     │
  └───────────────┴───────────────┘

  ── 尺寸怎么定的 ──
  Android 用 cells = ceil((size + 30) / 70) 从 dp 反推格数（2026-11-07 实测）。
    2 格宽：110dp → ceil(140/70) = 2 ✓（111dp 就变 3 格，所以 110 是上限）
    1 格高：40dp  → ceil(70/70)  = 1 ✓（41dp 变 2 格）
  所以 minWidth=110dp / minHeight=40dp，正好 2×1；
  里面两个 LinearLayout 各 layout_weight=1，于是各占 55dp —— 即每台设备 1×1。

  ── 为什么每格是上下排（图标在上、名字在下）而不是左右排 ──
  1×1 只有 55dp 宽。左右排的话：图标 12dp + 间距 + 文字只剩 35dp，
  9sp 汉字也只能放 3 个，长一点的名字必被截断。
  上下排把整条 49dp 宽度都留给文字，5 个汉字勉强放得下（9sp ≈ 9dp/字）。

  ── RemoteViews 的限制 ──
  只能用 LinearLayout / FrameLayout / TextView / ImageView 这些原生控件，
  不能用 ConstraintLayout、不能自定义 View、不能写 selector。
  所以状态（正常 / 名字不在 / 无障碍没开）是靠代码 setTextColor + setInt alpha 表达的，
  见 DeviceWidget.bindCell()。层级是写死的，不要试图重构。
-->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/widgetDevicesRoot"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/bg_widget_devices"
    android:orientation="horizontal"
    android:paddingStart="2dp"
    android:paddingTop="2dp"
    android:paddingEnd="2dp"
    android:paddingBottom="2dp">

    <!-- ══════════ 左半格：清单第 1 台 ══════════ -->
    <LinearLayout
        android:id="@+id/widgetCellLeft"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:clickable="true"
        android:focusable="true"
        android:gravity="center"
        android:orientation="vertical">

        <ImageView
            android:id="@+id/widgetIconLeft"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:contentDescription="@null"
            android:scaleType="fitCenter"
            android:src="@drawable/ic_widget_drop" />

        <TextView
            android:id="@+id/widgetNameLeft"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="1dp"
            android:ellipsize="end"
            android:includeFontPadding="false"
            android:maxLines="1"
            android:textColor="#FFFFFF"
            android:textSize="10sp"
            android:textStyle="bold" />

    </LinearLayout>

    <!--
        中间那条竖线：把 2×1 明确切成两个 1×1。

        这里必须用 FrameLayout 而不是 View —— RemoteViews 只允许白名单里的
        控件类，`android.view.View` 不在名单里，写了会当场炸：
          W/AppWidgetHostView: android.view.InflateException: Binary XML file
          line #76 in ...:layout/widget_devices: Class not allowed to be
          inflated android.view.View
        桌面上的表现是「载入窗口小部件时出现问题」。
        FrameLayout 在白名单里，给它一个背景色就等同于一条线。
    -->
    <FrameLayout
        android:layout_width="1dp"
        android:layout_height="20dp"
        android:layout_gravity="center_vertical"
        android:background="#33FFFFFF" />


    <!-- ══════════ 右半格：清单第 2 台 ══════════ -->
    <LinearLayout
        android:id="@+id/widgetCellRight"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:clickable="true"
        android:focusable="true"
        android:gravity="center"
        android:orientation="vertical">

        <ImageView
            android:id="@+id/widgetIconRight"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:contentDescription="@null"
            android:scaleType="fitCenter"
            android:src="@drawable/ic_widget_drop" />

        <TextView
            android:id="@+id/widgetNameRight"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="1dp"
            android:ellipsize="end"
            android:includeFontPadding="false"
            android:maxLines="1"
            android:textColor="#FFFFFF"
            android:textSize="10sp"
            android:textStyle="bold" />

    </LinearLayout>

</LinearLayout>
```

## 26. `app/src/main/res/drawable/bg_app.xml`（13 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item>
        <shape android:shape="rectangle">
            <gradient
                android:angle="315"
                android:startColor="#0E1A33"
                android:centerColor="#12224A"
                android:endColor="#070B14"
                android:type="linear" />
        </shape>
    </item>
</layer-list>
```

## 27. `app/src/main/res/drawable/bg_glass.xml`（6 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#1AFFFFFF" />
    <corners android:radius="20dp" />
    <stroke android:width="1dp" android:color="#33FFFFFF" />
</shape>
```

## 28. `app/src/main/res/drawable/bg_button.xml`（15 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="#C04E7BE8" />
            <corners android:radius="26dp" />
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <gradient android:angle="0" android:startColor="#5B8CFF" android:endColor="#7B5BFF" android:type="linear" />
            <corners android:radius="26dp" />
        </shape>
    </item>
</selector>
```

## 29. `app/src/main/res/drawable/bg_button_outline.xml`（17 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="#26FFFFFF" />
            <corners android:radius="22dp" />
            <stroke android:width="1dp" android:color="#66FFFFFF" />
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="#0DFFFFFF" />
            <corners android:radius="22dp" />
            <stroke android:width="1dp" android:color="#3DFFFFFF" />
        </shape>
    </item>
</selector>
```

## 30. `app/src/main/res/drawable/bg_row.xml`（17 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="#26FFFFFF" />
            <corners android:radius="18dp" />
            <stroke android:width="1dp" android:color="#4DFFFFFF" />
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="#14FFFFFF" />
            <corners android:radius="18dp" />
            <stroke android:width="1dp" android:color="#2EFFFFFF" />
        </shape>
    </item>
</selector>
```

## 31. `app/src/main/res/drawable/bg_widget_devices.xml`（24 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<!--
  2×1 设备小部件（左右两格）的底板。

  和 App 内其它卡片同一套「液态玻璃」语言：315° 渐变 + 大圆角 + 1dp 高光描边。
  但这里有个和小部件场景绑定的额外要求：**桌面上背景是用户的壁纸，不可控**。
  所以不能用纯透明 —— 遇到浅色壁纸时玻璃会整块消失，方块看起来像没加载出来。
  这里把最亮的一档压在 #33FFFFFF（约 20% 白），保证在任何壁纸上都能看出边界。

  圆角取 16dp（比之前单块的 18dp 略小）：一块板里塞了两格，
  圆角太大时中间那条竖线的位置会显得局促。
-->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <gradient
        android:angle="315"
        android:centerColor="#1FFFFFFF"
        android:endColor="#14FFFFFF"
        android:startColor="#3DFFFFFF" />
    <corners android:radius="16dp" />
    <stroke
        android:width="1dp"
        android:color="#4DFFFFFF" />
</shape>
```

## 32. `app/src/main/res/drawable/ic_widget_drop.xml`（23 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<!--
  小部件左侧那滴水。

  2×1 里它占 28dp，比 1×1 版本（26dp）略大一点 —— 横向放宽之后
  图标可以有呼吸空间，不会和右边的文字挤在一起。
  颜色 #8FD8FF 是全天候可见的浅青，在浅色和深色壁纸上都不发灰。
-->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">

    <path
        android:fillColor="#8FD8FF"
        android:pathData="M12,2.2 C12,2.2 5.2,10 5.2,14.2 A6.8,6.8 0 0,0 18.8,14.2 C18.8,10 12,2.2 12,2.2 Z" />

    <path
        android:fillColor="#CCFFFFFF"
        android:pathData="M9.6,13.6 A1.6,2.2 0 0,0 9.6,17.4 A1.7,2.4 0 0,1 9.6,13.6 Z" />

</vector>
```

## 33. `app/src/main/res/drawable/ic_launcher_background.xml`（10 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#0B4F8A"
        android:pathData="M0,0h108v108h-108z" />
</vector>
```

## 34. `app/src/main/res/drawable/ic_launcher_foreground.xml`（30 行）

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- 一个取景框加一道闪电：取景框代表「找设备」，闪电代表「一下就到位」。 -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">

    <!-- 左上角 -->
    <path
        android:fillColor="#8FD8FF"
        android:pathData="M32,34h10v4h-6v6h-4z" />
    <!-- 右上角 -->
    <path
        android:fillColor="#8FD8FF"
        android:pathData="M66,34h10v10h-4v-6h-6z" />
    <!-- 左下角 -->
    <path
        android:fillColor="#8FD8FF"
        android:pathData="M32,64h4v6h6v4h-10z" />
    <!-- 右下角 -->
    <path
        android:fillColor="#8FD8FF"
        android:pathData="M72,64h4v10h-10v-4h6z" />

    <!-- 中间闪电 -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M57,42l-14,18h10l-3,14l15,-20h-10z" />
</vector>
```

## 35. `gradlew`（249 行）

```
#!/bin/sh

#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
#
#   Gradle start up script for POSIX generated by Gradle.
#
#   Important for running:
#
#   (1) You need a POSIX-compliant shell to run this script. If your /bin/sh is
#       noncompliant, but you have some other compliant shell such as ksh or
#       bash, then to run this script, type that shell name before the whole
#       command line, like:
#
#           ksh Gradle
#
#       Busybox and similar reduced shells will NOT work, because this script
#       requires all of these POSIX shell features:
#         * functions;
#         * expansions «$var», «${var}», «${var:-default}», «${var+SET}»,
#           «${var#prefix}», «${var%suffix}», and «$( cmd )»;
#         * compound commands having a testable exit status, especially «case»;
#         * various built-in commands including «command», «set», and «ulimit».
#
#   Important for patching:
#
#   (2) This script targets any POSIX shell, so it avoids extensions provided
#       by Bash, Ksh, etc; in particular arrays are avoided.
#
#       The "traditional" practice of packing multiple parameters into a
#       space-separated string is a well documented source of bugs and security
#       problems, so this is (mostly) avoided, by progressively accumulating
#       options in "$@", and eventually passing that to Java.
#
#       Where the inherited environment variables (DEFAULT_JVM_OPTS, JAVA_OPTS,
#       and GRADLE_OPTS) rely on word-splitting, this is performed explicitly;
#       see the in-line comments for details.
#
#       There are tweaks for specific operating systems such as AIX, CygWin,
#       Darwin, MinGW, and NonStop.
#
#   (3) This script is generated from the Groovy template
#       https://github.com/gradle/gradle/blob/HEAD/subprojects/plugins/src/main/resources/org/gradle/api/internal/plugins/unixStartScript.txt
#       within the Gradle project.
#
#       You can find Gradle at https://github.com/gradle/gradle/.
#
##############################################################################

# Attempt to set APP_HOME

# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
    [ -h "$app_path" ]
do
    ls=$( ls -ld "$app_path" )
    link=${ls#*' -> '}
    case $link in             #(
      /*)   app_path=$link ;; #(
      *)    app_path=$APP_HOME$link ;;
    esac
done

# This is normally unused
# shellcheck disable=SC2034
APP_BASE_NAME=${0##*/}
# Discard cd standard output in case $CDPATH is set (https://github.com/gradle/gradle/issues/25036)
APP_HOME=$( cd "${APP_HOME:-./}" > /dev/null && pwd -P ) || exit

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$( uname )" in                #(
  CYGWIN* )         cygwin=true  ;; #(
  Darwin* )         darwin=true  ;; #(
  MSYS* | MINGW* )  msys=true    ;; #(
  NONSTOP* )        nonstop=true ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar


# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD=java
    if ! command -v java >/dev/null 2>&1
    then
        die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in #(
      max*)
        # In POSIX sh, ulimit -H is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039,SC3045
        MAX_FD=$( ulimit -H -n ) ||
            warn "Could not query maximum file descriptor limit"
    esac
    case $MAX_FD in  #(
      '' | soft) :;; #(
      *)
        # In POSIX sh, ulimit -n is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039,SC3045
        ulimit -n "$MAX_FD" ||
            warn "Could not set maximum file descriptor limit to $MAX_FD"
    esac
fi

# Collect all arguments for the java command, stacking in reverse order:
#   * args from the command line
#   * the main class name
#   * -classpath
#   * -D...appname settings
#   * --module-path (only if needed)
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, and GRADLE_OPTS environment variables.

# For Cygwin or MSYS, switch paths to Windows format before running java
if "$cygwin" || "$msys" ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
    CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )

    JAVACMD=$( cygpath --unix "$JAVACMD" )

    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    for arg do
        if
            case $arg in                                #(
              -*)   false ;;                            # don't mess with options #(
              /?*)  t=${arg#/} t=/${t%%/*}              # looks like a POSIX filepath
                    [ -e "$t" ] ;;                      #(
              *)    false ;;
            esac
        then
            arg=$( cygpath --path --ignore --mixed "$arg" )
        fi
        # Roll the args list around exactly as many times as the number of
        # args, so each arg winds up back in the position where it started, but
        # possibly modified.
        #
        # NB: a `for` loop captures its iteration list before it begins, so
        # changing the positional parameters here affects neither the number of
        # iterations, nor the values presented in `arg`.
        shift                   # remove old arg
        set -- "$@" "$arg"      # push replacement arg
    done
fi


# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Collect all arguments for the java command:
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, JAVA_OPTS, and optsEnvironmentVar are not allowed to contain shell fragments,
#     and any embedded shellness will be escaped.
#   * For example: A user cannot expect ${Hostname} to be expanded, as it is an environment variable and will be
#     treated as '${Hostname}' itself on the command line.

set -- \
        "-Dorg.gradle.appname=$APP_BASE_NAME" \
        -classpath "$CLASSPATH" \
        org.gradle.wrapper.GradleWrapperMain \
        "$@"

# Stop when "xargs" is not available.
if ! command -v xargs >/dev/null 2>&1
then
    die "xargs is not available"
fi

# Use "xargs" to parse quoted args.
#
# With -n1 it outputs one arg per line, with the quotes and backslashes removed.
#
# In Bash we could simply go:
#
#   readarray ARGS < <( xargs -n1 <<<"$var" ) &&
#   set -- "${ARGS[@]}" "$@"
#
# but POSIX shell has neither arrays nor command substitution, so instead we
# post-process each arg (as a line of input to sed) to backslash-escape any
# character that might be a shell metacharacter, then use eval to reverse
# that process (while maintaining the separation between arguments), and wrap
# the whole thing up as a single "set" statement.
#
# This will of course break if any of these variables contains a newline or
# an unmatched quote.
#

eval "set -- $(
        printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS" |
        xargs -n1 |
        sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
        tr '\n' ' '
    )" '"$@"'

exec "$JAVACMD" "$@"
```

## 36. `gradlew.bat`（92 行）

```bat
@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar


@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
```

## 37. `gradle/wrapper/gradle-wrapper.properties`（7 行）

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

