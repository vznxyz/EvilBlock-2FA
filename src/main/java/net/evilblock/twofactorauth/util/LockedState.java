package net.evilblock.twofactorauth.util;

import net.evilblock.twofactorauth.TwoFactorAuth;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class LockedState {

    private static String METADATA_KEY = "LOCKED";

    public static void lock(Player player, String message) {
        player.setMetadata(METADATA_KEY, new FixedMetadataValue(TwoFactorAuth.getInstance(), message));
    }

    public static void release(Player player) {
        player.removeMetadata(METADATA_KEY, TwoFactorAuth.getInstance());
    }

    public static boolean isLocked(Player player) {
        return player.hasMetadata(METADATA_KEY);
    }

    public static String getMessage(Player player) {
        return player.getMetadata(METADATA_KEY).get(0).asString();
    }

}
