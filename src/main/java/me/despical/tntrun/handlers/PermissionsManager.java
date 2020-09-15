package me.despical.tntrun.handlers;

import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.despical.tntrun.Main;
import static me.despical.tntrun.utils.Debugger.debug;

/**
* @author Despical
* <p>
* Created at 10.07.2020
*/
public class PermissionsManager {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static String joinFullPerm = "tntrun.fullgames";
	private static String joinPerm = "tntrun.join.<arena>";

	public static void init() {
		setupPermissions();
	}

	public static String getJoinFullGames() {
		return joinFullPerm;
	}

	private static void setJoinFullGames(String joinFullGames) {
		PermissionsManager.joinFullPerm = joinFullGames;
	}

	public static String getJoinPerm() {
		return joinPerm;
	}

	private static void setJoinPerm(String joinPerm) {
		PermissionsManager.joinPerm = joinPerm;
	}

	private static void setupPermissions() {
		PermissionsManager.setJoinFullGames(plugin.getConfig().getString("Basic-Permissions.Full-Games-Permission", "tntrun.fullgames"));
		PermissionsManager.setJoinPerm(plugin.getConfig().getString("Basic-Permissions.Join-Permission", "tntrun.join.<arena>"));

		debug(Level.INFO, "Basic permissions registered");
	}

	public static int getDoubleJumps(Player player) {
		for (String perm : plugin.getConfig().getStringList("Double-Jumps")) {
            if (perm.startsWith("tntrun") && player.hasPermission(perm)) {
            	return Integer.parseInt(perm.substring(perm.lastIndexOf('.') + 1));
            }
        }

		return plugin.getConfig().getInt("Default-Double-Jumps", 5);
	}
}
