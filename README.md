# 胖乖直达（V3）

打开这个 App 就能看到你要用的那几台设备；点一下设备名字，无障碍服务替你在胖乖生活 App 里连点几下——「饮水」分类、那台设备旁的「立即使用」、确认页的「领优惠券使用」、会员营销页的「放弃优惠 原价使用」——最后把你送到「解锁成功」。

**不需要支付宝登录。** 这是 V3 与前面两个版本最本质的区别。

「自动解锁」默认是关的。关着的时候，本应用把你送到设备确认页就撒手；打开之后才会替你按下那几下，一直走到「解锁成功」为止。**出水永远要靠饮水机机身上的实体「出水」键，本应用不会、也没有能力替你出水。**

桌面上还可以放一块 **2×1 小部件**（左格「北三201」、右格「201」，各占 1×1），点哪半边就去哪台，连设备行都不用点——见第 12 节。

---

## 0. 它到底做了什么

一次完整动作全部发生在**胖乖生活自己的界面里**，点的是它界面上真实存在、你也用手指点得到的控件：

| 步 | 动作 | 谁做的 | 自动解锁关 | 自动解锁开 |
|---|---|---|---|---|
| 1 | 拉起 `com.qiekj.user.MainActivity` | 本应用 | ✓ | ✓ |
| 2 | 首页分类条上点「饮水」 | 无障碍 | ✓ | ✓ |
| 3 | 设备列表里找到目标那台，点它的「立即使用」 | 无障碍 | ✓ | ✓ |
| 4 | 确认页上点「领优惠券使用」（文案会变，靠 id `tvUse` 认） | 无障碍 | ✗ 停在这 | ✓ |
| 5 | 若弹出会员营销页，点「放弃优惠 原价使用」 | 无障碍 | — | ✓ |
| 6 | 到「解锁成功 / 结束后将自动为您计费」 | — | — | 撒手并退出待命 |

第 5 步有两个挨得很近的按钮（间距约 220px）：

- **上面**「开通并领取优惠」→ 开通自动续费 **9.7 元/月**，**永远不会被点**
- **下面**「放弃优惠 原价使用」→ 不订阅、按原价用，这才是要点的

## 1. 为什么这条路能绕开支付宝

前面两个版本都卡在同一件事上：

- **V1（扫码 → 官方分发页 → 支付宝）**：官方分发页 `h5.qiekj.com/skip?SN=…` 在浏览器里是一个 App 下载引导页，只有伪装成支付宝 UA 时才会自己发起 `alipays://` 跳转。而支付宝在这个手机上处于「未登录」状态，跳过去落在 `com.ali.user.mobile.loginupgrade.activity.LoginActivity`。
- **V2（胖乖 App 的扫一扫）**：只能到扫码页，扫码这一步省不掉；而且胖乖的扫一扫页 `HomeScanCodeAct` 是 `exported=false`，只能从 App 内部点进去。

V3 换了个切入点：**胖乖 App 自己就有一份设备列表**。

`拉起 MainActivity → 点「饮水」` 之后进的是 `com.qiekj.user.ui.activity.home.MyDeviceAct`，实测内容（1080×2400）：

```
我的设备 [410,80][670,226]   查看附近设备 [386,263][695,317]
最近使用设备 [58,399][298,453]
北3栋201 [70,523][785,585]   空闲 [872,535][942,582]   XX学院-东校区 [70,605][785,652]   立即使用 [803,596][1010,665]
工楼3-2  [70,764][785,826]   空闲 [872,776][942,823]   XX学院-东校区 [70,846][785,893]   立即使用 [803,837][1010,906]
201     [70,1005][785,1067] 空闲 [872,1017][942,1064] XX学院 [70,1073][785,1120]       立即使用 [803,1109][1010,1178]
```

这一页的节点结构（`resource-id` 实测）：

- 每行的设备名是 `tvDeviceName`（`clickable=false`）
- 状态是 `tvStatus`（"空闲"）
- 位置是 `tvShopName`
- **按钮是 `tvUse`（"立即使用"，`clickable=true`）**

`tvUse` 是普通可点击节点，`performAction(ACTION_CLICK)` 直接就能点，不需要坐标手势——这一点比 V1 那个 WebView 里六层祖先都不可点的 React 节点好处理得多。

## 2. 设备名匹配：唯一真正有风险的地方

你叫它「北三201」，页面上写的是「北3栋201」。而列表里**同时还有一台叫「201」**。

这意味着：如果只做字符串包含判断，目标 `201` 会命中 `北3栋201`（因为规整之后 `北3201` 包含 `201`）——**那就开的是别人那台机器的水**。这是整条链路上最危险的失败方式。

所以匹配分三层（`Matcher.kt`）：

1. **规整**：剥掉「栋 / 楼 / 室 / 号 / 层 / - / 空格 / 括号」，中文数字转阿拉伯数字（五→5、两→2），全部小写
2. **rank = 2**：规整后完全相等
3. **rank = 1**：一方包含另一方，且短串长度 ≥ 3
4. **rank = 0**：不匹配

调用方（`onDeviceListPage`）**必须先找 rank=2，整个列表一个都没有才退用 rank=1**。

实测日志：

```
[Tap] 设备列表共 3 行：北3栋201 | 工楼3-2 | 201
[Tap] 匹配成功（完全相等）：目标「北三201」→规整「北3201」  页面「北3栋201」→规整「北3201」
```

```
[Tap] 设备列表共 3 行：北3栋201 | 工楼3-2 | 201
[Tap] 匹配成功（完全相等）：目标「201」→规整「201」  页面「201」→规整「201」
```

两次都是 rank=2，没有一次走到包含匹配。

## 3. 先过 MIUI 那道「应用跳转确认」

本应用拉胖乖，MIUI 会插一个确认框：

```
启动应用
胖乖直达 想要打开 胖乖生活，是否允许？
[拒绝]  [始终允许]
```

包名是 `com.miui.securitycenter`，`mCurrentFocus` 显示
`Window{… u0 com.miui.securitycenter/com.miui.wakepath.ui.ConfirmStartActivity}`。

