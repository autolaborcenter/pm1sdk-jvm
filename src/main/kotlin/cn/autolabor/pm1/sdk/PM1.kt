package cn.autolabor.pm1.sdk

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.DoubleByReference

private typealias Handler = Int

/**
 * 驱动函数
 */
object PM1 {
    enum class ParameterId {
        Width,
        Length,
        LeftRadius,
        RightRadius;

        val id get() = values().indexOf(this)
    }

    /**
     * 初始化
     * @param port 串口名字
     */
    @JvmStatic
    fun initialize(port: String = ""): String {
        native.clear_error_info()
        val ignore = DoubleByReference()
        onNative(native.initialize_c(port, ignore.pointer))
        return native.get_connected_port()
    }

    /**
     * 关闭
     */
    @JvmStatic
    fun shutdown() =
        onNative(native.shutdown())

    /**
     * 获取参数
     */
    operator fun get(id: ParameterId): Double {
        val ptr = DoubleByReference()
        onNative(native.get_parameter_c(id.id, ptr.pointer))
        return ptr.value
    }

    /**
     * 设置参数
     */
    operator fun set(id: ParameterId, value: Double) {
        onNative(native.set_parameter(id.id, value))
    }

    /**
     * 重置参数
     */
    fun reset(id: ParameterId) {
        onNative(native.reset_parameter(id.id))
    }

    /**
     * 关闭时不会引发异常
     */
    @JvmStatic
    fun safeShutdown(): String {
        val handler = native.shutdown()
        val error = native.get_error_info(handler)
        native.clear_error_info()
        return error
    }

    /**
     * 获取里程计数据
     */
    val odometry: Triple<Double, Double, Double>
        @JvmStatic
        get() {
            val stamp = DoubleByReference()
            val s = DoubleByReference()
            val sa = DoubleByReference()
            val x = DoubleByReference()
            val y = DoubleByReference()
            val theta = DoubleByReference()
            onNative(native.get_odometry_c(
                stamp.pointer,
                s.pointer, sa.pointer,
                x.pointer, y.pointer, theta.pointer
            ))
            return Triple(x.value, y.value, theta.value)
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
     * 设置控制使能
     */
    fun setCommandEnabled(value: Boolean) =
        onNative(native.set_command_enabled(value))

    /**
     * 控制机器人行驶
     * @param v 线速度
     * @param w 角速度
     */
    @JvmStatic
    fun drive(v: Double, w: Double) {
        onNative(native.drive_velocity(v, w))
    }

    /**
     * 控制机器人行驶
     * @param v 线速度
     * @param w 角速度
     */
    @JvmStatic
    fun driveSpatial(v: Double, w: Double, spatium: Double, angle: Double) {
        val ignore = DoubleByReference()
        onNative(native.drive_spatial_c(v, w, spatium, angle, ignore.pointer))
    }

    private fun onNative(handler: Handler) {
        val error = native.get_error_info(handler)
        if (error.isNotBlank()) {
            native.remove_error_info(handler)
            throw RuntimeException(error)
        }
    }

    private val native by lazy {
        try {
            Native.load("pm1_sdk_native", NativeFunctions::class.java)
        } catch (e: UnsatisfiedLinkError) {
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
    }

    @Suppress("FunctionName")
    private interface NativeFunctions : Library {
        fun get_error_info(handler: Handler): String

        fun remove_error_info(handler: Handler)

        fun clear_error_info()

        fun get_connected_port(): String

        fun initialize_c(port: String, progress: Pointer): Handler

        fun shutdown(): Handler

        fun get_default_parameter(id: Handler): Double

        fun get_parameter_c(id: Handler, value: Pointer): Handler

        fun set_parameter(id: Handler, value: Double): Handler

        fun reset_parameter(id: Handler): Handler

        fun get_rudder_c(value: Pointer): Handler

        fun get_odometry_c(stamp: Pointer,
                           s: Pointer, sa: Pointer,
                           x: Pointer, y: Pointer, theta: Pointer): Handler

        fun reset_odometry(): Handler

        fun set_command_enabled(value: Boolean): Handler

        fun set_enabled(value: Boolean): Handler

        fun check_state(): Byte

        fun drive_velocity(v: Double, w: Double): Handler

        fun drive_spatial_c(v: Double,
                            w: Double,
                            spatium: Double,
                            angle: Double,
                            progress: Pointer): Handler
    }
}
