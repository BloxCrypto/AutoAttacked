package com.kodari.autoattack;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;

public final class AutoAttackClient implements ClientModInitializer {
    private static int ticksSinceAttack = Integer.MAX_VALUE;
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(
            Identifier.of(AutoAttackMod.MOD_ID, "general")
    );
    private static KeyBinding toggleKey;
    private static KeyBinding aimAssistKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.autoattack.toggle", GLFW.GLFW_KEY_J, CATEGORY)
        );
        aimAssistKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.autoattack.aim_assist", GLFW.GLFW_KEY_R, CATEGORY)
        );
        AutoAttackConfig.get();
        ClientTickEvents.END_CLIENT_TICK.register(AutoAttackClient::tick);
    }

    private static void tick(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            AutoAttackConfig config = AutoAttackConfig.get();
            config.enabled = !config.enabled;
            ticksSinceAttack = Integer.MAX_VALUE;
            AutoAttackConfig.save();
            if (client.player != null) {
                client.player.sendMessage(
                        Text.translatable(config.enabled ? "message.autoattack.enabled" : "message.autoattack.disabled"),
                        true
                );
            }
        }

        while (aimAssistKey.wasPressed()) {
            AutoAttackConfig config = AutoAttackConfig.get();
            config.aimAssistEnabled = !config.aimAssistEnabled;
            AutoAttackConfig.save();
            if (client.player != null) {
                client.player.sendMessage(
                        Text.translatable(config.aimAssistEnabled
                                ? "message.autoattack.aim_assist_enabled"
                                : "message.autoattack.aim_assist_disabled"),
                        true
                );
            }
        }

        AutoAttackConfig config = AutoAttackConfig.get();
        if (client.player == null || client.currentScreen != null) {
            return;
        }

        if (config.aimAssistEnabled) {
            applyAimAssist(client, config);
        }

        if (!config.enabled || client.interactionManager == null) {
            return;
        }

        if (!config.autoInterval) {
            if (ticksSinceAttack < Integer.MAX_VALUE) {
                ticksSinceAttack++;
            }
            if (ticksSinceAttack < Math.max(1, config.attackInterval)) {
                return;
            }
        }

        PlayerEntity player = client.player;
        // The attack cooldown is calculated from the held hand/item attack-speed attributes.
        if (player.getAttackCooldownProgress(0.0f) < 1.0f) {
            return;
        }

        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof EntityHitResult entityHitResult)) {
            return;
        }

        Entity target = entityHitResult.getEntity();
        if (!(target instanceof LivingEntity livingTarget) || !livingTarget.isAlive()) {
            return;
        }

        float attackRange = Math.max(1.0f, config.attackRange);
        if (player.squaredDistanceTo(target) > attackRange * attackRange) {
            return;
        }

        if (target instanceof PlayerEntity && !config.attackPlayers) {
            return;
        }
        if (!(target instanceof PlayerEntity) && !config.attackMobs) {
            return;
        }

        client.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);
        ticksSinceAttack = 0;
    }

    private static void applyAimAssist(MinecraftClient client, AutoAttackConfig config) {
        if (client.world == null) {
            return;
        }

        PlayerEntity player = client.player;
        float range = Math.max(1.0f, config.aimAssistRange);
        float fov = MathHelper.clamp(config.aimAssistFov, 1.0f, 180.0f);

        LivingEntity target = client.world.getOtherEntities(
                        player,
                        player.getBoundingBox().expand(range),
                        entity -> entity instanceof LivingEntity livingEntity
                                && livingEntity.isAlive()
                                && isAllowedTarget(livingEntity, config)
                                && player.squaredDistanceTo(livingEntity) <= range * range
                                && player.canSee(livingEntity)
                ).stream()
                .map(entity -> (LivingEntity) entity)
                .filter(entity -> isWithinFov(player, entity, fov))
                .min(Comparator.comparingDouble(entity -> angleToTarget(player, entity)))
                .orElse(null);

        if (target == null) {
            return;
        }

        Rotation targetRotation = calculateRotations(player, target);
        float smoothness = MathHelper.clamp(config.aimAssistSmoothness, 0.01f, 1.0f);
        player.setYaw(interpolateRotation(player.getYaw(), targetRotation.yaw(), smoothness));
        player.setPitch(interpolateRotation(player.getPitch(), targetRotation.pitch(), smoothness));
    }

    private static boolean isAllowedTarget(LivingEntity target, AutoAttackConfig config) {
        if (target instanceof PlayerEntity) {
            return config.attackPlayers && !target.isSpectator();
        }
        return config.attackMobs;
    }

    private static boolean isWithinFov(PlayerEntity player, LivingEntity target, float fov) {
        Rotation rotation = calculateRotations(player, target);
        return Math.abs(MathHelper.wrapDegrees(rotation.yaw() - player.getYaw())) <= fov / 2.0f;
    }

    private static double angleToTarget(PlayerEntity player, LivingEntity target) {
        Rotation rotation = calculateRotations(player, target);
        return Math.abs(MathHelper.wrapDegrees(rotation.yaw() - player.getYaw()));
    }

    private static Rotation calculateRotations(PlayerEntity player, LivingEntity target) {
        Vec3d direction = target.getBoundingBox().getCenter().subtract(player.getCameraPosVec(1.0f));
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yaw = MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0f);
        float pitch = MathHelper.clamp(
                MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(direction.y, horizontalDistance))),
                -90.0f,
                90.0f
        );
        return new Rotation(yaw, pitch);
    }

    private static float interpolateRotation(float current, float target, float amount) {
        return current + MathHelper.wrapDegrees(target - current) * amount;
    }

    private record Rotation(float yaw, float pitch) {
    }
}