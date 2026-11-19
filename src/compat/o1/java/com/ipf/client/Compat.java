package com.ipf.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ContainerInput;

/** Minecraft 26.1: screens live on Minecraft, container clicks use ContainerInput. */
public final class Compat {
    private Compat() {}

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("ipf", "main"));

    /** Keys are looked up by name at runtime because the numeric key codes changed in 26.3. */
    public static KeyMapping createKeyMapping(String translationKey, String keyName) {
        return new KeyMapping(translationKey, InputConstants.getKey(keyName).getValue(), CATEGORY);
    }

    public static Packet<?> lookPacket(float yRot, float xRot, boolean onGround) {
        return new ServerboundMovePlayerPacket.Rot(yRot, xRot, onGround, false);
    }

    public static void throwStack(Minecraft client, int menuSlot) {
        client.gameMode.handleContainerInput(client.player.inventoryMenu.containerId, menuSlot, 1, ContainerInput.THROW, client.player);
    }

    public static Screen currentScreen(Minecraft client) { return client.screen; }

    public static void openScreen(Minecraft client, Screen screen) { client.setScreen(screen); }
}
