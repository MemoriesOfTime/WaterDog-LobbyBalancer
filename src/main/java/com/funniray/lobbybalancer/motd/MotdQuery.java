package com.funniray.lobbybalancer.motd;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * MOTD UDP 查询工具类，向指定地址发送 MOTD 数据包并解析返回的在线人数。
 */
public class MotdQuery {

    private static final int TIMEOUT_MS = 3000;

    // 29 字节的 MOTD 查询数据包
    private static final byte[] QUERY_PACKET = new byte[]{
            0x01, // Packet ID
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Timestamp
            0x00, (byte) 0xFF, (byte) 0xFF, 0x00, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
            (byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD,
            0x12, 0x34, 0x56, 0x78, // Client GUID
            0x00, 0x00, 0x00, 0x00, 0x00 // Padding
    };

    /**
     * 查询指定地址的 MOTD 并返回在线人数。
     *
     * @param address 子服地址
     * @return 在线人数，查询失败返回 -1
     */
    public static int queryPlayerCount(InetSocketAddress address) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);

            DatagramPacket sendPacket = new DatagramPacket(
                    QUERY_PACKET, QUERY_PACKET.length, address
            );
            socket.send(sendPacket);

            byte[] buffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(receivePacket);

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            String[] data = response.split(";");
            // data[4] = 在线人数
            if (data.length > 4) {
                return Integer.parseInt(data[4]);
            }
        } catch (Exception ignored) {
        }
        return -1;
    }
}