一开始那个框一闪而过、点不到，因为**本应用把任务退到了后台**（`moveTaskToBack`），而退到后台的 App 发起的 `startActivity` 会被判为「后台启动」，MIUI 才插这个框。去掉 `moveTaskToBack` 之后框仍然出现，但至少稳定可见、能被无障碍点掉。

`handleJumpConfirm()` 的限定条件收得很死：

1. 只有「本应用 → 胖乖」这一个组合才点，别的应用跳转框一律绕开
2. 文案里必须**同时**出现「胖乖直达」和「胖乖生活」
3. 点的是「始终允许」（点「拒绝」会让整条链路彻底走不通）
4. **一次待命窗口内只点一次**，用 `armToken` 绑定到具体某一轮，新一轮自动失效

实测：

```
[Tap] 遇到 MIUI 的跳转确认框：「胖乖直达 → 胖乖生活」
[Tap] performAction(ACTION_CLICK) 于第 0 层祖先 → true
[Tap] 点「始终允许」结果=true
```

## 4. 冷启动时的一个真坑：设备列表先空后满

点「饮水」之后，页面顶部「查看附近设备」先出现，recyclerView 里**一行都还没有**。实测从点下去到真正出行约 830ms。

第一版代码在这里直接判断「目标设备不在列表里」然后退出待命，链路断在半路。修法是区分两种"没有行"：

```kotlin
if (scan.deviceRows.isEmpty()) {
    note("设备列表已打开但还没渲染出行，等下一轮")
    return
}
```

修完的日志（800ms 后第二轮就齐了）：

```
[Tap] 点「饮水」结果=true
[Tap] 设备列表共 0 行：
[Tap] 设备列表已打开但还没渲染出行，等下一轮
[Tap] 设备列表共 3 行：北3栋201 | 工楼3-2 | 201
[Tap] 匹配成功（完全相等）
[Tap] 点「北3栋201」的「立即使用」结果=true
[Tap] 已到设备确认页（北3栋201），任务完成
```

## 5. 另一条真坑：隐式 LAUNCHER Intent 会抛异常

```kotlin
Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage("com.qiekj.user")
```

这条 Intent 的 `resolveActivity` 能查出 `SplashActivity`，但真去 `startActivity` 会抛：

```
ActivityNotFoundException: No Activity found to handle
  Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10000000 pkg=com.qiekj.user }
```

而显式组件 `setClassName("com.qiekj.user", "com.qiekj.user.MainActivity")` 两条路都通。所以 `Qk.buildLaunchIntent()` 现在**优先显式**，隐式只作极端兜底。

另外不要拉 `SplashActivity`——它是启动闪屏，会先播几秒再进首页，待命窗口经不起这个浪费。

## 6. 自动解锁：确认页 → 会员营销页 → 解锁成功

这是本次新增的一段，也是唯一一处「本应用会替你按下会开始计费的按钮」的地方。

### 6.1 确认页那个按钮

确认页是 `com.qiekj.user.ui.activity.scan.AfterPayUseAct`，底部那个大按钮：

```
tvUse   [46,2224][1034,2365]  clickable=true  文案 =「领优惠券使用」
```

**文案会变**（也出现过「立即使用」），所以**只能按 `resource-id` 认，不能按文本认**。

点它**不会立刻出水**，而是弹出会员营销页 `com.qiekj.user.ui.activity.web.TransparentWebViewAct`。这一页所有节点的 `resource-id` 全是空的，只能靠文本定位：

```
关闭 X        [951,533][1047,629]    clickable
会员服务协议   [192,1941][396,1991]   clickable
自动续费协议   [478,1941][682,1991]   clickable
开通并领取优惠 [44,2084][1036,2230]   clickable   ← 开通自动续费 9.7元/月，绝不能点
放弃优惠 原价使用 [44,2249][1036,2345] clickable   ← 要点的就是这个
```

两个按钮相距只有 ~220px，误点代价是一个每月 9.7 元的自动续费。所以这一页的识别**刻意做得极窄**：

```kotlin
// 文案含「放弃优惠」且不含「开通」才算命中。
findByTextContainsOnly(root, Qk.TEXT_DECLINE_OFFER, Qk.TEXT_SUBSCRIBE_OFFER)
```

宁可认不出来、停在原地，也绝不冒险点错。

点「放弃优惠 原价使用」之后进 `com.qiekj.user.ui.activity.scan.DeviceStartupAct`：

```
tv_after_state =「解锁成功」        [475,202][695,276]
tv_after_tip   =「结束后将自动为您计费」[0,293][1080,347]
```

**到这一页就 `disarm` + `stopPolling()`**，后面的出水与结束全部交给你。

### 6.2 为什么必须分步令牌，而不是一个布尔量

链路从「一次动作」变成了三串（设备行 → 确认页按钮 → 会员页放弃优惠）。原来 `JumpWindow` 只有一个 `fired: Boolean`，会出两种错：

- 点完设备行就把 `fired` 置 true → 确认页按钮**永远点不动**
- 或者反过来 → 确认页按钮被**连点三次**

改成分步令牌：

```kotlin
private val firedStages = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

fun claimStage(stage: String): Boolean {
    if (!isArmed()) return false
    if (!firedStages.add(stage)) return false
    return true
}
fun resetStage(stage: String) { firedStages.remove(stage) }
```

三个阶段名：`STAGE_DEVICE_ROW` / `STAGE_CONFIRM_USE` / `STAGE_DECLINE`。
只有 `clickNode()` 真的返回 false 时才 `resetStage`，允许下一轮重试。

### 6.3 确认页倒计时

确认页刚跳出来时，`tvUse` 可能已经在了，但页面还在加载、按钮按下会静默无效。所以这里等的是一个**时钟**而不是某个元素出现：

```kotlin
if (confirmSeenAt == 0L) { confirmSeenAt = now; ... }
val waitMs = Prefs.unlockDelayS(this) * 1000L
if (now - confirmSeenAt < waitMs) { note("确认页再等 ${waitMs - (now - confirmSeenAt)}ms 就点解锁按钮"); return }
```

默认 3 秒，可在设置页 0–30 秒之间调。

> 关于「再等」这条日志：它曾经每 600ms 轮询都打一次，启动日志瞬间被刷满。现在只在剩不到 1 秒时才出声，而且 `noteStep()` 做了同句去重。

