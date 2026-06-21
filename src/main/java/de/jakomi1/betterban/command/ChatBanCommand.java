package de.jakomi1.betterban.command;

import de.jakomi1.betterban.util.BanUtils;
import de.jakomi1.betterban.util.ChatBanUtils;
import de.jakomi1.betterban.util.DiscordUtils;
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

public class ChatBanCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {
        if (sender instanceof Player player && !isAdmin(player)) {
            sender.sendMessage(chatPrefix + ChatColor.RED + "You don't have permission for this.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(chatPrefix + ChatColor.RED +
                    "Usage: /chatban <Name> <Duration> [Reason...]");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = target.getUniqueId();

        if (!BanUtils.hasJoinedBefore(uuid)) {
            sender.sendMessage(chatPrefix + ChatColor.RED + "This player has never joined the server.");
            return true;
        }

        if (ChatBanUtils.isChatBanned(uuid)) {
            sender.sendMessage(chatPrefix + ChatColor.RED +
                    (target.getName() != null ? target.getName() : uuid.toString()) + " is already chat-banned!");
            return true;
        }

        String durationRaw = args[1].toLowerCase();
        boolean permanent = durationRaw.equalsIgnoreCase("permanent");
        long delta = 0L;

        if (!permanent) {
            try {
                delta = parseDuration(durationRaw);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(chatPrefix + ChatColor.RED +
                        "Invalid time format. Example: 10m, 2h, 1d, or 'permanent'.");
                return true;
            }
        }

        String reason = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim() : null;

        if (permanent) {
            ChatBanUtils.chatPermanentBan(uuid, reason);
        } else {
            ChatBanUtils.chatTempBan(uuid, delta, reason);
        }

        String name = Objects.toString(target.getName(), uuid.toString());
        String executor = sender instanceof Player ? sender.getName() : "the console";
        String remaining = permanent ? "permanent" : BanUtils.formatDuration(delta);

        sender.sendMessage(chatPrefix + ChatColor.YELLOW +
                name + " has been chat-banned (" + remaining + ").");
        if (reason != null && !reason.isBlank()) {
            sender.sendMessage(chatPrefix + ChatColor.GRAY + "Reason: " + reason);
        }

        DiscordUtils.sendColoredMessage(
                name + " was chat-banned by " + executor + " (" + remaining + ")" +
                        (reason != null ? "\n*Reason: " + reason : "*"),
                0xFFA500
        );


        Player onlineTarget = Bukkit.getPlayer(uuid);
        if (onlineTarget != null && onlineTarget.isOnline()) {
            onlineTarget.sendMessage(chatPrefix + ChatColor.YELLOW +
                    "You have been chat-banned by " + executor + " (" + remaining + ").");
            if (reason != null && !reason.isBlank()) {
                onlineTarget.sendMessage(chatPrefix + ChatColor.GRAY + "Reason: " + reason);
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

        if (args.length == 2) {
            return Stream.of("10m", "30m", "1h", "2h", "1d", "permanent")
                    .filter(opt -> opt.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
