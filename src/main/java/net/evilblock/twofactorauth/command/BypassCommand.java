package net.evilblock.twofactorauth.command;

import net.evilblock.twofactorauth.TwoFactorAuth;
import net.evilblock.twofactorauth.util.LockedState;
import net.evilblock.twofactorauth.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BypassCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			if (!Permissions.OTP_BYPASS_COMMAND.has(player)) {
				player.sendMessage(ChatColor.RED + "You don't have permission to execute that command.");
				return true;
			}

			if (TwoFactorAuth.getInstance().isBypassCommandOpOnly()) {
				player.sendMessage(ChatColor.RED + "This command can only be executed by console.");
				return true;
			}
		}

		if (args.length == 0) {
			return false;
		}

		Player target = Bukkit.getPlayer(args[0]);

		if (target == null) {
			sender.sendMessage(ChatColor.RED + "A player by that name couldn't be found.");
			return true;
		}

		if (!LockedState.isLocked(target)) {
			sender.sendMessage(ChatColor.RED + "That player doesn't need to be bypassed.");
			return true;
		}

		LockedState.isLocked(target);
		target.sendMessage(ChatColor.YELLOW + "An admin has granted you 2FA bypass.");

		sender.sendMessage(ChatColor.GREEN + "You granted " + target.getName() + " 2FA bypass.");

		return true;
	}

}
