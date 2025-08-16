package yusama125718.man10FightClubV2;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import yusama125718.man10FightClubCore.Bet;
import yusama125718.man10FightClubCore.Standby;

import static yusama125718.man10FightClubV2.Man10FightClubV2.entry_cost;
import static yusama125718.man10FightClubV2.Man10FightClubV2.prefix;

public class Game implements Listener {
    private Data.Stage stage;
    private Standby standby;
    private Bet bet;
    private Data.Mode mode;

    public Game(){}

    public Game(Data.Mode mode){
        int cost = 0;
        if (mode == Data.Mode.PRO || mode == Data.Mode.RATED){
            cost = entry_cost;
        }
        standby = new Standby("mfc_v2", prefix, prefix + "[%time%]", cost);
        mode = this.mode;
    }

    public void Entry(Player p){
        if (!p.hasPermission("mfcv2.p")){
            p.sendMessage(prefix + "§c権限がありません");
            return;
        }
        if (!standby.isEntry) standby.StartStandby(30);
        standby.Entry(p);
    }
}
