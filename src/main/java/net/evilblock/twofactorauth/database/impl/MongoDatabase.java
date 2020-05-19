package net.evilblock.twofactorauth.database.impl;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import java.security.GeneralSecurityException;
import java.util.UUID;
import net.evilblock.twofactorauth.database.Database;
import net.evilblock.twofactorauth.database.DatabaseOptions;
import net.evilblock.twofactorauth.totp.TotpUtil;
import org.bson.Document;

public class MongoDatabase implements Database {

	private MongoClient client;
	private com.mongodb.client.MongoDatabase database;
	private MongoCollection<Document> collection;

	public MongoDatabase(DatabaseOptions dbOptions) {
		if (dbOptions.isAuthentication()) {
			ServerAddress serverAddress = new ServerAddress(dbOptions.getHost(), dbOptions.getPort());

			MongoCredential credential = MongoCredential.createCredential(dbOptions.getUsername(), "admin", dbOptions.getPassword().toCharArray());

			this.client = new MongoClient(serverAddress, credential, MongoClientOptions.builder().build());
		} else {
			this.client = new MongoClient(dbOptions.getHost(), dbOptions.getPort());
		}

		this.database = this.client.getDatabase(dbOptions.getDbName());
		this.collection = this.database.getCollection("otp-users");
	}

	@Override
	public boolean isSetup(UUID uuid) {
		return this.collection.find(new Document("uuid", uuid.toString())).first() != null;
	}

	@Override
	public void setup(UUID uuid, String ipAddress, int code, String secret) {
		Document document = new Document();
		document.put("uuid", uuid.toString());
		document.put("ipAddress", ipAddress);
		document.put("secret", secret);

		this.collection.replaceOne(new Document("uuid", uuid.toString()), document, new ReplaceOptions().upsert(true));
	}

	@Override
	public boolean verifyCode(UUID uuid, String ipAddress, int code) {
		Document document = this.collection.find(new Document("uuid", uuid.toString())).first();

		if (document != null) {
			try {
				boolean valid = TotpUtil.validateCurrentNumber(document.getString("secret"), code, 250);

				if (valid) {
					this.collection.updateOne(new Document("uuid", uuid.toString()), new Document("$set", new Document("ipAddress", ipAddress)));
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
		Document document = this.collection.find(new Document("uuid", uuid.toString())).first();

		if (document != null) {
			return !document.getString("ipAddress").equalsIgnoreCase(ipAddress);
		}

		return false;
	}

	@Override
	public void close() {
		this.client.close();
	}

}
