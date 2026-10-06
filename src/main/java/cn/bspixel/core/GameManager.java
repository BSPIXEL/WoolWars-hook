package cn.bspixel.core;

import cn.bspixel.woolwarshook;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {

    private final woolwarshook plugin;
    private final ConcurrentHashMap<String, GameInfo> activeGames = new ConcurrentHashMap<>();

    public GameManager(woolwarshook plugin) {
        this.plugin = plugin;
    }

    public void registerGame(String gameId, String arenaName, List<String> team1, List<String> team2) {
        GameInfo gameInfo = new GameInfo(gameId, arenaName, team1, team2);
        activeGames.put(gameId, gameInfo);
        plugin.getLogger().info("Game registered: " + gameId + " on " + arenaName);
    }

    public void unregisterGame(String gameId) {
        activeGames.remove(gameId);
    }

    public GameInfo getGame(String gameId) {
        return activeGames.get(gameId);
    }

    public GameInfo getGameByArenaName(String arenaName) {
        for (GameInfo info : activeGames.values()) {
            if (info.getArenaName().equals(arenaName)) {
                return info;
            }
        }
        return null;
    }

    public List<String> getAllGameIds() {
        return new ArrayList<>(activeGames.keySet());
    }

    public static class GameInfo {
        private final String gameId;
        private final String arenaName;
        private final List<String> team1;
        private final List<String> team2;
        private final Map<String, Integer> playerKills = new HashMap<>();

        public GameInfo(String gameId, String arenaName, List<String> team1, List<String> team2) {
            this.gameId = gameId;
            this.arenaName = arenaName;
            this.team1 = team1;
            this.team2 = team2;
        }

        public void addPlayerKill(String playerName) {
            playerKills.put(playerName, playerKills.getOrDefault(playerName, 0) + 1);
        }

        public int getPlayerKillCount(String playerName) {
            return playerKills.getOrDefault(playerName, 0);
        }

        public Map<String, Integer> getAllPlayerKills() {
            return new HashMap<>(playerKills);
        }

        public String getGameId() { return gameId; }
        public String getArenaName() { return arenaName; }
        public List<String> getTeam1() { return team1; }
        public List<String> getTeam2() { return team2; }
    }
}