package net.evilblock.twofactorauth.database.impl;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import net.evilblock.twofactorauth.TwoFactorAuth;
import net.evilblock.twofactorauth.database.Database;
import net.evilblock.twofactorauth.totp.TotpUtil;

public class JsonDatabase implements Database {

	private Gson gson = new GsonBuilder()
			.registerTypeAdapter(User.class, new UserAdapter())
			.create();

	private Map<UUID, User> users = new HashMap<>(); // our user cache, initialized on startup

	public JsonDatabase() {
		loadMemory();
	}

	private File getDataFile() {
		return new File(TwoFactorAuth.getInstance().getDataFolder(), "otpUsers.json");
	}

	private void loadMemory() {
		File dataFile = getDataFile();
		if (dataFile.exists()) {
			try {
				BufferedReader memoryReader = Files.newReader(dataFile, Charsets.UTF_8);
				Type dataType = new TypeToken<List<User>>(){}.getType();
				List<User> data = gson.fromJson(memoryReader, dataType);

				for (User user : data) {
					this.users.put(user.uuid, user);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void saveMemory() {
		try {
			Files.write(gson.toJson(users.values()), getDataFile(), Charsets.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isSetup(UUID uuid) {
		return this.users.containsKey(uuid);
	}

	@Override
	public void setup(UUID uuid, String ipAddress, int code, String secret) {
		this.users.put(uuid, new User(uuid, ipAddress, secret));
		saveMemory();
	}

	@Override
	public boolean verifyCode(UUID uuid, String ipAddress, int code) {
		User user = this.users.get(uuid);

		if (user != null) {
			try {
				boolean valid = TotpUtil.validateCurrentNumber(user.secret, code, 250);

				if (valid) {
					user.ipAddress = ipAddress;
					saveMemory();
				}

				return valid;
			} catch (GeneralSecurityException e) {
				return false;
			}
		}

		return false;
	}

	@Override
	public boolean requiresAuthentication(UUID uuid, String ipAddress) {
		User user = this.users.get(uuid);

		if (user != null) {
			return !user.ipAddress.equalsIgnoreCase(ipAddress);
		}

		return false;
	}

	@Override
	public void close() {

	}

	@AllArgsConstructor
	class User {
		private UUID uuid;
		private String ipAddress;
		private String secret;
	}

	class UserAdapter implements JsonSerializer<User>, JsonDeserializer<User> {
		@Override
		public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("uuid", src.uuid.toString());
			object.addProperty("ipAddress", src.ipAddress);
			object.addProperty("secret", src.secret);
			return object;
		}

		@Override
		public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();

			return new User(
					UUID.fromString(object.get("uuid").getAsString()),
					object.get("ipAddress").getAsString(),
					object.get("secret").getAsString()
			);
		}
	}

}
