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
package io.sapphiremc.hideplayers.utils;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;

@UtilityClass
public class Formatter {

    private final Pattern HEX_COLORS_PATTERN = Pattern.compile("\\{#([a-fA-F0-9]{6})}");
    private final Pattern HEX_GRADIENT_PATTERN = Pattern.compile("\\{#([a-fA-F0-9]{6})(:#([a-fA-F0-9]{6}))+( )([^{}])*(})");

    /**
     * Finds simple and gradient hex patterns in string and converts it to Spigot format
     * @param text string to stylish
     * @return stylished string
     */
    public String format(String text) {
        if (text == null) {
            return null;
        }

        String mcVersion = Bukkit.getMinecraftVersion();
        if (mcVersion.startsWith("1.16") || mcVersion.startsWith("1.17") || mcVersion.startsWith("1.18") || mcVersion.startsWith("1.19")) {

            Matcher matcher = HEX_GRADIENT_PATTERN.matcher(text);

            StringBuilder stringBuilder = new StringBuilder();

            while (matcher.find()) {
                String gradient = matcher.group();

                int groups = 0;
                for (int i = 1; gradient.charAt(i) == '#'; i += 8) {
                    groups++;
                }

                Color[] colors = new Color[groups];
                for (int i = 0; i < groups; i++) {
                    colors[i] = ChatColor.of(gradient.substring((8 * i) + 1, (8 * i) + 8)).getColor();
                }

                String substring = gradient.substring((groups - 1) * 8 + 9, gradient.length() - 1);

                char[] chars = substring.toCharArray();

                StringBuilder gradientBuilder = new StringBuilder();

                int colorLength = chars.length / (colors.length - 1);
                int lastColorLength;
                if (colorLength == 0) {
                    colorLength = 1;
                    lastColorLength = 1;
                    colors = Arrays.copyOfRange(colors, 0, chars.length);
                } else {
                    lastColorLength = chars.length % (colorLength * (colors.length - 1)) + colorLength;
                }

                for (int i = 0; i < (colors.length - 1); i++) {
                    int currentColorLength = ((i == colors.length - 2) ? lastColorLength : colorLength);
                    for (int j = 0; j < currentColorLength; j++) {
                        Color color = calculateGradientColor(j + 1, currentColorLength, colors[i], colors[i + 1]);
                        ChatColor chatColor = ChatColor.of(color);

                        gradientBuilder.append(chatColor).append(chars[colorLength * i + j]);
                    }
                }

                matcher.appendReplacement(stringBuilder, gradientBuilder.toString());
            }

            matcher.appendTail(stringBuilder);
            text = stringBuilder.toString();

            matcher = HEX_COLORS_PATTERN.matcher(text);
            stringBuilder = new StringBuilder();

            while (matcher.find()) {
                String hexColorString = matcher.group();
                matcher.appendReplacement(stringBuilder, ChatColor.of(hexColorString.substring(1, hexColorString.length() - 1)).toString());
            }

            matcher.appendTail(stringBuilder);

            return ChatColor.translateAlternateColorCodes('&', stringBuilder.toString());
        } else {
            return ChatColor.translateAlternateColorCodes('&', text);
        }
    }

    private Color calculateGradientColor(int x, int parts, Color from, Color to) {
        double p = (double) (parts - x + 1) / (double) parts;

        return new Color(
                (int) (from.getRed() * p + to.getRed() * (1 - p)),
                (int) (from.getGreen() * p + to.getGreen() * (1 - p)),
                (int) (from.getBlue() * p + to.getBlue() * (1 - p))
        );
    }

    public List<String> format(List<String> l) {
        if (l == null)
            return null;

        return l.stream().map(Formatter::format).collect(Collectors.toList());
    }
}