package net.kjiang.bungeeaccess;

import net.md_5.bungee.api.plugin.Plugin;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by kjiang on 2017/1/17.
 */
public class BungeeAccess extends Plugin {

    public static List<String> playerWhiteList = new ArrayList<>();
    public static List<String> uuidWhiteList = new ArrayList<>();
    public static List<String> ipWhiteList = new ArrayList<>();
    public static List<String> playerBlackList = new ArrayList<>();
    public static List<String> uuidBlackList = new ArrayList<>();
    public static List<String> ipBlackList = new ArrayList<>();

    // checkOrder
    public static List<String> checkPriority;

    // checklist
    // initialize checklist, enable alphabetic sorting using lambda code, so you need java 8 to compile and run it.
    public static TreeMap<String, String> whiteListPlayer = new TreeMap<>((a,b) -> {
        int insensitive = String.CASE_INSENSITIVE_ORDER.compare(a, b);
        return insensitive==0 ? a.compareTo(b) : insensitive;
    });
    public static TreeMap<String, String> blackListPlayer = new TreeMap<>((a,b) -> {
        int insensitive = String.CASE_INSENSITIVE_ORDER.compare(a, b);
        return insensitive==0 ? a.compareTo(b) : insensitive;
    });
    public static List<int[]> whiteListIp = new ArrayList<>();
    public static List<int[]> blackListIp = new ArrayList<>();

    // create config handler
    ConfigHandler config = new ConfigHandler(this);
    Config config2 = new Config(this);

    @Override
    public void onEnable() {

        // load config.yml (just load the file, data processing will be later)
        config.loadConfig();
        config2.loadFile();

        // determine storage type and load whitelist and blacklist
        if ( config.getStorageType().equalsIgnoreCase("file") ) {
            // load white and black list from data.yml
            // and resolve uuid as well.
            config.loadData();
            config2.loadData();

            config2.getChecklistPriority();

            // Save data.yml
            //config.saveData();

        } else if ( config.getStorageType().equalsIgnoreCase("mysql") ) {
            getLogger().info("Support of MySQL is not implemented yet. Please set 'storage' into 'file'");
        }

        // debug
        //List<int[]> wlip = (ArrayList<int[]>) checklist.get("whiteListIp");
        //getLogger().info("debugging:");
        //System.out.println(wlip.size());
        //for (int[] l: wlip ) {
        //for (String l: wlip.values() ) {
        //for (String l: blackListPlayer.values() ) {
            //getLogger().info( String.valueOf(l[0]) );
            //getLogger().info( l );
            //System.out.println(l[0]);
        //}
        //getLogger().info("end of debug.");

        // register commands
        getProxy().getPluginManager().registerCommand(this, new BungeeAccessCommand(this));

        // register listener
        getProxy().getPluginManager().registerListener(this, new PlayerJoinListener(this));

        // debug
        //getLogger().info("hello, world!");
    }
}
