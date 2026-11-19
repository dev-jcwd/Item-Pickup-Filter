package com.ipf;

import java.util.ArrayList;
import java.util.List;

/** The "!ipf ..." chat commands. No Minecraft types, so it is shared by every version. */
public final class IpfCommand {
    private IpfCommand() {}

    /** What the command needs from the running game. */
    public interface Host {
        void feedback(String text);

        /** True if "namespace:path" is a registered item. */
        boolean itemExists(String id);
    }

    /** Matches "!ipf" and "!ipf <args>" but not other words that merely start with "!ipf". */
    public static boolean isCommand(String message) {
        return message.equals("!ipf") || message.startsWith("!ipf ");
    }

    public static void handle(String message, Host host) {
        String[] parts = message.trim().toLowerCase().split("\\s+");
        FilterConfig config = FilterConfig.INSTANCE;

        // "!ipf" or "!ipf help"
        if (parts.length == 1 || parts[1].equals("help")) {
            showHelp(host);
            return;
        }

        switch (parts[1]) {
            case "toggle" -> {
                config.setEnabled(!config.isEnabled);
                host.feedback("Filter is now " + (config.isEnabled ? "§aON" : "§cOFF"));
                FilterConfig.save();
            }
            case "add" -> {
                if (parts.length < 3) {
                    host.feedback("§cUsage: !ipf add <item> [item...]");
                    return;
                }
                boolean changed = false;
                for (int i = 2; i < parts.length; i++) {
                    String id = FilterConfig.normalizeItemId(parts[i]);
                    if (!host.itemExists(id)) {
                        host.feedback("§cUnknown item: §f" + id);
                    } else if (config.addItem(id)) {
                        host.feedback("Added §a" + id + "§f to the filter.");
                        changed = true;
                    } else {
                        host.feedback("§e" + id + "§f is already in the filter.");
                    }
                }
                if (changed) FilterConfig.save();
            }
            case "remove" -> {
                if (parts.length < 3) {
                    host.feedback("§cUsage: !ipf remove <item> [item...]");
                    return;
                }
                boolean changed = false;
                for (int i = 2; i < parts.length; i++) {
                    String id = FilterConfig.normalizeItemId(parts[i]);
                    if (config.removeItem(id)) {
                        host.feedback("Removed §c" + id + "§f from the filter.");
                        changed = true;
                    } else {
                        host.feedback("§cItem not found in filter: §f" + id);
                    }
                }
                if (changed) FilterConfig.save();
            }
            case "clear" -> {
                config.clearItems();
                host.feedback("§cFilter list cleared.");
                FilterConfig.save();
            }
            case "list" -> {
                if (config.items.isEmpty()) {
                    host.feedback("The filter list is empty.");
                } else {
                    host.feedback("Filtered Items: §b" + String.join("§f, §b", new ArrayList<>(config.items)));
                }
            }
            case "mode" -> {
                String mode = parts.length > 2 ? parts[2] : "";
                // "blacklist" is accepted too since that is the word most people use
                if (mode.equals("whitelist") || mode.equals("blocklist") || mode.equals("blacklist")) {
                    config.setWhitelist(mode.equals("whitelist"));
                    host.feedback("Mode set to §e" + (config.isWhitelist ? "Whitelist" : "Blocklist"));
                    if (config.isWhitelist && config.items.isEmpty()) {
                        host.feedback("§eThe whitelist is empty, so nothing is dropped until you add an item.");
                    }
                    FilterConfig.save();
                } else {
                    host.feedback("§cUsage: !ipf mode whitelist | blocklist");
                }
            }
            case "feedback" -> {
                String type = parts.length > 2 ? parts[2] : "";
                if (type.equals(FilterConfig.FEEDBACK_SILENT) || type.equals(FilterConfig.FEEDBACK_MESSAGE) || type.equals(FilterConfig.FEEDBACK_FULL)) {
                    config.setFeedbackType(type);
                    host.feedback("Feedback set to §e" + type);
                    FilterConfig.save();
                } else {
                    host.feedback("§cUsage: !ipf feedback silent | message | full");
                }
            }
            case "status" -> host.feedback("Status: " + (config.isEnabled ? "§aON" : "§cOFF")
                    + " §8| §fMode: §e" + (config.isWhitelist ? "Whitelist" : "Blocklist")
                    + " §8| §fItems: §b" + config.items.size());
            default -> showHelp(host);
        }
    }

    private static void showHelp(Host host) {
        List<String> lines = List.of(
                "§e=========IPF==========",
                "§f!ipf toggle",
                "§f!ipf status",
                "§f!ipf add | remove <item>",
                "§f!ipf list",
                "§f!ipf clear",
                "§f!ipf feedback <silent | message | full>",
                "§f!ipf mode <whitelist | blocklist>",
                "§e======================");
        lines.forEach(host::feedback);
    }
}
