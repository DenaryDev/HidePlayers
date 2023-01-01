/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package io.sapphiremc.hideplayers;

import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import io.sapphiremc.hideplayers.command.HidePlayersCommand;
import io.sapphiremc.hideplayers.config.ConfigManager;
import io.sapphiremc.hideplayers.lang.LanguageManager;
import io.sapphiremc.hideplayers.listeners.ItemListener;
import io.sapphiremc.hideplayers.listeners.PlayerListener;
import io.sapphiremc.hideplayers.placeholders.HidePlayersExpansion;
import io.sapphiremc.hideplayers.players.HidePlayersManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class HidePlayersPlugin extends JavaPlugin {

    @Getter
    private static HidePlayersPlugin instance;
    @Getter
    private ConfigManager configManager;
    @Getter
    private LanguageManager languageManager;
    @Getter
    private HidePlayersManager hidePlayersManager;

    @Getter
    private FileConfiguration configuration;
    @Getter
    private FileConfiguration usercache;

    @Getter
    private boolean load = false;

    private HidePlayersExpansion expansion;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs())
            return;

        // Item-NBT-API settings
        MinecraftVersion.disableUpdateCheck();
        MinecraftVersion.disableBStats();
        MinecraftVersion.replaceLogger(getLogger());

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.expansion = new HidePlayersExpansion(this);
        }

        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        this.hidePlayersManager = new HidePlayersManager(this);

        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ItemListener(this), this);

        PluginCommand command = this.getCommand("visibility");
        if (command != null) {
            command.setExecutor(new HidePlayersCommand(this));
        } else {
            logError("Command 'visibility' not found in plugin.yml!");
        }

        if (MinecraftVersion.getVersion().equals(MinecraftVersion.UNKNOWN)) {
            logError("This plugin contains Item-NBT-API, but it's incompatible with this version of minecraft, disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        onReload();
    }

    public void onReload() {
        load = false;
        if (expansion != null && expansion.isRegistered())
            expansion.unregister();

        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack item = player.getInventory().getItem(configuration.getInt("item.slot", 9) - 1);
            if (item != null && new NBTItem(item).getString("itemId").equals("hide-players-item")) {
                player.getInventory().remove(item);
            }
        }

        configuration = configManager.getConfig("config.yml");
        usercache = configManager.getConfig("usercache.yml");
        languageManager.reload(configuration);
        hidePlayersManager.reload(usercache);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (itemAllowed(player.getWorld())) {
                player.getInventory().setItem(configuration.getInt("item.slot", 9) - 1, hidePlayersManager.getItem(player));
            }
        }

        if (expansion != null)
            expansion.register();
        load = true;
    }

    @Override
    public void onDisable() {
        load = false;
        if (expansion != null && expansion.isRegistered())
            expansion.unregister();
        if (hidePlayersManager != null)
            hidePlayersManager.disable();
    }

    public boolean itemAllowed(World world) {
        if (configuration.getBoolean("item.enable", true)) {
            return !configuration.getStringList("item.disabled-worlds").contains(world.getName());
        }
        return false;
    }

    public void log(String s) {
        getSLF4JLogger().info(s);
    }

    public void logDebug(String s) {
        if (configuration.getBoolean("debug", false)) log("DEBUG: " + s);
    }

    public void logWarning(String s) {
        getSLF4JLogger().warn(s);
    }

    public void logError(String s) {
        getSLF4JLogger().error(s);
    }
}
