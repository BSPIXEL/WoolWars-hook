package cn.bspixel.core;

import cn.bspixel.woolwarshook;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final woolwarshook plugin;
    private FileConfiguration config;
    private File configFile;

    private String websocketHost;
    private int websocketPort;
    private List<String> rwwModes;
    private boolean debug;

    private int actionBarUpdateInterval;
    private int maxGameSetupRetries;

    private int websocketMaxReconnectAttempts;
    private int websocketReconnectDelaySeconds;
    private int websocketConnectionTimeoutSeconds;

    public ConfigManager(woolwarshook plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        loadValues();

        plugin.getLogger().info("Configuration loaded successfully!");
    }

    public void reloadConfig() {
        if (configFile != null && configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
            loadValues();
            plugin.getLogger().info("Configuration reloaded!");
        }
    }

    private void loadValues() {
        websocketHost = config.getString("bot.host", "127.0.0.1");
        websocketPort = config.getInt("bot.port", 25519);

        List<String> modes = config.getStringList("rww_modes");
        if (modes.isEmpty()) {
            modes = new ArrayList<>();
            modes.add("RWW");
        }
        rwwModes = modes;

        debug = config.getBoolean("debug", false);

        actionBarUpdateInterval = config.getInt("performance.action_bar_update_interval", 20);
        maxGameSetupRetries = config.getInt("performance.max_game_setup_retries", 5);

        websocketMaxReconnectAttempts = config.getInt("performance.websocket.max_reconnect_attempts", 5);
        websocketReconnectDelaySeconds = config.getInt("performance.websocket.reconnect_delay_seconds", 5);
        websocketConnectionTimeoutSeconds = config.getInt("performance.websocket.connection_timeout_seconds", 10);
    }

    public boolean isRWWMode(String groupName) {
        if (groupName == null || rwwModes == null) return false;

        String upperGroup = groupName.toUpperCase();
        return rwwModes.stream()
                .anyMatch(mode -> upperGroup.contains(mode.toUpperCase()));
    }

    public void saveConfig() {
        if (config != null && configFile != null) {
            try {
                config.save(configFile);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save config: " + e.getMessage());
            }
        }
    }

    public String getWebsocketHost() {
        return websocketHost;
    }

    public int getWebsocketPort() {
        return websocketPort;
    }

    public List<String> getRwwModes() {
        return new ArrayList<>(rwwModes);
    }

    public boolean isDebugEnabled() {
        return debug;
    }

    public int getActionBarUpdateInterval() {
        return actionBarUpdateInterval;
    }

    public int getMaxGameSetupRetries() {
        return maxGameSetupRetries;
    }

    public int getWebsocketMaxReconnectAttempts() {
        return websocketMaxReconnectAttempts;
    }

    public int getWebsocketReconnectDelaySeconds() {
        return websocketReconnectDelaySeconds;
    }

    public int getWebsocketConnectionTimeoutSeconds() {
        return websocketConnectionTimeoutSeconds;
    }
}