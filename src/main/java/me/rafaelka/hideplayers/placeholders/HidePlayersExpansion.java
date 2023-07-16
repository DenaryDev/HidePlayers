/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.rafaelka.hideplayers.placeholders;

import me.rafaelka.hideplayers.HidePlayersPlugin;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class HidePlayersExpansion extends PlaceholderExpansion {

    private final HidePlayersPlugin plugin;

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "hideplayers";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String identifier) {
        if (plugin.isLoad() && p != null) {
            switch (identifier) {
                case "status":
                    return plugin.getHidePlayersManager().isHidden(p) ? "hidden" : "visible";
                case "ishidden":
                    return plugin.getHidePlayersManager().isHidden(p) ? "yes" : "no";
                case "isvisible":
                    return plugin.getHidePlayersManager().isHidden(p) ? "no": "yes";
            }
        }
        return null;
    }
}
