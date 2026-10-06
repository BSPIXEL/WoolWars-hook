package cn.bspixel.service.game;

import cn.bspixel.woolwarshook;
import me.cubecrafter.woolwars.api.WoolWarsAPI;
import me.cubecrafter.woolwars.arena.Arena;
import me.cubecrafter.woolwars.storage.player.WoolPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameSetupService {

    private final woolwarshook plugin;
    private final Random random = new Random();

    public GameSetupService(woolwarshook plugin) {
        this.plugin = plugin;
    }

    private WoolPlayer toWoolPlayer(Player player) {
        if (player == null) return null;
        return WoolWarsAPI.getPlayer(player);
    }

    private Arena getArenaByPlayer(WoolPlayer woolPlayer) {
        if (woolPlayer == null) return null;
        try {
            return WoolWarsAPI.getArenaByPlayer(woolPlayer);
        } catch (Exception e) {
            return null;
        }
    }

    private int getPlayerCount(Arena arena) {
        try {
            java.lang.reflect.Method getPlayersMethod = arena.getClass().getMethod("getPlayers");
            Object result = getPlayersMethod.invoke(arena);
            if (result instanceof java.util.Collection) {
                return ((java.util.Collection<?>) result).size();
            }
        } catch (Exception e) {
        }
        return -1;
    }

    private Arena getRandomAvailableArena() {
        try {
            List<Arena> allArenas = WoolWarsAPI.getArenas();
            if (allArenas == null || allArenas.isEmpty()) {
                plugin.getLogger().warning("No arenas found!");
                return null;
            }

            List<Arena> availableArenas = new ArrayList<>();
            for (Arena arena : allArenas) {
                try {
                    int maxPlayers = -1;
                    try {
                        java.lang.reflect.Method getMaxPlayersMethod = arena.getClass().getMethod("getMaxPlayers");
                        maxPlayers = (int) getMaxPlayersMethod.invoke(arena);
                    } catch (Exception e) {
                    }

                    int currentPlayers = getPlayerCount(arena);

                    if (maxPlayers == -1 || currentPlayers < maxPlayers) {
                        availableArenas.add(arena);
                    }
                } catch (Exception e) {
                    availableArenas.add(arena);
                }
            }

            if (availableArenas.isEmpty()) {
                plugin.getLogger().warning("No available arenas found! Total arenas: " + allArenas.size());
                return null;
            }

            Arena selected = availableArenas.get(random.nextInt(availableArenas.size()));
            plugin.getLogger().info("Randomly selected arena: " + selected.getId() + " from " + availableArenas.size() + " available arenas");
            return selected;

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get random arena: " + e.getMessage());
            return null;
        }
    }

    public Result setupGame(String mapName, List<String> team1Names, List<String> team2Names, String gameId) {
        List<Player> team1Players = new ArrayList<>();
        List<Player> team2Players = new ArrayList<>();
        List<WoolPlayer> team1Wool = new ArrayList<>();
        List<WoolPlayer> team2Wool = new ArrayList<>();
        List<String> offline = new ArrayList<>();

        for (String name : team1Names) {
            Player p = Bukkit.getPlayerExact(name);
            if (p == null || !p.isOnline()) {
                offline.add(name);
            } else {
                WoolPlayer wp = toWoolPlayer(p);
                if (wp != null) {
                    team1Players.add(p);
                    team1Wool.add(wp);
                } else {
                    offline.add(name);
                }
            }
        }

        for (String name : team2Names) {
            Player p = Bukkit.getPlayerExact(name);
            if (p == null || !p.isOnline()) {
                offline.add(name);
            } else {
                WoolPlayer wp = toWoolPlayer(p);
                if (wp != null) {
                    team2Players.add(p);
                    team2Wool.add(wp);
                } else {
                    offline.add(name);
                }
            }
        }

        if (!offline.isEmpty()) {
            return new Result(false, null, offline);
        }

        Arena arena = getRandomAvailableArena();
        if (arena == null) {
            plugin.getLogger().severe("No available arena found!");
            return new Result(false, null, List.of("No available arena found"));
        }

        boolean anyPlaying = false;
        for (WoolPlayer wp : team1Wool) {
            if (WoolWarsAPI.isPlaying(wp)) {
                anyPlaying = true;
                break;
            }
        }
        if (!anyPlaying) {
            for (WoolPlayer wp : team2Wool) {
                if (WoolWarsAPI.isPlaying(wp)) {
                    anyPlaying = true;
                    break;
                }
            }
        }

        if (anyPlaying) {
            plugin.getLogger().warning("Some players are already in a game");
            return new Result(false, null, List.of("Some players are already in a game"));
        }

        for (int i = 0; i < team1Players.size(); i++) {
            Player p = team1Players.get(i);
            WoolPlayer wp = team1Wool.get(i);

            Arena current = getArenaByPlayer(wp);
            if (current != null && current != arena) {
                try {
                    current.removePlayer(wp, null);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to remove player from current arena: " + e.getMessage());
                }
            }

            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&lRWW &8» &a正在将你传送至游戏..."));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    arena.addPlayer(wp, false);
                    plugin.getLogger().info("Added " + p.getName() + " to arena " + arena.getId());
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to add player to arena: " + e.getMessage());
                }
            }, 30L);
        }

        for (int i = 0; i < team2Players.size(); i++) {
            Player p = team2Players.get(i);
            WoolPlayer wp = team2Wool.get(i);

            Arena current = getArenaByPlayer(wp);
            if (current != null && current != arena) {
                try {
                    current.removePlayer(wp, null);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to remove player from current arena: " + e.getMessage());
                }
            }

            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&lRWW &8» &a正在将你传送至游戏..."));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    arena.addPlayer(wp, false);
                    plugin.getLogger().info("Added " + p.getName() + " to arena " + arena.getId());
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to add player to arena: " + e.getMessage());
                }
            }, 30L);
        }

        plugin.getLogger().info("Game setup completed: " + gameId + " on arena " + arena.getId());
        return new Result(true, arena.getId(), offline);
    }

    public static class Result {
        private final boolean success;
        private final String arenaName;
        private final List<String> offlinePlayers;

        public Result(boolean success, String arenaName, List<String> offlinePlayers) {
            this.success = success;
            this.arenaName = arenaName;
            this.offlinePlayers = offlinePlayers;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getArenaName() {
            return arenaName;
        }

        public List<String> getOfflinePlayers() {
            return offlinePlayers;
        }
    }
}