package com.ipf.mixin;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerMixin {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void ipf$mutePickupSound(SoundInstance sound, CallbackInfo ci) {
        // If our filter just caught an item, and this sound is a pickup pop...
        if (PickupIllusionMixin.muteAudioTicks > 0) {
            if (sound.getId().getPath().equals("entity.item.pickup")) {
                ci.cancel(); // Assassinate the sound!
            }
        }
    }
}