package de.jakomi1.betterban.command;

import de.jakomi1.betterban.util.BanUtils;
import de.jakomi1.betterban.util.ChatBanUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

import java.util.Map;
import java.util.UUID;

import static de.jakomi1.betterban.BetterBan.chatPrefix;

public class ChatBanListCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {

        ChatBanUtils.clearExpiredChatBans();

        Map<UUID, Map<String, Object>> bans = ChatBanUtils.getAllChatBans();

        if (bans.isEmpty()) {
            sender.sendMessage(chatPrefix + ChatColor.GRAY + "No players are chat-banned.");
            return true;
        }

        sender.sendMessage(chatPrefix + ChatColor.YELLOW + "Chat-banned players:");

        for (Map.Entry<UUID, Map<String, Object>> entry : bans.entrySet()) {
            UUID uuid = entry.getKey();
            Map<String, Object> data = entry.getValue();
            OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
            String name = off.getName() != null ? off.getName() : uuid.toString();

            long endTimestamp = (long) data.get("end_timestamp");
            String reason = (String) data.get("reason");

            String suffix;
            if (endTimestamp == -1) {
                suffix = "Permanent";
            } else {
                long rem = endTimestamp - System.currentTimeMillis();
                if (rem < 0) rem = 0;
                suffix = BanUtils.formatDuration(rem);
            }

            sender.sendMessage(ChatColor.GRAY + name + " >> " + suffix);

            if (reason != null && !reason.isBlank()) {
                sender.sendMessage(ChatColor.DARK_GRAY + "-> Reason: " + reason);
            }
        }

        return true;
    }
}
