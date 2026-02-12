package com.funniray.lobbybalancer.motd;

import com.funniray.lobbybalancer.LobbyBalancer;
import com.funniray.lobbybalancer.Utils;
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定期查询所有大厅服务器的 MOTD 并缓存在线人数。
 */
public class MotdPlayerCounter {

    private final ConcurrentHashMap<String, Integer> playerCounts = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "MotdPlayerCounter");
        t.setDaemon(true);
        return t;
    });

    /**
     * 启动定时查询任务。
     *
     * @param intervalSeconds 查询间隔（秒）
     */
    public void start(int intervalSeconds) {
        scheduler.scheduleAtFixedRate(this::queryAll, 0, intervalSeconds, TimeUnit.SECONDS);
        LobbyBalancer.getInstance().getLogger().info("MOTD player counter started, interval: " + intervalSeconds + "s");
    }

    private void queryAll() {
        try {
            for (ServerInfo server : ProxyServer.getInstance().getServers()) {
                if (!Utils.isServerLobby(server)) {
                    continue;
                }
                InetSocketAddress address = server.getAddress();
                int count = MotdQuery.queryPlayerCount(address);
                playerCounts.put(server.getServerName(), count);
                LobbyBalancer.getInstance().getLogger().debug(
                        "MOTD query " + server.getServerName() + " (" + address + "): " + count
                );
            }
        } catch (Exception e) {
            LobbyBalancer.getInstance().getLogger().error("Error during MOTD query", e);
        }
    }

    /**
     * 获取缓存的服务器在线人数。
     *
     * @param serverName 服务器名称
     * @return 在线人数，无缓存时返回 -1
     */
    public int getPlayerCount(String serverName) {
        return playerCounts.getOrDefault(serverName, -1);
    }

    /**
     * 关闭定时查询任务。
     */
    public void shutdown() {
        scheduler.shutdownNow();
        LobbyBalancer.getInstance().getLogger().info("MOTD player counter stopped");
    }
}
