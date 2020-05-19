package net.evilblock.twofactorauth.command;

import net.evilblock.twofactorauth.TwoFactorAuth;
import net.evilblock.twofactorauth.util.LockedState;
import net.evilblock.twofactorauth.util.Tasks;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuthCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}

		Player player = (Player) sender;

		if (!LockedState.isLocked(player)) {
			player.sendMessage(ChatColor.RED + "You don't need to authenticate yourself.");
			return true;
		}

		String input = StringUtils.join(args).replace(" ", "");

		int code;
		try {
			code = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "The code you entered is invalid.");
			return true;
		}

		TwoFactorAuth plugin = TwoFactorAuth.getInstance();

		Tasks.async(() -> {
			boolean valid = plugin.getDatabaseImpl().verifyCode(
					player.getUniqueId(),
					player.getAddress().getAddress().getHostAddress(),
					code
			);

			if (valid) {
				LockedState.release(player);
				player.sendMessage(ChatColor.GREEN + "Your identity has been verified.");
			} else {
				player.sendMessage(ChatColor.RED + "Couldn't verify your identity. Check the code you entered and try again.");
			}
		});

		return true;
	}

}
