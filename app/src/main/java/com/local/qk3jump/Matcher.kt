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
