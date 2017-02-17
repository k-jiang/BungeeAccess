package net.kjiang.bungeeaccess;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by inventor on 2017/1/21.
 */
public class Config {
    private static Plugin plugin;

    //private static File configFile;
    //private static File dataFile;
    private static Configuration configConfiguration;
    private static Configuration dataConfiguration;

    //private static HashMap<String, TreeSet> data;

    // constructor
    public Config(Plugin _plugin) {
        // get plugin
        plugin = _plugin;
    }

    public static void loadFile() {
        // Load if configs exist. If no, create.
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir(); // create folder
        // open file config.yml
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = plugin.getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // open file data.yml
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try (InputStream in = plugin.getResourceAsStream("data.yml")) {
                Files.copy(in, dataFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // load config and data into "config" and "data"
        try {
            configConfiguration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            dataConfiguration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // verify
        //verifyConfig();
        //verifyData();
    }

    // verify configs
    /*private static void verifyConfig() {

    }
    private static void verifyData() {

    }*/

    // load and process white / black list from dataConfiguration
    public static void loadData(String listType, String objectType) {
        // variable for storing data read from data.yml config
        List<String> dataSet = new ArrayList<>();
        // load from data.yml configuration
        plugin.getLogger().info("loading "+listType+"-"+objectType);
        dataSet.addAll((dataConfiguration.getStringList(listType+"-"+objectType)));
        String checklistTitle = listType.substring(0, 5)+"List";

        // terminate if list is empty
        if (dataSet.isEmpty()) return;
        // deal with player uuid list
        if (objectType.equalsIgnoreCase("player")) {
            checklistTitle += "Player";

            // temporary list to store player checklist, enabled alphabetic sorting using lambda code, so you need java 8 to compile and run it.
            TreeMap<String, String> list = new TreeMap<>((a,b) -> {
                int insensitive = String.CASE_INSENSITIVE_ORDER.compare(a, b);
                return insensitive==0 ? a.compareTo(b) : insensitive;
            });

            // mark names that need to be resolved
            List<String> uuidResolveList = new ArrayList<>(); // storing names that need to be resolved
            // create checklist
            for (String s : dataSet) {
                //s.matches("|.+")
                if (s.contains("|")) {
                    // add username and uuid
                    /*(if (s.substring(s.indexOf("|") + 1).isEmpty()) {
                        // add names that need to be resolved into uuid
                        plugin.getLogger().info("player \"" + s + "\" has no uuid. Added into resolve list");
                        uuidResolveList.add(s);
                    } else {*/
                        list.put(s.substring(0, s.indexOf("|")), s.substring(s.indexOf("|") + 1));
                    //}
                } else {
                    // add username and default uuid 0
                    //list.put(s, "0");
                    // add names that need to be resolved into uuid
                    plugin.getLogger().info("player \"" + s + "\" has no uuid. Added into resolve list");
                    uuidResolveList.add(s);
                }
            }

            // check if uuid need to be resolve
            if (configConfiguration.getBoolean("uuid") && !uuidResolveList.isEmpty()) {
                // Resolve UUIDs
                Map resolvedUuid = fetchUuid(uuidResolveList);
                int i = -1;
                List<String> keys = new ArrayList<>(resolvedUuid.keySet()); // get actural names according from Mojang API
                Collections.sort(keys, String.CASE_INSENSITIVE_ORDER); // sort returned value by names
                // search config data list ... using sorted uuidResponse keys (names):
                for (String key : keys) {
                    String value = resolvedUuid.get(key).toString();    // get the response uuid
                    plugin.getLogger().info("Resolved player \"" + key + "\" uuid: " + value);
                    while (i<dataSet.size()-1) {
                        ++i; // Since all result are sorted alphabetically, we only need to search the next element.
                        //if ( i == sizeI ) break;
                        if (dataSet.get(i).equalsIgnoreCase(key)) {
                            // set name into config and checklist
                            dataSet.set(i, key + "|" + value);
                            //plugin.getLogger().info(dataSet.get(i));
                            // replace value in checklist
                            list.put(key, value);
                            break; // search the next value in returns.
                        } else if (!dataSet.get(i).contains("|")) {
                            // also save the default uuid "0" to data.yml if no response from Mojang found.
                            // as mentioned before, both dataWhitelistPlayer and uuidResopnse names (in this loop) are alphabetic sorted,
                            // so chance of overlooking any elements is impossible.
                            plugin.getLogger().info("No UUID is found for \""+key+"\", check your data.yml file if you are running in the online mode.");
                            list.put(dataSet.get(i), "0");
                            dataSet.set(i, dataSet.get(i) + "|0");
                        }
                        // just fyi: Usually you don't need to worry about a dead-loop would happen here. when the last element was found,
                        // the "break" in if-statement above will just break us out of this while-loop.
                    }
                    // plus, since there is no more keys in uuidResponse, this for-loop will break as well.
                }
                plugin.getLogger().info("info - dataSet.size:"+dataSet.size()+" keys.size"+keys.size());
                // in some case when Mojang could not find any uuid for us,
                // in other words if all the player names we find are wrong,
                // we should set uuid "0" for them as well so the next time plugin will not waste time on querying uuid.
                if (keys.isEmpty()) {
                    plugin.getLogger().info("No UUIDs are found by Mojang, check your data.yml file if you are running in the online mode.");
                    i = -1;
                    for (String s : uuidResolveList) {
                        while (i<dataSet.size()-1) {
                            i++;
                            if (dataSet.get(i).equalsIgnoreCase(s)) {
                                dataSet.set(i, s + "|0");
                                list.put(dataSet.get(i), s);
                                list.put(dataSet.get(i), "0");
                                break; // search the next value in uuidResolveList.
                            }
                        }
                    }
                }
                Collections.sort(dataSet, String.CASE_INSENSITIVE_ORDER);   // sort it alphabetically so we can search it

            } else {
                // if uuid is not enabled, set all uuid to 0 in checklist
                for (String s: uuidResolveList) {
                    list.put(s, "0");
                }
            }

            // insert into checklist
            if (listType.equalsIgnoreCase("whitelist")) {
                BungeeAccess.whiteListPlayer.putAll(list);
            } else if (listType.equalsIgnoreCase("blacklist")) {
                BungeeAccess.blackListPlayer.putAll(list);
            }
            //BungeeAccess.checklist.remove(checklistTitle); // remove old list before insert
            //BungeeAccess.checklist.put(checklistTitle, list);
        } else if (objectType.equalsIgnoreCase("ip")) {
            checklistTitle += "Ip";

            // temporary list to store ip checklist
            List<int[]> list = new ArrayList<>();
            // get keys
            //List<String> keys = new ArrayList<>(dataSet.keySet());
            // loop through ip lists
            //for (String s: dataSet) {
            String s;
            for (int key=0; key<dataSet.size(); key++) {
                int[] ipRange = new int[8];   // format: [ ipFrom, ipFromV6, ipFromV6, ipFromV6, ipTo, ipToV6, ipToV6, ipToV6 ]
                s = dataSet.get(key);
                // if it is a single ip ...
                if (!s.contains("-")) {
                    s = s+"-"+s;
                    dataSet.set(key, s);
                }
                // variables to store each boundaries of ip range
                List<String> ipAddressFrom = new ArrayList<>();
                List<String> ipAddressTo = new ArrayList<>();
                // convert ip to dec int
                if (s.contains(".")) {
                    // IPv4
                    ipAddressFrom.addAll(Arrays.asList(s.substring(0, s.indexOf("-")).split("\\.")));
                    ipAddressTo.addAll(Arrays.asList(s.substring(s.indexOf("-") + 1).split("\\.")));
                    ipRange[0] = ipRange[1] = ipRange[2] = ipRange[4] = ipRange[5] = ipRange[6] = 0;    // clear values that might leave by ipv6
                    ipRange[3] = Integer.parseInt(ipAddressFrom.get(0))*16777216+Integer.parseInt(ipAddressFrom.get(1))*65536+Integer.parseInt(ipAddressFrom.get(2))*256+Integer.parseInt(ipAddressFrom.get(3));
                    ipRange[7] = Integer.parseInt(ipAddressTo.get(0))*16777216+Integer.parseInt(ipAddressTo.get(1))*65536+Integer.parseInt(ipAddressTo.get(2))*256+Integer.parseInt(ipAddressTo.get(3));
                    list.add(ipRange);  // put it into list
                    //plugin.getLogger().info( Integer.toString(ipRange[0]) );
                } else if (s.contains(":")) {
                    // IPv6
                    ipAddressFrom.addAll(Arrays.asList(s.substring(0, s.indexOf("-")).split(":")));
                    ipAddressTo.addAll(Arrays.asList(s.substring(s.indexOf("-") + 1).split(":")));
                    int ipAddressFromSize = ipAddressFrom.size();
                    int ipAddressToSize = ipAddressTo.size();
                    int i;  // we need the i later on
                    // fill zeros ::
                    if ( ipAddressFromSize != 8 ) {
                        // fill up empty, extend StringList size ( fuck java )
                        for (i=0; i<9-ipAddressFromSize; i++) {
                            ipAddressFrom.add("0");
                        }
                        // shift tailing values to the end
                        for (i=ipAddressFromSize-1;i>1;i--) {
                            if (ipAddressFrom.get(i).isEmpty()) break;  // break when meet "::"
                            ipAddressFrom.set(i+8-ipAddressFromSize, ipAddressFrom.get(i));
                        }
                        // fill zeros
                        for (int p=i; p<i+9-ipAddressFromSize; p++) {
                            ipAddressFrom.set(p, "0");
                        }
                    }
                    if ( ipAddressToSize != 8 ) {
                        // fill up empty
                        for (i=0; i<9-ipAddressToSize; i++) {
                            ipAddressTo.add("0");
                        }
                        // shift tailing values to the end
                        for (i=ipAddressToSize-1;i>1;i--) {
                            if (ipAddressTo.get(i).isEmpty()) break;  // break when meet "::"
                            ipAddressTo.set(i+8-ipAddressToSize, ipAddressTo.get(i));
                        }
                        // fill zeros
                        for (int p=i; p<i+9-ipAddressToSize; p++) {
                            ipAddressTo.set(p, "0");
                        }
                    }
                    // convert blocks into int, 2 by 2 ...
                    for (int p=0; p<8; p+=2) {
                        ipRange[p/2] = Integer.parseInt(ipAddressFrom.get(p),16) * 65536 + Integer.parseInt(ipAddressFrom.get(p+1), 16);
                        ipRange[p/2+4] = Integer.parseInt(ipAddressTo.get(p),16) * 65536 + Integer.parseInt(ipAddressTo.get(p+1), 16);
                        list.add(ipRange);  // put it into list
                    }
                } else {
                    // config format error
                    continue;
                }

                // insert into checklist
                if (listType.equalsIgnoreCase("whitelist")) {
                    BungeeAccess.whiteListIp.addAll(list);
                } else if (listType.equalsIgnoreCase("blacklist")) {
                    BungeeAccess.blackListIp.addAll(list);
                }
                //BungeeAccess.checklist.remove(checklistTitle); // remove old list before insert
                //BungeeAccess.checklist.put(checklistTitle, list);
            }
        } else {
            // I have no idea why ppl don't put a correct "objectType" when using this method
            return;
        }


        // set data into dataConfiguration
        //dataConfiguration.set(listType+"-"+objectType, dataSet);
        // save data.yml
        saveData(listType+"-"+objectType, dataSet);
    }
    public static void loadData() {
        /*for (String s: data.keySet()) {
            loadData(s);
        }*/
        loadData("whitelist", "player");
        loadData("whitelist", "ip");
        loadData("blacklist", "player");
        loadData("blacklist", "ip");
    }

    // save changes to data.yml
    public static void saveData() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(dataConfiguration, new File(plugin.getDataFolder(), "data.yml"));
            plugin.getLogger().info("Saved changes to data.yml");
        } catch (IOException e) {
            // The file didn't get saved due to an error
            e.printStackTrace();
        }
    }
    public static void saveData(String path, Object value) {
        dataConfiguration.set(path, value);
        saveData();
    }

    // insert data

    // get storage
    public static String getStorageType() {
        return configConfiguration.getString("storage");
    }

    // determine checklist order
    //public static List<String> getChecklistPriority() {
        //return configConfiguration.getStringList("priority");
    public static void getChecklistPriority() {
        BungeeAccess.checkPriority = configConfiguration.getStringList("priority");
    }

    public static Boolean isUuidCheckEnabled() {
        return configConfiguration.getBoolean("uuid");
    }

    public static String getMessageBlacklistName() {
        return configConfiguration.getString("message.blacklistname");
    }

    public static String getMessageBlacklistUuid() {
        return configConfiguration.getString("message.blacklistuuid");
    }

    public static String getMessageBlacklistIp() {
        return configConfiguration.getString("message.blacklistip");
    }

    public static String getMessageBlacklistAll() {
        return configConfiguration.getString("message.blacklistall");
    }

    // resolve uuid
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
