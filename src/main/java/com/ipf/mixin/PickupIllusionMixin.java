package com.ipf.mixin;

import com.ipf.FilterConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class PickupIllusionMixin {
    
    // A tiny timer to tell the audio engine to shut up
    public static int muteAudioTicks = 0;

    @Inject(method = "onItemPickupAnimation", at = @At("HEAD"), cancellable = true)
    private void ipf$hidePickupVisuals(ItemPickupAnimationS2CPacket packet, CallbackInfo ci) {
        if (!FilterConfig.INSTANCE.isEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        
        // CRITICAL: Only hide the visual if WE are the ones picking it up!
        if (packet.getCollectorEntityId() != client.player.getId()) return;

        if (client.options.sneakKey.isPressed()) return;

        Entity entity = client.world.getEntityById(packet.getEntityId());
        if (entity instanceof ItemEntity itemEntity) {
            String itemId = Registries.ITEM.getId(itemEntity.getStack().getItem()).toString();
            
            boolean inList = FilterConfig.INSTANCE.items.contains(itemId);
            boolean shouldDrop = FilterConfig.INSTANCE.isWhitelist ? !inList : inList;

            if (shouldDrop) {
                // Raise the flag to mute the sound engine for the next 2 game ticks
                muteAudioTicks = 2; 
                ci.cancel(); 
            }
        }
    }
}