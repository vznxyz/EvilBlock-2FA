package net.evilblock.twofactorauth;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import net.evilblock.twofactorauth.command.AuthCommand;
import net.evilblock.twofactorauth.command.BypassCommand;
import net.evilblock.twofactorauth.command.SetupCommand;
import net.evilblock.twofactorauth.database.Database;
import net.evilblock.twofactorauth.database.DatabaseOptions;
import net.evilblock.twofactorauth.database.impl.JsonDatabase;
import net.evilblock.twofactorauth.database.impl.MongoDatabase;
import net.evilblock.twofactorauth.listener.PreventionListeners;
import net.evilblock.twofactorauth.listener.UserListeners;
import net.evilblock.twofactorauth.util.CommandMapUtil;
import net.evilblock.twofactorauth.util.FileConfig;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.plugin.java.JavaPlugin;

public class TwoFactorAuth extends JavaPlugin {

	@Getter private static TwoFactorAuth instance;

	private MemorySection mainConfig;
	private MemorySection langConfig;
	@Getter private Database databaseImpl; // can't name database because of bukkit :(

	@Override
	public void onEnable() {
		instance = this;

		this.mainConfig = new FileConfig(this, "config.yml").getConfig();
		this.langConfig = new FileConfig(this, "lang.yml").getConfig();

		switch (this.mainConfig.getString("database.implementation")) {
			case "JSON": {
				this.databaseImpl = new JsonDatabase();
			}
			break;
			case "MONGO": {
				this.databaseImpl = new MongoDatabase(DatabaseOptions.fromConfig(this.mainConfig));
			}
			break;
			default: {
				getLogger().severe("Database implementation must be either JSON or MONGO");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
		}

		continueLoad();
	}

	@Override
	public void onDisable() {
		if (this.databaseImpl != null) {
			try {
				this.databaseImpl.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void continueLoad() {
		CommandMapUtil.registerCommand(this, new AuthCommand(), "auth");
		CommandMapUtil.registerCommand(this, new SetupCommand(), "2fasetup", "setup2fa");
		CommandMapUtil.registerCommand(this, new BypassCommand(), "2fabypass");

		getServer().getPluginManager().registerEvents(new PreventionListeners(), this);
		getServer().getPluginManager().registerEvents(new UserListeners(), this);
	}

	public String getIssuerName() {
		return mainConfig.getString("settings.issuer-name", "EvilBlock 2FA");
	}

	public boolean isPermissionRequired() {
		return mainConfig.getBoolean("settings.require-permission", true);
	}

	public boolean isSetupRequired() {
		return mainConfig.getBoolean("settings.require-setup", true);
	}

	public List<String> getWhitelistedCommands() {
		return mainConfig.getStringList("settings.whitelisted-commands");
	}

	public String getAgreeText() {
		return mainConfig.getString("settings.agree-text", "yes");
	}

	public boolean isBypassCommandOpOnly() {
		return mainConfig.getBoolean("settings.bypass-command-op-only", true);
	}

	public boolean isUseBypassPermission() {
		return mainConfig.getBoolean("use-bypass-permission", false);
	}

	public boolean isDamageToLockedPlayersDisabled() {
		return mainConfig.getBoolean("settings.disable-damage-to-locked-players", true);
	}

	public List<String> getProvideCodePrompt() {
		return langConfig.getStringList("provide-code-prompt")
				.stream()
				.map(s -> ChatColor.translateAlternateColorCodes('&', s))
				.collect(Collectors.toList());
	}

	public List<String> getRequiredSetupPrompt() {
		return langConfig.getStringList("required-setup-prompt")
				.stream()
				.map(s -> ChatColor.translateAlternateColorCodes('&', s))
				.collect(Collectors.toList());
	}

	public List<String> getDisclaimerPrompt() {
		return langConfig.getStringList("disclaimer-prompt")
				.stream()
				.map(s -> ChatColor.translateAlternateColorCodes('&', s))
				.collect(Collectors.toList());
	}

	public List<String> getScanPrompt() {
		return langConfig.getStringList("scan-prompt")
				.stream()
				.map(s -> ChatColor.translateAlternateColorCodes('&', s))
				.collect(Collectors.toList());
	}

	public List<String> getSetupCompleteMessages() {
		return langConfig.getStringList("setup-complete")
				.stream()
				.map(s -> ChatColor.translateAlternateColorCodes('&', s))
				.collect(Collectors.toList());
	}

	public List<String> getSetupCancelledMessages() {
		return langConfig.getStringList("setup-cancelled")
				.stream()
				.map(s -> ChatColor.translateAlternateColorCodes('&', s))
				.collect(Collectors.toList());
	}

	public List<String> getSetupAbortedMessages() {
		return langConfig.getStringList("setup-aborted")
				.stream()
				.map(s -> ChatColor.translateAlternateColorCodes('&', s))
				.collect(Collectors.toList());
	}

}
