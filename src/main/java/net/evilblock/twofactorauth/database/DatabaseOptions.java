package net.evilblock.twofactorauth.database;

import lombok.Getter;
import org.bukkit.configuration.MemorySection;

@Getter
public class DatabaseOptions {

	private String host;
	private int port;
	private String dbName;
	private boolean authentication;
	private String username;
	private String password;

	public static DatabaseOptions fromConfig(MemorySection config) {
		DatabaseOptions options = new DatabaseOptions();
		options.host = config.getString("database.options.host");
		options.port = config.getInt("database.options.port");
		options.dbName = config.getString("database.options.dbName");
		options.authentication = config.getBoolean("database.options.authentication.enabled");
		options.username = config.getString("database.options.authentication.username");
		options.password = config.getString("database.options.authentication.password");
		return options;
	}

}
