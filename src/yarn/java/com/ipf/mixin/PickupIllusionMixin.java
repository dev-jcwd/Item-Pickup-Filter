package com.ipf.mixin;

import com.ipf.FilterConfig;
import com.ipf.client.FilterLogic;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class PickupIllusionMixin {

    // The pickup sound and the fly-to-player particle are both played inside this handler, so cancelling it hides both.
    @Inject(method = "onItemPickupAnimation", at = @At("HEAD"), cancellable = true)
    private void ipf$hidePickupVisuals(ItemPickupAnimationS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        // The handler first re-queues the packet from the network thread to the main thread and then runs again.
        // Only act on the main-thread pass: the world and our decision cache are not safe to touch from the network thread.
        if (!client.isOnThread()) return;
        if (client.world == null || client.player == null) return;

        // Only hide it when WE picked the item up.
        if (packet.getCollectorEntityId() != client.player.getId()) return;
        if (!FilterLogic.isFilteringNow(client)) return;

        Entity entity = client.world.getEntityById(packet.getEntityId());
        if (entity instanceof ItemEntity itemEntity
                && FilterConfig.INSTANCE.shouldDrop(itemEntity.getStack().getItem(), FilterLogic.ITEM_ID)) {
            ci.cancel();
        }
    }
}
