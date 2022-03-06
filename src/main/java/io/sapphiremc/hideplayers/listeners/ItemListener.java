/*
 * Copyright (c) 2022 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
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

        if (plugin.itemAllowed(player.getWorld()) && !plugin.getConfiguration().getBoolean("item.settings.droppable", false)) {
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

        if (plugin.itemAllowed(player.getWorld()) && plugin.getConfiguration().getBoolean("item.settings.restrict-movement", true)) {
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

        if (plugin.itemAllowed(player.getWorld()) && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
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
                    player.getInventory().setItem(plugin.getConfiguration().getInt("item.slot", 9) - 1, finalItem);
                    player.setCooldown(finalItem.getType(), 20);
                }
            }
        }
    }
}
