package net.kjiang.bungeeaccess;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by inventor on 2017/1/19.
 */
public class PlayerJoinListener implements Listener {
    private static Plugin plugin;

    // constructor
    public PlayerJoinListener(BungeeAccess _plugin) {
        plugin = _plugin;   // get plugin
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(LoginEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // get playerIpString
        String playerIpString = event.getConnection().getAddress().getHostString();
        Boolean playerIpIsV6 = playerIpString.contains(":");
        // convert playerIp
        int[] playerIp = new int[4];
        if (!playerIpIsV6) {
            String[] ip = playerIpString.split("\\.");
            playerIp[0] = playerIp[1] = playerIp[2] = 0;
            playerIp[3] = Integer.parseInt(ip[0])*16777216+Integer.parseInt(ip[1])*65536+Integer.parseInt(ip[2])*256+Integer.parseInt(ip[3]);
        } else {
            // ipv6
            // fill up ::
            if ( playerIpString.matches("::") ) {
                String fill = "0";
                for (int i = 0; i < 7 - playerIpString.split(":").length; i++) {
                    fill += ":0";
                }
                playerIpString.replace("::", fill);
            }
            // get ip sections
            String[] ip = playerIpString.split(":");
            playerIp[0] = Integer.parseInt(ip[0],16) * 65536 + Integer.parseInt(ip[1], 16);
            playerIp[1] = Integer.parseInt(ip[2],16) * 65536 + Integer.parseInt(ip[3], 16);
            playerIp[2] = Integer.parseInt(ip[4],16) * 65536 + Integer.parseInt(ip[5], 16);
            playerIp[3] = Integer.parseInt(ip[6],16) * 65536 + Integer.parseInt(ip[7], 16);
        }
        // get playerName
        String playerName = event.getConnection().getName();
        // get playerUuid
        String playerUuid = event.getConnection().getUniqueId().toString();

        //plugin.getLogger().info("[debug] Player "+playerName+" try to connect with uuid "+playerUuid+" and ip "+playerIpString);

        // search white/black list according to priority set on data.yml
        String uuid;
        for (String s: BungeeAccess.checkPriority) {
            plugin.getLogger().info("[debug] checking "+s);
            switch (s.toLowerCase()) {
                case "whitelist-player":
                    uuid = BungeeAccess.whiteListPlayer.get(playerName);
                    if (uuid != null) {
                        if (Config.isUuidCheckEnabled()) {
                            if (uuid.equals("0")) {
                                // offline record
                                //plugin.getLogger().info("[debug]player name is whitelisted.");
                                return; // terminate checking
                            } else if (uuid.equalsIgnoreCase(playerUuid) || BungeeAccess.whiteListPlayer.containsValue(playerUuid)) {
                                // online record
                                //plugin.getLogger().info("[debug]player UUID is whitelisted.");
                                return;
                            }
                        }
                        //plugin.getLogger().info("[debug]player is whitelisted.");
                        return;
                    }
                    break;
                case "whitelist-ip":
                    for (int[] i: BungeeAccess.whiteListIp) {
                        //plugin.getLogger().info("white list ip range: "+Integer.toString(i[0])+" "+Integer.toString(i[1])+" "+Integer.toString(i[2])+" "+Integer.toString(i[4])+" "+Integer.toString(i[5])+" "+Integer.toString(i[6])+" "+Integer.toString(i[7]));
                        for (int j=0; j<4; j++) {
                            if (i[j]>playerIp[j] || i[j+4]<playerIp[j]) {
                                //plugin.getLogger().info("ip break");
                                break;
                            } else if (j==3) {
                                plugin.getLogger().info("player IP is whitelisted. Skip checking");
                                return;
                            }
                        }
                    }
                    break;
                case "blacklist-player":
                    uuid = BungeeAccess.blackListPlayer.get(playerName);
                    if (uuid != null) {
                        if (Config.isUuidCheckEnabled()) {
                            if (uuid.equals("0")) {
                                // offline record
                                plugin.getLogger().info("player kicked due to blacklisted name.");
                                event.getConnection().disconnect(new TextComponent(Config.getMessageBlacklistName()));
                                return;
                            } else if (uuid.equalsIgnoreCase(playerUuid) || BungeeAccess.blackListPlayer.containsValue(playerUuid)) {
                                // online record
                                // player is on blacklist and the name record matched
                                // or maybe he changed his name, so we search the entire list
                                plugin.getLogger().info("player kicked due to blacklisted UUID.");
                                event.getConnection().disconnect(new TextComponent(Config.getMessageBlacklistUuid()));
                                return;
                            }
                        }
                        plugin.getLogger().info("player kicked due to blacklisted name.");
                        event.getConnection().disconnect(new TextComponent(Config.getMessageBlacklistName()));
                        return;
                    }
                    break;
                case "blacklist-ip":
                    //plugin.getLogger().info("player ip: "+Integer.toString(playerIp[0])+" "+Integer.toString(playerIp[1])+" "+Integer.toString(playerIp[2])+" "+Integer.toString(playerIp[3]));
                    for (int[] i: BungeeAccess.blackListIp) {
                        //plugin.getLogger().info("blacklisted ip range: "+Integer.toString(i[0])+" "+Integer.toString(i[1])+" "+Integer.toString(i[2])+" "+Integer.toString(i[4])+" "+Integer.toString(i[5])+" "+Integer.toString(i[6])+" "+Integer.toString(i[7]));
                        for (int j=0; j<4; j++) {
                            //plugin.getLogger().info("player"+Integer.toString(playerIp[j]));
                            //plugin.getLogger().info("from"+Integer.toString(i[j]));
                            //plugin.getLogger().info("to"+Integer.toString(i[j+4]));

                            if (i[j]>playerIp[j] || i[j+4]<playerIp[j]) {
                                //plugin.getLogger().info("ip break");
                                break;
                            } else if (j==3) {
                                plugin.getLogger().info("player kicked due to blacklisted IP.");
                                event.getConnection().disconnect(new TextComponent(Config.getMessageBlacklistIp()));
                                return;
                            }
                        }
                    }
                    break;
                case "blacklist-all":
                    plugin.getLogger().info("player kicked due to whitelist mode.");
                    event.getConnection().disconnect(new TextComponent(Config.getMessageBlacklistAll()));
                    return;
            }
        }

        return;
    }

}
