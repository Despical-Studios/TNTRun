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

package me.despical.tntrun.arena;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ArenaUtils {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	public static boolean areInSameArena(Player one, Player two) {
		Arena first = ArenaRegistry.getArena(one), second = ArenaRegistry.getArena(two);

		if (first == null || second == null) {
			return false;
		}

		return first.equals(second);
	}

	public static void hidePlayer(Player p, Arena arena) {
		for (Player player : arena.getPlayers()) {
			player.hidePlayer(plugin, p);
		}
	}

	public static void showPlayer(Player p, Arena arena) {
		for (Player player : arena.getPlayers()) {
			player.showPlayer(plugin, p);
		}
	}

	public static void hidePlayersOutsideTheGame(Player player, Arena arena) {
		for (Player players : plugin.getServer().getOnlinePlayers()) {
			if (arena.getPlayers().contains(players)) {
				continue;
			}

			player.hidePlayer(plugin, players);
			players.hidePlayer(plugin, player);
		}
	}

	public static void showPlayersOutsideTheGame(Player player, Arena arena) {
		for (Player players : plugin.getServer().getOnlinePlayers()) {
			if (arena.getPlayers().contains(players)) {
				continue;
			}

			player.showPlayer(plugin, players);
			players.showPlayer(plugin, player);
		}
	}

	public static void updateNameTagsVisibility(final Player p) {
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.NAME_TAGS_HIDDEN)) {
			return;
		}

		for (Player players : plugin.getServer().getOnlinePlayers()) {
			Arena arena = ArenaRegistry.getArena(players);

			if (arena == null) {
				continue;
			}

			Scoreboard scoreboard = players.getScoreboard();

			if (scoreboard == plugin.getServer().getScoreboardManager().getMainScoreboard()) {
				scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
			}

			Team team = scoreboard.getTeam("TRHide");

			if (team == null) {
				team = scoreboard.registerNewTeam("TRHide");
			}

			team.setAllowFriendlyFire(false);
			team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

			if (arena.getArenaState() == ArenaState.IN_GAME) {
				team.addEntry(p.getName());
			} else {
				team.removeEntry(p.getName());
			}

			players.setScoreboard(scoreboard);
		}
	}
}