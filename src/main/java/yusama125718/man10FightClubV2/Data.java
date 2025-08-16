package yusama125718.man10FightClubV2;

import org.bukkit.Location;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class Data {
    public class Stage{
        // 初期位置
        private List<Location> spawns;
        private List<String> start_commands;
        private int max_player;
        private int min_player;
        // 使用中か
        private boolean state;
        private String name;

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
        private PlayerInventory inv;

        public Kit(PlayerInventory Inv){
            inv = Inv;
        }

        public void setKit(PlayerInventory Inv){
            inv = Inv;
        }

        public PlayerInventory getKit(){
            return inv;
        }
    }

    public enum Mode{
        FREE,
        FREE_RATED,
        RATED,
        PRO
    }
}
