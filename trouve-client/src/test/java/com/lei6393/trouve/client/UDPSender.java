package com.lei6393.trouve.client;

import com.lei6393.trouve.core.connection.CenterURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * @author yulei
 * @date 2022/5/12 11:07
 */
public class UDPSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(UDPSender.class);

    private InetSocketAddress address;

    public UDPSender(String hostName, int port) {
        this.address = new InetSocketAddress(hostName, port);
    }

    public void push(CenterURI centerURI, Object data) {
        if (data instanceof byte[]) {
            if (((byte[]) data).length > 60 * 1024) {
                throw new RuntimeException("heart rate package is too long！");
            }
            try (DatagramSocket socket = new DatagramSocket()) {
                DatagramPacket packet = new DatagramPacket((byte[]) data, ((byte[]) data).length, address);
                socket.send(packet);
            } catch (Exception e) {
                LOGGER.error("udp channel push package error!", e);
            }
        }

    }
}
