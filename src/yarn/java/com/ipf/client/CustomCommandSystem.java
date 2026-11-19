package com.ipf.client;

import com.ipf.IpfCommand;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CustomCommandSystem {

    private static final IpfCommand.Host HOST = new IpfCommand.Host() {
        @Override
        public void feedback(String text) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§8[§bIPF§8] §f" + text), false);
            }
        }

        @Override
        public boolean itemExists(String id) {
            Identifier identifier = Identifier.tryParse(id);
            return identifier != null && Registries.ITEM.containsId(identifier);
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
