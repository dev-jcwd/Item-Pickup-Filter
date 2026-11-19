package com.ipf.mixin;

import com.ipf.FilterConfig;
import com.ipf.client.FilterLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class PickupIllusionMixin {

    // The pickup sound and the fly-to-player particle are both played inside this handler, so cancelling it hides both.
    @Inject(method = "handleTakeItemEntity", at = @At("HEAD"), cancellable = true)
    private void ipf$hidePickupVisuals(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        // The handler first re-queues the packet from the network thread to the main thread and then runs again.
        // Only act on the main-thread pass: the world and our decision cache are not safe to touch from the network thread.
        if (!client.isSameThread()) return;
        if (client.level == null || client.player == null) return;

        // Only hide it when WE picked the item up.
        if (packet.getPlayerId() != client.player.getId()) return;
        if (!FilterLogic.isFilteringNow(client)) return;

        Entity entity = client.level.getEntity(packet.getItemId());
        if (entity instanceof ItemEntity itemEntity
                && FilterConfig.INSTANCE.shouldDrop(itemEntity.getItem().getItem(), FilterLogic.ITEM_ID)) {
            ci.cancel();
        }
    }
}
