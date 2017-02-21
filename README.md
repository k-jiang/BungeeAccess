# BungeeAccess
An advanced white-list / blacklist plugin for BungeeCord.

## Description
This is a perfect white-list / black-list solution for your network, a BungeeCord-side plugin supports both player names and IPs. It has priority setting built in, so without any complicated setting, you can customize how does the white-list or black-list should be applied in order. It also supports IPv6, UUID, and (in the future) MySQL.  
If you are looking for an better all-in-one white-list / black-list , or you just sick with installing multiple plugins in each of your Spigot back-ends, then you may have to try this one.

** I have my personal life and works so I may not have too many time on checking every single bug report in the forum, so PLEASE PLEASE PLEASE submit your bugs or suggestion on [GitHub issue](https://github.com/k-jiang/BungeeAccess/issues). Still this is my first plugin, any other comments are welcomed.

## Features
- Whitelist and Blacklist on players / IP
- Customizable priority
- IP ranges supports, eg `192.0.2.0-192.0.2.15`
- IPv6
- UUID
- ~~MySQL~~ (Not implemented yet)
- ~~Permissions~~ (Planed)
- Better efficiency

## Installation and Settings
1. Drop the BungeeAccess.jar into your **BungeeCord Plugins folder**. Then restart your **BungeeCord**. You don't need to restart any of your Spigot / CraftBukkit back-ends.
2. Now you should find a `BungeeAccess` folder under `plugins`. Open and modify `config.yml` and `data.yml` accordingly.
3. Restart your BungeeCord again.

## Configurations
Most of config sections are self explained. `config.yml` contains main settings and `data.yml` stores Player / IP white-lists and black-lists.  
`config.yml`:
```
# BungeeAccess
# Control player accesses to BungeeCord instances.
# By: kjiang

# Enable online mode.
uuid: true

# Checking Priority
# This plugin will check blacklist / whitelist follow by the order listed below. eg, if "blacklistip" is listed on top of "whitelistplayer", any player join with blacklisted-ip will be kick even their name is on the whitelist-name.
# To modify the actural blacklist / whitelist, see data.yml
# "blacklist-all" means the player shall be kick if they does not meet the whitelist reqirement. This check should be at the end of priority list. You can remove this check if you don't want to have "whitelist mode" switched on.
# It is also suggested that you should remove any checks you don't need for a better efficiency.
priority:
- blacklist-ip
- blacklist-player
- whitelist-player
- whitelist-ip
- blacklist-all

# Custom messages when a player is kicked due to blacklist
message:
  blacklistname: "Your name is blacklisted on this server."
  blacklistuuid: "You are blacklisted on this server."
  blacklistip: "Your IP is blacklisted on this server."
  blacklistall: "Whitelist is enabled on this server. Please contact server admins to have your name on the list."

# Storage Type
# Right now it only supports "file" (data stored in data.yml) or "mysql".
storage: file

# MySQL Settings
db-host: localhost
db-port: 3306
db-user: root
db-pass: 123456
db-database: bungeeaccess
db-table-prefix: ba_

```
`data.yml`:
```
# BungeeAccess
# This file used to store whitelists and blacklists.

# Whitelisted players
# UUIDs are optional. If you set "uuid" to true, BungeeAccess will fetch UUID for you.
whitelist-player:
  - Notch|069a79f4-44e9-4726-a5be-fca90e38aaf5
  - a_good_player_name|00000000-0000-4000-8000-000000000000
  - another_good_player_name|0
  - put_your_name_here|0

# Whitelisted ip addresses
# Supports ip address range
whitelist-ip:
  - 127.0.0.1
  - 127.0.1.0-127.0.1.255

# Blacklisted players
blacklist-player:
  - a_bad_player|0

# Blacklisted ip addresses
blacklist-ip:
  - 192.0.2.0-192.0.2.31
```

### * About "priority"
As explained above, checking could be turned off if you don't need it. For example:
```
priority:
- whitelist-player
- blacklist-ip
```
That means when player join your network, the plugin will check if this player is on the whitelist first, then it will lookup player's IP address. Consider a player with its name on the whitelist and IP on the blacklist at the same time, in this player will still be allowed to join the server because plugin checks it name first and he / she fits the list.  
And yes, the `blacklist-all` can be removed if you want "innocent" players be able to join your server.

### * About UUID in "data.yml"
For player name, you can just put it name on the list, like:
```
  - Notch
  - a_good_player_name
  - another_good_player_name
  - put_your_name_here
```
The plugin will resolve UUID automatically for you if you have `uuid: true` on your config.yml.  

### * About IP address and IP ranges
You can put ip ranges as you want, formats as `[ip begin]-[ip end]` e.g. `127.0.0.1-127.0.0.255`. Using IP ranges instead of individual IPs could boost performance. It is very useful when you want to block player join from certain geographic regions. You could visit [he.net](http://bgp.he.net) for geolocation ip block search.  
Noticed that the ip range does not support CIDR format (e.g. 127.0.0.0/24) yet. Check [here](http://www.techzoom.net/tools/IPAddressCalculator) if you want to convert from CIDR to IP ranges.  
For better efficiency, single IP address WILL be converted into something like `127.0.0.1-127.0.0.1`. So try not to touch it and forget about your OCD :p

## Commands
(Sorry it is not implemented yet. Future edition will include player and IP add / removal functions.)

## Bugs and Supports
All bugs and features requests should only be submitted on the [GitHub issue Page](https://github.com/k-jiang/BungeeAccess/issues).
Please do NOT post any bugs on the comment section. Otherwise I will just simply ignore you.

## TO-DOs
- support CIDR
- MySQL
- basic commands
- reload command
- code clean up

## License
The plugin is licensed under [Apache-2.0](https://github.com/k-jiang/BungeeAccess/blob/master/LICENSE). Terms and conditions are applied.