### 6.4 一个真 bug：`performAction` 返回 true 不等于页面跳了

这是整条链路里最隐蔽的一处，必须写下来。

`AccessibilityNodeInfo.performAction(ACTION_CLICK)` 返回 `true`，只代表**这个点击事件被系统受理了**，不代表页面真的做出了反应。实测抓到过一次：

```
[Tap] 会员页点「放弃优惠 原价使用」结果=true
（然后 25 秒什么都没有）
[Tap] 待命已超时（25010ms > 25000ms），不再动手
```

dump 一看，页面**还停在会员页**。原因是 `TransparentWebViewAct` 是 WebView，那一下点下去时 JS 侧还没把点击接上，事件被静默吞掉了。而分步令牌已经置位，于是重试被挡住，链路干等到窗口超时。

修法：**点过之后隔 1.5 秒再看一眼，页面还停着就撤掉令牌重试，最多 3 次。**

```kotlin
private const val RETRY_AFTER_MS = 1500L
private const val MAX_ATTEMPTS = 3
```

确认页与会员页各有一个 `clickedAt` / `attempts` 计数器（`@Volatile`），换轮待命时一起清零。

### 6.5 重试引出的第二个坑：会员页关掉时，底下的确认页会「回魂」

加上重试之后立刻撞到新问题。会员页点「放弃优惠」成功，但那层 WebView 要过半秒才真正销毁；这段过渡时间里，下面那层确认页重新变成活动窗口。于是：

```
[Tap] 会员页点「放弃优惠 原价使用」结果=true
[Tap] 确认页还停着（上次点击 2947ms 前），重试第 2 次
[Tap] 确认页点「立即使用」（自动解锁）结果=true   ← 白点一次
```

确认页按钮被重复点了一次。虽然这次没出事（最终仍然解锁成功），但这是不可接受的：重复点可能又弹回会员页，也可能在确认页上做出别的动作。

修法：**如果会员页那一步已经走过，就要求离上一次会员页点击至少 2.5 秒，才允许重新判断确认页。**

```kotlin
private const val DECLINE_SETTLE_MS = 2500L

if (JumpWindow.alreadyDone(STAGE_DECLINE) && declineClickedAt > 0L) {
    if (SystemClock.elapsedRealtime() - declineClickedAt < DECLINE_SETTLE_MS) {
        note("会员页刚关，等它彻底退干净再判断")
        return
    }
}
```

顺带在这轮里证实了一件事：确认页按钮的文案**会在「领优惠券使用」和「立即使用」之间变**。所以这里一律按 `resource-id`（`com.qiekj.user:id/tvUse`）认按钮，绝不按文字。日志里打印文案只是为了给人看。

## 7. 广告：怎么绕开的

胖乖的广告位实测有四个，全部记进 `Qk.AD_ZONE_IDS`：

| id | 位置 | 会挡什么 |
|---|---|---|
| `fl_ad_container` / `ksad_container` | 确认页顶部快手联盟 | 不挡底部按钮，但内部 TextView 全部 `clickable=true` |
| `rl_float_ad` | 设备列表右下角悬浮 | **不挡** `tvUse`，但会持续挡视线 |
| `include_rebate_guide` | 首页「抖音商城下单返积分」 | 不挡「饮水」 |

治理分两层：

**第一层：统一闸门。** `clickNode()` 开头先过 `isAdTarget()`，只要目标自己或往上 8 层祖先命中 `AD_ZONE_IDS`，或者文案命中 `AD_TEXT_HINTS`（「点击即玩」「左滑点击即玩」「广告」），一律拒点：

```
[Tap] 拒绝点击广告节点：「点击即玩」
```

**唯一例外**是 `channel_banner_close_iv`——它物理上在 `rl_float_ad` 里面，但它是「关掉这个广告」这个动作本身：

```kotlin
// 悬浮广告的关闭按钮本身就在 rl_float_ad 里，但它是「关广告」而不是「点广告」，
// 所以必须在闸门里给它开一个口子，否则这个按钮永远点不动。
if (viewId.endsWith("channel_banner_close_iv")) return false
```

实测点它只关广告、页面不动、焦点不变、`rl_float_ad` 从 dump 里消失，**不挡设备行**（各行 y596–1178）。

**第二层：顺手清掉。** 设备列表页找到目标之后、点「立即使用」之前，先点掉悬浮广告，然后 `return` 等下一轮（让页面重排完再点设备，免得点偏）。一轮只点一次，避免「关了又弹、弹了又关」的死循环。

**第三层：会员页本身也是广告。** 它被当成一道必过关卡自动通过——点「放弃优惠 原价使用」，也就是「不订阅」。这是唯一正确的选择。

至于开屏广告：**冷启动和热启动都没有**。`am start` 后逐 500–700ms 采样 10–12 次，`mCurrentFocus` 始终是 `MainActivity`，不走 `SplashActivity`。

## 8. 端到端实测记录（2026-11-07，Redmi K40S / MIUI V816 / Android 14）

### 8.1 自动解锁关闭：到确认页就停

**北三201 → 北3栋201**

```
[Life] ═══ JumpActivity 启动 ═══
[Life] 调试=false  步进等待=1200ms  窗口=25000ms  胖乖版本=1.145.0  已安装=true
[Jump] 目标簿共 2 台：北三201, 201
[Jump] a) 显式入口可用：com.qiekj.user/com.qiekj.user.MainActivity
[Jump] resolveActivity = com.qiekj.user/com.qiekj.user.MainActivity  调试=false  无障碍已连接=true
[Tap] 进入待命（窗口 25000ms），只点目标设备那一行的「立即使用」
[Tap] 开始轮询页面状态（每 600ms 一次）
[Jump] ✓ 已拉起胖乖，等待无障碍把它领到「北三201」
[Tap] 遇到 MIUI 的跳转确认框：「胖乖直达 → 胖乖生活」
[Tap] 点「始终允许」结果=true
[Tap] 在胖乖首页，点「饮水」进入设备列表（目标设备=北三201）
[Tap] 点「饮水」结果=true
[Tap] 设备列表共 3 行：北3栋201 | 工楼3-2 | 201
[Tap] 匹配成功（完全相等）：目标「北三201」→规整「北3201」  页面「北3栋201」→规整「北3201」
[Tap] 点「北3栋201」的「立即使用」结果=true
[Tap] 已到设备确认页（北3栋201），任务完成
```

