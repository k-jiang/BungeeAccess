package net.kjiang.bungeeaccess;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by kjiang on 2017/1/18.
 */
public class BungeeAccessCommand extends Command {
    private static Plugin plugin;

    // constructor
    public BungeeAccessCommand(BungeeAccess _plugin) {
        // setting main commands and permission
        super("bga", _plugin.getDescription().getName().toLowerCase()+".admin", _plugin.getDescription().getName().toLowerCase(), "bungeea");
        plugin = _plugin;
    }

    // executor
    @Override
    public void execute(CommandSender sender, String[] args) {
        //sender.sendMessage(new ComponentBuilder("You have issued command: " + args.toString()).color(ChatColor.DARK_GREEN).create());
        if (args.length>0) {
            switch (args[0]) {
                case "wl":
                case "whitelist":
                    if (args.length>1) {
                        switch (args[1]) {
                            case "add":
                                if (args.length>2) {
                                    // support adding multiple elements at the same time
                                    Map<String, String> dataSet = new HashMap<String, String>();
                                    for (Integer i=2; i<args.length; i++){
                                        String element = args[i];
                                        if (element.matches("^\\d{8}-\\d{4}-\\d{4}-\\d{4}-\\d{12}$")) {
                                            // uuid
                                            dataSet.put(element, "whitelistuuid");
                                            //ConfigHandler.insertData("whitelistuuid", Arrays.asList(element));
                                            //sender.sendMessage(new ComponentBuilder("uuid " + element + " is added into whitelist.").create());
                                        } else if (element.matches("^[a-zA-Z0-9_]+$")) {
                                            // name
                                            dataSet.put(element, "whitelistplayer");
                                            //ConfigHandler.insertData("whitelistplayer", Arrays.asList(element));
                                            //sender.sendMessage(new ComponentBuilder("player " + element + " is added into whitelist.").create());
                                        } else if (element.matches("^\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}(?:\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3})?$")) {
                                            // ip address or ip range
                                            dataSet.put(element, "whitelistip");
                                            //ConfigHandler.insertData("whitelistip", Arrays.asList(element));
                                            //sender.sendMessage(new ComponentBuilder("ip/ip-range " + element + " is added into whitelist.").create());
                                        } else {
                                            sender.sendMessage(new ComponentBuilder("Format error. Please specified playername/uuid/ip you want to add.\neg: /bga wl add a_name").create());
                                        }
                                    }
                                    sender.sendMessage(new ComponentBuilder("player/uuid/ip are added into whitelist.").create());
                                    ConfigHandler.insertData(dataSet);
                                } else {
                                    sender.sendMessage(new ComponentBuilder("please specified playername/uuid/ip you want to add.\neg: /bga wl add a_name").create());
                                }
                                break;
                            case "del":
                                break;
                            case "list":
                            default:
                                // show list
                                String list;
                                if (args.length>2) {
                                    if (args[2].getClass().isEnum()) ;
                                    list = ConfigHandler.getDataListString("whitelist", "player", (Integer.valueOf(args[2])-1)*10, Integer.valueOf(args[2])*10);
                                } else {
                                    list = ConfigHandler.getDataListString("whitelist", "player", 0, 10);
                                }
                                sender.sendMessage(new ComponentBuilder("whitelisted player: " + list + " ...\n(there might be more after, use \"/bga whitelist list [page]\" to see more.)").create());
                        }
                    } else {

                    }
                    break;
                case "blacklist":

                    break;
                case "version":
                case "info":
                default:
                    this.showInfo(sender);  // name, version
                    this.showInfoDetails(sender);   // show settings etc
            }
        } else {
            // if no args, show simple info
            this.showInfo(sender);
        }
    }

    private void showInfo(CommandSender sender) {
        // name and version
        sender.sendMessage(new ComponentBuilder(plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion()+" by kjiang.").color(ChatColor.GOLD).create());
    }

    private void showInfoDetails(CommandSender sender) {
        // show storage type
        sender.sendMessage(new ComponentBuilder("Storage: ").append(ConfigHandler.getStorageType()).color(ChatColor.YELLOW).create());
        // list priority settings
        sender.sendMessage(new ComponentBuilder("Priority:")
                .append("\n  Highest - ").append(ConfigHandler.getPriority(0)).color(ChatColor.WHITE)
                .append("\n          - ").append(ConfigHandler.getPriority(1)).color(ChatColor.GRAY)
                .append("\n          - ").append(ConfigHandler.getPriority(2)).color(ChatColor.GRAY)
                .append("\n  Lowest  - ").append(ConfigHandler.getPriority(3)).color(ChatColor.DARK_GRAY)
                .create());
    }
}
