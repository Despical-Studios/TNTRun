/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2020 Despical
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

package me.despical.tntrun.handlers;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaState;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class BungeeManager implements Listener {

	private final Main plugin;
	private final Map<ArenaState, String> gameStateToString = new EnumMap<>(ArenaState.class);
	private final String MOTD;

	public BungeeManager(Main plugin) {
		this.plugin = plugin;

		gameStateToString.put(ArenaState.WAITING_FOR_PLAYERS, plugin.getChatManager().colorRawMessage(ConfigUtils.getConfig(plugin, "bungee").getString("MOTD.Game-States.Inactive", "Inactive")));
		gameStateToString.put(ArenaState.STARTING, plugin.getChatManager().colorRawMessage(ConfigUtils.getConfig(plugin, "bungee").getString("MOTD.Game-States.Starting", "Starting")));
		gameStateToString.put(ArenaState.IN_GAME, plugin.getChatManager().colorRawMessage(ConfigUtils.getConfig(plugin, "bungee").getString("MOTD.Game-States.In-Game", "In-Game")));
		gameStateToString.put(ArenaState.ENDING, plugin.getChatManager().colorRawMessage(ConfigUtils.getConfig(plugin, "bungee").getString("MOTD.Game-States.Ending", "Ending")));
		gameStateToString.put(ArenaState.RESTARTING, plugin.getChatManager().colorRawMessage(ConfigUtils.getConfig(plugin, "bungee").getString("MOTD.Game-States.Restarting", "Restarting")));

		MOTD = plugin.getChatManager().colorRawMessage(ConfigUtils.getConfig(plugin, "bungee").getString("MOTD.Message", "The actual game state of TNT Run is %state%"));

		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void connectToHub(Player player) {
		if (!ConfigUtils.getConfig(plugin, "bungee").getBoolean("Connect-To-Hub", true)) {
			return;
		}

		ByteArrayDataOutput out = ByteStreams.newDataOutput();

		out.writeUTF("Connect");
		out.writeUTF(getHubServerName());

		player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
	}

	private ArenaState getArenaState() {
		return ArenaRegistry.getArenas().get(ArenaRegistry.getBungeeArena()).getArenaState();
	}

	private String getHubServerName() {
		return ConfigUtils.getConfig(plugin, "bungee").getString("Hub");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerListPing(ServerListPingEvent event) {
		if (!ConfigUtils.getConfig(plugin, "bungee").getBoolean("MOTD.Manager", false)) {
			return;
		}

		if (ArenaRegistry.getArenas().isEmpty()) {
			return;
		}

		event.setMaxPlayers(ArenaRegistry.getArenas().get(ArenaRegistry.getBungeeArena()).getMaximumPlayers());
		event.setMotd(MOTD.replace("%state%", gameStateToString.get(getArenaState())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(final PlayerJoinEvent event) {
		event.setJoinMessage("");
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> ArenaManager.joinAttempt(event.getPlayer(), ArenaRegistry.getArenas().get(ArenaRegistry.getBungeeArena())), 1L);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent event) {
		event.setQuitMessage("");

		if (ArenaRegistry.getArena(event.getPlayer()) != null) {
			ArenaManager.leaveAttempt(event.getPlayer(), ArenaRegistry.getArenas().get(ArenaRegistry.getBungeeArena()));
		}
	}
}