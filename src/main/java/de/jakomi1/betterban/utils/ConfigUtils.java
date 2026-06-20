package de.jakomi1.betterban.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static de.jakomi1.betterban.BetterBan.dataFolder;

public class ConfigUtils {
    private static File configFile;
    private static FileConfiguration config;

    private static final Map<String, Object> cache = new HashMap<>();

    private static final String DEFAULT_PREFIX = "§7[§4BB§7]";

    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("(?i)(?:§|&)[0-9A-FK-OR]");

    public static void loadConfig() {
        configFile = new File(dataFolder, "config.yml");

        if (!configFile.exists()) {
            try {
                dataFolder.mkdirs();
                configFile.createNewFile();

                config = YamlConfiguration.loadConfiguration(configFile);

                config.set("webhook-url", "");
                config.set("enable-webhook", false);

                config.set("prefix", DEFAULT_PREFIX);

                config.save(configFile);

                Bukkit.getLogger().info("Created new config.yml with default keys");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);

            if (!config.contains("webhook-url")) config.set("webhook-url", "");
            if (!config.contains("enable-webhook")) config.set("enable-webhook", false);
            if (!config.contains("prefix")) config.set("prefix", DEFAULT_PREFIX);

            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cache.put("webhook-url", config.getString("webhook-url", ""));
        cache.put("enable-webhook", config.getBoolean("enable-webhook", false));
        cache.put("prefix", config.getString("prefix", DEFAULT_PREFIX));
    }

    public static String getWebhook() {
        if (!cache.containsKey("webhook-url")) loadConfig();
        return (String) cache.getOrDefault("webhook-url", "");
    }

    public static boolean isWebhookEnabled() {
        if (!cache.containsKey("enable-webhook")) loadConfig();
        return (boolean) cache.getOrDefault("enable-webhook", false);
    }

    public static void setWebhook(String url) {
        if (config == null) loadConfig();
        cache.put("webhook-url", url);
        config.set("webhook-url", url);
        saveConfig();
    }

    public static void setEnableWebhook(boolean enabled) {
        if (config == null) loadConfig();
        cache.put("enable-webhook", enabled);
        config.set("enable-webhook", enabled);
        saveConfig();
    }

    public static String getPrefixRawFromConfig() {
        if (!cache.containsKey("prefix")) loadConfig();
        return (String) cache.getOrDefault("prefix", DEFAULT_PREFIX);
    }


    public static String getPrefixStyled() {
        String raw = getPrefixRawFromConfig();
        if (raw == null) raw = DEFAULT_PREFIX;

        // Normalisiere: ersetze vorhandene § durch & als "alternate code", dann übersetze
        String asAmp = raw.replace('§', '&');
        return ChatColor.translateAlternateColorCodes('&', asAmp) + " ";
    }


    public static String getPrefixRaw() {
        String raw = getPrefixRawFromConfig();
        if (raw == null) raw = DEFAULT_PREFIX;

        String cleaned = LEGACY_COLOR_PATTERN.matcher(raw).replaceAll("");

        cleaned = cleaned.replace("§", "").replace("&", "");

        return cleaned;
    }

    public static void setPrefix(String prefix) {
        if (prefix == null) throw new IllegalArgumentException("prefix must not be null");
        if (config == null) loadConfig();
        cache.put("prefix", prefix);
        config.set("prefix", prefix);
        saveConfig();
    }

    private static void saveConfig() {
        try {
            if (config != null) config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
