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

package me.despical.tntrun.events;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.handlers.ChatManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Pattern;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ChatEvents extends ListenerAdapter {

	private final ChatManager chatManager;
	private final boolean chatFormatEnabled, disableSeparateChat;

	public ChatEvents(Main plugin) {
		super (plugin);
		this.chatManager = plugin.getChatManager();
		this.chatFormatEnabled = plugin.getConfigPreferences().getOption(ConfigPreferences.Option.CHAT_FORMAT_ENABLED);
		this.disableSeparateChat = plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_SEPARATE_CHAT);
	}

	@EventHandler
	public void onChatInGame(AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		final Arena arena = ArenaRegistry.getArena(player);

		if (arena == null) {
			if (!disableSeparateChat) {
				ArenaRegistry.getArenas().forEach(loopArena -> loopArena.getPlayers().forEach(p -> event.getRecipients().remove(p)));
			}

			return;
		}

		if (chatFormatEnabled) {
			final String message = formatChatPlaceholders(chatManager.message("in-game.game-chat-format"), player, event.getMessage().replaceAll(Pattern.quote("[$\\]"), ""));

			if (!disableSeparateChat) {
				event.setCancelled(true);

				final boolean dead = !arena.getPlayersLeft().contains(player);

				for (Player p : arena.getPlayers()) {
					if (dead && arena.getPlayersLeft().contains(p)) {
						continue;
					}

					p.sendMessage(dead ? formatChatPlaceholders(chatManager.message("in-game.game-death-format"), player, null) + message : message);
				}

				plugin.getServer().getConsoleSender().sendMessage(message);
			} else {
				event.setMessage(message);
			}
		}
	}

	private String formatChatPlaceholders(String message, Player player, String saidMessage) {
		String formatted = message;
		formatted = StringUtils.replace(formatted, "%player%", player.getName());
		formatted = StringUtils.replace(formatted, "%message%", ChatColor.stripColor(saidMessage));

		if (chatManager.isPapiEnabled()) {
			formatted = PlaceholderAPI.setPlaceholders(player, formatted);
		}

		return chatManager.color(formatted);
	}
}