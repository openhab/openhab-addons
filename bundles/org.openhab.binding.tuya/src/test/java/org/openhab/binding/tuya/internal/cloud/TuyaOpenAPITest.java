/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tuya.internal.cloud;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceSchema;
import org.openhab.binding.tuya.internal.cloud.dto.Token;
import org.openhab.binding.tuya.internal.config.ProjectConfiguration;
import org.openhab.binding.tuya.internal.util.CryptoUtil;
import org.openhab.core.test.java.JavaTest;

import com.google.gson.Gson;

/**
 * The {@link TuyaOpenAPITest} is a test class for the {@link TuyaOpenAPI} class
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class TuyaOpenAPITest extends JavaTest {
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) ApiStatusCallback callback;
    private @Mock @NonNullByDefault({}) ScheduledExecutorService scheduler;
    private final Gson gson = new Gson();

    private final String clientId = "1KAD46OrT9HafiKdsXeg";
    private final String secret = "4OHBOnWOqaEC1mWXOpVL3yV50s0qGSRC";
    private final String accessToken = "3f4eda2bdec17232f67c0b188af3eec1";
    private final long now = 1588925778000L;
    private final String nonce = "5138cc3a9033d69856923fd07b491173";
    private final Map<String, String> headers = Map.of(//
            "area_id", "29a33e8796834b1efa6", //
            "call_id", "8afdb70ab2ed11eb85290242ac130003", //
            "client_id", clientId);
    private final List<String> signHeaders = List.of("area_id", "call_id");

    @Test
    public void signTokenRequest() {
        String path = "/v1.0/token";
        Map<String, String> params = Map.of( //
                "grant_type", "1");

        ProjectConfiguration configuration = new ProjectConfiguration();
        configuration.accessId = clientId;
        configuration.accessSecret = secret;
        TuyaOpenAPI api = new TuyaOpenAPI(callback, scheduler, gson, httpClient);
        api.setConfiguration(configuration);
        String signedString = api.signRequest(HttpMethod.GET, path, headers, signHeaders, params, null, nonce, now);

        Assertions.assertEquals("9E48A3E93B302EEECC803C7241985D0A34EB944F40FB573C7B5C2A82158AF13E", signedString);
    }

    @Test
    public void signServiceRequest() {
        String path = "/v2.0/apps/schema/users";
        Map<String, String> params = Map.of( //
                "page_no", "1", //
                "page_size", "50");

        Token token = new Token(accessToken, "", "", 0);

        ProjectConfiguration configuration = new ProjectConfiguration();
        configuration.accessId = clientId;
        configuration.accessSecret = secret;
        TuyaOpenAPI api = new TuyaOpenAPI(callback, scheduler, gson, httpClient);
        api.setConfiguration(configuration);
        api.setToken(token);

        String signedString = api.signRequest(HttpMethod.GET, path, headers, signHeaders, params, null, nonce, now);

        Assertions.assertEquals("AE4481C692AA80B25F3A7E12C3A5FD9BBF6251539DD78E565A1A72A508A88784", signedString);
    }

    @Test
    public void decryptTest() {
        String data = "AAAADF3anfyV36xCpZWsTDMtD0q0fsd5VXfX16x7lKc7yA8QFDnGixeCpmfE8OYFDWEx+8+pcn6JrjIXGHMLAXpeHamsUpnms8bBjfBj4KC8N4UUkT2WW15bwpAi1uQiY5j3XCrKb+VnHmG1cXL3yTi02URvwPfCBNoBB1X7ABsHNaPC6zJhYEcTwEc0Rmlk72qr4pEoweQxlZbhGsTb7VQAvPhjUV8Pzycms8kl9pt1fc/rMDc58vDP0ieThScQiYn4+3pbNKq+amzRdKIYmbI8aS9D97QmduRlqimeh6ve1KH9egtEvaigbAtcpHWyw6FB9ApCqoYuGBig8rO8GDlKdA==";
        String password = "8699163a36d6cecc04df6000b7a580f5";
        long t = 1636568272;

        String decryptResult = CryptoUtil.decryptAesGcm(data, password, t);
        Assertions.assertNotNull(decryptResult);

        // data contains 4-byte length, 12 byte IV, 128bits AuthTag
        Assertions.assertEquals(227, Objects.requireNonNull(decryptResult).length());
    }

    @Test
    public void schemaDecodeRange() {
        String value = "{\\\"range\\\":[\\\"white\\\",\\\"colour\\\",\\\"scene\\\",\\\"music\\\"]}";
        DeviceSchema.EnumRange range = Objects
                .requireNonNull(gson.fromJson(value.replaceAll("\\\\", ""), DeviceSchema.EnumRange.class));

        Assertions.assertEquals(4, range.range.size());
    }
}
