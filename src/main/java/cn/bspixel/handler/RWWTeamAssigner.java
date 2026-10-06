package cn.bspixel.handler;

import me.cubecrafter.woolwars.api.WoolWarsAPI;
import me.cubecrafter.woolwars.arena.Arena;
import me.cubecrafter.woolwars.arena.team.Team;
import me.cubecrafter.woolwars.arena.team.TeamAssigner;
import me.cubecrafter.woolwars.storage.player.WoolPlayer;
import org.bukkit.entity.Player;
import java.util.List;

public class RWWTeamAssigner extends TeamAssigner {

    private final List<Player> team1;
    private final List<Player> team2;

    public RWWTeamAssigner(List<Player> team1, List<Player> team2) {
        this.team1 = team1;
        this.team2 = team2;
    }

    @Override
    public void assign(Arena arena) {
        List<Team> teams = arena.getTeams();
        if (teams.size() < 2) return;

        Team t1 = teams.get(0);
        Team t2 = teams.get(1);

        for (Player p : team1) {
            WoolPlayer wp = WoolWarsAPI.getPlayer(p);
            if (wp != null) {
                t1.addMember(wp);
            }
        }

        for (Player p : team2) {
            WoolPlayer wp = WoolWarsAPI.getPlayer(p);
            if (wp != null) {
                t2.addMember(wp);
            }
        }
    }
}