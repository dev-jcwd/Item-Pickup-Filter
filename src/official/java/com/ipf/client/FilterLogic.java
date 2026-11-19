package com.ipf.client;

import com.ipf.FilterConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class FilterLogic {
    /** Limits how many stacks are thrown per tick so a full inventory cannot flood the connection. */
    private static final int MAX_DROPS_PER_TICK = 4;

    /** Item to "namespace:path". Non-capturing, so passing it around allocates nothing. */
    public static final Function<Item, String> ITEM_ID = item -> BuiltInRegistries.ITEM.getKey(item).toString();

    /**
     * True when the filter should act right now. Shared with the pickup-animation mixin so the animation is only
     * hidden for items this class will actually throw back.
     * Does nothing while a container (chest, furnace, ...) is open: the throw packets target the player's own
     * inventory menu, and dropping items the player is busy moving would desync it.
     */
    public static boolean isFilteringNow(Minecraft client) {
        LocalPlayer player = client.player;
        return player != null
                && client.gameMode != null
                && client.getConnection() != null
                && FilterConfig.INSTANCE.isActive()
                && !player.isSpectator()
                && !client.options.keyShift.isDown() // hold sneak to keep everything
                && player.containerMenu == player.inventoryMenu;
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isFilteringNow(client)) return;

            LocalPlayer player = client.player;
            FilterConfig config = FilterConfig.INSTANCE;
            Inventory inventory = player.getInventory();

            int dropped = 0;
            for (int slot = 0; slot < 36 && dropped < MAX_DROPS_PER_TICK; slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (stack.isEmpty() || !config.shouldDrop(stack.getItem(), ITEM_ID)) continue;

                // Throw straight down once for the whole batch: aim the server-side player down, throw, aim back.
                if (dropped == 0) aimServerSide(client, 90.0F);
                throwSlot(client, slot);
                notifyDropped(player, ITEM_ID.apply(stack.getItem()));
                dropped++;
            }
            if (dropped > 0) aimServerSide(client, player.getXRot());
        });
    }

    /** Tells the server the player looks at this pitch. The client-side camera is never touched. */
    private static void aimServerSide(Minecraft client, float pitch) {
        ClientPacketListener connection = client.getConnection();
        LocalPlayer player = client.player;
        connection.send(Compat.lookPacket(player.getYRot(), pitch, player.onGround()));
    }

    private static void throwSlot(Minecraft client, int inventorySlot) {
        // Inventory index 0-8 is the hotbar, which the player menu numbers 36-44; 9-35 map 1:1.
        int menuSlot = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
        Compat.throwStack(client, menuSlot);
    }

    private static void notifyDropped(LocalPlayer player, String itemId) {
        String feedback = FilterConfig.INSTANCE.feedbackType;
        if (FilterConfig.FEEDBACK_FULL.equals(feedback)) {
            player.sendSystemMessage(Component.literal("§8[§bIPF§8] §cDropped: §f" + itemId));
        } else if (FilterConfig.FEEDBACK_MESSAGE.equals(feedback)) {
            player.sendOverlayMessage(Component.literal("§cDropped: §f" + itemId));
        }
    }
}
