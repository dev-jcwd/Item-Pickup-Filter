package com.ipf.client;

import com.ipf.FilterConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConfigScreenBuilder {
    public static Screen build(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Item Pickup Filter"));

        builder.setSavingRunnable(() -> {
            FilterConfig.save();
        });

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("Settings"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Filter Enabled"), FilterConfig.INSTANCE.isEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> FilterConfig.INSTANCE.isEnabled = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Mode (ON = Whitelist, OFF = Blocklist)"), FilterConfig.INSTANCE.isWhitelist)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> FilterConfig.INSTANCE.isWhitelist = newValue)
                .build());

        // This creates the explicit Dropdown Menu you requested
        general.addEntry(entryBuilder.startStringDropdownMenu(Text.literal("Feedback Type"), FilterConfig.INSTANCE.feedbackType)
                .setDefaultValue("message")
                .setSelections(List.of("silent", "message", "full"))
                .setSaveConsumer(newValue -> FilterConfig.INSTANCE.feedbackType = newValue)
                .build());

        // This list now features Autofill formatting for the "minecraft:" prefix
        general.addEntry(entryBuilder.startStrList(Text.literal("Filtered Items"), new ArrayList<>(FilterConfig.INSTANCE.items))
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Text.literal("E.g. 'dirt' or 'cobblestone' (auto-adds minecraft:)"))
                .setSaveConsumer(newValue -> {
                    HashSet<String> formattedItems = new HashSet<>();
                    for (String item : newValue) {
                        item = item.trim().toLowerCase();
                        if (!item.isEmpty()) {
                            // If the player forgot the prefix, add it automatically
                            if (!item.contains(":")) {
                                item = "minecraft:" + item;
                            }
                            formattedItems.add(item);
                        }
                    }
                    FilterConfig.INSTANCE.items = formattedItems;
                })
                .build());

        return builder.build();
    }
}