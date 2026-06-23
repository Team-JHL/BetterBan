package de.jakomi1.betterban.util;

import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static de.jakomi1.betterban.scheduler.Scheduler.runAsync;

public class DiscordUtils {

    public static void sendColoredMessage(String content, int color) {
        if (content == null || content.isEmpty()) {
            Bukkit.getLogger().warning("Discord webhook: message is empty, not sending!");
            return;
        }

        runAsync(() -> {
            try {
                String escaped = content.replace("\"", "\\\"").replace("\n", "\\n");
                String jsonPayload = "{"
                        + "\"embeds\":[{"
                        + "\"description\":\"" + escaped + "\","
                        + "\"color\":" + color
                        + "}]"
                        + "}";
                sendWebhook(jsonPayload);
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error sending Discord webhook:");
                e.printStackTrace();
            }
        });
    }

    public static void sendWebhookMessage(String path, int color, Object... replacements) {
        String description = ConfigUtils.getWebhookMessage(path, replacements);
        if (description.isBlank()) {
            return;
        }

        sendWebhookEmbed(description, color);
    }

    public static void sendBanPermanentWebhook(String player, String executor, String reason) {
        String description = ConfigUtils.getWebhookMessage(
                "discord.ban.permanent",
                "player", player,
                "executor", executor
        );

        if (reason != null && !reason.isBlank()) {
            description += "\n" + ConfigUtils.getWebhookMessage("discord.ban.reason", "reason", reason);
        }

        sendWebhookEmbed(description, 0xFF0000);
    }

    public static void sendBanTempWebhook(String player, String executor, String duration, String reason) {
        String description = ConfigUtils.getWebhookMessage(
                "discord.ban.temp",
                "player", player,
                "executor", executor,
                "duration", duration
        );

        if (reason != null && !reason.isBlank()) {
            description += "\n" + ConfigUtils.getWebhookMessage("discord.ban.reason", "reason", reason);
        }

        sendWebhookEmbed(description, 0xFF0000);
    }

    public static void sendUnbanWebhook(String player, String executor) {
        sendWebhookEmbed(
                ConfigUtils.getWebhookMessage("discord.unban.default", "player", player, "executor", executor),
                0x00FF00
        );
    }

    public static void sendKickWebhook(String player, String executor, String reason) {
        String description = ConfigUtils.getWebhookMessage(
                "discord.kick.default",
                "player", player,
                "executor", executor
        );

        if (reason != null && !reason.isBlank()) {
            description += "\n" + ConfigUtils.getWebhookMessage("discord.kick.reason", "reason", reason);
        }

        sendWebhookEmbed(description, 0xFFA500);
    }

    public static void sendChatBanPermanentWebhook(String player, String executor, String reason) {
        String description = ConfigUtils.getWebhookMessage(
                "discord.chatban.permanent",
                "player", player,
                "executor", executor
        );

        if (reason != null && !reason.isBlank()) {
            description += "\n" + ConfigUtils.getWebhookMessage("discord.chatban.reason", "reason", reason);
        }

        sendWebhookEmbed(description, 0xFFA500);
    }

    public static void sendChatBanTempWebhook(String player, String executor, String duration, String reason) {
        String description = ConfigUtils.getWebhookMessage(
                "discord.chatban.temp",
                "player", player,
                "executor", executor,
                "duration", duration
        );

        if (reason != null && !reason.isBlank()) {
            description += "\n" + ConfigUtils.getWebhookMessage("discord.chatban.reason", "reason", reason);
        }

        sendWebhookEmbed(description, 0xFFA500);
    }

    public static void sendChatUnbanWebhook(String player, String executor) {
        sendWebhookEmbed(
                ConfigUtils.getWebhookMessage("discord.chatunban.default", "player", player, "executor", executor),
                0x00FF00
        );
    }

    public static void sendVoiceBanPermanentWebhook(String player, String executor, String reason) {
        String description = ConfigUtils.getWebhookMessage(
                "discord.voiceban.permanent",
                "player", player,
                "executor", executor
        );

        if (reason != null && !reason.isBlank()) {
            description += "\n" + ConfigUtils.getWebhookMessage("discord.voiceban.reason", "reason", reason);
        }

        sendWebhookEmbed(description, 0xFFA500);
    }

    public static void sendVoiceBanTempWebhook(String player, String executor, String duration, String reason) {
        String description = ConfigUtils.getWebhookMessage(
                "discord.voiceban.temp",
                "player", player,
                "executor", executor,
                "duration", duration
        );

        if (reason != null && !reason.isBlank()) {
            description += "\n" + ConfigUtils.getWebhookMessage("discord.voiceban.reason", "reason", reason);
        }

        sendWebhookEmbed(description, 0xFFA500);
    }

    public static void sendVoiceUnbanWebhook(String player, String executor) {
        sendWebhookEmbed(
                ConfigUtils.getWebhookMessage("discord.voiceunban.default", "player", player, "executor", executor),
                0x00FF00
        );
    }

    private static void sendWebhookEmbed(String description, int color) {
        if (description == null || description.isBlank()) {
            return;
        }

        if (!ConfigUtils.isWebhookEnabled()) {
            return;
        }

        String webhook = ConfigUtils.getWebhook();
        if (webhook == null || webhook.isBlank()) {
            return;
        }

        runAsync(() -> {
            try {
                String escaped = description.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n");

                String jsonPayload = "{"
                        + "\"embeds\":[{"
                        + "\"description\":\"" + escaped + "\","
                        + "\"color\":" + color
                        + "}]"
                        + "}";

                sendWebhook(jsonPayload);
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error sending Discord webhook:");
                e.printStackTrace();
            }
        });
    }

    private static void sendWebhook(String jsonPayload) throws Exception {
        if (!ConfigUtils.isWebhookEnabled()) return;

        String webhook = ConfigUtils.getWebhook();
        if (webhook == null || webhook.isBlank()) return;

        URL url = new URL(webhook);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 204) {
            Bukkit.getLogger().severe("Discord webhook error: HTTP " + responseCode);
        }
    }

}