/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2023 Despical
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.tntrun.events.spectator;

import me.despical.tntrun.Main;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.events.ListenerAdapter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class SpectatorEvents extends ListenerAdapter {

	public SpectatorEvents(Main plugin) {
		super (plugin);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onSpectatorTarget(EntityTargetEvent e) {
		if (!(e.getTarget() instanceof Player)) {
			return;
		}

		if (plugin.getUserManager().getUser((Player) e.getTarget()).isSpectator()) {
			e.setCancelled(true);
			e.setTarget(null);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onSpectatorTarget(EntityTargetLivingEntityEvent e) {
		if (!(e.getTarget() instanceof Player)) {
			return;
		}

		if (plugin.getUserManager().getUser((Player) e.getTarget()).isSpectator()) {
			e.setCancelled(true);
			e.setTarget(null);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (plugin.getUserManager().getUser(event.getPlayer()).isSpectator()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		if (plugin.getUserManager().getUser(event.getPlayer()).isSpectator()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onDropItem(PlayerDropItemEvent event) {
		if (plugin.getUserManager().getUser(event.getPlayer()).isSpectator()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		if (plugin.getUserManager().getUser(event.getPlayer()).isSpectator()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInteract(PlayerInteractEntityEvent event) {
		if (plugin.getUserManager().getUser(event.getPlayer()).isSpectator()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onShear(PlayerShearEntityEvent event) {
		if (plugin.getUserManager().getUser(event.getPlayer()).isSpectator()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onConsume(PlayerItemConsumeEvent event) {
		if (plugin.getUserManager().getUser(event.getPlayer()).isSpectator()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getEntity();

		if (plugin.getUserManager().getUser(player).isSpectator()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getEntity();

		if (!plugin.getUserManager().getUser(player).isSpectator() || ArenaRegistry.getArena(player) == null) {
			return;
		}

		if (player.getLocation().getY() < 1) {
			player.teleport(ArenaRegistry.getArena(player).getLobbyLocation());
		}

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onDamageByBlock(EntityDamageByBlockEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getEntity();

		if (plugin.getUserManager().getUser(player).isSpectator()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onDamageByEntity(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getDamager();

		if (plugin.getUserManager().getUser(player).isSpectator()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPickup(PlayerPickupItemEvent event) {
		if (plugin.getUserManager().getUser(event.getPlayer()).isSpectator()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onRightClick(PlayerInteractEvent event) {
		if (ArenaRegistry.isInArena(event.getPlayer()) && plugin.getUserManager().getUser(event.getPlayer()).isSpectator()) {
			event.setCancelled(true);
		}
	}
}