package com.ipf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Persistent filter settings plus the "should this item be dropped" decision.
 * Contains no Minecraft types so the same file is used for every Minecraft version.
 * Everything here runs on the client main thread only.
 */
public class FilterConfig {
    public static final String FEEDBACK_SILENT = "silent";
    public static final String FEEDBACK_MESSAGE = "message";
    public static final String FEEDBACK_FULL = "full";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("ipf.json");

    public static FilterConfig INSTANCE = new FilterConfig();

    // Field names are the JSON keys of existing config files, do not rename.
    public boolean isEnabled = true;
    public boolean isWhitelist = false;
    public Set<String> items = new LinkedHashSet<>();
    public String feedbackType = FEEDBACK_MESSAGE;

    /** Item -> drop? Item types are interned by the registry, so identity lookups are safe. Cleared on every change. */
    private final transient Map<Object, Boolean> dropCache = new IdentityHashMap<>();

    // ---- decision -------------------------------------------------------------------------------------------

    /**
     * An empty whitelist would mean "drop everything you own", which destroys a whole inventory the moment the mode
     * is toggled, so it is treated as "not configured yet" and does nothing.
     */
    public boolean isActive() {
        return isEnabled && !(isWhitelist && items.isEmpty());
    }

    /** @param idOf turns an item into its "namespace:path" id; only called once per item type until the config changes. */
    public <T> boolean shouldDrop(T item, Function<T, String> idOf) {
        Boolean cached = dropCache.get(item);
        if (cached == null) {
            boolean inList = items.contains(idOf.apply(item));
            cached = isWhitelist != inList ? Boolean.TRUE : Boolean.FALSE; // whitelist: drop what is NOT listed
            dropCache.put(item, cached);
        }
        return cached;
    }

    // ---- mutators (every change must clear the cache) --------------------------------------------------------

    public void setEnabled(boolean value) { isEnabled = value; changed(); }

    public void setWhitelist(boolean value) { isWhitelist = value; changed(); }

    public void setFeedbackType(String value) { feedbackType = normalizeFeedback(value); }

    public boolean addItem(String id) { boolean added = items.add(id); changed(); return added; }

    public boolean removeItem(String id) { boolean removed = items.remove(id); changed(); return removed; }

    public void clearItems() { items.clear(); changed(); }

    public void setItems(Set<String> newItems) { items = new LinkedHashSet<>(newItems); changed(); }

    private void changed() { dropCache.clear(); }

    // ---- helpers ---------------------------------------------------------------------------------------------

    /** Lower-cases, trims and adds the "minecraft:" namespace. Returns null for blank input. */
    public static String normalizeItemId(String raw) {
        if (raw == null) return null;
        String id = raw.trim().toLowerCase();
        if (id.isEmpty()) return null;
        return id.contains(":") ? id : "minecraft:" + id;
    }

    private static String normalizeFeedback(String value) {
        if (FEEDBACK_SILENT.equals(value) || FEEDBACK_FULL.equals(value)) return value;
        return FEEDBACK_MESSAGE;
    }

    // ---- persistence -----------------------------------------------------------------------------------------

    public static void load() {
        if (!Files.exists(CONFIG_FILE)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
            FilterConfig loaded = GSON.fromJson(reader, FilterConfig.class);
            if (loaded != null) {
                // Rebuild through the setters so a hand-edited file cannot leave nulls or bad ids behind.
                Set<String> cleaned = new LinkedHashSet<>();
                if (loaded.items != null) {
                    for (String raw : loaded.items) {
                        String id = normalizeItemId(raw);
                        if (id != null) cleaned.add(id);
                    }
                }
                loaded.items = cleaned;
                loaded.feedbackType = normalizeFeedback(loaded.feedbackType);
                INSTANCE = loaded;
            }
        } catch (IOException | JsonParseException e) {
            // Keep the broken file as .bak so the user's list is not lost when we write defaults over it.
            System.err.println("[IPF] Failed to read config, starting with defaults: " + e);
            try {
                Files.move(CONFIG_FILE, CONFIG_FILE.resolveSibling("ipf.json.bak"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {}
        }
    }

    public static void save() {
        Path tmp = CONFIG_FILE.resolveSibling("ipf.json.tmp");
        try (Writer writer = Files.newBufferedWriter(tmp)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            System.err.println("[IPF] Failed to save config: " + e);
            return;
        }
        try { // write-then-rename so a crash mid-save cannot truncate the real file
            Files.move(tmp, CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("[IPF] Failed to replace config: " + e);
        }
    }
}
