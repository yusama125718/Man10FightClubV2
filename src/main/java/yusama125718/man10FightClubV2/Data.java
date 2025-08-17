package yusama125718.man10FightClubV2;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class Data {
    public class Stage{
        // 初期位置
        public List<Location> spawns;
        public List<String> start_commands;
        public int max_player;
        public int min_player;
        // 使用中か
        public boolean state;
        public String name;

        public Stage(String Name){
            spawns = new ArrayList<>();
            start_commands = new ArrayList<>();
            max_player = 0;
            min_player = 0;
            state = false;
            name = Name;
        }
    }

    public class Kit{
        public PlayerInventory inv;

        public Kit(PlayerInventory Inv){
            inv = Inv;
        }
    }

    public enum Mode{
        FREE("FREE"),
        FREE_RATED("FREE_RATED"),
        STANDARD("STANDARD"),
        PRO("PRO");

        private final String label;

        // コンストラクタ
        Mode(String label) {
            this.label = label;
        }

        // getter
        public String getLabel() {
            return label;
        }

        public static Mode getMode(String str){
            switch (str){
                case "FREE" -> {
                    return FREE;
                }
                case "FREE_RATED" -> {
                    return FREE_RATED;
                }
                case "STANDARD" -> {
                    return STANDARD;
                }
                case "PRO" -> {
                    return PRO;
                }
                default -> {
                    return null;
                }
            }
        }
    }

    public enum GameStatus{
        None,
        Entry,
        Bet,
        Game,
        End;
    }

    public static class Ratio{
        public int pro;
        public int standard;
        public int free;
        public String mcid;

        public Ratio(String m){
            pro = -1;
            standard = -1;
            free = -1;
            mcid = m;
        }

        public int getRatio(Mode mode){
            switch (mode){
                case PRO -> {
                    return pro;
                }
                case STANDARD -> {
                    return standard;
                }
                case FREE_RATED -> {
                    return free;
                }
            }
            return -1;
        }

        public void setRatio(Mode mode, int value){
            switch (mode){
                case PRO -> pro = value;
                case STANDARD -> standard = value;
                case FREE_RATED -> free = value;
            }
        }
    }

    public static class Instance{
        public Location spawn;
        public World lobby;
        public String name;

        public Instance(String n, World l, Location s){
            spawn = s;
            name = n;
            lobby = l;
        }
    }

    public static class PlayerTmp{
        public PlayerInventory inv;
        public boolean is_dead;

        public PlayerTmp(Player p){
            inv = p.getInventory();
            is_dead = false;
        }
    }
}
