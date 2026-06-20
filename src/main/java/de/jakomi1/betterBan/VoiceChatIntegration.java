package de.jakomi1.betterBan;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;

public class VoiceChatIntegration implements VoicechatPlugin {

    private VoicechatApi api;

    @Override
    public String getPluginId() {
        return "betterban";
    }

    @Override
    public void initialize(VoicechatApi api) {
        this.api = api;

        BetterBan.plugin.getLogger().info("BetterBan wurde mit Simple Voice Chat verbunden");
    }

}