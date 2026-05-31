package com.ipf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class FilterConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("ipf.json");

    public static FilterConfig INSTANCE = new FilterConfig();

    public boolean isEnabled = true;
    public boolean isWhitelist = false;
    public Set<String> items = new HashSet<>();
    public String feedbackType = "message"; 

    public static void load() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, FilterConfig.class);
                if (INSTANCE.items == null) INSTANCE.items = new HashSet<>();
            } catch (IOException e) {
                System.err.println("[IPF] Failed to load config!");
            }
        } else {
            save(); 
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            System.err.println("[IPF] Failed to save config!");
        }
    }
}