package me.shark0822.tradeAndBalance.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();

    private static final Map<String, String> CUSTOM_COLOR_MAP = Map.ofEntries(
            Map.entry("BLACK", "&0"),
            Map.entry("DARK_BLUE", "&1"),
            Map.entry("DARK_GREEN", "&2"),
            Map.entry("DARK_AQUA", "&3"),
            Map.entry("DARK_RED", "&4"),
            Map.entry("DARK_PURPLE", "&5"),
            Map.entry("GOLD", "&6"),
            Map.entry("GRAY", "&7"),
            Map.entry("DARK_GRAY", "&8"),
            Map.entry("BLUE", "&9"),
            Map.entry("GREEN", "&a"),
            Map.entry("AQUA", "&b"),
            Map.entry("RED", "&c"),
            Map.entry("LIGHT_PURPLE", "&d"),
            Map.entry("YELLOW", "&e"),
            Map.entry("WHITE", "&f"),

            Map.entry("OBFUSCATED", "&k"),
            Map.entry("BOLD", "&l"),
            Map.entry("STRIKETHROUGH", "&m"),
            Map.entry("UNDERLINE", "&n"),
            Map.entry("ITALIC", "&o"),
            Map.entry("RESET", "&r")
    );

    public static Component format(String message) {
        if (message == null) return Component.empty();

        String processed = replaceCustomTags(message);
        processed = ChatColor.translateAlternateColorCodes('&', processed);
        processed = convertHexToSectionFormat(processed);

        return LegacyComponentSerializer.legacySection().deserialize(processed).decoration(TextDecoration.ITALIC, false);
    }

    public static String colorize(String string) {
        if (string == null) return null;

        String processed = replaceCustomTags(string);
        processed = ChatColor.translateAlternateColorCodes('&', processed);
        processed = convertHexToSectionFormat(processed);

        return processed;
    }

    private static String replaceCustomTags(String message) {
        for (Map.Entry<String, String> entry : CUSTOM_COLOR_MAP.entrySet()) {
            String key = "&" + entry.getKey();
            String value = entry.getValue();
            message = message.replace(key, value);
        }
        return message;
    }

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private static String convertHexToSectionFormat(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static String componentToLegacy(Component component) {
        return legacySerializer.serialize(component);
    }
}
