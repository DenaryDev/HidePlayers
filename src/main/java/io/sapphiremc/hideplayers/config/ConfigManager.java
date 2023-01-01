/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package io.sapphiremc.hideplayers.config;

import io.sapphiremc.hideplayers.HidePlayersPlugin;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

@RequiredArgsConstructor
public class ConfigManager {

    private final HidePlayersPlugin plugin;

    public FileConfiguration getConfig(String fileName) {
        return getConfig(new File(plugin.getDataFolder(), fileName));
    }

    public FileConfiguration getConfig(File file) {
        String fileName = file.getName();

        FileConfiguration config = new YamlConfiguration();
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            if (fileName.equals("usercache.yml")) {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    this.plugin.getLogger().log(Level.SEVERE, "Unable to create " + fileName + " file!", ex);
                }
            } else {
                plugin.saveResource(fileName, false);
            }
        }

        try {
            config.load(file);
            return fileName.equals("config.yml") ? checkVersion(config, fileName) : config;
        } catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Cannot read configuration " + file.getPath(), ex);
        } catch (InvalidConfigurationException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Detected invalid configuration in file: " + file.getPath(), ex);
        }

        return null;
    }

    private FileConfiguration checkVersion(FileConfiguration config, String name) {
        String version = "1.0";

        String configVersion = config.getString("file-version", "0.0");
        if (!version.equals(configVersion)) {
            File file = new File(plugin.getDataFolder(), name);
            file.renameTo(new File(plugin.getDataFolder(), name.replace(".yml", "-v" + configVersion + ".yml")));

            plugin.saveResource(name, false);
            return YamlConfiguration.loadConfiguration(file);
        }
        return config;
    }

    public void saveConfig(FileConfiguration config, String name) {
        try {
            config.save(new File(plugin.getDataFolder(), name));
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Could not save config " + name, ex);
        }
    }
}
