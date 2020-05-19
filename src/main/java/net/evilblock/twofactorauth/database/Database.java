package net.evilblock.twofactorauth.database;

import java.io.Closeable;
import java.util.UUID;

public interface Database extends Closeable {

	boolean isSetup(UUID uuid);

	void setup(UUID uuid, String ipAddress, int code, String secret);

	boolean verifyCode(UUID uuid, String ipAddress, int code);

	boolean requiresAuthentication(UUID uuid, String ipAddress);

}
