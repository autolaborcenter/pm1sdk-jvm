package cn.autolabor.pm1.sdk

fun main() {
    try {
        println("connected: ${PM1.initialize()}")
        PM1.locked = false
        val time = System.currentTimeMillis()
        while (System.currentTimeMillis() - time < 5000) {
            PM1.drive(1.0, 0.0)
            println(PM1.odometry.x)
        }
    } catch (e: Exception) {
        println(e.message)
    } finally {
        PM1.safeShutdown()
    }
}
