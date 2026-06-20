package de.jakomi1.betterBan;

import de.jakomi1.betterBan.commands.*;
import de.jakomi1.betterBan.database.Database;
import de.jakomi1.betterBan.listener.ChatListener;
import de.jakomi1.betterBan.listener.JoinListener;
import de.jakomi1.betterBan.utils.ConfigUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.Objects;

import static de.jakomi1.betterBan.utils.ConfigUtils.loadConfig;

public final class BetterBan extends JavaPlugin {

    public static String chatPrefix ;
    public static Plugin plugin;
    public static File dataFolder;
    @Override
    public void onEnable() {
        plugin = this;
        dataFolder = getDataFolder();
        new Metrics(this, 32083);

        loadConfig();
        if (!dataFolder.exists()) {
            if (dataFolder.mkdirs()) {
                getLogger().info("Created plugin folder: " + dataFolder.getPath());
            } else {
                getLogger().warning("Couldn't create plugin folder: " + dataFolder.getPath());
            }
        }

        if (getServer().getPluginManager().isPluginEnabled("voicechat")) {
            try {
                Class<?> serviceClass =
                        Class.forName("de.maxhenkel.voicechat.api.BukkitVoicechatService");

                Object service = getServer()
                        .getServicesManager()
                        .load((Class) serviceClass);

                if (service != null) {
                    Class<?> integrationClass =
                            Class.forName("de.jakomi1.betterBan.VoiceChatIntegration");

                    Object integration =
                            integrationClass.getDeclaredConstructor().newInstance();

                    serviceClass
                            .getMethod("registerPlugin",
                                    Class.forName("de.maxhenkel.voicechat.api.VoicechatPlugin"))
                            .invoke(service, integration);

                    getLogger().info("SimpleVoiceChat is installed and loaded");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Database.init();
        registerCommands();
        chatPrefix = ConfigUtils.getPrefixStyled();
        registerListeners();
    }

    private void registerCommands() {
        registerCommand("ban", new BanCommand(), new BanCommand());
        registerCommand("banlist", new BanListCommand(), new EmptyTabCompleter());
        registerCommand("unban", new UnbanCommand(), new UnbanCommand());
        registerCommand("tempban", new TempBanCommand(), new TempBanCommand());
        registerCommand("kick", new KickCommand(), new KickCommand());
        registerCommand("chatban", new ChatBanCommand(), new ChatBanCommand());
        registerCommand("chatbanlist", new ChatBanListCommand(), new EmptyTabCompleter());
        registerCommand("chatunban", new ChatUnbanCommand(), new ChatUnbanCommand());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
    }

    private void registerCommand(String command, CommandExecutor executor, TabCompleter tabCompleter) {
        Objects.requireNonNull(getServer().getPluginCommand(command)).setExecutor(executor);
        Objects.requireNonNull(getServer().getPluginCommand(command)).setTabCompleter(tabCompleter);
    }


    @Override
    public void onDisable() {

    }
    public static boolean isAdmin(Player player) {
        return player.isOp();
    }


}
