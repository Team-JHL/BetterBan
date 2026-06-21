package de.jakomi1.betterban.command;

import de.jakomi1.betterban.util.DiscordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.jakomi1.betterban.BetterBan.chatPrefix;
import static de.jakomi1.betterban.BetterBan.isAdmin;

public class KickCommand implements CommandExecutor, TabCompleter {

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
            sender.sendMessage(chatPrefix + ChatColor.RED + "Usage: /kick <Name> [Reason]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(chatPrefix + ChatColor.RED + "This player is not online.");
            return true;
        }

        if (sender instanceof Player && ((Player) sender).getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(chatPrefix + ChatColor.RED + "You cannot kick yourself.");
            return true;
        }

        String reason = null;
        if (args.length >= 2) {
            reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
            if (reason.isBlank()) reason = null;
        }

        String kickMessage = chatPrefix + ChatColor.RED + "You have been kicked from the server!";
        if (reason != null) {
            kickMessage += ChatColor.GRAY + "\nReason: " + reason;
        }


        String executor = sender instanceof Player ? sender.getName() : "the console";

        String discordMsg = target.getName() + " was kicked by " + executor
                + (reason != null ? "\n*Reason: " + reason : "*");
        DiscordUtils.sendColoredMessage(discordMsg, 16753920);

        String executorFeedback = chatPrefix + ChatColor.YELLOW + target.getName()
                + " was kicked by " + executor
                + (reason != null ? " Reason: " + reason : "");
        sender.sendMessage(executorFeedback);

        target.kickPlayer(kickMessage);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      Command command,
                                      String alias,
                                      String[] args) {

        if (sender instanceof Player player && !isAdmin(player)) {
            return List.of();
        }

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
