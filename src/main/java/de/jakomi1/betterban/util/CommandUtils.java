package de.jakomi1.betterban.util;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static de.jakomi1.betterban.BetterBan.plugin;

public class CommandUtils {

    private static CommandMap getCommandMap() {
        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            return (CommandMap) f.get(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void registerDynamicCommand(@NotNull String name,
                                              @NotNull CommandExecutor executor,
                                              @Nullable TabCompleter tabCompleter,
                                              @Nullable Permission permission) {
        registerDynamicCommand(name, executor, tabCompleter, List.of(),permission);
    }

    public static void registerDynamicCommand(@NotNull String name,
                                              @NotNull CommandExecutor executor,
                                              @Nullable TabCompleter tabCompleter) {
        registerDynamicCommand(name, executor, tabCompleter, List.of(), null);
    }


    public static void registerDynamicCommand(@NotNull String name,
                                              @NotNull CommandExecutor executor,
                                              @Nullable TabCompleter tabCompleter,
                                              @Nullable List<String> aliases,
                                              @Nullable Permission permission) {

        CommandMap commandMap = getCommandMap();
        if (commandMap == null) {
            plugin.getLogger().warning("There is no Bukkit CommandMap!");
            return;
        }

        if (aliases == null) aliases = Collections.emptyList();
        if (tabCompleter == null) tabCompleter = (sender, command, alias, args) -> Collections.emptyList();

        final TabCompleter finalTabCompleter = tabCompleter;

        Command dynamicCommand = new Command(name, name + "-command", "", aliases) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
                if (permission == null || !sender.hasPermission(permission)) {
                    return true;
                }
                return executor.onCommand(sender, this, label, args);
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
                List<String> result = finalTabCompleter.onTabComplete(sender, this, alias, args);
                return result != null ? result : Collections.emptyList();
            }
        };
        if(permission != null) {
            dynamicCommand.setPermission(permission.getName());
        }

        commandMap.register(plugin.getName(), dynamicCommand);
    }


}