落点 `AfterPayUseAct`，页面内容：
`XX学院` / `北3栋201` / `XX学院-东校区` / `选择支付方式` / `支付宝支付` / `本单可用小票，小票余额40.03元` / `免费饮水 -0.50` / `优惠券 4张可领` / `胖乖积分 24 自动抵扣`。

**201**

```
[Tap] 设备列表共 3 行：北3栋201 | 工楼3-2 | 201
[Tap] 匹配成功（完全相等）：目标「201」→规整「201」  页面「201」→规整「201」
[Tap] 已到设备确认页（201），任务完成
```

落点同为 `AfterPayUseAct`，页面 `201` / `XX学院` / `小票余额46.66元` / `云闪付免密支付`。

**两次都没点确认页上那个按钮，都没开始出水。**

### 8.2 调试模式：只打日志不动手

`dry_run=true` 时照样拉起胖乖，只是每一处要动手的地方改成打日志：

```
[Tap] 在胖乖首页，点「饮水」进入设备列表（目标设备=北三201）
[Tap] 调试模式：本应点「饮水」，未动手
[Tap] 进度：调试：停在首页，未点分类

[Tap] 设备列表共 3 行：北3栋201 | 工楼3-2 | 201
[Tap] 匹配成功（完全相等）：目标「北三201」→规整「北3201」  页面「北3栋201」→规整「北3201」
[Tap] 调试模式：本应点掉右下角悬浮广告，未动手
[Tap] 调试模式：本应点「北3栋201」这一行的「立即使用」，未动手
[Tap] 进度：调试：停在设备列表，目标=北3栋201

[Tap] 自动解锁已开启：已到设备确认页（北3栋201），等 3000ms 后点「领优惠券使用」
[Tap] 进度：确认页：等待中
[Tap] 确认页再等 2365ms 就点解锁按钮
...
[Tap] 调试模式：本应点确认页的「领优惠券使用」，未动手
[Tap] 进度：调试：停在确认页，未解锁
```

> 调试模式**故意不拦「拉起胖乖」**。早期版本在 dry-run 时直接 return，结果无障碍服务根本看不到任何页面，整条链路无从验证。要拦的是「点击」，不是「启动」。

### 8.3 自动解锁开启：真机全链路（2026-11-07 10:52:52）

窗口 60 秒、解锁等待 3 秒、目标「北三201」。放手之后**全程无人干预**：

```
[Tap] 确认页再等 378ms 就点解锁按钮
[Tap] 确认页点「领优惠券使用」（自动解锁）
[Tap] performAction(ACTION_CLICK) 于第 0 层祖先 → true
[Tap] 确认页点「领优惠券使用」结果=true
[Tap] 进度：已点确认页「领优惠券使用」

[Tap] 会员页点「放弃优惠 原价使用」（不订阅，按原价使用）
[Tap] performAction(ACTION_CLICK) 于第 0 层祖先 → true
[Tap] 会员页点「放弃优惠 原价使用」结果=true
[Tap] 进度：已拒绝会员优惠，按原价使用

[Tap] 已解锁（北3栋201） —— 到这儿就停，出水请按饮水机机身上的「出水」键
[Tap] 退出待命：已解锁，任务完成
[Tap] 停止轮询
```

`mCurrentFocus` 依次是：
`JumpActivity`（本应用）→ `MainActivity` → `AfterPayUseAct` → `TransparentWebViewAct` → `DeviceStartupAct`。

全程 6 秒左右（从点下设备到「解锁成功」）。

> **这次是真的执行了解锁动作。** 页面自述「结束后将自动为您计费」，但**没有按机身「出水」键、没有出水**，因此不应产生水量费用。同一台机器在调查阶段也被手动解锁过两次（为了 dump 页面结构），情况相同。使用者需要知道：自动解锁一旦打开，这条链路的最后一步就是真实生效的。

### 8.4 加上重试与退干净守卫之后的最终一次（2026-11-07 11:00:12）

这是最后验证过的一版，从点下「北三201」到「解锁成功」全程 13 秒，中间只因为自愈模块多花了一点时间。完整日志（未删减）：

```
[Life] ═══ JumpActivity 启动 ═══
[Life] 调试=false  步进等待=1200ms  窗口=25000ms  胖乖版本=1.145.0  已安装=true
[Jump] 目标簿共 2 台：北三201, 201
[Tap] 无障碍服务已连接（canPerformGestures 见配置）
[Tap] 无障碍服务已解绑
[Jump] 无障碍没就绪，先自愈再看
[A11y] 检测到无障碍授权被清空（多半是从最近任务划掉了 App），开始写回
[A11y]   原有服务列表=com.local.qrjump/.AutoUnlockService
[A11y]   写回后=com.local.qrjump/.AutoUnlockService:com.local.qk3jump/com.local.qk3jump.ScanTapService
[A11y]   write(enabledFirst=true) 两次写入结果=true/true
[A11y] ✓ 自愈成功（顺序 A：先 enabled 后 services）
[Jump] a) 显式入口可用：com.qiekj.user/com.qiekj.user.MainActivity
[Jump] ═══ 准备直达「北三201」═══
[Jump] 启动 Intent = action=null data=null pkg=null component=com.qiekj.user/.MainActivity categories=null flags=0x10000000
[Jump] resolveActivity = com.qiekj.user/com.qiekj.user.MainActivity  调试=false  无障碍已连接=true
[Tap] 进入待命（窗口 25000ms）
[Tap] 无障碍服务已连接（canPerformGestures 见配置）
[Tap] 绑定后发现仍在待命窗口内，直接启动轮询（不依赖事件）
[Tap] 开始轮询页面状态（每 600ms 一次）
[Tap] 前台不是胖乖（当前=com.local.qk3jump），不动手
[Jump] ✓ 已拉起胖乖，等待无障碍把它领到「北三201」
[Tap] 当前页面既不是首页、设备列表、确认页，也不是会员页/解锁页，等下一轮
[Tap] 在胖乖首页，点「饮水」进入设备列表（目标设备=北三201）
[Tap] performAction(ACTION_CLICK) 于第 1 层祖先 → true
[Tap] 点「饮水」结果=true
[Tap] 进度：已点「饮水」
[Tap] 设备列表共 3 行：北3栋201 | 工楼3-2 | 201
[Match] 设备列表里目标那台显示为「北3栋201」
[Tap] 匹配成功（完全相等）：目标「北三201」→规整「北3201」  页面「北3栋201」→规整「北3201」
[Tap] performAction(ACTION_CLICK) 于第 0 层祖先 → true
[Tap] 点「北3栋201」的「立即使用」结果=true
[Tap] 进度：已点「北3栋201」的立即使用
[Tap] 自动解锁已开启：已到设备确认页（北3栋201），等 3000ms 后点「领优惠券使用」
[Tap] 进度：确认页：等待中
[Tap] 确认页再过 575ms 就点解锁按钮
[Tap] 确认页点「领优惠券使用」（自动解锁）
[Tap] performAction(ACTION_CLICK) 于第 0 层祖先 → true
[Tap] 确认页点「领优惠券使用」结果=true
[Tap] 进度：已点确认页「领优惠券使用」
[Tap] 会员页点「放弃优惠 原价使用」（不订阅，按原价使用）
[Tap] performAction(ACTION_CLICK) 于第 0 层祖先 → true
[Tap] 会员页点「放弃优惠 原价使用」结果=true
[Tap] 进度：已拒绝会员优惠，按原价使用
[Tap] 会员页刚关，等它彻底退干净再判断
[Tap] 已解锁（北3栋201） —— 到这儿就停，出水请按饮水机机身上的「出水」键
[Tap] 退出待命：已解锁，任务完成
[Tap] 停止轮询
```

