package com.ipf.client;

import com.ipf.IpfCommand;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class CustomCommandSystem {

    private static final IpfCommand.Host HOST = new IpfCommand.Host() {
        @Override
        public void feedback(String text) {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                client.player.sendSystemMessage(Component.literal("§8[§bIPF§8] §f" + text));
            }
        }

        @Override
        public boolean itemExists(String id) {
            Identifier identifier = Identifier.tryParse(id);
            return identifier != null && BuiltInRegistries.ITEM.containsKey(identifier);
        }
    };

    public static void register() {
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (IpfCommand.isCommand(message)) {
                IpfCommand.handle(message, HOST);
                return false; // keep it away from the server
            }
            return true;
        });
    }
}
