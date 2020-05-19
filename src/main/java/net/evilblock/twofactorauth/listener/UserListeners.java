package net.evilblock.twofactorauth.listener;

import java.util.ArrayList;
import java.util.List;

import net.evilblock.twofactorauth.TwoFactorAuth;
import net.evilblock.twofactorauth.util.LockedState;
import net.evilblock.twofactorauth.util.Permissions;
import net.evilblock.twofactorauth.util.Tasks;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class UserListeners implements Listener {

    /**
     * Instead of using the {@link org.bukkit.event.player.AsyncPlayerPreLoginEvent}, we wait until the player
	 * actually joins the server and gets assigned their permissions to run our checks. This way we can allow
	 * users to be assigned OTP via permission instead of a manual list maintained by a server operator.
     * <p>
     * Also, we have the added bonus of working with the {@link Player} object directly.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        TwoFactorAuth plugin = TwoFactorAuth.getInstance();
        Player player = event.getPlayer();

        if (plugin.isUseBypassPermission()) {
            if (Permissions.OTP_BYPASS.has(player)) {
                return;
            }
        }

        if (Permissions.OTP_ACCESS.has(player)) {
            Tasks.async(() -> {
                if (plugin.getDatabaseImpl().isSetup(player.getUniqueId())) {
                    if (plugin.getDatabaseImpl().requiresAuthentication(player.getUniqueId(), player.getAddress().getAddress().getHostAddress())) {
                        List<String> messages = new ArrayList<>();

                        for (String message : plugin.getProvideCodePrompt()) {
                            messages.add(message);
                            player.sendMessage(message);
                        }

                        LockedState.lock(player, StringUtils.join(messages, ' '));
                    }
                } else {
                    if (plugin.isSetupRequired()) {
                        List<String> messages = new ArrayList<>();

                        for (String message : TwoFactorAuth.getInstance().getRequiredSetupPrompt()) {
                            messages.add(message);
                            player.sendMessage(message);
                        }

                        LockedState.lock(player, StringUtils.join(messages, ' '));
                    }
                }
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (LockedState.isLocked(player)) {
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (itemStack.getType() == Material.MAP && itemStack.getItemMeta().hasLore()) {
                    final List<String> lore = itemStack.getItemMeta().getLore();

                    if (!lore.isEmpty() && lore.get(0).equalsIgnoreCase("QR Code Map")) {
                        player.getInventory().remove(itemStack);
                        player.updateInventory();
                    }
                }
            }
        }
    }

}