两处值得注意的地方：

1. **`[A11y] ✓ 自愈成功（顺序 A：先 enabled 后 services）`** —— 每次 `adb install -r` 之后无障碍授权都会被系统清掉，`A11yGuard` 自己写回去了，不用手动再跑一遍 `settings put`。这是 MIUI 上的写序（`enabledFirst=true`）；ColorOS / realme 上要反过来，代码里两种都试。
2. **`会员页刚关，等它彻底退干净再判断` 只出现一次** —— 这一行是 6.5 那个守卫在起作用。它出现之后就直接等到「解锁成功」，没有再去点第二遍确认页。守卫生效前，同样的场景会多打出一行「重试第 2 次」并多点击一次。

## 9. 编译与安装

工具链在 A 盘（详见 V1 的 README）。构建：

```powershell
cd <工程目录>
$env:JAVA_HOME='<JDK 目录>'; $env:ANDROID_HOME='<SDK 目录>'; $env:ANDROID_SDK_ROOT='<SDK 目录>'; $env:GRADLE_USER_HOME='<GRADLE_USER_HOME>'
$env:PATH='<JDK 目录>\bin;<Gradle 目录>\bin;<SDK 目录>\platform-tools;<SDK 目录>\cmdline-tools\latest\bin;'+$env:PATH
gradle assembleDebug --console=plain
```

产物：`app\build\outputs\apk\debug\app-debug.apk`，约 5.6 MB（没有相机、没有 ML Kit，所以比 V1 的 29 MB 小得多）。

**首次安装必被 MIUI 拦**（`INSTALL_FAILED_USER_RESTRICTED`，因为没插 SIM 卡 → MIUI「USB安装」开关开不了）。走文件管理器：

```powershell
adb -s <你的设备序列号> push app-debug.apk /sdcard/Download/Qk3Jump-debug.apk
adb -s <你的设备序列号> shell "input keyevent 224; wm dismiss-keyguard"
adb -s <你的设备序列号> shell "am force-stop com.miui.packageinstaller"
adb -s <你的设备序列号> shell "am start -n com.android.fileexplorer/com.android.fileexplorer.FileExplorerTabActivity"
```

然后在手机上点：底部「手机」→ **先往下滑**找到 `Download` → 点进去 → **再往下滑**找到 `Qk3Jump-debug.apk` → 点它 → 「继续安装」→ 勾选「已了解此应用未经安全检测」→ 「继续安装」→ 弹「开启增强防护」时点「放弃」。

如果弹「未查询到此应用的 ICP 备案信息」，点「继续安装」即可。

**装成功后 `adb install -r` 就畅通了**，后续迭代不必再走文件管理器：

```powershell
adb -s <你的设备序列号> install -r -t app\build\outputs\apk\debug\app-debug.apk
```

## 10. 授权两件事

### 10.1 无障碍（必需）

设置 → 无障碍 → 已下载的服务 → 「胖乖直达：自动点「饮水」与设备」→ 打开。

或者命令行：

```powershell
adb -s <你的设备序列号> shell "settings put secure accessibility_enabled 1"
adb -s <你的设备序列号> shell "settings put secure enabled_accessibility_services com.local.qk3jump/com.local.qk3jump.ScanTapService"
```

> **顺序不能反。** 先写 `enabled_accessibility_services` 再写 `accessibility_enabled=1` 时，后者会被系统重置回 0。
>
> 如果要和 V1 共存（V1 也在用无障碍），用冒号拼起来：
> `com.local.qrjump/com.local.qrjump.AutoUnlockService:com.local.qk3jump/com.local.qk3jump.ScanTapService`

> **`adb install -r` 重装会撤销无障碍授权**，重装后必须再授权一次。

### 10.2 WRITE_SECURE_SETTINGS（可选，只为了自愈）

```powershell
adb -s <你的设备序列号> shell "pm grant com.local.qk3jump android.permission.WRITE_SECURE_SETTINGS"
```

这条权限的作用：MIUI 把「从最近任务划掉卡片」当强制停止，会连 `accessibility_enabled` 一起清空。拿到这条权限后本应用能自己写回来，你只需要重新点开 App。实测日志：

```
[A11y] 检测到无障碍授权被清空（多半是从最近任务划掉了 App），开始写回
[A11y]   原有服务列表=com.local.qrjump/.AutoUnlockService
[A11y]   写回后=com.local.qrjump/.AutoUnlockService:com.local.qk3jump/com.local.qk3jump.ScanTapService
[A11y] ✓ 自愈成功（顺序 A：先 enabled 后 services）
```

