package cn.bspixel.commands.impl;

import cn.bspixel.woolwarshook;
import cn.bspixel.commands.BaseCommand;
import org.bukkit.command.CommandSender;

public class ConfigCommand extends BaseCommand {

    public ConfigCommand(woolwarshook plugin) {
        super(plugin, "config", "rww.admin", "Manage plugin configuration", "/config [reload]");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                plugin.getConfigManager().reloadConfig();
                sendMessage(sender, "&aConfig reloaded successfully!");
                break;
            case "info":
                sendConfigInfo(sender);
                break;
            default:
                sendMessage(sender, "&cUnknown subcommand. Available: reload, info");
                break;
        }

        return true;
    }

    private void sendConfigInfo(CommandSender sender) {
        sendMessage(sender, "&6=== RWW Plugin Configuration ===");
        sendMessage(sender, "&eWebSocket Host: &f" + plugin.getConfigManager().getWebsocketHost());
        sendMessage(sender, "&eWebSocket Port: &f" + plugin.getConfigManager().getWebsocketPort());
        sendMessage(sender, "&eRWW Modes: &f" + String.join(", ", plugin.getConfigManager().getRwwModes()));
        sendMessage(sender, "&eDebug Mode: &f" + plugin.getConfigManager().isDebugEnabled());
        sendMessage(sender, "&eWebSocket Connected: &f" + (plugin.getWebSocketManager().isConnected() ? "Yes" : "No"));
    }
}