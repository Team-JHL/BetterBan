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
import java.util.stream.Stream;

import static de.jakomi1.betterban.BetterBan.chatPrefix;
import static de.jakomi1.betterban.BetterBan.isAdmin;
import static de.jakomi1.betterban.util.TextUtils.parseDuration;

public class TempBanCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !isAdmin(player)) {
            sender.sendMessage(chatPrefix + ChatColor.RED + TextUtils.lang("messages.error.no_permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(chatPrefix + ChatColor.RED + TextUtils.lang("messages.error.usage_tempban"));
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

        long delta;
        try {
            delta = parseDuration(args[1].toLowerCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(chatPrefix + ChatColor.RED + TextUtils.lang("messages.error.invalid_time"));
            return true;
        }

        String reason = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim() : null;

        BanUtils.tempBan(uuid, delta, reason);

        String name = Objects.toString(target.getName(), uuid.toString());
        String executor = sender instanceof Player ? sender.getName() : "the console";
        String remaining = BanUtils.formatDuration(delta);

        sender.sendMessage(chatPrefix + ChatColor.YELLOW + TextUtils.lang("messages.success.banned_temp", "player", name, "duration", remaining));
        if (reason != null && !reason.isBlank()) {
            sender.sendMessage(chatPrefix + ChatColor.GRAY + TextUtils.lang("messages.success.reason", "reason", reason));
        }

        DiscordUtils.sendBanTempWebhook(name, executor, remaining , reason);

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().kickPlayer(
                    chatPrefix + ChatColor.RED + TextUtils.lang("messages.kick.banned") +
                            (reason != null && !reason.isBlank() ? "\n" + TextUtils.lang("messages.kick.reason", "reason", reason) : "")
            );
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

        if (args.length == 2) {
            return Stream.of("10m", "30m", "1h", "2h", "1d")
                    .filter(opt -> opt.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}