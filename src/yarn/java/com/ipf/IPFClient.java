package com.ipf;

import com.ipf.client.CustomCommandSystem;
import com.ipf.client.CustomKeybinds;
import com.ipf.client.FilterLogic;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;

public class IPFClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FilterConfig.load();
        CustomKeybinds.register();
        FilterLogic.register();
        CustomCommandSystem.register();

        // Welcome message when joining a singleplayer world or a server
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
            // client.player is not set yet while JOIN fires, so look it up on the main thread afterwards
            client.execute(() -> {
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§8[§bIPF§8] §aItem Pickup Filter is active! §fUse §e!ipf §ffor more info!"), false);
                }
            }));
    }
}
