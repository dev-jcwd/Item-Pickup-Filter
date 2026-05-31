package com.ipf.mixin;

import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {
    
    @Inject(method = "shouldPause", at = @At("HEAD"), cancellable = true)
    private void ipf$unpauseConfig(CallbackInfoReturnable<Boolean> cir) {
        // If the open screen is a Cloth Config screen, tell the game NOT to pause
        if (this.getClass().getName().contains("ClothConfig")) {
            cir.setReturnValue(false);
        }
    }
}