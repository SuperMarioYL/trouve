package com.lei6393.trouve.core.data.instance;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author leiyu
 * @date 2022/6/8 14:58
 */
public class InstanceTest {

    @Test
    public void hashcodeTest() {
        Instance instance1 = new Instance();
        instance1.setIp("127.0.0.1");
        instance1.setPort(8080);
        instance1.setInstanceId("xxxxx");
        instance1.setServiceName("service");
        instance1.setGroupName("afsfsfsdf");

        Instance instance2 = new Instance();
        instance2.setIp("127.0.0.1");
        instance2.setPort(8080);
        instance2.setInstanceId("xxxxx");
        instance2.setServiceName("service");
        instance2.setGroupName("afsfsfsdf");

        Assert.assertTrue(instance1.equals(instance2));

        Assert.assertTrue(instance1.hashCode() == instance2.hashCode());
    }
}
