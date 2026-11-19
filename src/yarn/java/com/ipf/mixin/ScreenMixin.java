package com.ipf.mixin;

import com.ipf.client.ConfigScreenBuilder;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {

    // Keep the world running while OUR config screen is open (other mods' Cloth screens are left alone).
    @Inject(method = "shouldPause", at = @At("HEAD"), cancellable = true)
    private void ipf$unpauseConfig(CallbackInfoReturnable<Boolean> cir) {
        if (ConfigScreenBuilder.isOurs((Screen) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
