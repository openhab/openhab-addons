/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mideaac.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mideaac.internal.security.TokenKey;

/**
 * Testing of the {@link MideaACConfigurationTest} Configuration
 *
 * @author Robert Eckhoff - Initial contribution
 */
@NonNullByDefault
public class MideaACConfigurationTest {

    MideaACConfiguration config = new MideaACConfiguration();

    /**
     * Test for valid Configs
     */
    @Test
    public void testValidConfigs() {
        config.ipAddress = "192.168.0.1";
        config.ipPort = "6444";
        config.deviceId = "1234567890";
        assertTrue(config.isValid());
        assertFalse(config.isDiscoveryNeeded());
    }

    /**
     * Test for non-valid configs
     */
    @Test
    public void testnonValidConfigs() {
        config.ipAddress = "192.168.0.1";
        config.ipPort = "";
        config.deviceId = "1234567890";
        assertFalse(config.isValid());
        assertTrue(config.isDiscoveryNeeded());
    }

    /**
     * Test for bad IP configs
     */
    @Test
    public void testBadIpConfigs() {
        config.ipAddress = "192.1680.1";
        config.ipPort = "6444";
        config.deviceId = "1234567890";
        assertTrue(config.isValid());
        assertTrue(config.isDiscoveryNeeded());
    }

    /**
     * Test to return cloud provider
     */
    @Test
    public void testCloudProvider() {
        config.cloud = "NetHome Plus";
        assertEquals(config.cloud, "NetHome Plus");
    }

    /**
     * Test to return token and key pair
     */
    @Test
    public void testTokenKey() {
        config.token = "D24046B597DB9C8A7CA029660BC606F3FD7EBF12693E73B2EF1FFE4C3B7CA00C824E408C9F3CE972CC0D3F8250AD79D0E67B101B47AC2DD84B396E52EA05193F";
        config.key = "97c65a4eed4f49fda06a1a51d5cbd61d2c9b81d103ca4ca689f352a07a16fae6";
        TokenKey tokenKey = new TokenKey(config.token, config.key);
        String tokenTest = tokenKey.token();
        String keyTest = tokenKey.key();
        assertEquals(config.token, tokenTest);
        assertEquals(config.key, keyTest);
    }
}
