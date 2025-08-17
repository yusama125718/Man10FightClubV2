package yusama125718.man10FightClubV2;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Man10FightClubV2 extends JavaPlugin {

    public static JavaPlugin mfc;
    public static String prefix;
    public static boolean system;
    public static int entry_cost;
    public static float prise_ratio;
    public static List<Data.Stage> stages;
    public static List<Data.Kit> kits;
    // 試合中のプレイヤー(MCID)
    public static List<String> fighters;
    // インスタンス一覧
    public static Map<String, Data.Instance> instances;
    public static boolean pro_join_standard;

    @Override
    public void onEnable() {
        mfc = this;
        getServer().getPluginManager().registerEvents(new Game(), this);
        mfc.saveDefaultConfig();
        Thread th = new Thread(() -> {
            MySQLManager mysql = new MySQLManager(mfc, "man10_mfc_v2");
            mysql.execute("create table if not exists user_data(id int auto_increment, mode varchar(20), created_at datetime, updated_at datetime, mcid varchar(16), uuid varchar(36), rate integer, win integer, lose integer, deleted_at datetime, primary key(id))");
            mysql.execute("create table if not exists rate_log(id int auto_increment, mode varchar(20), time datetime, mcid varchar(16), uuid varchar(36), rate_diff integer, note varchar(120), primary key(id))");
        });
        th.start();
        prefix = mfc.getConfig().getString("prefix") + "§r";
        system = mfc.getConfig().getBoolean("system");
        entry_cost = mfc.getConfig().getInt("entry_cost");
        prise_ratio = (float) mfc.getConfig().getDouble("prise_ratio");
        pro_join_standard = mfc.getConfig().getBoolean("pro_join_standard");
        stages = new ArrayList<>();
        fighters = new ArrayList<>();
        instances = new HashMap<>();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
