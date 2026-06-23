package de.jakomi1.betterban.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static de.jakomi1.betterban.BetterBan.dataFolder;

public final class ConfigUtils {

    private static final Pattern LEGACY = Pattern.compile("(?i)(?:§|&)[0-9A-FK-OR]");

    private static final Map<String, Object> configCache = new HashMap<>();
    private static final Map<String, Object> langCache = new HashMap<>();
    private static final Map<String, FileConfiguration> langConfigs = new HashMap<>();

    private static File configFile;
    private static FileConfiguration config;

    private static String loadedLang;

    private ConfigUtils() {}

    public static void loadConfig() {
        require();

        configFile = new File(dataFolder, "config.yml");
        copy("config.yml", configFile);

        config = YamlConfiguration.loadConfiguration(configFile);

        YamlConfiguration def = loadResource("config.yml");
        if (merge(config, def)) saveConfig();

        configCache.clear();
        cache(config, configCache);

        syncLangFiles();
        loadLanguage();
    }

    public static void reloadConfig() {
        config = null;
        configFile = null;
        configCache.clear();
        langCache.clear();
        langConfigs.clear();
        loadConfig();
    }

    public static void syncLangFiles() {
        File dir = new File(dataFolder, "lang");
        if (!dir.exists()) dir.mkdirs();

        copy("lang/en_us.yml", new File(dir, "en_us.yml"));
        copy("lang/de_de.yml", new File(dir, "de_de.yml"));

        langConfigs.clear();
        langCache.clear();
        loadedLang = null;
    }

    public static Object getConfig(String path) {
        if (path == null || path.isBlank()) return null;
        if (config == null) loadConfig();

        Object v = configCache.get(path);
        return v != null ? v : config.get(path);
    }

    public static String getString(String path) {
        Object v = getConfig(path);
        return v == null ? null : String.valueOf(v);
    }

    public static boolean getBoolean(String path) {
        Object v = getConfig(path);
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return Boolean.parseBoolean(s);
        return false;
    }

    public static void setConfig(String path, Object value) {
        if (config == null) loadConfig();

        if (value == null) configCache.remove(path);
        else configCache.put(path, value);

        config.set(path, value);
        saveConfig();
    }

    public static String getPrefixStyled() {
        String p = getString("prefix");
        if (p == null || p.isEmpty()) return "";
        return ChatColor.translateAlternateColorCodes('&', p.replace('§', '&')) + " ";
    }

    public static String getLanguage() {
        String l = getString("language");
        return (l == null || l.isBlank()) ? "en_us" : l.toLowerCase();
    }

    public static void loadLanguage() {
        String lang = getLanguage();
        if (lang.equalsIgnoreCase(loadedLang) && !langCache.isEmpty()) return;

        File file = ensureLang(lang);
        FileConfiguration cfg = loadLang(file, lang);

        langCache.clear();
        cache(cfg, langCache);

        loadedLang = lang;
    }

    public static String getMessage(String path) {
        if (path == null || path.isBlank()) return "";
        if (langCache.isEmpty()) loadLanguage();

        Object v = langCache.get(path);
        return v == null ? path : ChatColor.translateAlternateColorCodes('&', String.valueOf(v));
    }
    public static String getWebhookLanguageResolved() {
        String l = getWebhookLanguage();
        if ("default".equalsIgnoreCase(l)) return getLanguage();
        return l;
    }

    public static String getWebhookLanguage() {
        String l = getString("webhook-language");
        return (l == null || l.isBlank()) ? "default" : l.toLowerCase();
    }

    public static String getWebhookMessage(String path, Object... repl) {
        if (path == null || path.isBlank()) return "";

        String lang = getWebhookLanguageResolved();
        String msg = getLangValue(lang, path);

        if (msg == null) msg = path;

        return ChatColor.translateAlternateColorCodes('&', apply(msg, repl));
    }

    public static boolean isWebhookEnabled() {
        return getBoolean("enable-webhook");
    }

    public static String getWebhook() {
        String w = getString("webhook-url");
        return w == null ? "" : w;
    }

    private static String getLangValue(String lang, String path) {
        FileConfiguration cfg = getLangConfig(lang);
        String v = cfg.getString(path);

        if (v != null) return v;

        if (!"en_us".equalsIgnoreCase(lang)) {
            return getLangConfig("en_us").getString(path);
        }
        return null;
    }

    private static FileConfiguration getLangConfig(String lang) {
        lang = lang.toLowerCase();

        if (langConfigs.containsKey(lang)) return langConfigs.get(lang);

        File file = ensureLang(lang);
        FileConfiguration cfg = loadLang(file, lang);

        langConfigs.put(lang, cfg);
        return cfg;
    }

    private static File ensureLang(String lang) {
        File dir = new File(dataFolder, "lang");
        dir.mkdirs();

        File f = new File(dir, lang + ".yml");
        copy("lang/" + lang + ".yml", f);
        return f;
    }

    private static FileConfiguration loadLang(File file, String lang) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration def = loadResource("lang/" + lang + ".yml");

        if (def == null) def = loadResource("lang/en_us.yml");

        if (merge(cfg, def)) save(cfg, file);

        return cfg;
    }

    private static YamlConfiguration loadResource(String path) {
        try (InputStream in = ConfigUtils.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) return null;
            return YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            Bukkit.getLogger().warning("Missing resource: " + path);
            return null;
        }
    }

    private static void copy(String res, File target) {
        try {
            if (!target.getParentFile().exists()) target.getParentFile().mkdirs();

            if (target.exists()) return;

            try (InputStream in = ConfigUtils.class.getClassLoader().getResourceAsStream(res)) {
                if (in == null) {
                    Bukkit.getLogger().warning("Missing: " + res);
                    return;
                }
                Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveConfig() {
        try {
            if (config != null) config.save(configFile);
        } catch (IOException ignored) {}
    }

    private static void save(FileConfiguration cfg, File f) {
        try {
            cfg.save(f);
        } catch (IOException ignored) {}
    }

    private static boolean merge(ConfigurationSection t, ConfigurationSection d) {
        if (t == null || d == null) return false;

        boolean changed = false;

        for (String k : d.getKeys(false)) {
            if (d.isConfigurationSection(k)) {
                ConfigurationSection s = t.getConfigurationSection(k);
                if (s == null) s = t.createSection(k);
                changed |= merge(s, d.getConfigurationSection(k));
            } else if (!t.contains(k)) {
                t.set(k, d.get(k));
                changed = true;
            }
        }
        return changed;
    }

    private static void cache(FileConfiguration src, Map<String, Object> map) {
        for (String k : src.getKeys(true)) {
            if (!src.isConfigurationSection(k)) map.put(k, src.get(k));
        }
    }

    private static String apply(String msg, Object... r) {
        if (r == null) return msg;

        for (int i = 0; i + 1 < r.length; i += 2) {
            msg = msg.replace("%" + r[i] + "%", String.valueOf(r[i + 1]));
        }
        return msg;
    }

    private static void require() {
        if (dataFolder == null) throw new IllegalStateException("dataFolder null");
    }
}