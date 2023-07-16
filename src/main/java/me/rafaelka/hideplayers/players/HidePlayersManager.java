/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.rafaelka.hideplayers.players;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.rafaelka.hideplayers.HidePlayersPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@RequiredArgsConstructor
public class HidePlayersManager {

    private final HidePlayersPlugin plugin;
    private final Map<UUID, Boolean> players = new HashMap<>();
    private FileConfiguration usercache;

    public void reload(FileConfiguration usercache) {
        this.usercache = usercache;
        disable();

        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayer(player);
        }
    }

    public void disable() {
        if (players.size() > 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                unloadPlayer(player.getUniqueId());
            }
        }
    }

    public void loadPlayer(final Player player) {
        final UUID uuid = player.getUniqueId();
        boolean hidden = false;

        if (usercache.contains("users." + uuid)) {
            hidden = usercache.getBoolean("users." + uuid + ".hidden");
        } else {
            usercache.createSection("users." + uuid);
            usercache.set("users." + uuid + ".hidden", false);
            saveUserCache();
        }

        players.put(uuid, hidden);
        if (hidden) hideAll(player);
    }

    public void unloadPlayer(final UUID uuid) {
        savePlayer(uuid);
        players.remove(uuid);
    }

    private void savePlayer(final UUID uuid) {
        usercache.set("users." + uuid + ".hidden", players.get(uuid));
        saveUserCache();
    }

    public boolean isHidden(final Player player) {
        return players.get(player.getUniqueId());
    }

    public void showAll(final Player player) {
        players.remove(player.getUniqueId());
        for (final Player p : Bukkit.getOnlinePlayers()) {
            player.showPlayer(plugin, p);
        }
        players.put(player.getUniqueId(), false);
        savePlayer(player.getUniqueId());

        if (plugin.itemAllowed(player.getWorld())) {
            player.getInventory().setItem(plugin.getConfiguration().getInt("item.slot", 9) - 1, getItem(player));
        }
    }

    public void hideAll(final Player player) {
        players.remove(player.getUniqueId());
        for (final Player p : Bukkit.getOnlinePlayers()) {
            player.hidePlayer(plugin, p);
        }
        players.put(player.getUniqueId(), true);
        savePlayer(player.getUniqueId());

        if (plugin.itemAllowed(player.getWorld())) {
            player.getInventory().setItem(plugin.getConfiguration().getInt("item.slot", 9) - 1, getItem(player));
        }
    }

    private void saveUserCache() {
        plugin.getConfigManager().saveConfig(usercache, "usercache.yml");
    }

    @SuppressWarnings("deprecation")
    public ItemStack getItem(Player player) {
        boolean hidden = isHidden(player);

        Material material;
        if (hidden) {
            material = Material.matchMaterial(plugin.getConfiguration().getString("item.hidden-material", "GRAY_DYE"));
        } else {
            material = Material.matchMaterial(plugin.getConfiguration().getString("item.visible-material", "LIME_DYE"));
        }
        if (material == null) {
            plugin.logError(plugin.getConfiguration().getString("item.visible-material") + " is invalid material for toggle item!");
            return null;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getLanguageManager().getTranslation(player, "item." + (hidden ? "hidden" : "visible") + ".displayname"));
        meta.setLore(plugin.getLanguageManager().getTranslationList(player, "item." + (hidden ? "hidden" : "visible") + ".lore"));
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("itemId", "hide-players-item");
        nbtItem.setString("status", hidden ? "hidden" : "visible");

        return nbtItem.getItem();
    }
}
