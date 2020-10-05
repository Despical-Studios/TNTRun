package me.despical.tntrun.events.spectator.components;

import com.github.despical.inventoryframework.GuiItem;
import com.github.despical.inventoryframework.pane.StaticPane;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.events.spectator.SpectatorSettingsMenu;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 05.10.2020
 */
public class MiscComponents implements SpectatorSettingComponent {

    private SpectatorSettingsMenu spectatorSettingsMenu;

    @Override
    public void prepare(SpectatorSettingsMenu spectatorSettingsMenu) {
        this.spectatorSettingsMenu = spectatorSettingsMenu;
    }

    @Override
    public void injectComponents(StaticPane pane) {
        Main plugin = spectatorSettingsMenu.getPlugin();
        Player player = spectatorSettingsMenu.getPlayer();
        FileConfiguration config = ConfigUtils.getConfig(plugin, "messages");

        pane.addItem(new GuiItem(new ItemBuilder(Material.ENDER_PEARL)
                .name(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Disable-Night-Vision"))
                .lore(colorizeLore(plugin, config.getStringList("In-Game.Spectator.Settings-Menu.Disable-Night-Vision-Lore")))
                .build(), e -> {
            player.closeInventory();
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }),2,2);

        pane.addItem(new GuiItem(new ItemBuilder(Material.REDSTONE)
                .name(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Hide-Spectators"))
                .lore(colorizeLore(plugin, config.getStringList("In-Game.Spectator.Settings-Menu.Hide-Spectators-Lore")))
                .build(), e -> {
            Arena arena = ArenaRegistry.getArena(player);

            player.closeInventory();
            arena.getPlayers().stream().filter(p -> plugin.getUserManager().getUser(p).isSpectator()).forEach(p -> player.hidePlayer(plugin, p));
        }),3,2);
    }

    private List<String> colorizeLore(Main plugin, List<String> lore) {
        return lore.stream().map(str -> plugin.getChatManager().colorRawMessage(str)).collect(Collectors.toList());
    }
}