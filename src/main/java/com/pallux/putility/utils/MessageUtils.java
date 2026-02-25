package com.pallux.putility.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MessageUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final boolean PAPI_ENABLED;

    static {
        PAPI_ENABLED = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    private MessageUtils() {}

    /**
     * Parse a raw string (legacy color codes, &#RRGGBB hex, PlaceholderAPI) into a Component.
     */
    public static Component parse(String raw) {
        return parse(raw, null);
    }

    public static Component parse(String raw, Player player) {
        if (raw == null) return Component.empty();
        String processed = raw;

        // Apply PlaceholderAPI
        if (PAPI_ENABLED && player != null) {
            processed = PlaceholderAPI.setPlaceholders(player, processed);
        }

        // Convert &#RRGGBB → §x§R§R§G§G§B§B
        processed = translateHex(processed);

        // Convert legacy &x codes
        return LegacyComponentSerializer.legacyAmpersand().deserialize(processed);
    }

    public static Component parse(String raw, Player player, Map<String, String> placeholders) {
        if (raw == null) return Component.empty();
        String processed = raw;
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                processed = processed.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return parse(processed, player);
    }

    public static List<Component> parseList(List<String> lines, Player player, Map<String, String> placeholders) {
        return lines.stream()
                .map(line -> parse(line, player, placeholders))
                .collect(Collectors.toList());
    }

    public static String translateHex(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /** Strip formatting for display name comparisons */
    public static String stripColor(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text).toString();
    }
}
