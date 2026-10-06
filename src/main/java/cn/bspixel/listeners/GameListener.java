package cn.bspixel.listeners;

import cn.bspixel.woolwarshook;
import cn.bspixel.core.GameManager;
import cn.bspixel.core.GameManager.GameInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.cubecrafter.woolwars.api.events.arena.GameEndEvent;
import me.cubecrafter.woolwars.api.events.arena.GameStartEvent;
import me.cubecrafter.woolwars.api.events.player.PlayerKillEvent;
import me.cubecrafter.woolwars.arena.Arena;
import me.cubecrafter.woolwars.arena.team.Team;
import me.cubecrafter.woolwars.storage.player.WoolPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class GameListener implements Listener {

    private final woolwarshook plugin;

    public GameListener(woolwarshook plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStart(GameStartEvent event) {
        Arena arena = event.getArena();
        if (arena == null) return;

        String arenaName = arena.getId();
        String gameId = arenaName + "_" + System.currentTimeMillis();

        List<String> team1 = new ArrayList<>();
        List<String> team2 = new ArrayList<>();

        for (Team team : arena.getTeams()) {
            String teamName = team.getName();
            for (WoolPlayer woolPlayer : team.getMembers()) {
                String playerName = woolPlayer.getPlayer().getName();
                if (teamName.equalsIgnoreCase("red")) {
                    team1.add(playerName);
                } else if (teamName.equalsIgnoreCase("blue")) {
                    team2.add(playerName);
                }
            }
        }

        plugin.getGameManager().registerGame(gameId, arenaName, team1, team2);
        plugin.getLogger().info("游戏已注册: " + gameId + ", 竞技场: " + arenaName);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKill(PlayerKillEvent event) {
        Arena arena = event.getArena();
        if (arena == null || event.getAttacker() == null) return;

        GameManager gameManager = plugin.getGameManager();
        if (gameManager == null) return;

        GameInfo gameInfo = gameManager.getGameByArenaName(arena.getId());
        if (gameInfo == null) return;

        String attackerName = event.getAttacker().getPlayer().getName();
        gameInfo.addPlayerKill(attackerName);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameEnd(GameEndEvent event) {
        Arena arena = event.getArena();
        if (arena == null) return;

        GameManager gameManager = plugin.getGameManager();
        if (gameManager == null) return;

        GameInfo targetGame = gameManager.getGameByArenaName(arena.getId());
        if (targetGame == null) return;

        Team winnerTeam = event.getWinnerTeam();
        int winningTeamNumber = 1;
        if (winnerTeam != null) {
            winningTeamNumber = winnerTeam.getName().equalsIgnoreCase("blue") ? 2 : 1;
        }

        int maxKills = 0;
        String mvp = null;
        for (var entry : targetGame.getAllPlayerKills().entrySet()) {
            if (entry.getValue() > maxKills) {
                maxKills = entry.getValue();
                mvp = entry.getKey();
            }
        }

        JsonObject data = new JsonObject();
        data.addProperty("type", "scoring");
        data.addProperty("gameid", targetGame.getGameId());
        data.addProperty("winningTeamNumber", winningTeamNumber);

        if (mvp != null && maxKills > 0) {
            JsonArray mvps = new JsonArray();
            mvps.add(new JsonPrimitive(mvp));
            data.add("mvps", mvps);
        }

        JsonObject players = new JsonObject();
        for (var entry : targetGame.getAllPlayerKills().entrySet()) {
            JsonObject playerStats = new JsonObject();
            playerStats.addProperty("kills", entry.getValue());
            players.add(entry.getKey(), playerStats);
        }
        data.add("players", players);

        if (plugin.getWebSocketManager() != null && plugin.getWebSocketManager().isConnected()) {
            plugin.getWebSocketManager().sendMessage(data);
            plugin.getLogger().info("已发送 scoring 数据，gameid: " + targetGame.getGameId());
        }

        gameManager.unregisterGame(targetGame.getGameId());
    }
}