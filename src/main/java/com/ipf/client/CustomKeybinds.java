package com.ipf.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class CustomKeybinds {
    public static KeyBinding openGuiKey;

    public static void register() {
        // Reverted to the Pre-1.21.9 String Category
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.ipf.opengui", 
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,   
                "category.ipf.main" 
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(ConfigScreenBuilder.build(client.currentScreen));
                }
            }
        });
    }
}