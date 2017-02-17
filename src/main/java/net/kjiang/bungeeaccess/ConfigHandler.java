package net.kjiang.bungeeaccess;

import java.util.*;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Created by kjiang on 2017/1/18.
 */
public class ConfigHandler {
    private static Plugin plugin;

    private static File configFile;
    private static File dataFile;
    private static Configuration config; // bungee config
    private static Configuration data; // data

    // data read from config
    private static List<String> dataWhitelistPlayer = null;

    // constructor
    public ConfigHandler(Plugin _plugin) {
        // get plugin
        plugin = _plugin;
    }

    public static void loadConfig() {
        // Load if configs exist. If no, create.
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir(); // create folder
        // open file config.yml
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = plugin.getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // open file data.yml
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try (InputStream in = plugin.getResourceAsStream("data.yml")) {
                Files.copy(in, dataFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // load config and data into "config" and "data"
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            data = ConfigurationProvider.getProvider(YamlConfiguration.class).load(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getStorageType() {
        return config.getString("storage");
    }

    public static Boolean isUuidCheckEnabled() {
        return config.getBoolean("uuid");
    }

    // get Player White List
    // This method is NOT for loading white/black list, use load() instead.
    public static List<String> getDataList(String type, String category) {
        return data.getStringList(type+"-"+category);
    }
    public static String getDataListString(String type, String category, Integer from, Integer to) {
        List dataList = data.getStringList(type+"-"+category);
        Integer max = dataList.size();
        String element;
        // prevent out-of-index
        if (from>max) return "";
        if (to>max) to = max;
        // the first element
        element = dataList.get(from).toString();
        String list = element.substring(0, element.indexOf("|"));
        // the following elements
        for (Integer i=from+1; i<to; ++i) {
            element = dataList.get(i).toString();
            if (element.contains("|")) list = list.concat(", ").concat(element.substring(0, element.indexOf("|")));
            else list = list.concat(", ").concat(element);
        }
        return list;
    }

    public static List<String> getPriority() {
        return config.getStringList("priority");
    }
    public static String getPriority(int rank) {
        return config.getStringList("priority").get(rank);
    }

    // load all types of data
    // TODO: load ip,player,blacklist...
    public static void loadData() {
        int i;

        // used to mark names that need to be resolved
        List<String> uuidResolveList = new ArrayList<>(); // storing names that need to be resolved



        // get player whitelist data, this is used to save config back to data.yml
        dataWhitelistPlayer = data.getStringList("whitelist-player");
        // sort list
        Collections.sort(dataWhitelistPlayer, String.CASE_INSENSITIVE_ORDER);
        // load uuid from data
        i = -1;
        for (String s : dataWhitelistPlayer) {
            i++;
            if (s.contains("|")) {
                BungeeAccess.playerWhiteList.add(s.substring(0, s.indexOf("|")));
                BungeeAccess.uuidWhiteList.add(s.substring(s.indexOf("|") + 1));
            } else {
                BungeeAccess.playerWhiteList.add(s);
                BungeeAccess.uuidWhiteList.add("0"); // set default value to 0
                // add names that need to be resolved into uuid
                if (config.getBoolean("uuid")) {
                    plugin.getLogger().info("player \"" + s + "\" has no uuid. Added into resolve list");
                    uuidResolveList.add(s);
                }
            }
        }

        // Resolve whitelist UUIDs.
        if (!uuidResolveList.isEmpty()) {
            Map uuidResponse = fetchUuid(uuidResolveList);
            // add resolved uuid into uuid list
            //int dataWhitelistPlayerSize = dataWhitelistPlayer.size();
            i = -1;
            List<String> keys = new ArrayList<>(uuidResponse.keySet()); // get actural names according from Mojang API
            Collections.sort(keys, String.CASE_INSENSITIVE_ORDER); // sort returned value by names
            // search config data list ... using sorted uuidResponse keys (names):
            for (String key : keys) {
                String value = uuidResponse.get(key).toString();    // get the response uuid
                plugin.getLogger().info("Resolved player \"" + key + "\" uuid: " + value);
                while (true) {
                    ++i; // Since all result are sorted alphabetically, we only need to search the next element.
                    //if ( i == sizeI ) break;
                    if (dataWhitelistPlayer.get(i).equalsIgnoreCase(key)) {
                        // set name into config and checklist
                        dataWhitelistPlayer.set(i, key + "|" + value);
                        plugin.getLogger().info(dataWhitelistPlayer.get(i));
                        BungeeAccess.playerWhiteList.set(i, key);
                        BungeeAccess.uuidWhiteList.set(i, value);
                        break; // search the next value in returns.
                    } else if (!dataWhitelistPlayer.get(i).contains("|")) {
                        // also save the default uuid "0" to data.yml if no response from Mojang found.
                        // as mentioned before, both dataWhitelistPlayer and uuidResopnse names (in this loop) are alphabetic sorted,
                        // so chance of overlooking any elements is impossible.
                        plugin.getLogger().info("No UUID is found for \""+key+"\", check your data.yml file if you are running in the online mode.");
                        dataWhitelistPlayer.set(i, dataWhitelistPlayer.get(i) + "|0");
                    }
                    // just fyi: don't worry about a dead-loop would happen here. when the last element was found,
                    // the "break" in if-statement above will just break us out of this while-loop.
                }
                // plus, since there is no more keys in uuidResponse, this for-loop will break as well.
            }
            // in some case when Mojang could not find any uuid for us,
            // in other words if all the player names we find are wrong,
            // we should set uuid "0" for them as well so the next time plugin will not waste time on querying uuid.
            if (keys.isEmpty()) {
                plugin.getLogger().info("No UUIDs are found by Mojang, check your data.yml file if you are running in the online mode.");
                i = -1;
                for (String s : uuidResolveList) {
                    while (true) {
                        i++;
                        if (dataWhitelistPlayer.get(i).equalsIgnoreCase(s)) {
                            dataWhitelistPlayer.set(i, s + "|0");
                            BungeeAccess.playerWhiteList.set(i, s);
                            BungeeAccess.uuidWhiteList.set(i, "0");
                            break; // search the next value in uuidResolveList.
                        }
                    }
                }
            }
            // flush into output stream, and this.save() the information will be saved into file.
            saveData();
        }
    }

    // add and save elements to data.yml
    // TODO: add duplicate detect
    // TODO: change dataWhitelistPlayer, whitelist etc into HashSet, so duplicates and sort are automatically done!
    public static void insertData(Map<String, String> dataSet) {
        // in this case keys acturally are values (names/uuids/ips).
        List<String> keys = new ArrayList<>(dataSet.keySet());
        for (String key: keys) {
            switch (dataSet.get(key)) {
                case "whitelistuuid":
                    // TODO
                case "whitelistplayer":
                    BungeeAccess.playerWhiteList.add(key); // set data to checklist
                    dataWhitelistPlayer.add(key); // set data to data file
                    break;
                case "whitelistip":
                    break;
                case "blacklistuuid":
                case "blacklistplayer":
                    break;
                case "blacklistip":
                    break;
            }
        }
        // sort data alphabetically
        Collections.sort(dataWhitelistPlayer, String.CASE_INSENSITIVE_ORDER); // sort returned value by names
        Collections.sort(BungeeAccess.playerWhiteList, String.CASE_INSENSITIVE_ORDER); // sort returned value by names
        //Collections.sort(dataWhitelistIp, String.CASE_INSENSITIVE_ORDER);
        saveData(); // saving into data.yml
    }
    public static void insertData(String category, List<String> data) {
        switch (category.toLowerCase()) {
            case "whitelistuuid":
                // TODO: find name of uuid...
            case "whitelistplayer":
                // TODO: try to resolve uuid first...
                //Map uuidResponse = fetchUuid(data);
                //...
                // then, insert into data list:
                for (String s: data) {
                    BungeeAccess.playerWhiteList.add(s); // set data to checklist
                    dataWhitelistPlayer.add(s); // set data to data file
                }
                // sort data alphabetically
                Collections.sort(dataWhitelistPlayer, String.CASE_INSENSITIVE_ORDER); // sort returned value by names
                Collections.sort(BungeeAccess.playerWhiteList, String.CASE_INSENSITIVE_ORDER); // sort returned value by names
                break;
            case "whitelistip":
                for (String s: data) {
                    //dataWhitelistIp.add(s);
                }
                break;
        }
        saveData(); // saving into data.yml
    }

    // save changes to data.yml
    public static void saveData() {
        data.set("whitelist-player", dataWhitelistPlayer);
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(data, new File(plugin.getDataFolder(), "data.yml"));
            plugin.getLogger().info("Saved changes to data.yml");
        } catch (IOException e) {
            // The file didn't get saved due to an error
            e.printStackTrace();
        }
    }

    private static Map<String, UUID> fetchUuid(List<String> names) {
        UUIDFetcher fetcher = new UUIDFetcher(names);
        try {
            plugin.getLogger().info("Resolving UUIDs ...");
            return fetcher.call();
        } catch (Exception e) {
            plugin.getLogger().warning("Exception while fetching UUID for whitelist");
            e.printStackTrace();
        }
        return null;
    }
}
