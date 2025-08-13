package yusama125718.man10FightClubV2;

import org.bukkit.plugin.java.JavaPlugin;

public final class Man10FightClubV2 extends JavaPlugin {

    public static JavaPlugin mfc;

    @Override
    public void onEnable() {
        mfc = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
