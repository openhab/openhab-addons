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
package org.openhab.binding.philipsair.internal.connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.core.util.HexUtils;

/**
 * Tests the key exchange and response handling of {@link PhilipsAirHttpAPIConnection}.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PhilipsAirHttpAPIConnectionTest {

    private static final String SESSION_KEY = "00112233445566778899AABBCCDDEEFF";
    private static final String STATUS = "{\"pwr\":\"1\",\"om\":\"2\",\"pm25\":7}";

    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) Request request;
    private @Mock @NonNullByDefault({}) ContentResponse response;

    private final PhilipsAirConfiguration config = new PhilipsAirConfiguration();

    @BeforeEach
    public void setUp() throws Exception {
        config.setHost("1.1.1.1");
        config.setRefreshInterval(5);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.method(any(HttpMethod.class))).thenReturn(request);
        when(request.content(any(ContentProvider.class))).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        when(request.send()).thenReturn(response);
    }

    /**
     * Builds the device reply of the key exchange. With 'hellman' 1 the shared secret is 1 for any private exponent,
     * so the session key is encrypted with the first 16 bytes of its 128 byte encoding, which are all zero.
     */
    private static String keyExchangeResponse() throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[16], "AES"), new IvParameterSpec(new byte[16]));
        String key = HexUtils.bytesToHex(cipher.doFinal(HexUtils.hexToBytes(SESSION_KEY)));
        return "{\"key\":\"" + key + "\",\"hellman\":\"1\"}";
    }

    private static String encrypt(String content) throws Exception {
        PhilipsAirCipher cipher = new PhilipsAirCipher();
        cipher.initKey(SESSION_KEY);
        String encrypted = cipher.encrypt(content);
        assertNotNull(encrypted);
        return encrypted;
    }

    @Test
    public void emptyKeyIsExchanged() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(keyExchangeResponse(), encrypt(STATUS));

        PhilipsAirHttpAPIConnection connection = new PhilipsAirHttpAPIConnection(config, httpClient);

        assertEquals(SESSION_KEY, config.getKey());
        verify(request).method(HttpMethod.PUT);
        PhilipsAirPurifierDataDTO data = connection.getAirPurifierStatus("1.1.1.1");
        assertNotNull(data);
        assertEquals("1", data.getPower());
        assertEquals(7, data.getPm25());
    }

    @Test
    public void invalidKeyExchangeResponseIsReportedAsApiException() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{}", "", "null");

        PhilipsAirHttpAPIConnection connection = new PhilipsAirHttpAPIConnection(config, httpClient);

        assertEquals("", config.getKey());
        assertThrows(PhilipsAirAPIException.class, () -> connection.getAirPurifierStatus("1.1.1.1"));
    }

    @Test
    public void errorResponseIsNotDecrypted() throws Exception {
        config.setKey(SESSION_KEY);
        when(response.getStatus()).thenReturn(404);
        when(response.getContentAsString()).thenReturn("Not Found");

        PhilipsAirHttpAPIConnection connection = new PhilipsAirHttpAPIConnection(config, httpClient);

        PhilipsAirAPIException e = assertThrows(PhilipsAirAPIException.class,
                () -> connection.getAirPurifierStatus("1.1.1.1"));
        assertEquals("Error with status 404", e.getMessage());
        assertEquals(SESSION_KEY, config.getKey());
    }

    @Test
    public void tooManyRequestsStartsCooldown() throws Exception {
        config.setKey(SESSION_KEY);
        when(response.getStatus()).thenReturn(429);
        when(response.getContentAsString()).thenReturn("Too Many Requests");

        PhilipsAirHttpAPIConnection connection = new PhilipsAirHttpAPIConnection(config, httpClient);

        assertThrows(PhilipsAirAPIException.class, () -> connection.getAirPurifierStatus("1.1.1.1"));
        assertThrows(PhilipsAirAPIException.class, () -> connection.getAirPurifierDevice("1.1.1.1"));
        verify(request, times(1)).send();
    }
}