不给也能用，只是每次划掉卡片都得手动进设置重开一次。

## 11. 界面

三屏，深色底 + 一层玻璃卡片：

- **启动页**：标题「胖乖直达」+ 右上「设置」+ 状态行 + 设备行（只有名字，没有副标题）+ 底部「加一台」「清空全部」
- **设置页**（从上到下）：
  - 行为
    - 调试模式（只看不动）
    - 每一步之间等 N 毫秒（滑杆）
    - 待命窗口 N 秒（滑杆）
    - **自动解锁（会真的开始计费）** —— 打开时先弹一段风险说明，要点「我明白，打开」才生效
    - **解锁等待 N 秒**（滑杆 0–30）
  - 无障碍：状态行 + 两个按钮
- **无障碍服务说明**：系统设置里显示为「胖乖直达：自动点「饮水」与设备」

截图：`shot_v3_list.png`（启动页）、`shot_v3_landing.png`（确认页落点）、`shot_set3.png`（自动解锁的风险说明弹窗）。

外加一块 2×1 桌面小部件（左右两台各占一格），外观见 `shot_widget.png`（下一节详述）。

## 12. 桌面小部件（一块 2×1，左右各一台）

连「打开 App → 点设备行」这一步也省掉：桌面上放**一块** 2×1 小部件，里面并排两台机器，**点左半边去清单第 1 台、点右半边去第 2 台**。

```
┌─────────────────┬─────────────────┐
│    💧 宿舍A      │    💧 宿舍B      │   ← 一块 2×1 组件
└─────────────────┴─────────────────┘
      点这里              点这里
     每格 1×1，各点各的
```

| 半边 | 目标 |
|---|---|
| 左 | 设备清单里的第 1 台（最近用的那台） |
| 右 | 设备清单里的第 2 台 |

**左右两格不是写死的名字，而是每次重画时读一遍设备清单**（`DeviceWidget.slots()` 取 `TargetStore.all()` 的前两台）。这样设备改名、增删、换顺序，桌面跟着变，不必重新编译；仓库里也不会留下任何人的真实宿舍号。

清单不足两台时，空着的那格显示「加一台」，点进去就是 App 的添加流程。

整块由一个 provider（`com.local.qk3jump.DeviceWidget`）负责，内部是两个带各自 `PendingIntent` 的可点区域，不是两块独立的部件。

### 12.1 为什么是一块板里两格，而不是两块 2×1

第一版做成了两块独立的 2×1（每块一台），桌面上会占两行、玻璃底板各自一块，看起来不像一件东西，也和「一个组件、两台设备」的直觉对不上。

现在是一块整板 + 中间一条 1dp 竖线：视觉上是一件东西，交互上是两个 1×1。两个可点区域各绑一个 `PendingIntent`，**requestCode 用设备名的 hashCode**——用固定值的话后创建的那个会覆盖前一个，点哪半边都去同一台设备（这是 `RemoteViews` 上最经典的坑，实测两块旧部件确实会互相覆盖）。

### 12.2 为什么没有配置页

`requestPinAppWidget`（应用内请求把部件钉到桌面）在 MIUI 上是**假成功**：API 返回 true，桌面什么都不会出现，logcat 里是

```
E/AddItemActivity-PinShortcutRequestUtils: add widget failed, com.local.qk3jump has no permission
```

所以只能**长按桌面手动拖**。而手动拖的时候，如果 provider 声明了 `android:configure`，拖完还会弹一个配置页让你选设备——多一次点击，还容易出现「拖到桌面才发现还要选设备」的错愕。

而这个部件**根本不需要选**：它显示的就是清单前两台，拖出来即成品。**一个 provider、一份 info、零配置页。**

### 12.3 尺寸为什么是 110dp × 40dp

启动器按「格子」算尺寸，换算公式是：

```
cells = ceil((size_dp + 30) / 70)
```

反推：

| 想要的格数 | 上限 | 说明 |
|---|---|---|
| 宽 2 格 | **110dp** | 写 111dp 就变 3 格 |
| 高 1 格 | **40dp** | 写 41dp 就变 2 格高 |

`widget_devices_info.xml` 里因此是 `minWidth="110dp" minHeight="40dp"`（外加 `targetCellWidth="2" targetCellHeight="1"`，Android 12+ 优先看这两个）。里面两个格子各 `layout_weight="1"`，于是各占 55dp = **每台设备 1×1**。

**验证办法**：在小部件选择器里看到「2 x 1」这三个字，就说明尺寸对了——那是系统自己算出来的，不是你写的。

> 实测 MIUI 实际给的绘制空间是 473×303px（约 180×115dp），比声明的 110×40dp 大不少，所以字号定 10sp、水滴 20dp 在这个空间里是合适的；如果换到格宽更紧凑的启动器（比如原生 Pixel），同样布局会自动按 55dp 的格子渲染，文字仍能放下 5 个汉字。

### 12.4 一个必踩的坑：`<View>` 不能被 RemoteViews 膨胀

中间那条分隔线，第一版写的是 `<View>`，桌面直接报：

```
W/AppWidgetHostView: android.view.InflateException: Binary XML file line #76
  in ...:layout/widget_devices: Class not allowed to be inflated android.view.View
```

界面上的表现是灰底白字「载入窗口小部件时出现问题」。

**根因**：`RemoteViews` 只允许白名单里的控件类（`LinearLayout` / `FrameLayout` / `RelativeLayout` / `GridLayout` / `TextView` / `ImageView` / `Button` / `ProgressBar` / `Chronometer` / `AnalogClock` / `ViewFlipper` 等），**`android.view.View` 不在名单里**。

**修法**：换成 `FrameLayout` 给个背景色，等同一条线。

```xml
<FrameLayout
    android:layout_width="1dp"
    android:layout_height="20dp"
    android:layout_gravity="center_vertical"
    android:background="#33FFFFFF" />
```

### 12.5 MIUI 上怎么拖（只能手动，而且必须用 motionevent）

