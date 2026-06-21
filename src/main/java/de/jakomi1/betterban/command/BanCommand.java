package de.jakomi1.betterban.command;

import de.jakomi1.betterban.util.BanUtils;
import de.jakomi1.betterban.util.DiscordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.jakomi1.betterban.BetterBan.chatPrefix;
import static de.jakomi1.betterban.BetterBan.isAdmin;

public class BanCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {

        if (sender instanceof Player player && !isAdmin(player)) {
            sender.sendMessage(chatPrefix + ChatColor.RED + "You don't have permission for this.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(chatPrefix + ChatColor.RED + "Usage: /ban <Name> [Reason...]");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = target.getUniqueId();

        if (!BanUtils.hasJoinedBefore(uuid)) {
            sender.sendMessage(chatPrefix + ChatColor.RED + "This player has never joined the server.");
            return true;
        }

        if (BanUtils.isBanned(uuid)) {
            sender.sendMessage(chatPrefix + ChatColor.RED +
                    (target.getName() != null ? target.getName() : uuid.toString()) + " is already banned!");
            return true;
        }

        String reason = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim() : null;

        BanUtils.permanentBan(uuid, reason);

        String name = target.getName() != null ? target.getName() : uuid.toString();
        String executor = sender instanceof Player ? sender.getName() : "the console";

        sender.sendMessage(chatPrefix + ChatColor.YELLOW + name + " has been permanently banned.");
        if (reason != null && !reason.isBlank()) {
            sender.sendMessage(chatPrefix + ChatColor.GRAY + "-> Reason: " + reason);
        }

        DiscordUtils.sendColoredMessage(
                name + " was permanently banned by " + executor +
                        (reason != null ? "\n*Reason: " + reason + "*": ""),
                0xFF0000
        );

        if (target.isOnline()) {
            Player online = target.getPlayer();
            if (online != null) {
                online.kickPlayer(BanUtils.getBanMessage(uuid));
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      Command command,
                                      String alias,
                                      String[] args) {
        if (sender instanceof Player player && !isAdmin(player)) return List.of();

        if (args.length == 1) {

            return Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(p -> BanUtils.hasJoinedBefore(p.getUniqueId()))
                    .map(OfflinePlayer::getName)
                    .filter(Objects::nonNull)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
