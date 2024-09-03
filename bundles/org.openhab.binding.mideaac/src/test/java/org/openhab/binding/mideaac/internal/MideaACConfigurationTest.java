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

    MideaACConfiguration mideaACConfiguration = new MideaACConfiguration();

    @Test
    public void testValidConfigs() {
        String ip = "192.168.0.1";
        String port = "6444";
        String deviceId = "1234567890";
        mideaACConfiguration.setIpAddress(ip);
        mideaACConfiguration.setIpPort(port);
        mideaACConfiguration.setDeviceId(deviceId);
        String ipTest = "";
        String portTest = "";
        String idTest = "";
        ipTest = mideaACConfiguration.getIpAddress();
        portTest = mideaACConfiguration.getIpPort();
        idTest = mideaACConfiguration.getDeviceId();
        assertEquals(ip, ipTest);
        assertEquals(port, portTest);
        assertEquals(deviceId, idTest);
        assertTrue(mideaACConfiguration.isValid());
        assertFalse(mideaACConfiguration.isDiscoveryNeeded());
    }

    @Test
    public void testnonValidConfigs() {
        String ip = "192.168.0.1";
        String port = "";
        String deviceId = "1234567890";
        mideaACConfiguration.setIpAddress(ip);
        mideaACConfiguration.setIpPort(port);
        mideaACConfiguration.setDeviceId(deviceId);
        String ipTest = "";
        String portTest = "";
        String idTest = "";
        ipTest = mideaACConfiguration.getIpAddress();
        portTest = mideaACConfiguration.getIpPort();
        idTest = mideaACConfiguration.getDeviceId();
        assertEquals(ip, ipTest);
        assertEquals(port, portTest);
        assertEquals(deviceId, idTest);
        assertFalse(mideaACConfiguration.isValid());
        assertTrue(mideaACConfiguration.isDiscoveryNeeded());
    }

    @Test
    public void testBadIpConfigs() {
        String ip = "192.1680.1";
        String port = "6444";
        String deviceId = "1234567890";
        mideaACConfiguration.setIpAddress(ip);
        mideaACConfiguration.setIpPort(port);
        mideaACConfiguration.setDeviceId(deviceId);
        String ipTest = "";
        String portTest = "";
        String idTest = "";
        ipTest = mideaACConfiguration.getIpAddress();
        portTest = mideaACConfiguration.getIpPort();
        idTest = mideaACConfiguration.getDeviceId();
        assertEquals(ip, ipTest);
        assertEquals(port, portTest);
        assertEquals(deviceId, idTest);
        assertTrue(mideaACConfiguration.isValid());
        assertTrue(mideaACConfiguration.isDiscoveryNeeded());
    }

    @Test
    public void testCloudProvider() {
        String cloudProvider = "NetHome Plus";
        mideaACConfiguration.setCloud(cloudProvider);
        String cloudTest = "";
        cloudTest = mideaACConfiguration.getCloud();
        assertEquals(cloudProvider, cloudTest);
    }

    @Test
    public void testTokenKey() {
        String token = "D24046B597DB9C8A7CA029660BC606F3FD7EBF12693E73B2EF1FFE4C3B7CA00C824E408C9F3CE972CC0D3F8250AD79D0E67B101B47AC2DD84B396E52EA05193F";
        String key = "97c65a4eed4f49fda06a1a51d5cbd61d2c9b81d103ca4ca689f352a07a16fae6";
        String tokenTest = "";
        String keyTest = "";
        String tokenTest1 = "";
        String keyTest1 = "";
        TokenKey tokenKey = new TokenKey(token, key);
        mideaACConfiguration.setToken(token);
        mideaACConfiguration.setKey(key);
        tokenTest = tokenKey.getToken();
        keyTest = tokenKey.getKey();
        tokenTest1 = mideaACConfiguration.getToken();
        keyTest1 = mideaACConfiguration.getKey();
        assertEquals(token, tokenTest);
        assertEquals(key, keyTest);
        assertEquals(token, tokenTest1);
        assertEquals(key, keyTest1);
    }
}
