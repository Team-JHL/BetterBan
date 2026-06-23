package de.jakomi1.betterban.command;

import de.jakomi1.betterban.util.BanUtils;
import de.jakomi1.betterban.util.DiscordUtils;
import de.jakomi1.betterban.util.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.jakomi1.betterban.BetterBan.chatPrefix;
import static de.jakomi1.betterban.BetterBan.isAdmin;

public class BanCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !isAdmin(player)) {
            sender.sendMessage(chatPrefix + ChatColor.RED + TextUtils.lang("messages.error.no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(chatPrefix + ChatColor.RED + TextUtils.lang("messages.error.usage_ban"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = target.getUniqueId();

        if (!BanUtils.hasJoinedBefore(uuid)) {
            sender.sendMessage(chatPrefix + ChatColor.RED + TextUtils.lang("messages.error.player_never_joined"));
            return true;
        }

        if (BanUtils.isBanned(uuid)) {
            sender.sendMessage(chatPrefix + ChatColor.RED +
                    (target.getName() != null ? target.getName() : uuid.toString()) + " " + TextUtils.lang("messages.error.already_banned"));
            return true;
        }

        String reason = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim() : null;

        BanUtils.permanentBan(uuid, reason);

        String name = target.getName() != null ? target.getName() : uuid.toString();
        String executor = sender instanceof Player ? sender.getName() : "the console";

        sender.sendMessage(chatPrefix + ChatColor.YELLOW + TextUtils.lang("messages.success.banned_permanent", "player", name));
        if (reason != null && !reason.isBlank()) {
            sender.sendMessage(chatPrefix + ChatColor.GRAY + TextUtils.lang("messages.success.reason", "reason", reason));
        }

        DiscordUtils.sendBanPermanentWebhook(name, executor, reason);

        if (target.isOnline()) {
            Player online = target.getPlayer();
            if (online != null) {
                online.kickPlayer(BanUtils.getBanMessage(uuid));
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
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