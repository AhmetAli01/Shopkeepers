package com.nisovin.shopkeepers.shopobjects.fancynpcs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.fancyinnovations.fancynpcs.api.Npc;
import com.fancyinnovations.fancynpcs.api.events.NpcDeleteEvent;
import com.fancyinnovations.fancynpcs.api.events.NpcDespawnEvent;
import com.fancyinnovations.fancynpcs.api.events.NpcSpawnEvent;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.util.logging.Log;

/**
 * Listens to FancyNpcs events to keep shopkeeper data in sync.
 * <p>
 * This is the FancyNpcs equivalent of {@code CitizensListener}.
 * </p>
 */
class FancyNpcsListener implements Listener {

	private final ShopkeepersPlugin plugin;
	private final FancyNpcsShops fancyNpcsShops;

	FancyNpcsListener(ShopkeepersPlugin plugin, FancyNpcsShops fancyNpcsShops) {
		assert plugin != null && fancyNpcsShops != null;
		this.plugin = plugin;
		this.fancyNpcsShops = fancyNpcsShops;
	}

	void onEnable() {
	}

	void onDisable() {
	}

	// NPC SPAWNING

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onNpcSpawn(NpcSpawnEvent event) {
		Npc npc = event.getNpc();
		if (npc == null) return;

		Shopkeeper shopkeeper = fancyNpcsShops.getShopkeeper(npc);
		if (shopkeeper == null) return;

		Log.debug(() -> shopkeeper.getLogPrefix() + "FancyNpc NPC has been spawned.");

		SKFancyNpcShopObject shopObject = (SKFancyNpcShopObject) shopkeeper.getShopObject();
		Player player = event.getPlayer();
		// FancyNpcs spawns NPCs per-player (packet-based), not with actual Bukkit entities.
		// We update the shopkeeper location based on NPC data location if location changed.
		Location npcLoc = npc.getData().getLocation();
		if (npcLoc != null) {
			shopObject.onNpcTeleport(npcLoc);
		}
	}

	// NPC DESPAWNING

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onNpcDespawn(NpcDespawnEvent event) {
		Npc npc = event.getNpc();
		if (npc == null) return;

		Shopkeeper shopkeeper = fancyNpcsShops.getShopkeeper(npc);
		if (shopkeeper == null) return;

		Log.debug(() -> shopkeeper.getLogPrefix() + "FancyNpc NPC is despawned.");

		SKFancyNpcShopObject shopObject = (SKFancyNpcShopObject) shopkeeper.getShopObject();
		shopObject.setEntity(null);
	}

	// NPC DELETION

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onNpcDelete(NpcDeleteEvent event) {
		Npc npc = event.getNpc();
		if (npc == null) return;

		List<? extends Shopkeeper> shopkeepers = fancyNpcsShops.getShopkeepers(npc);
		if (!shopkeepers.isEmpty()) {
			new ArrayList<>(shopkeepers).forEach(shopkeeper -> {
				assert shopkeeper.getShopObject() instanceof SKFancyNpcShopObject;
				// Handle without player (NpcDeleteEvent doesn't provide who deleted it directly):
				((SKFancyNpcShopObject) shopkeeper.getShopObject()).onNpcDeleted(null);
			});
		}
	}
}
