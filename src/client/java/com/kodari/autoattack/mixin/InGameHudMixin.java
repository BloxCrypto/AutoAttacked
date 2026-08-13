package com.kodari.autoattack.mixin;

import com.kodari.autoattack.AutoAttackConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void autoattack$hideScoreboard(
            DrawContext context,
            RenderTickCounter tickCounter,
            CallbackInfo callbackInfo
    ) {
        if (AutoAttackConfig.get().removedScoreboard) {
            callbackInfo.cancel();
        }
    }
}