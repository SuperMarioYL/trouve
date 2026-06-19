package com.lei6393.trouve.client.sender;

import com.lei6393.trouve.client.bean.ServerAddressParam;
import com.lei6393.trouve.core.connection.CenterURI;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 客户端注册中心地址故障转移回归测试（v1.2）。
 * <p>
 * 修复前 {@code realAddress()} 用 {@code findAny().get()} 钉死单一地址、无故障转移；
 * 修复后改为轮询游标，重试会切换到下一个地址。
 *
 * @author trouve
 */
public class AbstractSenderFailoverTest {

    /**
     * 记录每次尝试命中的注册中心 host，并恒定返回 false 以触发重试。
     */
    static class RecordingSender extends AbstractSender<String> {
        final List<String> hosts = new ArrayList<>();

        RecordingSender(List<ServerAddressParam> addressParams) {
            super(addressParams, CenterURI.INSTANCE);
        }

        @Override
        protected boolean realRegister(String data) {
            hosts.add(realUrl().host());
            return false;
        }

        @Override
        protected boolean realUpdate(String data) {
            hosts.add(realUrl().host());
            return false;
        }

        @Override
        protected boolean realRemove(String data) {
            hosts.add(realUrl().host());
            return false;
        }
    }

    private ServerAddressParam addr(String host) {
        return ServerAddressParam.of("http://" + host + ":8888");
    }

    @Test
    public void retry_rotatesAcrossAddresses() {
        List<ServerAddressParam> addressParams = new ArrayList<>();
        addressParams.add(addr("10.0.0.1"));
        addressParams.add(addr("10.0.0.2"));

        RecordingSender sender = new RecordingSender(addressParams);
        sender.setRetryCount(2);

        boolean ok = sender.register("payload");

        // 两次尝试都返回 false
        assertFalse(ok);
        assertEquals(2, sender.hosts.size());

        // 关键断言：两次尝试命中两个不同地址，证明重试时发生了故障转移
        Set<String> distinct = new LinkedHashSet<>(sender.hosts);
        assertEquals(2, distinct.size());
        assertTrue(distinct.contains("10.0.0.1"));
        assertTrue(distinct.contains("10.0.0.2"));
    }
}
