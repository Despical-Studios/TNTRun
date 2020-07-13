package me.despical.tntrun.events.spectator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.item.ItemBuilder;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
@Deprecated
public class SpectatorSettingsMenu implements Listener {

	private String inventoryName;
	private String speedOptionName;
	private Inventory inv;

	public SpectatorSettingsMenu(JavaPlugin plugin, String inventoryName, String speedOptionName) {
		this.inventoryName = inventoryName;
		this.speedOptionName = speedOptionName;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		initInventory();
	}

	public void openSpectatorSettingsMenu(Player player) {
		player.openInventory(this.inv);
	}

	@EventHandler
	public void onSpectatorMenuClick(InventoryClickEvent e) {
		if (e.getInventory() == null || !e.getView().getTitle().equals(color(inventoryName))) {
			return;
		}
		if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) {
			return;
		}
		Player p = (Player) e.getWhoClicked();
		p.closeInventory();
		if (e.getCurrentItem().getType() == Material.LEATHER_BOOTS) {
			p.removePotionEffect(PotionEffectType.SPEED);
			p.setFlySpeed(0.15f);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false), false);
		} else if (e.getCurrentItem().getType() == Material.CHAINMAIL_BOOTS) {
			p.removePotionEffect(PotionEffectType.SPEED);
			p.setFlySpeed(0.2f);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false), false);
		} else if (e.getCurrentItem().getType() == Material.IRON_BOOTS) {
			p.removePotionEffect(PotionEffectType.SPEED);
			p.setFlySpeed(0.25f);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false), false);
		} else if (e.getCurrentItem().getType() == XMaterial.GOLDEN_BOOTS.parseMaterial()) {
			p.removePotionEffect(PotionEffectType.SPEED);
			p.setFlySpeed(0.3f);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, false, false), false);
		} else if (e.getCurrentItem().getType() == Material.DIAMOND_BOOTS) {
			p.removePotionEffect(PotionEffectType.SPEED);
			p.setFlySpeed(0.35f);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 4, false, false), false);
		}
	}

	private void initInventory() {
		Inventory inv = Bukkit.createInventory(null, 9 * 3, inventoryName);
		inv.setItem(11, new ItemBuilder(Material.LEATHER_BOOTS).name(color(speedOptionName + " I")).build());
		inv.setItem(12, new ItemBuilder(Material.CHAINMAIL_BOOTS).name(color(speedOptionName + " II")).build());
		inv.setItem(13, new ItemBuilder(Material.IRON_BOOTS).name(color(speedOptionName + " III")).build());
		inv.setItem(14, new ItemBuilder(XMaterial.GOLDEN_BOOTS.parseItem()).name(color(speedOptionName + " IV")).build());
		inv.setItem(15, new ItemBuilder(Material.DIAMOND_BOOTS).name(color(speedOptionName + " V")).build());
		this.inv = inv;
	}

	private String color(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}