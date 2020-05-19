package net.evilblock.twofactorauth.totp;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import java.awt.image.BufferedImage;
import java.util.*;

@AllArgsConstructor
public class QCodeMapRenderer extends MapRenderer {

	private UUID uuid;
	private BufferedImage image;

	@Override
	public void render(MapView map, MapCanvas canvas, Player player) {
		if (player.getUniqueId().equals(uuid)) {
			canvas.drawImage(0, 0, this.image);
		}
	}

}
