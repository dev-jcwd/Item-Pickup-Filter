package com.ipf.client;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Identifier;

/** Minecraft 1.21.9 - 1.21.11: key categories are objects that must be registered once. */
public final class Compat {
    private Compat() {}

    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("ipf", "main"));

    public static KeyBinding createKeyBinding(String translationKey, int keyCode) {
        return new KeyBinding(translationKey, InputUtil.Type.KEYSYM, keyCode, CATEGORY);
    }

    public static Packet<?> lookPacket(float yaw, float pitch, boolean onGround) {
        return new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, onGround, false);
    }
}
