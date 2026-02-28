/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mideaac.internal.Utils;
import org.openhab.binding.mideaac.internal.cloud.CloudProvider;

import com.google.gson.JsonObject;

/**
 * The {@link SecurityTest} tests methods and compares
 * them to the expected result with sample data.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class SecurityTest {

    @Test
    public void testGetUdpId() {
        // Cloud Provider has no effect. Based on the deviceId
        CloudProvider cloudProvider = new CloudProvider("", "", "", "", "", "", "", "");
        Security security = new Security(cloudProvider);

        String deviceId = "151749305185620";
        long deviceIdAsInteger = Long.valueOf(deviceId);
        byte[] sixByteArray = Utils.toIntTo6ByteArray(deviceIdAsInteger, ByteOrder.BIG_ENDIAN);
        String udpid = security.getUdpId(sixByteArray);
        byte[] expectedArray = new byte[] { -118, 3, -29, 110, 53, 84 };
        assertArrayEquals(expectedArray, sixByteArray);
        assertEquals("c52a10094ba5dc9866cdf657606d4bbd", udpid);
    }

    @Test
    public void testGetUdpIdLittle() {
        // Cloud Provider has no effect. Based on the deviceId
        CloudProvider cloudProvider = new CloudProvider("", "", "", "", "", "", "", "");
        Security security = new Security(cloudProvider);

        String deviceId = "151732605161920";
        long deviceIdAsInteger = Long.valueOf(deviceId);
        byte[] sixByteArray = Utils.toIntTo6ByteArray(deviceIdAsInteger, ByteOrder.LITTLE_ENDIAN);
        String udpid = security.getUdpId(sixByteArray);
        byte[] expectedArray = new byte[] { -64, 17, 8, 0, 0, -118 };
        assertArrayEquals(expectedArray, sixByteArray);
        assertEquals("1a795626332686b426df2939df3e9e3a", udpid);
    }

    @Test
    public void testencryptIamPassword() {
        // Cloud provider appkey is used to encrypt
        CloudProvider cloudProvider = new CloudProvider("MSmartHome", "ac21b9f9cbfe4ca5a88562ef25e2b768", "", "", "",
                "", "", "");
        Security security = new Security(cloudProvider);

        String loginId = "39c5f83c-63d0-4b4b-8f44-2823699857cd";
        String password = "mYPaSsWoRd";
        String encryptedPassword = security.encryptIamPassword(loginId, password);
        assertEquals("e1ce0ff005e35c2a4832afba5310ee3f265663319146ba0a2e35974eef54d08a", encryptedPassword);
    }

    @Test
    public void testencryptPassword() {
        // Cloud provider appkey is used to encrypt
        CloudProvider cloudProvider = new CloudProvider("NetHome Plus", "3742e9e5842d4ad59c2db887e12449f9", "", "", "",
                "", "", "");
        Security security = new Security(cloudProvider);

        String loginId = "0cff1f62-37a2-4500-a4d5-be5924e02823";
        String password = "mYPaSsWoRd";
        String encryptedPassword = security.encryptPassword(loginId, password);
        assertEquals("178026cb7c5a299d6abd1cd779d11bf1a47045b09e410db5fe139f7b25c8eb35", encryptedPassword);
    }

    @Test
    public void testSign1() {
        // Cloud provider url used to create the sign (with the endpoint)
        CloudProvider cloudProvider = new CloudProvider("NetHome Plus", "3742e9e5842d4ad59c2db887e12449f9", "",
                "https://mapp.appsmb.com", "", "", "", "");
        Security security = new Security(cloudProvider);

        String url = "https://mapp.appsmb.com/v1/user/login/id/get";

        // Create JsonObject using Gson
        JsonObject data = new JsonObject();
        data.addProperty("appId", "1017");
        data.addProperty("format", 2);
        data.addProperty("clientType", 1);
        data.addProperty("language", "en_US");
        data.addProperty("src", "1017");
        data.addProperty("stamp", "20250327172341");
        data.addProperty("loginAccount", "myemail@gmail.com");

        // Sign the data
        String sign = security.sign(url, data);

        // Verify the signature
        assertEquals("648ed795a7b0faf566226cc5abc72bd32a51ede0ffbf9649b34c0f25a3508ef4", sign);
    }

    @Test
    public void testSign2() {
        // Cloud provider url used to create the sign (with the endpoint)
        CloudProvider cloudProvider = new CloudProvider("NetHome Plus", "3742e9e5842d4ad59c2db887e12449f9", "",
                "https://mapp.appsmb.com", "", "", "", "");
        Security security = new Security(cloudProvider);

        String url = "https://mapp.appsmb.com/v1/user/login";

        // Create JsonObject using Gson
        JsonObject data = new JsonObject();
        data.addProperty("appId", "1017");
        data.addProperty("format", 2);
        data.addProperty("clientType", 1);
        data.addProperty("language", "en_US");
        data.addProperty("src", "1017");
        data.addProperty("stamp", "20250327172342");
        data.addProperty("loginAccount", "myemail@gmail.com");
        data.addProperty("password", "178026cb7c5a299d6abd1cd779d11bf1a47045b09e410db5fe139f7b25c8eb35");

        // Sign the data
        String sign = security.sign(url, data);

        // Verify the signature
        assertEquals("43e1031addb18994d686cae8ac3612e75e4862eea67c7a5b9dde19c68508b90b", sign);
    }

    @Test
    public void testSign3() {
        // Cloud provider url used to create the sign (with the endpoint)
        CloudProvider cloudProvider = new CloudProvider("NetHome Plus", "3742e9e5842d4ad59c2db887e12449f9", "",
                "https://mapp.appsmb.com", "", "", "", "");
        Security security = new Security(cloudProvider);

        String url = "https://mapp.appsmb.com/v1/iot/secure/getToken";

        // Create JsonObject using Gson
        JsonObject data = new JsonObject();
        data.addProperty("appId", "1017");
        data.addProperty("format", 2);
        data.addProperty("clientType", 1);
        data.addProperty("language", "en_US");
        data.addProperty("src", "1017");
        data.addProperty("stamp", "20250331202017");
        data.addProperty("udpid", "c52a10094ba5dc9866cdf657606d4bbd");
        data.addProperty("sessionId", "a4261dbfd2e748fb89da33a2be3ace3020250325232016876");

        // Sign the data
        String sign = security.sign(url, data);

        // Verify the signature
        assertEquals("4cbd636506460ba43251ae254ac1a36457b6e976639cd29b0628818228eefd9d", sign);
    }

    @Test
    public void testnewSign() {
        // Cloud provider that uses newSign ie. proxied
        CloudProvider cloudProvider = new CloudProvider("MSmartHome", "ac21b9f9cbfe4ca5a88562ef25e2b768", "1010",
                "https://mp-prod.appsmb.com/mas/v5/app/proxy?alias=", "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S",
                "meicloud", "PROD_VnoClJI9aikS8dyy", "v5");
        Security security = new Security(cloudProvider);

        String json = "{\"appId\":\"1010\",\"format\":2,\"clientType\":1,\"language\":\"en_US\",\"src\":\"1010\",\"stamp\":\"20250331151111\",\"loginAccount\":\"myemail@gmail.com\",\"reqId\":\"f2c3e2c3365a4d4f\"}";
        String random = "1742860285";
        String proxysign = security.newSign(json, random);
        assertEquals("1afdddedb746d70086dc3c50b35a8ece1e4aadc0030b58e73542528461769a9c", proxysign);
    }
}
