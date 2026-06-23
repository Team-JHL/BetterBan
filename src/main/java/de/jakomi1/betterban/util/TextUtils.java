package de.jakomi1.betterban.util;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class TextUtils {

    @SuppressWarnings({"unchecked", ""})
    public static void sendActionBar(Player player, String message) {
        try {
            Method method = player.getClass().getMethod("sendActionBar", String.class);
            method.invoke(player, message.replace("\n", " "));
            return;
        } catch (Exception ignored) {
        }

        try {
            Method spigotMethod = player.getClass().getMethod("spigot");
            Object spigot = spigotMethod.invoke(player);

            Method sendMessage = spigot.getClass().getMethod(
                    "sendMessage",
                    Class.forName("net.md_5.bungee.api.ChatMessageType"),
                    Class.forName("net.md_5.bungee.api.chat.BaseComponent[]")
            );

            Class<?> chatMessageType = Class.forName("net.md_5.bungee.api.ChatMessageType");
            Class<?> baseComponentArray = Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            Class<?> textComponent = Class.forName("net.md_5.bungee.api.chat.TextComponent");

            Object actionBarEnum = Enum.valueOf((Class<Enum>) chatMessageType, "ACTION_BAR");

            Object component = textComponent.getConstructor(String.class)
                    .newInstance(message.replace("\n", " "));

            Object array = java.lang.reflect.Array.newInstance(baseComponentArray, 1);
            java.lang.reflect.Array.set(array, 0, component);

            sendMessage.invoke(spigot, actionBarEnum, array);
            return;
        } catch (Exception ignored) {
        }

        player.sendMessage(message);
    }

    public static long parseDuration(String input) {
        if (input.endsWith("m")) {
            return Long.parseLong(input.substring(0, input.length() - 1)) * 60_000L;
        } else if (input.endsWith("h")) {
            return Long.parseLong(input.substring(0, input.length() - 1)) * 60L * 60_000L;
        } else if (input.endsWith("d")) {
            return Long.parseLong(input.substring(0, input.length() - 1)) * 24L * 60L * 60_000L;
        }

        throw new IllegalArgumentException("Invalid time format");
    }

    public static String lang(String path) {
        return ConfigUtils.getMessage(path);
    }

    public static String lang(String path, Object... replacements) {
        String message = ConfigUtils.getMessage(path);

        if (message == null) {
            return path;
        }

        if (replacements == null || replacements.length == 0) {
            return message;
        }

        for (int i = 0; i + 1 < replacements.length; i += 2) {
            String placeholder = String.valueOf(replacements[i]);
            String value = String.valueOf(replacements[i + 1]);
            message = message.replace("%" + placeholder + "%", value);
        }

        return message;
    }
}