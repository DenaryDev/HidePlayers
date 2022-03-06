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

        if (plugin.itemAllowed(player.getWorld()) && plugin.getConfiguration().getBoolean("item.triggers.join", true)) {
            player.getInventory().setItem(plugin.getConfiguration().getInt("item.slot", 9) - 1, plugin.getHidePlayersManager().getItem(player));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItem(plugin.getConfiguration().getInt("item.slot", 9) - 1);
        if (item != null && new NBTItem(item).getString("itemId").equals("hide-players-item")) {
            player.getInventory().remove(item);
        }

        plugin.getHidePlayersManager().unloadPlayer(player.getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        if (plugin.itemAllowed(player.getWorld()) && !plugin.getConfiguration().getBoolean("item.settings.drop-on-death", false) && Boolean.FALSE.equals(player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY))) {
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

        if (plugin.itemAllowed(player.getWorld()) && plugin.getConfiguration().getBoolean("item.triggers.respawn", true)) {
            player.getInventory().setItem(plugin.getConfiguration().getInt("item.slot", 9) - 1, plugin.getHidePlayersManager().getItem(player));
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (plugin.itemAllowed(player.getWorld()) && plugin.getConfiguration().getBoolean("item.triggers.world-switch", false)) {
            player.getInventory().setItem(plugin.getConfiguration().getInt("item.slot", 9) - 1, plugin.getHidePlayersManager().getItem(player));
        }

        if (!plugin.itemAllowed(player.getWorld())) {
            ItemStack item = player.getInventory().getItem(plugin.getConfiguration().getInt("item.slot", 9) - 1);
            if (item != null && new NBTItem(item).getString("itemId").equals("hide-players-item")) {
                player.getInventory().remove(item);
            }
        }
    }
}
