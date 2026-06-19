package com.lei6393.trouve.server.auth;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link RegistryAuthenticator} 控制面鉴权回归测试（v2.1）。
 *
 * @author trouve
 */
public class RegistryAuthenticatorTest {

    @After
    public void tearDown() {
        RegistryAuthenticator.configure(null);
    }

    @Test
    public void disabledByDefault_allowsAll() {
        RegistryAuthenticator.configure(null);
        assertFalse(RegistryAuthenticator.isEnabled());
        assertTrue(RegistryAuthenticator.authenticateToken(null));
        assertTrue(RegistryAuthenticator.authenticateToken("anything"));
    }

    @Test
    public void blankToken_disablesAuth() {
        RegistryAuthenticator.configure("   ");
        assertFalse(RegistryAuthenticator.isEnabled());
        assertTrue(RegistryAuthenticator.authenticateToken(null));
    }

    @Test
    public void configuredToken_requiresMatch() {
        RegistryAuthenticator.configure("s3cret");
        assertTrue(RegistryAuthenticator.isEnabled());
        assertTrue(RegistryAuthenticator.authenticateToken("s3cret"));
        assertFalse(RegistryAuthenticator.authenticateToken("wrong"));
        assertFalse(RegistryAuthenticator.authenticateToken(null));
        assertFalse(RegistryAuthenticator.authenticateToken("s3cre"));   // 长度不同
        assertFalse(RegistryAuthenticator.authenticateToken("s3cretX")); // 长度不同
    }
}
