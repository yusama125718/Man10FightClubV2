package yusama125718.man10FightClubV2;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import yusama125718.man10FightClubCore.Bet;
import yusama125718.man10FightClubCore.Standby;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.ToDoubleFunction;

import static yusama125718.man10FightClubV2.Man10FightClubV2.*;

public class Game implements Listener {
    private Data.Stage stage;
    private Standby standby;
    private Bet bet;
    private Data.Mode mode;
    private Data.Instance instance;
    private Map<String, Data.Ratio> ratio_data;
    private Map<UUID, String> match_player;
    private Data.Stage match_stage;
    private Data.Kit match_kit;
    private Map<String, Data.PlayerTmp> fighter_tmp;
    private Data.GameStatus status;

    public Game(){}

    public Game(Data.Instance i, Data.Mode m){
        instance = i;
        int cost = 0;
        if (mode == Data.Mode.PRO || mode == Data.Mode.STANDARD){
            cost = entry_cost;
        }
        standby = new Standby("mfc_v2_" + instance.name, prefix, prefix + "[%time%]", cost);
        mode = m;
        ratio_data = new HashMap<>();
        status = Data.GameStatus.None;
    }

    public void Entry(Player p){
        if (!p.hasPermission("mfcv2.p")){
            p.sendMessage(prefix + "§c権限がありません");
            return;
        }
        if (status != Data.GameStatus.None && status != Data.GameStatus.Entry){
            p.sendMessage(prefix + "§c現在エントリーできません");
            return;
        }
        Thread th = new Thread(() -> {
            MySQLManager mysql = new MySQLManager(mfc, "man10_mfc_v2");
            // レートデータ取得
            if (mode != Data.Mode.FREE){
                boolean isPro = false;
                Data.Ratio ratio = new Data.Ratio(p.getName());
                try (ResultSet set = mysql.query("SELECT mode, rate FROM C WHERE uuid = '" + p.getUniqueId() + "' AND deleted_at IS NULL;")) {
                    while (set.next()){
                        Data.Mode mode = Data.Mode.getMode(set.getString("mode"));
                        if (mode == Data.Mode.PRO) isPro = true;
                        ratio.setRatio(mode, set.getInt("rate"));
                    }
                }catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                if (!isPro && mode == Data.Mode.PRO){
                    p.sendMessage(Component.text(prefix + "§cあなたはProの選手ではありません"));
                    return;
                }
                if (isPro && !pro_join_standard && mode == Data.Mode.STANDARD){
                    p.sendMessage(Component.text(prefix + "§cProの選手はStandardに参加できません"));
                    return;
                }
                if (ratio.getRatio(mode) == -1){
                    if (!mysql.execute("INSERT INTO user_data (mode, created_at, updated_at, mcid, uuid, rate, win, lose) VALUES ('" + mode.getLabel() + "' + '" + LocalDateTime.now() + "', '" + LocalDateTime.now() + "', '" + p.getName() + "', '" + p.getUniqueId() + "', 100, 0, 0)")){
                        p.sendMessage(Component.text(prefix + "§cDBの保存に失敗しました"));
                        return;
                    }
                    p.sendMessage(Component.text(prefix + "§eデータがないので新規データを作成しました"));
                    ratio.setRatio(mode, 100);
                }
                ratio_data.put(p.getName(), ratio);
            }
            if (!standby.isEntry) {
                status = Data.GameStatus.Entry;
                standby.StartStandby(30);
                standby.score_board.ShowToWorld(instance.lobby);
                standby.boss_bar.ShowToWorld(instance.lobby);
            }
            standby.Entry(p);
        });
        th.start();
    }

    public void CancelEntry(Player p){
        if (!p.hasPermission("mfcv2.p")){
            p.sendMessage(prefix + "§c権限がありません");
            return;
        }
        standby.Cancel(p);
    }

