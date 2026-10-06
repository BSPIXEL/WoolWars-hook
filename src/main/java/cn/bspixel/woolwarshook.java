package cn.bspixel;

import cn.bspixel.commands.CommandRegistry;
import cn.bspixel.core.ConfigManager;
import cn.bspixel.core.GameManager;
import cn.bspixel.core.WebSocketManager;
import cn.bspixel.listeners.GameListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;

public class woolwarshook extends JavaPlugin {
    private static woolwarshook instance;

    private ConfigManager configManager;
    private WebSocketManager webSocketManager;
    private GameManager gameManager;
    private CommandRegistry commandRegistry;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);
        PluginManager pluginManager = getServer().getPluginManager();
        Plugin woolWars = pluginManager.getPlugin("WoolWars");
        if (woolWars == null || !woolWars.isEnabled()) {
            getLogger().severe("WoolWars 插件未找到或未启用！正在禁用本插件...");
            pluginManager.disablePlugin(this);
            return;
        }
        gameManager = new GameManager(this);
        webSocketManager = new WebSocketManager(this);
        pluginManager.registerEvents(new GameListener(this), this);
        getLogger().info("GameListener已开始工作");
        commandRegistry = new CommandRegistry(this);
        commandRegistry.registerAll();
        webSocketManager.connect();
        getLogger().info("WoolWars 插件已找到，WoolWarsHook 启用成功！");
        getLogger().info("Made BY bsskddd | BSPIXEL NETWORK");
    }

    @Override
    public void onDisable() {
        getLogger().info("WoolWarsHook 已禁用！");
    }

    public static woolwarshook getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public WebSocketManager getWebSocketManager() {
        return webSocketManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public List<String> getGameIdList() {
        if (gameManager == null) {
            return List.of();
        }
        return gameManager.getAllGameIds();
    }
}