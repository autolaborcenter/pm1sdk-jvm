package cn.autolabor.pm1.sdk;

public class Main {
    public static void main(String[] args) {
        try {
            String com = PM1.initialize("");
            System.out.println(String.format("connected: %s", com));
            PM1.setLocked(false);
            long time = System.currentTimeMillis();
            while (System.currentTimeMillis() - time < 5000) {
                PM1.drive(1.0, 0.0);
                System.out.println(PM1.getOdometry().getX());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            PM1.safeShutdown();
        }
    }
}
