[![release workflow badge](https://github.com/funniray/waterdog-lobbybalancer/actions/workflows/release.yml/badge.svg)](https://github.com/funniray/waterdog-lobbybalancer/releases/latest)
## Waterdog-LobbyBalancer
WaterdogPE plugin for balancing lobbies

[中文文档](README_zh.md)

Lobby servers are gathered from the WaterdogPE config with the lobby prefix. By default, this is `lobby`.

With the default configuration, lobby1, lobby2, and lobby3 would be detected as lobby servers, but game1 or game2 would not.

You can transfer players to a pseudo-random lobby server by sending a transfer packet with the lobby prefix. By default, `/server lobby` will send them to a pseudo-random lobby server.

### Downloads
Download from [Jenkins CI](https://motci.cn/job/Waterdog-LobbyBalancer/)

### Building
Build and compile with maven using `mvn package`

### Config
```yaml
#Any lobbies must start with this string. Anything after it doesn't matter
#Transfers to this prefix will send a player to a lobby, acting the same as if they join
lobbyprefix: lobby

#Minimum players in a lobby before balancing to another server
#A lobby must have x amount of players before another lobby starts getting players
#Helps your server in theory not look dead if all lobbies barely have players
#By default, lobby1 will fill up until it get to 10 players, then lobby2 will start filling up.
#Once all lobby servers get to the minimum players, then the player will join the server with the least amount of players
#This order is determined by the order of your servers in the WaterdogPE config
minplayers: 10

#Whether to register the /lobby command
#Set to false to disable the lobby command
lobbycommand: true

#Whether to use MOTD query to get real player counts from downstream servers
#Enable this for multi-proxy deployments where each proxy only knows its own players
#When enabled, player counts are fetched directly from servers via UDP MOTD protocol
use-motd-query: false

#MOTD query interval in seconds
motd-query-interval: 30
```

### MOTD Query
In a multi-proxy deployment, each WaterdogPE proxy only knows the players routed through itself. This leads to inaccurate load balancing.

When `use-motd-query` is enabled, the plugin queries each downstream server directly via UDP MOTD protocol to get the real total player count. Servers that fail to respond are considered offline and excluded from balancing.

### Commands/Permissions
| Command          | Permission          | Description                      |
|------------------|---------------------|----------------------------------|
| /\<lobbyprefix\> | lobbybalancer.lobby | Sends executer to a lobby server |
