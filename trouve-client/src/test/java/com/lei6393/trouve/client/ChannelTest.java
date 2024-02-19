package com.lei6393.trouve.client;

import org.junit.Test;

/**
 * @author leiyu
 * @date 2022/5/12 11:18
 */
public class ChannelTest {

    @Test
    public void test() throws Exception {
//        new Thread(() -> {
//            UDPSender channel = new UDPSender("localhost", 8088);
//            for (int i = 0; i < 10; i++) {
//                channel.push(Connection.INSTANCE, new HealthChecker(5, "127.0.0.1:9090"));
//                try {
//                    Thread.sleep(1000);
//                } catch (Exception e) {
//                    // ignore
//                }
//            }
//        }).start();
//
//        new Thread(() -> {
//            try (DatagramSocket socket = new DatagramSocket(new InetSocketAddress("localhost", 8088))) {
//                while (true) {
//                    byte[] bytes = new byte[60 * 1024];
//                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
//                    socket.receive(packet);
//                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
//                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
//                    HealthChecker message = (HealthChecker) objectInputStream.readObject();
//                }
//            } catch (Exception e) {
//
//            }
//        }).start();
//
//        Thread.sleep(10000);
    }
}