    @EventHandler
    public void Matching(Standby.MFCStandbyEndEvent e){
        if (!e.getSystemName().equals("mfc_v2_" + instance.name)) return;
        Map<String, UUID> entries = e.getPlayers();
        List<Data.Ratio> target = new ArrayList<>();
        // 試合中のプレイヤーを除外
        for (String mcid : entries.keySet()){
            Player p = Bukkit.getPlayer(entries.get(mcid));
            // オフラインプレイヤーは参加登録からも除外
            if (p == null || !p.isOnline()){
                if (p != null) standby.Cancel(p);

            }
            if (fighters.contains(entries.get(mcid))) continue;
            target.add(ratio_data.get(mcid));
        }
        // 試合可能なステージをリストアップ
        List<Data.Stage> maps = new ArrayList<>();
        for (Data.Stage s : stages){
            if (s.state || s.min_player > target.size()) continue;
            maps.add(s);
        }
        // 試合可能なステージがない場合もう一度募集開始
        if (maps.isEmpty()){
            standby.score_board.BroadCast(prefix + "§c試合可能なステージがないので受付を延長します");
            standby.StartStandby(30);
            return;
        }
        // マップ抽選
        Random rand = new Random();
        int num = rand.nextInt(maps.size());
        match_stage = maps.get(num);
        // キット抽選
        num = rand.nextInt(kits.size());
        match_kit = kits.get(num);
        // 使用中に変更
        match_stage.state = true;
        int cnt = 0;
        match_player = new HashMap<>();
        if (mode == Data.Mode.FREE){
            // 完全ランダム
            while (match_stage.max_player >= cnt && !target.isEmpty()){
                num = rand.nextInt(target.size());
                String mcid = target.get(num).mcid;
                match_player.put(entries.get(mcid), mcid);
                standby.RemovePlayer(mcid);
                fighters.add(mcid);
                cnt++;
            }
        }
        else {
            // 重みつき
            List<Data.Ratio> chosen = new ArrayList<>(match_stage.max_player);
            while (match_stage.max_player >= cnt && !target.isEmpty()) {
                double center = chosen.stream().mapToDouble(r -> r.getRatio(mode)).average().orElse(chosen.get(0).getRatio(mode));
                ToDoubleFunction<Data.Ratio> wf = (r) -> weightLinearWithFloor(center, r.getRatio(mode));
                int idx = weightedPickIndex(target, wf);
                String mcid = target.get(idx).mcid;
                match_player.put(entries.get(mcid), mcid);
                standby.RemovePlayer(mcid);
                chosen.add(target.remove(idx));
                fighters.add(mcid);
                cnt++;
            }
        }
        // フリーの場合そのまま始める
        if (mode == Data.Mode.FREE || mode == Data.Mode.FREE_RATED){
            StartMatch();
            return;
        }
        status = Data.GameStatus.Bet;
        // そうでなければベットタイムが始まる
        bet = new Bet(new HashMap<>(match_player), standby);
        bet.StartBet(40);
        bet.score_board.BroadCast(prefix + "§e§lマッチングしました");
        bet.score_board.BroadCast("§cステージ:§b" + match_stage.name);
        bet.score_board.BroadCast("§e§lプレイヤー一覧");
        for (UUID id : match_player.keySet()){
            bet.score_board.BroadCast("§c§l" + match_player.get(id) + " レート:" + ratio_data.get(match_player.get(id)).getRatio(mode));
        }
        bet.score_board.BroadCast("§e§l/mfc bet [MCID] [金額]でベットしよう！");
    }

    @EventHandler
    public void EndBet(Bet.MFCBetEndEvent e){
        if (!e.getSystemName().equals("mfc_v2_" + instance.name)) return;
        StartMatch();
    }

    public void StartMatch(){
        status = Data.GameStatus.Game;
        fighter_tmp = new HashMap<>();
        int cnt = 0;
        for (UUID id : match_player.keySet()){
            Player p = Bukkit.getPlayer(id);
            Data.PlayerTmp tmp = new Data.PlayerTmp(p);
            if (!p.isOnline()) tmp.is_dead = true;
            fighter_tmp.put(match_player.get(id), tmp);
            p.getInventory().clear();
            cnt++;
        }
    }

    private int weightedPickIndex(List<Data.Ratio> items, ToDoubleFunction<Data.Ratio> wfn) {
        final Random rng = new Random();
        double sum = 0, acc = 0;
        double[] w = new double[items.size()];
        for (int i = 0; i < items.size(); i++) { w[i] = Math.max(0, wfn.applyAsDouble(items.get(i))); sum += w[i]; }
        if (sum <= 0) return rng.nextInt(items.size());
        double r = rng.nextDouble() * sum;
        for (int i = 0; i < w.length; i++) { acc += w[i]; if (r <= acc) return i; }
        return w.length - 1;
    }


    private static double weightLinearWithFloor(double center, double x) {
        double d = Math.abs(x - center);
        // 頭の数字が下限の確率(0.01~0.05がいいらしい)
        // 割り算の後ろがどれくらいの差まで優遇されるか
        return 0.03 + Math.max(0.0, 1.0 - d / (double) 300);
    }
}
