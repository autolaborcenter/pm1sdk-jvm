import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.DoubleByReference
import java.lang.RuntimeException

private typealias Handler = Int

/**
 * 里程计数据
 */
data class Odometry(
    val s: Double, val sa: Double,
    val x: Double, val y: Double, val theta: Double,
    val vx: Double, val vy: Double, val w: Double
)

/**
 * 驱动函数
 */
object PM1 {
    /**
     * 初始化
     * @param port 串口名字
     */
    @JvmStatic
    fun initialize(port: String = ""): String {
        native.clear_error_info()
        val ignore = DoubleByReference()
        onNative(native.initialize_c(port, ignore.pointer))
        return native.get_current_port()
    }

    /**
     * 关闭
     */
    @JvmStatic
    fun shutdown() =
        onNative(native.shutdown())

    /**
     * 关闭时不会引发异常
     */
    @JvmStatic
    fun safeShutdown() {
        native.shutdown()
    }

    /**
     * 获取里程计数据
     */
    val odometry: Odometry
        @JvmStatic
        get() {
            val s = DoubleByReference()
            val sa = DoubleByReference()
            val x = DoubleByReference()
            val y = DoubleByReference()
            val theta = DoubleByReference()
            val vx = DoubleByReference()
            val vy = DoubleByReference()
            val w = DoubleByReference()
            onNative(native.get_odometry_c(
                s.pointer, sa.pointer,
                x.pointer, y.pointer, theta.pointer,
                vx.pointer, vy.pointer, w.pointer
            ))
            return Odometry(s.value, sa.value,
                            x.value, y.value, theta.value,
                            vx.value, vy.value, w.value)
        }

    /**
     * 清零里程计
     */
    @JvmStatic
    fun resetOdometry() =
        onNative(native.reset_odometry())

    /**
     * 查看使能状态
     */
    var locked: Boolean
        @JvmStatic
        get() {
            return when (native.check_state().toInt()) {
                0x01        -> false
                !in 0..0x7f -> true
                else        -> throw RuntimeException("chassis offline or in error state")
            }
        }
        @JvmStatic
        set(value) {
            onNative(native.set_enabled(!value))
        }

    /**
     * 控制机器人行驶
     * @param v 线速度
     * @param w 角速度
     */
    @JvmStatic
    fun drive(v: Double, w: Double) {
        onNative(native.drive_velocity(v, w))
    }

    private fun onNative(handler: Handler) {
        val error = native.get_error_info(handler)
        if (error.isNotBlank()) {
            native.remove_error_info(handler)
            throw RuntimeException(error)
        }
    }

    private val native by lazy {
        val os = System.getProperty("os.name").toLowerCase()
        val x64 = "64" in System.getProperty("os.arch")
        val path =
            when {
                "win" in os && x64   -> "win_x64/"
                "win" in os          -> "win_x86/"
                "linux" in os && x64 -> "linux_x64/lib"
                else                 -> throw RuntimeException("unsupported platform")
            }
        Native.load("${path}pm1_sdk_native", NativeFunctions::class.java)
    }

    @Suppress("FunctionName")
    private interface NativeFunctions : Library {
        fun get_error_info(handler: Handler): String

        fun remove_error_info(handler: Handler)

        fun clear_error_info()

        fun get_current_port(): String

        fun initialize_c(port: String, progress: Pointer): Handler

        fun shutdown(): Handler

        fun get_default_parameter(id: Handler): Double

        fun get_parameter_c(id: Handler, value: Pointer): Handler

        fun set_parameter(id: Handler, value: Double): Handler

        fun reset_parameter(id: Handler): Handler

        fun get_odometry_c(s: Pointer, sa: Pointer,
                           x: Pointer, y: Pointer, theta: Pointer,
                           vx: Pointer, vy: Pointer, w: Pointer): Handler

        fun reset_odometry(): Handler

        fun set_enabled(value: Boolean): Handler

        fun check_state(): Byte

        fun drive_velocity(v: Double, w: Double): Handler
    }
}
