package de.jakomi1.betterban.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import static de.jakomi1.betterban.BetterBan.plugin;

public class PermissionUtils {
    public static Permission registerPermission(@NotNull String permissionID, PermissionDefault permissionDefault) {
        if (Bukkit.getPluginManager().getPermission(plugin+permissionID) == null) {
            Permission perm = new Permission(plugin+permissionID, permissionDefault);
            Bukkit.getPluginManager().addPermission(perm);
            return perm;
        }
        return null;
    }

    public static void setPermission(Player player, @NotNull String permissionID,boolean value) {
        player.addAttachment(plugin).setPermission(plugin+permissionID, value);
    }

    public static boolean hasPermission(Player player, @NotNull String permissionID) {
       return player.hasPermission(plugin+permissionID);
    }

}
