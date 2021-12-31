package cz.sognus.TelePlusPlus;

import cz.sognus.TelePlusPlus.listeners.TPEntityListener;
import cz.sognus.TelePlusPlus.listeners.TPPlayerListener;
import cz.sognus.TelePlusPlus.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class TelePlusPlus extends JavaPlugin {
    public static TelePlusPlus plugin = null;
    public static Logger log;
    public PermissionsManager pm;
    public SettingsManager sm;
    public RequestsManager rm;
    public TeleportManager tm;
    public ToggleManager tgm;
    public GlassedManager gm;
    public CommandManager cm;
    public MoverManager mm;
    public ItemManager im;
    private TPPlayerListener playerListener;
    private TPEntityListener entityListener;

    public void onEnable() {
        playerListener = new TPPlayerListener(this);
        entityListener = new TPEntityListener(this);
        pm = new PermissionsManager(this);
        sm = new SettingsManager(this);
        rm = new RequestsManager(this);
        tm = new TeleportManager(this);
        cm = new CommandManager(this);
        tgm = new ToggleManager(this);
        gm = new GlassedManager(this);
        im = new ItemManager(this);
        mm = new MoverManager();

        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(entityListener, this);
        log = getLogger();

        getCommand("tp").setExecutor(cm);

        plugin = this;
    }
}