1. 长按桌面空白处进编辑模式。**`input swipe x y x y 900` 不行**，必须用 motionevent 按住不动：
   ```
   adb shell input motionevent DOWN 540 1100
   （等 900ms）
   adb shell input motionevent UP 540 1100
   ```
   底部会出现「添加小部件」。
2. 点它 → 全屏选择器 → 需要**向下滚 5~6 次**才滚到本应用（每次 `input swipe 540 1900 540 800 300`）。
3. 拖到桌面：**DOWN → 等 1000ms → 多次 MOVE（间隔 120ms）→ UP**。这一步也不能用 `input swipe`，会被当成滚动：
   ```
   adb shell input motionevent DOWN 200 2182
   adb shell input motionevent MOVE 200 2100
   adb shell input motionevent MOVE 260 1600
   adb shell input motionevent MOVE 290 1200
   adb shell input motionevent MOVE 300 880
   adb shell input motionevent UP 300 880
   ```

> **桌面页顺序会变**：`am force-stop` 之后按 HOME 回到哪一页不固定，坐标不能跨轮沿用。每次点部件前先 `uiautomator dump`，用部件上那两个 `TextView` 的 text 去取 bounds，别沿用上一轮的坐标——曾经用过期的坐标点到「微信」图标，直接把聊天界面拉起来了。

### 12.6 点下去之后发生了什么

```
11-07 11:22:46.109  [Widget] 从小部件进入，目标「宿舍A」     ← 点左半边
11-07 11:22:46.146  [Widget] 目标簿变化，刷新 1 个实例
11-07 11:22:46.271  [Jump] ═══ 准备直达「宿舍A」═══

11-07 11:22:58.979  [Widget] 从小部件进入，目标「宿舍B」     ← 点右半边
11-07 11:22:58.979  [Widget] 目标簿变化，刷新 1 个实例
11-07 11:22:59.135  [Jump] ═══ 准备直达「宿舍B」═══
```

小部件不是「直接拉起胖乖」，而是**带一个目标名进 `JumpActivity`**，由它去查设备清单、再走同一条链路。这样：

- 设备清单被清空 / 改了名，小部件不会静默失败，会停在本应用里告诉你「小部件要去的「X」已经不在设备清单里了」
- 调试模式照旧生效（小部件也一样只看不动）
- 无障碍没开也能点，只是进去之后会提示去开

### 12.7 状态怎么表达（1×1 装不下副标题）

1×1 只有 55dp 宽、40dp 高，放不下「设备名 + 状态副标题」两行，所以状态改用**颜色 + 图标淡化**：

| 表现 | 含义 |
|---|---|
| 名字纯白、水滴亮青 | 一切正常 |
| 名字降到 55% 白、水滴降到 35% 透明度 | 胖乖没装 / 无障碍没开 / 名字不在清单里 |

具体缺什么，点进去 `JumpActivity` 会在状态行里写清楚。设备被改名/删掉时，那一格直接显示「不在清单」代替名字，点进去能改。

## 13. 六个可调项（设置页）

| 项 | 默认 | 说明 |
|---|---|---|
| 调试模式 | 关 | 只记录会点哪一行、会拉起什么，不真的点。验证匹配正确性时先开这个，绝不会碰到解锁 |
| 步进等待 | 1200 ms | 每一步之间等多久。机器慢、页面渲染慢就往右调 |
| 待命窗口 | 25 秒 | 从拉起胖乖开始，多久之内允许自动点击。超过就撒手，不会在你手动用 App 时突然点一下 |
| **自动解锁** | **关** | 打开后会替你点确认页按钮、走完会员页、直到「解锁成功」 |
| **解锁等待** | **3 秒** | 确认页出现后等多久才点。太早点下去会静默无效 |
| 设备清单 | 宿舍A / 宿舍B（占位） | 首次启动自动写入两台占位名，请改成你自己的；可以加、改名、删、清空 |

> 用滑杆时如果觉得拖不动，直接**点滑杆上目标位置**也生效（实测点比拖更稳，拖动需要按在滑块上）。

## 14. 红线（写在这里，改代码前先读）

1. **只做入口替换 + 界面辅助点击。** 点的是胖乖界面上真实存在的控件，等价于你用手指点。没有注入事件、没有改 App、没有 hook。
2. **不抓包、不逆向私有接口、不构造订单、不模拟支付、不碰账号与登录态。**
3. **永远不点「开通并领取优惠」。** 那个按钮会开通 9.7 元/月的自动续费。会员页只认可「放弃优惠 原价使用」，并且识别条件刻意收窄到「含『放弃优惠』且不含『开通』」，认不出来就停下。
4. **不出水。** 自动解锁最多走到「解锁成功」，出水必须由你按饮水机机身上的实体「出水」键。本应用没有、也不会去模拟这个动作。
5. **自动解锁默认关闭**，打开要经过一次显式风险确认。
6. **不为了保活做常驻通知、前台服务自启、互相拉起。** 掉了授权就让你点一下提示条。
7. **只存设备名字。** 设备簿里没有 URL、没有 token、没有订单号、没有账号——胖乖设备列表上本来也只有它给设备起的名字。

## 15. 已知限制

1. **设备必须已经在胖乖 App 里出现过。** `MyDeviceAct` 显示的是「最近使用设备」，如果你从没在胖乖里扫过某台机器，它不会出现在列表里，V3 也就找不到它。
2. **设备名必须能对上。** 你写的名字和页面显示的名字要靠规整后相等或包含才能匹配。写「北三201」能对上「北3栋201」，但如果你只写「201」（而列表里同时有一台就叫「201」），按第 2 节的两轮挑选规则会先选中完全相等的那台。
3. **确认页按钮文案会变**（「领优惠券使用」/「立即使用」都出现过），所以只能按 `resource-id` `tvUse` 认。如果哪天胖乖改掉这个 id，第 4 步会失效——日志会打「确认页上没找到底部那个使用按钮（tvUse）」。
4. **会员页全靠文本匹配**，因为那一页所有 `resource-id` 都是空的。如果按钮文案改了，识别会失败并停住（不会乱点）。
5. **依赖 MIUI 跳转框能被点掉。** 如果哪天 MIUI 换成不可点击的强提示，`handleJumpConfirm` 会失效，届时需要改用 `startActivityForResult` + 用户手动确认。
6. **每次 `adb install -r` 都要重新授权无障碍**，这是系统行为，不是本应用的 bug。
7. **没在别的 ROM 上测过。** 只在 Redmi K40S（MIUI V816 / Android 14）上跑通过。ColorOS / realme 上无障碍授权被封，自愈做不到（见 V1 的 README 6.8 节）。
8. **V3 没有桌面小部件**（V1 有 1×1 部件）。要一键直达目前还是要点开 App 再点设备行。
9. **点击是「尽力而为」，不是事务。** `performAction(ACTION_CLICK)` 返回 true 只说明事件被受理（见 6.4）。代码里已经加了「1.5 秒没跳就重试、最多 3 次」，但 3 次之后仍然可能失败，那时会明确打「点了 N 次都没跳过去，停手」并把控制权交回你。它不会为了凑成功去乱点别的按钮。

