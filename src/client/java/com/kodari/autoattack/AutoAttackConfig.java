package com.kodari.autoattack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AutoAttackConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("autoattack.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final AutoAttackConfig INSTANCE = load();

    public boolean enabled = false;
    public boolean attackPlayers = true;
    public boolean attackMobs = true;
    public float attackRange = 3.0f;
    public boolean autoInterval = true;
    public int attackInterval = 10;
    public boolean aimAssistEnabled = false;
    public float aimAssistFov = 90.0f;
    public float aimAssistSmoothness = 0.25f;
    public float aimAssistRange = 6.0f;

    public static AutoAttackConfig get() {
        return INSTANCE;
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException exception) {
            AutoAttackMod.LOGGER.error("Could not save auto-attack config", exception);
        }
    }

    private static AutoAttackConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            return new AutoAttackConfig();
        }

        try {
            AutoAttackConfig config = GSON.fromJson(Files.readString(CONFIG_PATH), AutoAttackConfig.class);
            return config == null ? new AutoAttackConfig() : config;
        } catch (IOException | JsonParseException exception) {
            AutoAttackMod.LOGGER.error("Could not load auto-attack config", exception);
            return new AutoAttackConfig();
        }
    }
}