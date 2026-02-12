/*
 *    LobbyBalancer - WaterdogPE plugin for balancing lobbies
 *    Copyright (C) 2021  Funniray
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    I am available for any questions/requests: funniray10@gmail.com
 */

package com.funniray.lobbybalancer;

import com.funniray.lobbybalancer.motd.MotdPlayerCounter;
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.network.serverinfo.BedrockServerInfo;
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class Utils {

    public static @Nullable ServerInfo findServer(@Nullable ServerInfo oldServer) {
        //We only want lobby servers that are online

        LobbyBalancer.getInstance().getLogger().debug(" >>> Got request to join server");

        MotdPlayerCounter motdCounter = LobbyBalancer.getInstance().getMotdPlayerCounter();

        List<ServerInfo> lobbies = ProxyServer.getInstance().getServers()
                .stream()
                .filter(Utils::isServerLobby)
                .filter((serverInfo -> !serverInfo.equals(oldServer)))
                .filter(serverInfo -> {
                    if (motdCounter != null) {
                        return motdCounter.getPlayerCount(serverInfo.getServerName()) >= 0;
                    }
                    return true;
                })
                .toList();

        //If a server has less than the minimum players, prioritize them
        for(ServerInfo lobby : lobbies) {
            if (getPlayerCount(lobby) < LobbyBalancer.getInstance().getConfig().getInt("minplayers")) {
                LobbyBalancer.getInstance().getLogger().debug(" >>> Decided server based off of minimum players");
                return lobby;
            }
        }

        //Otherwise, return the server with the least amount of players
        List<ServerInfo> sortedLobbies = lobbies.stream()
                .sorted(Comparator.comparingInt(Utils::getPlayerCount))
                .toList();

        if (sortedLobbies.isEmpty()) {
            LobbyBalancer.getInstance().getLogger().fatal("Failed to find a valid server to join");
            return null;
        }

        LobbyBalancer.getInstance().getLogger().debug(" >>> Decided server based off of the least amount of players");

        return sortedLobbies.get(0);
    }

    /**
     * 获取服务器在线人数。若 MOTD 查询启用则使用 MOTD 数据，否则回退到代理内部记录。
     */
    public static int getPlayerCount(ServerInfo server) {
        MotdPlayerCounter motdCounter = LobbyBalancer.getInstance().getMotdPlayerCounter();
        if (motdCounter != null) {
            int count = motdCounter.getPlayerCount(server.getServerName());
            if (count >= 0) {
                return count;
            }
        }
        return server.getPlayers().size();
    }

    public static void createLobby() {
        String lobbyPrefix = LobbyBalancer.getInstance().getConfig().getString("lobbyprefix");

        //Ensure a server with the lobby prefix doesn't already exist
        if (ProxyServer.getInstance().getServerInfo(lobbyPrefix) != null) {
            LobbyBalancer.getInstance().getLogger().fatal("A server with the name "+lobbyPrefix+" already exists. Players won't be able to join this server, as any attempts to join this server will make them join a pseudo-random lobby server.");
            return;
        }

        List<ServerInfo> lobbyServers = ProxyServer.getInstance().getServers()
                .stream()
                .filter(Utils::isServerLobby)
                .toList();

        if (lobbyServers.isEmpty()) {
            LobbyBalancer.getInstance().getLogger().fatal("No lobby servers found with prefix: " + lobbyPrefix);
            return;
        }

        //We have to put in a random address
        ServerInfo baseInfo = lobbyServers.get(0);

        ServerInfo baseLobby = new BedrockServerInfo(lobbyPrefix,baseInfo.getAddress(), null);
        ProxyServer.getInstance().registerServerInfo(baseLobby);
    }

    //Get all lobby servers as long as their name isn't exactly the lobby prefix
    public static boolean isServerLobby(ServerInfo server) {
        String lobbyPrefix = LobbyBalancer.getInstance().getConfig().getString("lobbyprefix");
        return server.getServerName().startsWith(lobbyPrefix) && !server.getServerName().equals(lobbyPrefix);
    }
}
