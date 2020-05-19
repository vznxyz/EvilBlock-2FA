package net.evilblock.twofactorauth.listener;

import net.evilblock.twofactorauth.TwoFactorAuth;
import net.evilblock.twofactorauth.util.LockedState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PreventionListeners implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (LockedState.isLocked(event.getPlayer())) {
			String command = event.getMessage().toLowerCase();

			// iterate whitelisted commands instead of using .contains because of case sensitivity
			for (String whitelistedCommand : TwoFactorAuth.getInstance().getWhitelistedCommands()) {
				if (command.equalsIgnoreCase(whitelistedCommand)) {
					event.getPlayer().sendMessage(LockedState.getMessage(event.getPlayer()));
					event.setCancelled(true);
					break;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if (LockedState.isLocked(event.getPlayer())) {
			event.getPlayer().sendMessage(LockedState.getMessage(event.getPlayer()));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (LockedState.isLocked(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (LockedState.isLocked((Player) event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (LockedState.isLocked(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (LockedState.isLocked(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (TwoFactorAuth.getInstance().isDamageToLockedPlayersDisabled()) {
			if (event.getDamager() instanceof Player && LockedState.isLocked((Player) event.getDamager())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (LockedState.isLocked(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if (LockedState.isLocked(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if (LockedState.isLocked(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent event) {
		if (LockedState.isLocked((Player) event.getWhoClicked())) {
			event.setCancelled(true);
		}
	}

}
