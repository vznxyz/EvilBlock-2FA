package net.evilblock.twofactorauth.util;

import org.bukkit.entity.Player;

public enum Permissions {

    OTP_ACCESS("2fa.access"),
    OTP_BYPASS("2fa.bypass");

    private String permission;

    Permissions (String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    public boolean has(Player player) {
        return player.hasPermission(permission);
    }

}
