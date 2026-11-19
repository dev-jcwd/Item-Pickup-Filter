package com.ipf.client;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/** Minecraft 1.21 - 1.21.1: String key categories, look packet without the horizontalCollision flag. */
public final class Compat {
    private Compat() {}

    public static KeyBinding createKeyBinding(String translationKey, int keyCode) {
        return new KeyBinding(translationKey, InputUtil.Type.KEYSYM, keyCode, "category.ipf.main");
    }

    public static Packet<?> lookPacket(float yaw, float pitch, boolean onGround) {
        return new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, onGround);
    }
}
