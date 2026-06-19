package com.lei6393.trouve.server.dispatch;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link GatewayDrain} 优雅停机协调回归测试（v2.x）。
 *
 * @author trouve
 */
public class GatewayDrainTest {

    @After
    public void reset() {
        GatewayDrain.resetForTest();
    }

    @Test
    public void drainingFlag_toggles() {
        assertFalse(GatewayDrain.isDraining());
        GatewayDrain.startDraining();
        assertTrue(GatewayDrain.isDraining());
    }

    @Test
    public void awaitDrain_zeroInFlight_returnsImmediately() {
        assertEquals(0, GatewayDrain.inFlight());
        assertTrue(GatewayDrain.awaitDrain(200));
    }

    @Test
    public void awaitDrain_timesOutWhileInFlight_thenSucceedsAfterExit() {
        GatewayDrain.enter();
        assertEquals(1, GatewayDrain.inFlight());

        long start = System.currentTimeMillis();
        assertFalse(GatewayDrain.awaitDrain(120));
        assertTrue(System.currentTimeMillis() - start >= 100);

        GatewayDrain.exit();
        assertEquals(0, GatewayDrain.inFlight());
        assertTrue(GatewayDrain.awaitDrain(200));
    }
}
