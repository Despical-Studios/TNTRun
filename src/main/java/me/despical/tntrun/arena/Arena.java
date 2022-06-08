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

package me.despical.tntrun.arena;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.LogUtils;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.api.events.game.TRGameStartEvent;
import me.despical.tntrun.api.events.game.TRGameStateChangeEvent;
import me.despical.tntrun.arena.managers.ScoreboardManager;
import me.despical.tntrun.arena.options.ArenaOption;
import me.despical.tntrun.handlers.rewards.Reward;
import me.despical.tntrun.user.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class Arena extends BukkitRunnable {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final String id;

	private final Set<Player> players = new HashSet<>();
	private final Set<BlockState> destroyedBlocks = new HashSet<>();

	private final Map<ArenaOption, Integer> arenaOptions = new EnumMap<>(ArenaOption.class);
	private final Map<GameLocation, Location> gameLocations = new EnumMap<>(GameLocation.class);

	private ArenaState arenaState = ArenaState.INACTIVE;
	private BossBar gameBar;
	private final ScoreboardManager scoreboardManager;

	private String mapName = "";
	private boolean ready;
	private boolean forceStart = false;

	public Arena(String id) {
		this.id = id;

		for (ArenaOption option : ArenaOption.values()) {
			arenaOptions.put(option, option.getDefaultValue());
		}

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
			gameBar = Bukkit.createBossBar(plugin.getChatManager().message("Bossbar.Main-Title"), BarColor.BLUE, BarStyle.SOLID);
		}

		scoreboardManager = new ScoreboardManager(plugin, this);
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	@Override
	public void run() {
		if (players.isEmpty() && arenaState == ArenaState.WAITING_FOR_PLAYERS) {
			return;
		}

		switch (getArenaState()) {
			case WAITING_FOR_PLAYERS:
				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
					plugin.getServer().setWhitelist(false);
				}

				if (players.size() < getMinimumPlayers()) {
					if (getTimer() <= 0) {
						setTimer(45);
						broadcastMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().formatMessage(this, plugin.getChatManager().message("In-Game.Messages.Lobby-Messages.Waiting-For-Players"), getMinimumPlayers()));
						break;
					}
				} else {
					if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
						gameBar.setTitle(plugin.getChatManager().message("Bossbar.Waiting-For-Players"));
					}

					broadcastMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().message("In-Game.Messages.Lobby-Messages.Enough-Players-To-Start"));

					setArenaState(ArenaState.STARTING);
					setTimer(plugin.getConfig().getInt("Starting-Waiting-Time", 60));
					showPlayers();
				}

				setTimer(getTimer() - 1);
				break;
			case STARTING:
				if (getPlayers().size() == getMaximumPlayers() && getTimer() >= plugin.getConfig().getInt("Start-Time-On-Full-Lobby", 15) && !forceStart) {
					setTimer(plugin.getConfig().getInt("Start-Time-On-Full-Lobby", 45));
					broadcastMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().message("In-Game.Messages.Lobby-Messages.Start-In").replace("%time%", String.valueOf(getTimer())));
				}

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
					gameBar.setTitle(plugin.getChatManager().message("Bossbar.Starting-In").replace("%time%", String.valueOf(getTimer())));
					gameBar.setProgress(getTimer() / plugin.getConfig().getDouble("Starting-Waiting-Time", 60));
				}

				for (Player player : getPlayers()) {
					player.setExp((float) (getTimer() / plugin.getConfig().getDouble("Starting-Waiting-Time", 60)));
					player.setLevel(getTimer());
				}

				if (getPlayers().size() < getMinimumPlayers() && !forceStart) {
					if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
						gameBar.setTitle(plugin.getChatManager().message("Bossbar.Waiting-For-Players"));
						gameBar.setProgress(1.0);
					}

					broadcastMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().formatMessage(this, plugin.getChatManager().message("In-Game.Messages.Lobby-Messages.Waiting-For-Players"), getMinimumPlayers()));

					setArenaState(ArenaState.WAITING_FOR_PLAYERS);
					Bukkit.getPluginManager().callEvent(new TRGameStartEvent(this));
					setTimer(15);

					for (Player player : getPlayers()) {
						player.setExp(1);
						player.setLevel(0);
					}

					if (forceStart) {
						forceStart = false;
					}

					break;
				}

				if (getTimer() == 0 || forceStart) {
					TRGameStartEvent gameStartEvent = new TRGameStartEvent(this);

					Bukkit.getPluginManager().callEvent(gameStartEvent);
					setArenaState(ArenaState.IN_GAME);

					if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
						gameBar.setProgress(1.0);
					}

					setTimer(5);

					if (players.isEmpty()) {
						break;
					}

					teleportAllToStartLocation();

					for (Player player : getPlayers()) {
						ArenaUtils.updateNameTagsVisibility(player);

						player.getInventory().clear();
						player.setGameMode(GameMode.ADVENTURE);

						ArenaUtils.hidePlayersOutsideTheGame(player, this);

						plugin.getUserManager().getUser(player).addStat(StatsStorage.StatisticType.GAMES_PLAYED, 1);
						plugin.getUserManager().getUser(player).setStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS, plugin.getPermissionManager().getDoubleJumps(player));

						setTimer(2);

						player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
						player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
						player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().message("In-Game.Messages.Lobby-Messages.Game-Started"));
						player.getInventory().setItem(plugin.getItemManager().getSpecialItem("Double-Jump").getSlot(), plugin.getItemManager().getSpecialItem("Double-Jump").getItemStack());
						player.updateInventory();
					}
				}

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
					gameBar.setTitle(plugin.getChatManager().message("Bossbar.In-Game-Info"));
				}

				if (forceStart) {
					forceStart = false;
				}

				setTimer(getTimer() - 1);
				break;
			case IN_GAME:
				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
					plugin.getServer().setWhitelist(getMaximumPlayers() <= getPlayers().size());
				}

				if (getPlayersLeft().size() < 2) {
					ArenaManager.stopGame(false, this);
					return;
				}

				for (Player player : getPlayersLeft()) {
					if (getTimer() % 30 == 0) {
						plugin.getUserManager().getUser(player).addStat(StatsStorage.StatisticType.LOCAL_COINS, 15);
						plugin.getUserManager().getUser(player).addStat(StatsStorage.StatisticType.COINS, 15);
						player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().message("In-Game.Messages.Earned-Coin"));
					}

					if (plugin.getUserManager().getUser(player).getCooldown("double_jump") > 0) {
						player.setAllowFlight(false);
					} else if (plugin.getUserManager().getUser(player).getStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS) > 0)
						player.setAllowFlight(true);
				}

				getPlayersLeft().forEach(player -> plugin.getUserManager().getUser(player).addStat(StatsStorage.StatisticType.LOCAL_SURVIVE, 1));
				setTimer(getTimer() + 1);
				break;
			case ENDING:
				scoreboardManager.stopAllScoreboards();

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
					plugin.getServer().setWhitelist(false);
				}

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
					gameBar.setTitle(plugin.getChatManager().message("Bossbar.Game-Ended"));
				}

				List<Player> playersToQuit = new ArrayList<>(getPlayers());

				for (Player player : playersToQuit) {
					scoreboardManager.removeScoreboard(player);
					player.setGameMode(GameMode.SURVIVAL);

					for (Player players : Bukkit.getOnlinePlayers()) {
						player.showPlayer(plugin, players);

						if (ArenaRegistry.isInArena(players)) {
							players.showPlayer(plugin, player);
						}
					}

					player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
					player.setWalkSpeed(0.2f);
					player.setFlying(false);
					player.setAllowFlight(false);
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.setFireTicks(0);
					player.setFoodLevel(20);

					doBarAction(BarAction.REMOVE, player);
				}

				teleportAllToEndLocation();

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					for (Player player : getPlayers()) {
						InventorySerializer.loadInventory(plugin, player);
					}
				}

				broadcastMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().message("Commands.Teleported-To-The-Lobby"));

				for (User user : plugin.getUserManager().getUsers(this)) {
					user.setSpectator(false);
					user.getPlayer().setCollidable(true);
				}

				plugin.getRewardsFactory().performReward(this, Reward.RewardType.END_GAME);

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
					if (ConfigUtils.getConfig(plugin, "bungee").getBoolean("Shutdown-When-Game-Ends")) {
						plugin.getServer().shutdown();
					}
				}

				setArenaState(ArenaState.RESTARTING);
				break;
			case RESTARTING:
				getPlayers().clear();

				if (destroyedBlocks.size() > 0) {
					Iterator<BlockState> iterator = destroyedBlocks.iterator();

					while (iterator.hasNext()) {
						BlockState bs = iterator.next();
						bs.update(true);
						iterator.remove();
					}
				}

				setArenaState(ArenaState.WAITING_FOR_PLAYERS);

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
					ArenaRegistry.shuffleBungeeArena();

					for (Player player : Bukkit.getOnlinePlayers()) {
						ArenaManager.joinAttempt(player, ArenaRegistry.getBungeeArena());
					}
				}

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
					gameBar.setTitle(plugin.getChatManager().message("Bossbar.Waiting-For-Players"));
				}

				break;
			default:
				break;
		}
	}

	public void setForceStart(boolean forceStart) {
		this.forceStart = forceStart;
	}

	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	public String getId() {
		return id;
	}

	public int getMinimumPlayers() {
		return getOption(ArenaOption.MINIMUM_PLAYERS);
	}

	public void setMinimumPlayers(int minimumPlayers) {
		setOptionValue(ArenaOption.MINIMUM_PLAYERS, Math.min(2, minimumPlayers));
	}

	public String getMapName() {
		return mapName;
	}

	public void setMapName(String mapname) {
		this.mapName = mapname;
	}

	public int getTimer() {
		return getOption(ArenaOption.TIMER);
	}

	public void setTimer(int timer) {
		setOptionValue(ArenaOption.TIMER, timer);
	}

	public int getMaximumPlayers() {
		return getOption(ArenaOption.MAXIMUM_PLAYERS);
	}

	public void setMaximumPlayers(int maximumPlayers) {
		setOptionValue(ArenaOption.MAXIMUM_PLAYERS, maximumPlayers);
	}

	public ArenaState getArenaState() {
		return arenaState;
	}

	public void setArenaState(ArenaState arenaState) {
		this.arenaState = arenaState;
		TRGameStateChangeEvent gameStateChangeEvent = new TRGameStateChangeEvent(this, arenaState);
		Bukkit.getPluginManager().callEvent(gameStateChangeEvent);
	}

	public Set<Player> getPlayers() {
		return new HashSet<>(players);
	}

	public void teleportToLobby(Player player) {
		player.setFoodLevel(20);
		player.setFlySpeed(.1F);
		player.setWalkSpeed(.2F);
		player.setFlying(false);
		player.setAllowFlight(false);
		player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

		Location location = getLobbyLocation();

		if (location == null) {
			System.out.print("Lobby location isn't intialized for arena " + getId());
			return;
		}

		player.teleport(location);
	}

	public void doBarAction(BarAction action, Player player) {
		if (gameBar == null) return;

		if (action == BarAction.ADD) {
			gameBar.addPlayer(player);
		} else {
			gameBar.removePlayer(player);
		}
	}

	public Location getLobbyLocation() {
		return gameLocations.get(GameLocation.LOBBY);
	}

	public void setLobbyLocation(Location loc) {
		gameLocations.put(GameLocation.LOBBY, loc);
	}

	public Set<BlockState> getDestroyedBlocks() {
		return destroyedBlocks;
	}

	public void teleportToStartLocation(Player player) {
		player.teleport(getLobbyLocation());
	}

	private void teleportAllToStartLocation() {
		players.forEach(this::teleportToStartLocation);
	}

	public void teleportAllToEndLocation() {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED) && ConfigUtils.getConfig(plugin, "bungee").getBoolean("End-Location-Hub", true)) {
			for (Player player : getPlayers()) {
				plugin.getBungeeManager().connectToHub(player);
			}

			return;
		}

		Location location = getEndLocation();

		if (location == null) {
			location = getLobbyLocation();
			System.out.print("EndLocation for arena " + getId() + " isn't intialized!");
		}

		if (location != null) {
			for (Player player : getPlayers()) {
				player.teleport(location);
			}
		}
	}

	public void teleportToEndLocation(Player player) {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED) && ConfigUtils.getConfig(plugin, "bungee").getBoolean("End-Location-Hub", true)) {
			plugin.getBungeeManager().connectToHub(player);
			return;
		}

		Location location = getEndLocation();

		if (location == null) {
			location = getLobbyLocation();
			System.out.print("EndLocation for arena " + getId() + " isn't intialized!");
		}

		player.teleport(location);
	}

	public Location getEndLocation() {
		return gameLocations.get(GameLocation.END);
	}

	public void setEndLocation(Location endLoc) {
		gameLocations.put(GameLocation.END, endLoc);
	}

	public void start() {
		LogUtils.log("[{0}] Game instance started.", id);

		startRemovingBlock();
		runTaskTimer(plugin, 20L, 20L);
		setArenaState(ArenaState.RESTARTING);
	}

	public void addPlayer(Player player) {
		players.add(player);
	}

	public void removePlayer(Player player) {
		players.remove(player);
	}

	public List<Player> getPlayersLeft() {
		List<Player> players = new ArrayList<>();

		for (User user : plugin.getUserManager().getUsers(this)) {
			if (!user.isSpectator()) {
				players.add(user.getPlayer());
			}
		}

		return players;
	}

	public void showPlayers() {
		for (Player player : players) {
			for (Player p : players) {
				player.showPlayer(plugin, p);
				p.showPlayer(plugin, player);
			}
		}
	}

	public void broadcastMessage(String message) {
		getPlayers().forEach(player -> player.sendMessage(message));
	}

	public void startRemovingBlock() {
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			for (Player player : getPlayersLeft()) {
				if (arenaState != ArenaState.IN_GAME) {
					return;
				}

				if (getTimer() <= plugin.getConfig().getInt("Start-Block-Remove", 5)) {
					return;
				}

				for (Block block : getRemovableBlocks(player)) {
					if (!plugin.getConfig().getStringList("Whitelisted-Blocks").contains(block.getType().name())) {
						continue;
					}

					destroyedBlocks.add(block.getState());
					plugin.getServer().getScheduler().runTaskLater(plugin, () -> block.setType(Material.AIR), plugin.getConfig().getLong("Block-Remove-Delay", 12L));
				}
			}
		}, 0L, 1L);
	}

	private List<Block> getRemovableBlocks(Player player) {
		List<Block> removableBlocks = new ArrayList<>();
		Location playerLocation = player.getLocation();

		for (double ox = -0.2; ox <= 0.2; ox += 0.2) {
			for (double oz = -0.2; oz <= 0.2; oz += 0.2) {
				Block block = playerLocation.add(ox, 0, oz).getBlock().getRelative(BlockFace.DOWN);

				removableBlocks.add(block);
				removableBlocks.add(block.getRelative(BlockFace.DOWN));
				removableBlocks.add(block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN));
			}
		}

		return removableBlocks;
	}

	public int getOption(ArenaOption option) {
		return arenaOptions.get(option);
	}

	public void setOptionValue(ArenaOption option, int value) {
		arenaOptions.put(option, value);
	}

	public enum BarAction {
		ADD, REMOVE
	}

	public enum GameLocation {
		LOBBY, END
	}
}