## 16. 出问题怎么排查

```powershell
adb -s <你的设备序列号> logcat -c
adb -s <你的设备序列号> shell "am start -W -n com.local.qk3jump/.JumpActivity"
# 点一台设备，然后
adb -s <你的设备序列号> logcat -d -s Qk3Jump:V
```

TAG 恒为 `Qk3Jump`，按 area 分组：

| area | 含义 |
|---|---|
| `Life` | 启动、配置、生命周期 |
| `Jump` | 目标簿、启动 Intent、拉起结果 |
| `Tap` | 无障碍侧的页面识别与点击（含自动解锁三步） |
| `Match` | 设备名匹配（含规整前后的值） |
| `A11y` | 无障碍授权状态与自愈 |

五个最常见的断点：

1. **停在「无障碍服务没开」** → 授权没了，去设置里重开，或跑 10.1 那两条命令
2. **停在 MIUI 跳转确认框** → 看日志有没有 `遇到 MIUI 的跳转确认框`；没有就说明本应用没被允许点它
3. **设备列表共 N 行里没有你的目标** → 看紧接着的 `不匹配：…→rank=0`，对照规整后的值改设备名
4. **停在确认页不动** → 先看是不是 `自动解锁已开启`；没开是预期行为。开了还不动就看有没有 `确认页再过 NNNNms 就点解锁按钮`，有的话是在倒计时；超过 `解锁等待` 还没点，看有没有 `确认页点「…」结果=false`，或者 `确认页还停着（上次点击 Nms 前），重试第 N 次`（说明点了没生效，正在重试）
5. **停在会员页不动** → 说明 `findByTextContainsOnly` 没认出「放弃优惠 原价使用」。这是安全的失败方式（不会误点开通按钮），但需要按当时页面文案修 `Qk.TEXT_DECLINE_OFFER`
6. **看到「点了 3 次都没跳过去，停手」** → 重试上限到了。这不是 bug，是设计：宁可停手也不继续盲点。这时多半是页面卡死或者按钮真被改了，手动点一下就能继续

---

## 17. 改成你自己的设备（克隆之后从这儿开始）

这个仓库里**没有写死任何人的宿舍号**。首次启动会往设备清单里写两台占位设备「宿舍A」「宿舍B」，你把它们改成自己的就行。

### 17.1 三步走

1. **装 App、给无障碍授权**（第 10 节），先在胖乖 App 里手动扫一次你自己的机器，让它出现在「我的设备」里
2. **打开本应用，点那一行右侧的「⋯」→ 改名**，把占位名换成胖乖设备列表里显示的**那个名字**
3. **长按桌面空白 → 添加小部件 → 找到「胖乖直达」→ 拖到桌面**（第 12.5 节）

改名之后桌面小部件会自动跟着变（`JumpActivity.render()` 里调了 `DeviceWidget.refreshAll()`），不用重新拖。

### 17.2 名字怎么写才能匹配上

名字要能和胖乖列表里显示的串**规整后相等或包含**。规整规则（`Matcher.normalize()`）：剥掉「栋 / 楼 / 室 / 号 / 层 / - / 空格 / 括号」，中文数字转阿拉伯数字，转小写。

| 胖乖显示 | 你可以写 | 规整后 |
|---|---|---|
| `北3栋201` | `北三201` | 都是 `北3201` ✓ |
| `北3栋201` | `北3栋201` | 都是 `北3201` ✓ |
| `洗衣房-3号` | `洗衣房3号` | 都是 `洗衣房3` ✓ |
| `A栋101` | `101` | `a1` vs `101` ✗ 对不上 |

写错了不要紧：日志里会打出每一行的 `rank=` 值（`Match` tag），照着它改就行。

### 17.3 如果你有超过两台设备

小部件只显示清单的**前两台**（按最近使用时间排序）。想换显示哪两台，在 App 里点一下你想让它排前面那台的设备行，它的时间戳就会刷新到最前，小部件随之更新。

要显示三台以上，得改 `DeviceWidget.SLOT_COUNT` 和 `widget_devices.xml` 的格子数，同时把 `widget_devices_info.xml` 的 `targetCellWidth` 调大——注意尺寸换算是 `cells = ceil((size_dp + 30) / 70)`，2 格宽上限 110dp，3 格宽上限 180dp。

### 17.4 想改包名

改三处：`app/build.gradle.kts` 的 `namespace` 与 `applicationId`、`AndroidManifest.xml` 里 `accessibility_service_config.xml` 的引用、以及 `A11yGuard.kt` 里拼接无障碍组件名的地方（它用的是 `context.packageName`，通常不用改）。

改完包名意味着**无障碍授权会丢失**，要重新授权一次。

---

## 你会贴回来的东西

我会把下面这些贴回对话，由你据此判断哪一档生效、以及要改哪里：

1. **完整 logcat**：`adb -s <你的设备序列号> logcat -d -s Qk3Jump:V` 的全部输出（从 `═══ JumpActivity 启动 ═══` 到最后一行）
2. **三种策略各自的落点**：
   - 自动解锁关闭时，停在哪个 Activity、页面上写着什么
   - 自动解锁打开时，是否出现会员营销页、点完之后落在哪
   - 调试模式下，日志说「本应点 X」的那个 X 是否就是你想要的那个控件
3. **失败时的界面截图**，以及当时的 `adb shell dumpsys window | grep mCurrentFocus`