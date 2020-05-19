package net.evilblock.twofactorauth.util;

import net.evilblock.twofactorauth.TwoFactorAuth;

public class Tasks {

    public static void sync(Runnable runnable) {
        TwoFactorAuth.getInstance().getServer().getScheduler().runTask(TwoFactorAuth.getInstance(), runnable);
    }

    public static void delayed(long delay, Runnable runnable) {
        TwoFactorAuth.getInstance().getServer().getScheduler().runTaskLater(TwoFactorAuth.getInstance(), runnable, delay);
    }

    public static void timer(long delay, long interval, Runnable runnable) {
        TwoFactorAuth.getInstance().getServer().getScheduler().runTaskTimer(TwoFactorAuth.getInstance(), runnable, delay, interval);
    }

    public static void async(Runnable runnable) {
        TwoFactorAuth.getInstance().getServer().getScheduler().runTaskAsynchronously(TwoFactorAuth.getInstance(), runnable);
    }

    public static void asyncDelayed(long delay, Runnable runnable) {
        TwoFactorAuth.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(TwoFactorAuth.getInstance(), runnable, delay);
    }

    public static void asyncTimer(long delay, long interval, Runnable runnable) {
        TwoFactorAuth.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(TwoFactorAuth.getInstance(), runnable, delay, interval);
    }

}
