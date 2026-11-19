package com.ipf.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public class CustomKeybinds {
    public static KeyMapping openGuiKey;

    public static void register() {
        openGuiKey = KeyMappingHelper.registerKeyMapping(Compat.createKeyMapping("key.ipf.opengui", "key.keyboard.n"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.consumeClick()) {
                if (client.player != null) {
                    Compat.openScreen(client, ConfigScreenBuilder.build(Compat.currentScreen(client)));
                }
            }
        });
    }
}
