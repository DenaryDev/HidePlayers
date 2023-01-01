/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package io.sapphiremc.hideplayers.lang;

import io.sapphiremc.hideplayers.HidePlayersPlugin;
import io.sapphiremc.hideplayers.utils.Formatter;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class LanguageManager {

    private final HidePlayersPlugin plugin;
    private final Map<String, FileConfiguration> languages = new HashMap<>();

    private String defaultLang;
    private boolean usePlayerLang;

    @SuppressWarnings("all")
    public void reload(@NotNull FileConfiguration config) {
        if (this.languages.size() > 0) languages.clear();

        usePlayerLang = config.getBoolean("lang.use-player-lang", true);
        if (!config.contains("lang.default")) {
            plugin.logWarning("Default language not specified in plugin config, using en_us");
            defaultLang = "en_us";
        } else {
            defaultLang = config.getString("lang.default");
        }

        plugin.logDebug("Loading language files...");
        File langDir = new File(plugin.getDataFolder() + File.separator + "lang");
        if (!langDir.exists()) langDir.mkdirs();

        List<String> names = List.of(langDir.list());

        if (!names.contains("en_us.yml")) {
            plugin.saveResource(new File("lang/en_us.yml").getPath(), false);
        }
        if (!names.contains("ru_ru.yml")) {
            plugin.saveResource(new File("lang/ru_ru.yml").getPath(), false);
        }

        plugin.logDebug("Found " + names.size() + " language files in " + langDir);

        for (File file : langDir.listFiles()) {
            if (!file.getName().endsWith(".yml")) {
                plugin.logDebug("Skipping file " + file.getName());
                continue;
            }

            languages.put(file.getName().replace(".yml", ""), plugin.getConfigManager().getConfig(file));
        }

        if (!languages.keySet().contains(defaultLang)) {
            plugin.logWarning("The language file " + defaultLang + ".yml does not exist in " + langDir.getPath() + " folder, using file en_us.yml");
        }

        plugin.log("Loaded " + languages.size() + " languages.");
    }

    @NotNull
    public String getTranslation(@Nullable Player player, @NotNull String key) {
        String ret = getLangFile(player).getString(key);
        if (ret == null)
            return "<missing key: " + key + ">";

        return Formatter.format(ret);
    }

    @NotNull
    public List<String> getTranslationList(@Nullable Player player, @NotNull String key) {
        return Formatter.format(getLangFile(player).getStringList(key));
    }

    @NotNull
    private FileConfiguration getLangFile(@Nullable Player player) {
        String langKey;
        if (player != null && usePlayerLang) {
            @SuppressWarnings("deprecation")
            String playerLang = player.getLocale();
            if (languages.containsKey(playerLang)) {
                langKey = playerLang;
            } else {
                plugin.logDebug("Cannot find language " + playerLang + " for player " + player.getName());
                langKey = defaultLang;
            }
        } else {
            langKey = defaultLang;
        }

        return languages.get(langKey);
    }
}
