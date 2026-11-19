package com.ipf.client;

import com.ipf.FilterConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ConfigScreenBuilder {
    public static Screen build(Screen parent) {
        FilterConfig config = FilterConfig.INSTANCE;

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Item Pickup Filter"));

        builder.setSavingRunnable(FilterConfig::save);

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("Settings"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Filter Enabled"), config.isEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(config::setEnabled)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Mode (ON = Whitelist, OFF = Blocklist)"), config.isWhitelist)
                .setDefaultValue(false)
                .setTooltip(Text.literal("An empty whitelist does nothing, so toggling it never throws away your inventory."))
                .setSaveConsumer(config::setWhitelist)
                .build());

        general.addEntry(entryBuilder.startStringDropdownMenu(Text.literal("Feedback Type"), config.feedbackType)
                .setDefaultValue(FilterConfig.FEEDBACK_MESSAGE)
                .setSelections(List.of(FilterConfig.FEEDBACK_SILENT, FilterConfig.FEEDBACK_MESSAGE, FilterConfig.FEEDBACK_FULL))
                .setSaveConsumer(config::setFeedbackType)
                .build());

        general.addEntry(entryBuilder.startStrList(Text.literal("Filtered Items"), new ArrayList<>(config.items))
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Text.literal("E.g. 'dirt' or 'cobblestone' (auto-adds minecraft:)"))
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

    private static WeakReference<Screen> ours = new WeakReference<>(null);

    public static boolean isOurs(Screen screen) {
        return screen == ours.get();
    }

    /** Rejects text that can never be an item id. Ids from mods that are not installed are still allowed. */
    private static Optional<Text> validateItem(String raw) {
        String id = FilterConfig.normalizeItemId(raw);
        if (id != null && Identifier.tryParse(id) == null) {
            return Optional.of(Text.literal("Not a valid item id"));
        }
        return Optional.empty();
    }
}
