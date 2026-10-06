package cn.bspixel.service;

import cn.bspixel.core.WebSocketManager;
import cn.bspixel.service.game.GameSetupService;
import cn.bspixel.woolwarshook;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class MessageService {

    private final WebSocketManager webSocketManager;
    private final woolwarshook plugin;
    private final GameSetupService gameSetupService;

    public MessageService(WebSocketManager webSocketManager, woolwarshook plugin) {
        this.webSocketManager = webSocketManager;
        this.plugin = plugin;
        this.gameSetupService = new GameSetupService(plugin);
    }

    public void handleMessage(String message) {
        if (message == null || message.trim().isEmpty()) return;
        if (isNonJsonContent(message)) return;

        try {
            JsonObject json = new JsonParser().parse(message).getAsJsonObject();
            if (!json.has("type")) return;

            String type = json.get("type").getAsString();
            handleMessageByType(type, json);

        } catch (Exception e) {
        }
    }

    private boolean isNonJsonContent(String message) {
        String trimmed = message.trim();
        return trimmed.startsWith("HTTP/") ||
                trimmed.startsWith("Date:") ||
                trimmed.startsWith("Server:") ||
                trimmed.startsWith("Content-") ||
                trimmed.startsWith("Connection:") ||
                (!trimmed.startsWith("{") && !trimmed.startsWith("["));
    }

    private void handleMessageByType(String type, JsonObject json) {
        switch (type.toUpperCase()) {
            case "WARP_PLAYERS":
            case "WARPPLAYERS":
                handleWarpPlayers(json);
                break;
            default:
                break;
        }
    }

    private void handleWarpPlayers(JsonObject json) {
        String requestId = json.has("request_id") ? json.get("request_id").getAsString() : null;

        try {
            if (!json.has("game_id") || !json.has("map")) {
                sendErrorResponse("Missing required fields", "game_id or map missing", requestId);
                return;
            }

            String gameId = json.get("game_id").getAsString();
            String mapName = json.get("map").getAsString();

            JsonArray team1Array = json.has("team1") ? json.getAsJsonArray("team1") : new JsonArray();
            JsonArray team2Array = json.has("team2") ? json.getAsJsonArray("team2") : new JsonArray();

            List<String> team1 = extractPlayerNames(team1Array);
            List<String> team2 = extractPlayerNames(team2Array);

            Bukkit.getScheduler().runTask(plugin, () -> {
                GameSetupService.Result result = gameSetupService.setupGame(
                        mapName, team1, team2, gameId);

                if (result.isSuccess()) {
                    sendWarpSuccessResponse(gameId, result.getArenaName(), requestId);
                } else {
                    sendWarpFailureResponse(gameId, mapName, result, requestId);
                }
            });

        } catch (Exception e) {
            plugin.getLogger().severe("Error processing warp_players: " + e.getMessage());
            sendErrorResponse("Failed to process warp_players", e.getMessage(), requestId);
        }
    }

    private List<String> extractPlayerNames(JsonArray playersArray) {
        List<String> names = new ArrayList<>();
        if (playersArray == null) return names;

        for (int i = 0; i < playersArray.size(); i++) {
            try {
                if (playersArray.get(i).isJsonPrimitive()) {
                    names.add(playersArray.get(i).getAsString());
                } else {
                    JsonObject playerJson = playersArray.get(i).getAsJsonObject();
                    if (playerJson.has("ign")) {
                        names.add(playerJson.get("ign").getAsString());
                    }
                }
            } catch (Exception ignored) {}
        }
        return names;
    }

    private void sendWarpSuccessResponse(String gameId, String arenaName, String requestId) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "WARP_SUCCESS");
        response.addProperty("game_id", gameId);
        response.addProperty("map", arenaName);
        if (requestId != null) response.addProperty("request_id", requestId);
        webSocketManager.sendMessage(response);
    }

    private void sendWarpFailureResponse(String gameId, String mapName, GameSetupService.Result result, String requestId) {
        JsonObject response = new JsonObject();

        if (result.getOfflinePlayers() != null && !result.getOfflinePlayers().isEmpty()) {
            response.addProperty("type", "WARP_FAILED_OFFLINE_PLAYERS");
            JsonArray offline = new JsonArray();

            for (String player : result.getOfflinePlayers()) {
                offline.add(new JsonPrimitive(player));
            }

            response.add("offline_players", offline);
        } else {
            response.addProperty("type", "WARP_FAILED_ARENA_NOT_FOUND");
        }

        response.addProperty("game_id", gameId);
        response.addProperty("map", mapName);
        if (requestId != null) response.addProperty("request_id", requestId);
        webSocketManager.sendMessage(response);
    }

    private void sendErrorResponse(String error, String details, String requestId) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "ERROR");
        response.addProperty("error", error);
        response.addProperty("details", details);
        if (requestId != null) response.addProperty("request_id", requestId);
        webSocketManager.sendMessage(response);
    }
}