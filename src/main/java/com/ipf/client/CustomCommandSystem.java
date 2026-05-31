package com.ipf.client;

import com.ipf.FilterConfig;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class CustomCommandSystem {

    public static void register() {
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message.startsWith("!ipf")) {
                handleCommand(message);
                return false; // Prevent message from going to server
            }
            return true; // Let normal chat pass
        });
    }

    private static void handleCommand(String message) {
        String[] parts = message.toLowerCase().split(" ");
        
        // If they just type "!ipf" or "!ipf help", show the menu
        if (parts.length == 1 || parts[1].equals("help")) {
            showHelpMenu();
            return;
        }

        String action = parts[1];

        switch (action) {
            case "toggle":
                FilterConfig.INSTANCE.isEnabled = !FilterConfig.INSTANCE.isEnabled;
                sendFeedback("Filter is now " + (FilterConfig.INSTANCE.isEnabled ? "§aON" : "§cOFF"));
                FilterConfig.save();
                break;
            case "add":
                if (parts.length > 2) {
                    String item = formatItem(parts[2]);
                    FilterConfig.INSTANCE.items.add(item);
                    sendFeedback("Added §a" + item + "§f to the filter.");
                    FilterConfig.save();
                } else {
                    sendFeedback("§cUsage: !ipf add <item>");
                }
                break;
            case "remove":
                if (parts.length > 2) {
                    String item = formatItem(parts[2]);
                    if (FilterConfig.INSTANCE.items.remove(item)) {
                        sendFeedback("Removed §c" + item + "§f from the filter.");
                        FilterConfig.save();
                    } else {
                        sendFeedback("§cItem not found in filter.");
                    }
                } else {
                    sendFeedback("§cUsage: !ipf remove <item>");
                }
                break;
            case "clear":
                FilterConfig.INSTANCE.items.clear();
                sendFeedback("§cFilter list cleared.");
                FilterConfig.save();
                break;
            case "list":
                sendFeedback("Filtered Items: §b" + String.join("§f, §b", FilterConfig.INSTANCE.items));
                break;
            case "mode":
                if (parts.length > 2 && (parts[2].equals("whitelist") || parts[2].equals("blocklist"))) {
                    FilterConfig.INSTANCE.isWhitelist = parts[2].equals("whitelist");
                    sendFeedback("Mode set to §e" + (FilterConfig.INSTANCE.isWhitelist ? "Whitelist" : "Blocklist"));
                    FilterConfig.save();
                } else {
                    sendFeedback("§cUsage: !ipf mode whitelist | blocklist");
                }
                break;
            case "feedback":
                if (parts.length > 2 && (parts[2].equals("silent") || parts[2].equals("message") || parts[2].equals("full"))) {
                    FilterConfig.INSTANCE.feedbackType = parts[2];
                    sendFeedback("Feedback set to §e" + parts[2]);
                    FilterConfig.save();
                } else {
                    sendFeedback("§cUsage: !ipf feedback silent | message | full");
                }
                break;
            case "status":
                sendFeedback("Status: " + (FilterConfig.INSTANCE.isEnabled ? "§aON" : "§cOFF") +
                             " §8| §fMode: §e" + (FilterConfig.INSTANCE.isWhitelist ? "Whitelist" : "Blocklist") +
                             " §8| §fItems: §b" + FilterConfig.INSTANCE.items.size());
                break;
            default:
                showHelpMenu();
                break;
        }
    }

    // Automatically adds the "minecraft:" prefix if the player forgets it
    private static String formatItem(String item) {
        return item.contains(":") ? item : "minecraft:" + item;
    }

    // Your custom formatted menu!
    private static void showHelpMenu() {
        sendFeedback("§e=========IPF==========");
        sendFeedback("§f!ipf toggle");
        sendFeedback("§f!ipf status");
        sendFeedback("§f!ipf add | remove <item>");
        sendFeedback("§f!ipf list");
        sendFeedback("§f!ipf clear");
        sendFeedback("§f!ipf feedback <silent | message | full>");
        sendFeedback("§f!ipf mode <whitelist | blocklist>");
        sendFeedback("§e======================");
    }

    private static void sendFeedback(String text) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§8[§bIPF§8] §f" + text), false);
        }
    }
}