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

package me.despical.tntrun.arena.options;

import me.despical.tntrun.Main;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public enum ArenaOption {

	TIMER(0),

	MINIMUM_PLAYERS(2),

	MAXIMUM_PLAYERS(12),

	WAITING_TIME("Starting-Waiting-Time", 60),

	START_TIME("Start-Time-On-Full-Lobby", 15);

	int defaultValue;

	ArenaOption(int defaultValue) {
		this.defaultValue = defaultValue;
	}

	ArenaOption(String path, int defaultValue) {
		final Main plugin = JavaPlugin.getPlugin(Main.class);

		this.defaultValue = plugin.getConfig().getInt(path, defaultValue);
	}

	public int getDefaultValue() {
		return defaultValue;
	}
}