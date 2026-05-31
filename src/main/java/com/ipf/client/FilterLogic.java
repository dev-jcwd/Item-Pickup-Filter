package com.ipf.client;

import com.ipf.FilterConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class FilterLogic {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || !FilterConfig.INSTANCE.isEnabled) return;
            
            if (client.options.sneakKey.isPressed()) return;

            for (int i = 0; i < 36; i++) {
                ItemStack stack = client.player.getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                    
                    boolean inList = FilterConfig.INSTANCE.items.contains(itemId);
                    boolean shouldDrop = FilterConfig.INSTANCE.isWhitelist ? !inList : inList;

                    if (shouldDrop) {
                        dropItem(client, i, itemId);
                    }
                }
            }
        });
    }

    private static void dropItem(MinecraftClient client, int slot, String itemId) {
        int screenSlot = slot < 9 ? slot + 36 : slot;

        // Added the 'false' parameter back in for 1.21.8
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                client.player.getYaw(), 
                90.0F, 
                client.player.isOnGround(),
                false 
            ));
        }

        client.interactionManager.clickSlot(
            client.player.playerScreenHandler.syncId, 
            screenSlot, 
            1, 
            SlotActionType.THROW, 
            client.player
        );

        // Added the 'false' parameter back in for 1.21.8
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                client.player.getYaw(), 
                client.player.getPitch(), 
                client.player.isOnGround(),
                false
            ));
        }

        String feedback = FilterConfig.INSTANCE.feedbackType;
        if (feedback.equals("full")) {
            client.player.sendMessage(Text.literal("§8[§bIPF§8] §cDropped: §f" + itemId), false);
        } else if (feedback.equals("message")) {
            client.player.sendMessage(Text.literal("§cDropped: §f" + itemId), true);
        }
    }
}