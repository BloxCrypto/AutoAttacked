package com.kodari.autoattack;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AutoAttackModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return AutoAttackModMenu::createConfigScreen;
    }

    private static Screen createConfigScreen(Screen parent) {
        AutoAttackConfig config = AutoAttackConfig.get();
        List<String> mobIds = getMobIds();
        if (!config.autoAttackMobsConfigured) {
            config.autoAttackMobs = new ArrayList<>(mobIds);
            config.autoAttackMobsConfigured = true;
        }
        if (!config.aimAssistMobsConfigured) {
            config.aimAssistMobs = new ArrayList<>(mobIds);
            config.aimAssistMobsConfigured = true;
        }
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("title.autoattack.config"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("category.autoattack.general"))
                        .option(booleanOption(
                                "option.autoattack.enabled",
                                "Enable automatic attacks",
                                false,
                                () -> config.enabled,
                                value -> config.enabled = value
                        ))
                        .option(booleanOption(
                                "option.autoattack.attack_players",
                                "Attack players",
                                true,
                                () -> config.attackPlayers,
                                value -> config.attackPlayers = value
                        ))
                        .option(booleanOption(
                                "option.autoattack.attack_mobs",
                                "Attack mobs",
                                true,
                                () -> config.attackMobs,
                                value -> config.attackMobs = value
                        ))
                        .option(booleanOption(
                                "option.autoattack.crits_only",
                                "Only attack while performing a critical hit",
                                false,
                                () -> config.critsOnly,
                                value -> {
                                    config.critsOnly = value;
                                    AutoAttackConfig.save();
                                }
                        ))
                        .option(booleanOption(
                                "option.autoattack.attack_non_entities",
                                "Attack non-living entities",
                                true,
                                () -> config.attackNonEntities,
                                value -> {
                                    config.attackNonEntities = value;
                                    AutoAttackConfig.save();
                                }
                        ))
                        .group(mobListOption(
                                "option.autoattack.selected_mobs",
                                "Mobs that Auto Attack can target",
                                () -> config.autoAttackMobs,
                                value -> {
                                    config.autoAttackMobs = value;
                                    config.autoAttackMobsConfigured = true;
                                    AutoAttackConfig.save();
                                },
                                mobIds
                        ))
                        .option(floatOption(
                                "option.autoattack.attack_range",
                                "Maximum distance for automatic attacks",
                                3.0f,
                                () -> config.attackRange,
                                value -> {
                                    config.attackRange = value;
                                    AutoAttackConfig.save();
                                },
                                1.0f,
                                6.0f,
                                0.1f
                        ))
                        .option(booleanOption(
                                "option.autoattack.auto_interval",
                                "Use the held item attack cooldown automatically",
                                true,
                                () -> config.autoInterval,
                                value -> {
                                    config.autoInterval = value;
                                    AutoAttackConfig.save();
                                }
                        ))
                        .option(integerOption(
                                "option.autoattack.attack_interval",
                                "Ticks between automatic attacks",
                                10,
                                () -> config.attackInterval,
                                value -> {
                                    config.attackInterval = value;
                                    AutoAttackConfig.save();
                                },
                                1,
                                20,
                                1
                        ))
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("category.autoattack.utility"))
                        .option(booleanOption(
                                "option.autoattack.no_break_delay",
                                "Remove the delay between breaking blocks",
                                false,
                                () -> config.noBreakDelay,
                                value -> {
                                    config.noBreakDelay = value;
                                    AutoAttackConfig.save();
                                }
                        ))
                        .option(booleanOption(
                                "option.autoattack.removed_scoreboard",
                                "Hide the server scoreboard",
                                false,
                                () -> config.removedScoreboard,
                                value -> {
                                    config.removedScoreboard = value;
                                    AutoAttackConfig.save();
                                }
                        ))
                        .option(booleanOption(
                                "option.autoattack.fast_break",
                                "Mine blocks faster",
                                false,
                                () -> config.fastBreak,
                                value -> {
                                    config.fastBreak = value;
                                    AutoAttackConfig.save();
                                }
                        ))
                        .option(floatOption(
                                "option.autoattack.fast_break_speed",
                                "Fast Break speed multiplier",
                                2.0f,
                                () -> config.fastBreakSpeed,
                                value -> {
                                    config.fastBreakSpeed = value;
                                    AutoAttackConfig.save();
                                },
                                1.0f,
                                10.0f,
                                0.1f
                        ))
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("category.autoattack.aim_assist"))
                        .option(booleanOption(
                                "option.autoattack.aim_assist_enabled",
                                "Enable aim assist",
                                false,
                                () -> config.aimAssistEnabled,
                                value -> {
                                    config.aimAssistEnabled = value;
                                    AutoAttackConfig.save();
                                }
                        ))
                        .group(mobListOption(
                                "option.autoattack.aim_assist_selected_mobs",
                                "Mobs that Aim Assist can target",
                                () -> config.aimAssistMobs,
                                value -> {
                                    config.aimAssistMobs = value;
                                    config.aimAssistMobsConfigured = true;
                                    AutoAttackConfig.save();
                                },
                                mobIds
                        ))
                        .option(floatOption(
                                "option.autoattack.aim_assist_fov",
                                "Horizontal field of view for aim assist",
                                90.0f,
                                () -> config.aimAssistFov,
                                value -> {
                                    config.aimAssistFov = value;
                                    AutoAttackConfig.save();
                                },
                                1.0f,
                                180.0f,
                                1.0f
                        ))
                        .option(floatOption(
                                "option.autoattack.aim_assist_smoothness",
                                "How quickly aim assist moves toward a target",
                                0.25f,
                                () -> config.aimAssistSmoothness,
                                value -> {
                                    config.aimAssistSmoothness = value;
                                    AutoAttackConfig.save();
                                },
                                0.01f,
                                1.0f,
                                0.01f
                        ))
                        .option(floatOption(
                                "option.autoattack.aim_assist_range",
                                "Maximum distance for aim assist",
                                6.0f,
                                () -> config.aimAssistRange,
                                value -> {
                                    config.aimAssistRange = value;
                                    AutoAttackConfig.save();
                                },
                                1.0f,
                                12.0f,
                                0.1f
                        ))
                        .build())
                .build()
                .generateScreen(parent);
    }

    private static List<String> getMobIds() {
        return Registries.ENTITY_TYPE.getIds().stream()
                .filter(id -> Registries.ENTITY_TYPE.get(id).getSpawnGroup() != SpawnGroup.MISC)
                .map(Object::toString)
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private static ListOption<String> mobListOption(
            String name,
            String description,
            java.util.function.Supplier<List<String>> getter,
            java.util.function.Consumer<List<String>> setter,
            List<String> availableValues
    ) {
        return ListOption.<String>createBuilder()
                .name(Text.translatable(name))
                .description(OptionDescription.of(Text.literal(description)))
                .binding(
                        new ArrayList<>(availableValues),
                        () -> new ArrayList<>(getter.get()),
                        value -> setter.accept(new ArrayList<>(value))
                )
                .controller(option -> DropdownStringControllerBuilder.create(option)
                        .values(availableValues))
                .initial(availableValues.get(0))
                .collapsed(true)
                .build();
    }

    private static Option<Boolean> booleanOption(
            String name,
            String description,
            boolean defaultValue,
            java.util.function.Supplier<Boolean> getter,
            java.util.function.Consumer<Boolean> setter
    ) {
        return Option.<Boolean>createBuilder()
                .name(Text.translatable(name))
                .description(OptionDescription.of(Text.literal(description)))
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    private static Option<Float> floatOption(
            String name,
            String description,
            float defaultValue,
            java.util.function.Supplier<Float> getter,
            java.util.function.Consumer<Float> setter,
            float min,
            float max,
            float step
    ) {
        return Option.<Float>createBuilder()
                .name(Text.translatable(name))
                .description(OptionDescription.of(Text.literal(description)))
                .binding(defaultValue, getter, setter)
                .controller(option -> FloatSliderControllerBuilder.create(option)
                        .range(min, max)
                        .step(step))
                .build();
    }

    private static Option<Integer> integerOption(
            String name,
            String description,
            int defaultValue,
            java.util.function.Supplier<Integer> getter,
            java.util.function.Consumer<Integer> setter,
            int min,
            int max,
            int step
    ) {
        return Option.<Integer>createBuilder()
                .name(Text.translatable(name))
                .description(OptionDescription.of(Text.literal(description)))
                .binding(defaultValue, getter, setter)
                .controller(option -> IntegerSliderControllerBuilder.create(option)
                        .range(min, max)
                        .step(step))
                .build();
    }
}