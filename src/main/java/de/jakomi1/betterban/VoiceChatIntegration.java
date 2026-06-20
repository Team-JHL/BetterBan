package de.jakomi1.betterban;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;

public class VoiceChatIntegration implements VoicechatPlugin {

    public static VoicechatApi api;

    @Override
    public String getPluginId() {
        return "betterban";
    }

    @Override
    public void initialize(VoicechatApi api) {
        VoiceChatIntegration.api = api;
        BetterBan.plugin.getLogger().info("BetterBan wurde mit Simple Voice Chat verbunden");
    }

}