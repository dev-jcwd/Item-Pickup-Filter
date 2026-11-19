package com.ipf.client;

import com.ipf.FilterConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ConfigScreenBuilder {
    private static WeakReference<Screen> ours = new WeakReference<>(null);

    public static Screen build(Screen parent) {
        FilterConfig config = FilterConfig.INSTANCE;

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Item Pickup Filter"));

        builder.setSavingRunnable(FilterConfig::save);

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("Settings"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Filter Enabled"), config.isEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(config::setEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Mode (ON = Whitelist, OFF = Blocklist)"), config.isWhitelist)
                .setDefaultValue(false)
                .setTooltip(Component.literal("An empty whitelist does nothing, so toggling it never throws away your inventory."))
                .setSaveConsumer(config::setWhitelist)
                .build());

        general.addEntry(entryBuilder.startStringDropdownMenu(Component.literal("Feedback Type"), config.feedbackType)
                .setDefaultValue(FilterConfig.FEEDBACK_MESSAGE)
                .setSelections(List.of(FilterConfig.FEEDBACK_SILENT, FilterConfig.FEEDBACK_MESSAGE, FilterConfig.FEEDBACK_FULL))
                .setSaveConsumer(config::setFeedbackType)
                .build());

        general.addEntry(entryBuilder.startStrList(Component.literal("Filtered Items"), new ArrayList<>(config.items))
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Component.literal("E.g. 'dirt' or 'cobblestone' (auto-adds minecraft:)"))
                .setCellErrorSupplier(ConfigScreenBuilder::validateItem)
                .setSaveConsumer(newValue -> {
                    Set<String> formatted = new LinkedHashSet<>();
                    for (String raw : newValue) {
                        String id = FilterConfig.normalizeItemId(raw);
                        if (id != null) formatted.add(id);
                    }
                    config.setItems(formatted);
                })
                .build());

        Screen screen = builder.build();
        ours = new WeakReference<>(screen);
        return screen;
    }

    public static boolean isOurs(Screen screen) {
        return screen == ours.get();
    }

    /** Rejects text that can never be an item id. Ids from mods that are not installed are still allowed. */
    private static Optional<Component> validateItem(String raw) {
        String id = FilterConfig.normalizeItemId(raw);
        if (id != null && Identifier.tryParse(id) == null) {
            return Optional.of(Component.literal("Not a valid item id"));
        }
        return Optional.empty();
    }
}
