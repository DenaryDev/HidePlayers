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
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class PlayerListener implements Listener {

    private final HidePlayersPlugin plugin;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getHidePlayersManager().loadPlayer(player);

        if (plugin.getConfiguration().getBoolean("item.enabled") && plugin.getConfiguration().getBoolean("item.triggers.join")) {
            player.getInventory().setItem(plugin.getConfiguration().getInt("item.slot") - 1, plugin.getHidePlayersManager().getItem(player));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItem(plugin.getConfiguration().getInt("item.slot") - 1);
        if (item != null && new NBTItem(item).getString("itemId").equals("hide-players-item")) {
            player.getInventory().remove(item);
        }

        plugin.getHidePlayersManager().unloadPlayer(player.getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfiguration().getBoolean("item.enabled") && !plugin.getConfiguration().getBoolean("item.settings.drop-on-death") && Boolean.FALSE.equals(player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY))) {
            for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
                ItemStack item = player.getInventory().getItem(slot);
                if (item != null && !item.getType().isAir() && new NBTItem(item).getString("itemId").equals("hide-players-item")) {
                    player.getInventory().remove(item);
                    event.getDrops().remove(item);
                }
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfiguration().getBoolean("item.enabled") && plugin.getConfiguration().getBoolean("item.triggers.respawn")) {
            player.getInventory().setItem(plugin.getConfiguration().getInt("item.slot") - 1, plugin.getHidePlayersManager().getItem(player));
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfiguration().getBoolean("item.enabled") && plugin.getConfiguration().getBoolean("item.triggers.world-switch")) {
            player.getInventory().setItem(plugin.getConfiguration().getInt("item.slot") - 1, plugin.getHidePlayersManager().getItem(player));
        }
    }
}
