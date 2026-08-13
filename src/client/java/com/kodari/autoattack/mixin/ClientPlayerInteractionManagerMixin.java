package com.kodari.autoattack.mixin;

import com.kodari.autoattack.AutoAttackConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Shadow
    private int blockBreakingCooldown;

    @Inject(method = "tick", at = @At("HEAD"))
    private void autoattack$removeBreakDelay(CallbackInfo callbackInfo) {
        if (AutoAttackConfig.get().noBreakDelay) {
            this.blockBreakingCooldown = 0;
        }
    }

    @ModifyExpressionValue(
            method = "updateBlockBreakingProgress",
            at = @org.spongepowered.asm.mixin.injection.At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"
            )
    )
    private float autoattack$fastBreak(float original) {
        AutoAttackConfig config = AutoAttackConfig.get();
        if (!config.fastBreak) {
            return original;
        }
        return original * Math.max(1.0f, config.fastBreakSpeed);
    }
}