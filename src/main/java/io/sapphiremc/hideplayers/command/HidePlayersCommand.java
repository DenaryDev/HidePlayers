/*
 * Copyright (c) 2022 DenaryDev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package io.sapphiremc.hideplayers.command;

import io.sapphiremc.hideplayers.HidePlayersPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class HidePlayersCommand implements CommandExecutor, TabCompleter {

    private final HidePlayersPlugin plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            usage(sender, label, "help");
            return true;
        }

        Player p = sender instanceof Player ? (Player) sender : null;

        switch (args[0]) {
            case "reload" -> {
                if (args.length == 1) {
                    if (sender.hasPermission("hideplayers.command.reload")) {
                        plugin.onReload();
                        sender.sendMessage(plugin.getLanguageManager().getTranslation(p, "command.reload.success"));
                    } else {
                        sender.sendMessage(plugin.getLanguageManager().getTranslation(p, "error.no-permission"));
                    }
                } else {
                    usage(sender, label, "reload");
                }
            }
            case "toggle" -> {
                if (sender instanceof Player) {
                    if (args.length == 1) {
                        if (sender.hasPermission("hideplayers.command.toggle")) {
                            if (plugin.getHidePlayersManager().isHidden(p)) {
                                plugin.getHidePlayersManager().showAll(p);
                                sender.sendMessage(plugin.getLanguageManager().getTranslation(p, "command.toggle.success")
                                        .replace("%action%", plugin.getLanguageManager().getTranslation(p, "command.toggle.action.visible")));
                            } else {
                                plugin.getHidePlayersManager().hideAll(p);
                                sender.sendMessage(plugin.getLanguageManager().getTranslation(p, "command.toggle.success")
                                        .replace("%action%", plugin.getLanguageManager().getTranslation(p, "command.toggle.action.hidden")));
                            }
                        } else {
                            sender.sendMessage(plugin.getLanguageManager().getTranslation(p, "error.no-permission"));
                        }
                    } else {
                        usage(sender, label, "toggle");
                    }
                } else {
                    sender.sendMessage(plugin.getLanguageManager().getTranslation(p, "error.players-only"));
                }
            }
            case "help" -> help(sender, label);
            default -> usage(sender, label, "help");
        }

        return true;
    }

    private void usage(CommandSender sender, String label, String s) {
        Player p = sender instanceof Player ? (Player) sender : null;
        sender.sendMessage(plugin.getLanguageManager().getTranslation(p, "command.generic.usage")
                .replace("%usage%", plugin.getLanguageManager().getTranslation(p, "command." + s + ".usage").replace("%label%", label)));
    }

    private void help(CommandSender sender, String label) {
        Player p = sender instanceof Player ? (Player) sender : null;
        List<String> helpLines = new ArrayList<>();
        helpLines.add(plugin.getLanguageManager().getTranslation(p, "command.help.header"));

        if (sender.hasPermission("sapphirehideplayers.command.reload")) {
            helpLines.add(plugin.getLanguageManager().getTranslation(p, "command.help.command")
                    .replace("%usage%", plugin.getLanguageManager().getTranslation(p, "command.reload.usage").replace("%label%", label))
                    .replace("%info%", plugin.getLanguageManager().getTranslation(p, "command.reload.info")));
        }

        if (sender instanceof Player && sender.hasPermission("hideplayers.command.toggle")) {
            helpLines.add(plugin.getLanguageManager().getTranslation(p, "command.help.command")
                    .replace("%usage%", plugin.getLanguageManager().getTranslation(p, "command.toggle.usage").replace("%label%", "prefix"))
                    .replace("%info%", plugin.getLanguageManager().getTranslation(p, "command.toggle.info")));
        }

        helpLines.add(" ");

        sender.sendMessage(helpLines.toArray(new String[0]));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equals("visibility")) {
            List<String> aliases = new ArrayList<>();

            if (args.length == 1) {
                if (args[0].isEmpty()) {
                    if (sender instanceof Player && sender.hasPermission("hideplayers.command.toggle")) aliases.add("toggle");
                    if (sender.hasPermission("hideplayers.command.reload")) aliases.add("reload");
                } else {
                    if ("toggle".contains(args[0].toLowerCase()) && (sender instanceof Player && sender.hasPermission("hideplayers.command.toggle"))) aliases.add("toggle");
                    if ("reload".contains(args[0].toLowerCase()) && sender.hasPermission("hideplayers.command.reload")) aliases.add("reload");
                }
            } else if (args.length > 1) {
                aliases = Collections.emptyList();
            }

            return aliases;
        }

        return null;
    }
}
