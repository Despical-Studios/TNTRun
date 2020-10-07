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

package me.despical.tntrun.commands.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.despical.tntrun.commands.SubCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class HelpCommand extends SubCommand {
	
	public HelpCommand() {
		super("help");
		setPermission("tntrun.admin");
	}

	@Override
	public String getPossibleArguments() {
		return null;
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		sender.sendMessage("");
		sender.sendMessage(getPlugin().getChatManager().colorRawMessage("&3&l---- TNT Run Admin Commands ----"));
		sender.sendMessage("");

		getPlugin().getCommandHandler().getSubCommands().stream().filter(subCommand -> subCommand.getType() == CommandType.GENERIC).forEach(subCommand -> {
			String usage = "/" + label + " " + subCommand.getName() + (subCommand.getPossibleArguments().length() > 0 ? " " + subCommand.getPossibleArguments() : "");

			if (sender instanceof Player) {
				List<String> help = new ArrayList<>();

				help.add(ChatColor.DARK_AQUA + usage);

				for (String tutLine : subCommand.getTutorial()) {
					help.add(ChatColor.AQUA + tutLine);
				}

				((Player) sender).spigot().sendMessage(new ComponentBuilder(usage)
						.color(ChatColor.AQUA)
						.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(String.join("\n", help))))
						.create());
			} else {
				sender.sendMessage(ChatColor.AQUA + usage);
			}
		});

		if (sender instanceof Player) {
			sendHoverTip((Player) sender);
		}
	}
	
	public static void sendHoverTip(Player player) {
		player.sendMessage("");

		player.spigot().sendMessage(new ComponentBuilder("TIP:").color(ChatColor.YELLOW).bold(true)
			.append(" Try to ", FormatRetention.NONE).color(ChatColor.GRAY)
			.append("hover").color(ChatColor.WHITE).underlined(true)
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.LIGHT_PURPLE + "Hover on the commands to get info about them.")))
			.append(" or ", FormatRetention.NONE).color(ChatColor.GRAY)
			.append("click").color(ChatColor.WHITE).underlined(true)
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.LIGHT_PURPLE + "Click on the commands to insert them in the chat.")))
			.append(" on the commands!", FormatRetention.NONE).color(ChatColor.GRAY)
			.create());
	}

	@Override
	public List<String> getTutorial() {
		return null;
	}

	@Override
	public CommandType getType() {
		return CommandType.HIDDEN;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.BOTH;
	}
}
