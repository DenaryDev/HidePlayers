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
package io.sapphiremc.hideplayers.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.sapphiremc.hideplayers.HidePlayersPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class ItemListener implements Listener {

    private final HidePlayersPlugin plugin;

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfiguration().getBoolean("item.enabled") && !plugin.getConfiguration().getBoolean("item.settings.droppable")) {
            ItemStack item = event.getItemDrop().getItemStack();
            if (new NBTItem(item).getString("itemId").equals("hide-players-item")) {
                event.setCancelled(true);
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (plugin.getConfiguration().getBoolean("item.enabled") && plugin.getConfiguration().getBoolean("item.settings.restrict-movement")) {
            ItemStack item = event.getCurrentItem();
            if (item != null && !item.getType().isAir() && new NBTItem(item).getString("itemId").equals("hide-players-item")) {
                event.setCancelled(true);
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onToggle(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfiguration().getBoolean("item.enabled") && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            ItemStack item = event.getItem();
            if (item != null) {
                NBTItem nbtItem = new NBTItem(item);
                if (player.getCooldown(item.getType()) == 0 && !item.getType().isAir() && nbtItem.getString("itemId").equals("hide-players-item")) {
                    String status = nbtItem.getString("status");
                    if (status.equals("visible")) {
                        plugin.getHidePlayersManager().hideAll(player);
                        player.sendMessage(plugin.getLanguageManager().getTranslation(player, "command.toggle.success")
                                .replace("%action%", plugin.getLanguageManager().getTranslation(player, "command.toggle.action.hidden")));
                    } else if (status.equals("hidden")) {
                        plugin.getHidePlayersManager().showAll(player);
                        player.sendMessage(plugin.getLanguageManager().getTranslation(player, "command.toggle.success")
                                .replace("%action%", plugin.getLanguageManager().getTranslation(player, "command.toggle.action.visible")));
                    }
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setUseInteractedBlock(Event.Result.DENY);

                    ItemStack finalItem = plugin.getHidePlayersManager().getItem(player);
                    player.getInventory().setItem(plugin.getConfiguration().getInt("item.slot") - 1, finalItem);
                    player.setCooldown(finalItem.getType(), 20);
                }
            }
        }
    }
}
