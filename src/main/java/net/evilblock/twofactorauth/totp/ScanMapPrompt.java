package net.evilblock.twofactorauth.totp;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Collections;
import javax.imageio.ImageIO;
import net.evilblock.twofactorauth.TwoFactorAuth;
import net.evilblock.twofactorauth.util.LockedState;
import net.evilblock.twofactorauth.util.Tasks;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;

public class ScanMapPrompt extends StringPrompt {

	private int failures = 0;

	@Override
	public String getPromptText(ConversationContext context) {
		Player player = (Player) context.getForWhom();

		if (this.failures == 0) {
			Tasks.async(() -> {
				String secret = generateSecret();
				BufferedImage image = generateImage(player, secret);

				if (image != null) {
					MapView mapView = Bukkit.getServer().createMap(player.getWorld());
					mapView.getRenderers().forEach(mapView::removeRenderer);
					mapView.addRenderer(new QCodeMapRenderer(player.getUniqueId(), image));

					ItemStack mapItem = new ItemStack(Material.MAP, 1, mapView.getId());
					ItemMeta mapMeta = mapItem.getItemMeta();

					mapMeta.setLore(Collections.singletonList("QR Code Map"));
					mapItem.setItemMeta(mapMeta);

					context.setSessionData("secret", secret);
					context.setSessionData("map", mapItem);

					player.sendMap(mapView);
					player.getInventory().addItem(mapItem);
					player.updateInventory();
				}
			});
		}

		return StringUtils.join(TwoFactorAuth.getInstance().getScanPrompt(), ' ');
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		ItemStack mapItem = (ItemStack) context.getSessionData("map");
		String secret = (String) context.getSessionData("secret");

		Player player = (Player) context.getForWhom();
		player.getInventory().remove(mapItem);

		int code;
		try {
			code = Integer.parseInt(input.replace(" ", ""));
		} catch (NumberFormatException e) {
			if (this.failures++ >= 3) {
				for (String message : TwoFactorAuth.getInstance().getSetupCancelledMessages()) {
					context.getForWhom().sendRawMessage(message);
				}

				return Prompt.END_OF_CONVERSATION;
			}

			context.getForWhom().sendRawMessage("");
			context.getForWhom().sendRawMessage(ChatColor.RED.toString() + "\"" + input + "\" isn't a valid code. Let's try that again.");
			return this;
		}

		LockedState.release(player);

		for (String message : TwoFactorAuth.getInstance().getSetupCompleteMessages()) {
			context.getForWhom().sendRawMessage(message);
		}

		Tasks.async(() -> {
			TwoFactorAuth.getInstance().getDatabaseImpl().setup(
					player.getUniqueId(),
					player.getAddress().getAddress().getHostAddress(),
					code,
					secret
			);
		});

		return Prompt.END_OF_CONVERSATION;
	}

	private BufferedImage generateImage(Player player, String secret) {
		Escaper urlEscaper = UrlEscapers.urlFragmentEscaper();

		String issuer = TwoFactorAuth.getInstance().getIssuerName();
		String url = "otpauth://totp/" + urlEscaper.escape(player.getName()) + "?secret=" + secret + "&issuer=" + urlEscaper.escape(issuer);
		String imageUrl = String.format(IMAGE_URL_FORMAT, URLEncoder.encode(url));

		try {
			return ImageIO.read(new URL(imageUrl));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static final SecureRandom SECURE_RANDOM;
	private static final Base32 BASE_32_ENCODER = new Base32();
	private static final String IMAGE_URL_FORMAT = "https://www.google.com/chart?chs=130x130&chld=M%%7C0&cht=qr&chl=%s";

	static {
		try {
			SECURE_RANDOM = SecureRandom.getInstance("SHA1PRNG", "SUN");
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new RuntimeException("This should never happen");
		}
	}

	private static String generateSecret() {
		byte[] secretKey = new byte[10];
		SECURE_RANDOM.nextBytes(secretKey);
		return BASE_32_ENCODER.encodeToString(secretKey);
	}

}