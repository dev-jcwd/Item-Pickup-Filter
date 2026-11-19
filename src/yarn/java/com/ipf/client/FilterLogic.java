package com.ipf.client;

import com.ipf.FilterConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.function.Function;

public class FilterLogic {
    /** Limits how many stacks are thrown per tick so a full inventory cannot flood the connection. */
    private static final int MAX_DROPS_PER_TICK = 4;

    /** Item to "namespace:path". Non-capturing, so passing it around allocates nothing. */
    public static final Function<Item, String> ITEM_ID = item -> Registries.ITEM.getId(item).toString();

    /**
     * True when the filter should act right now. Shared with the pickup-animation mixin so the animation is only
     * hidden for items this class will actually throw back.
     * Does nothing while a container (chest, furnace, ...) is open: the throw packets target the player's own
     * inventory menu, and dropping items the player is busy moving would desync it.
     */
    public static boolean isFilteringNow(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        return player != null
                && client.interactionManager != null
                && client.getNetworkHandler() != null
                && FilterConfig.INSTANCE.isActive()
                && !player.isSpectator()
                && !client.options.sneakKey.isPressed() // hold sneak to keep everything
                && player.currentScreenHandler == player.playerScreenHandler;
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isFilteringNow(client)) return;

            ClientPlayerEntity player = client.player;
            FilterConfig config = FilterConfig.INSTANCE;
            PlayerInventory inventory = player.getInventory();

            int dropped = 0;
            for (int slot = 0; slot < 36 && dropped < MAX_DROPS_PER_TICK; slot++) {
                ItemStack stack = inventory.getStack(slot);
                if (stack.isEmpty() || !config.shouldDrop(stack.getItem(), ITEM_ID)) continue;

                // Throw straight down once for the whole batch: aim the server-side player down, throw, aim back.
                if (dropped == 0) aimServerSide(client, 90.0F);
                throwSlot(client, slot);
                notifyDropped(player, ITEM_ID.apply(stack.getItem()));
                dropped++;
            }
            if (dropped > 0) aimServerSide(client, player.getPitch());
        });
    }

    /** Tells the server the player looks at this pitch. The client-side camera is never touched. */
    private static void aimServerSide(MinecraftClient client, float pitch) {
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        ClientPlayerEntity player = client.player;
        handler.sendPacket(Compat.lookPacket(player.getYaw(), pitch, player.isOnGround()));
    }

    private static void throwSlot(MinecraftClient client, int inventorySlot) {
        // Inventory index 0-8 is the hotbar, which the player menu numbers 36-44; 9-35 map 1:1.
        int menuSlot = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
        client.interactionManager.clickSlot(
                client.player.playerScreenHandler.syncId,
                menuSlot,
                1, // button 1 = throw the whole stack
                SlotActionType.THROW,
                client.player);
    }

    private static void notifyDropped(ClientPlayerEntity player, String itemId) {
        String feedback = FilterConfig.INSTANCE.feedbackType;
        if (FilterConfig.FEEDBACK_FULL.equals(feedback)) {
            player.sendMessage(Text.literal("§8[§bIPF§8] §cDropped: §f" + itemId), false);
        } else if (FilterConfig.FEEDBACK_MESSAGE.equals(feedback)) {
            player.sendMessage(Text.literal("§cDropped: §f" + itemId), true);
        }
    }
}
