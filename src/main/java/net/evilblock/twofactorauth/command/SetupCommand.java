package net.evilblock.twofactorauth.command;

import net.evilblock.twofactorauth.TwoFactorAuth;
import net.evilblock.twofactorauth.totp.DisclaimerPrompt;
import net.evilblock.twofactorauth.util.Permissions;
import net.evilblock.twofactorauth.util.Tasks;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

public class SetupCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}

		TwoFactorAuth plugin = TwoFactorAuth.getInstance();
		Player player = (Player) sender;

		if (!Permissions.OTP_ACCESS.has(player) && plugin.isPermissionRequired()) {
			player.sendMessage(ChatColor.RED + "You don't have permission to setup 2FA.");
			return true;
		}

		Tasks.async(() -> {
			if (plugin.getDatabaseImpl().isSetup(player.getUniqueId())) {
				player.sendMessage(ChatColor.RED + "You already have 2FA setup!");
				return;
			}

			ConversationFactory factory = new ConversationFactory(plugin)
					.withFirstPrompt(new DisclaimerPrompt())
					.withLocalEcho(false)
					.thatExcludesNonPlayersWithMessage("Go away evil console!");

			player.beginConversation(factory.buildConversation(player));
		});

		return true;
	}
}
