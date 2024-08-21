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
package org.openhab.binding.androidtv.internal.protocol.philipstv.pairing;

import static org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.BASE_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.EMPTY;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.SLASH;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Formatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManagerUtil;
import org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.pairing.model.AuthDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.pairing.model.DeviceDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.pairing.model.FinishPairingDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.pairing.model.PairingDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.pairing.model.RequestCodeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PhilipsTVPairing} is responsible for the initial pairing process with the Philips TV.
 * The outcome of this one-time pairing is a registered user with password, which will be used for
 * controlling the tv.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public class PhilipsTVPairing {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static String authTimestamp = "";

    private static String authKey = "";

    private static String deviceId = "";

    private final String pairingBasePath = BASE_PATH + "pair" + SLASH;

    public void requestPairingPin(HttpHost target)
            throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RequestCodeDTO requestCodeDTO = new RequestCodeDTO(
                Stream.of("read", "write", "control").collect(Collectors.toList()), createDeviceSpecification());

        CloseableHttpClient httpClient = ConnectionManagerUtil.createSharedHttpClient(target, EMPTY, EMPTY);
        ConnectionManager connectionManager = new ConnectionManager(httpClient, target);
        String requestCodeJson = OBJECT_MAPPER.writeValueAsString(requestCodeDTO);
        String requestPairingCodePath = pairingBasePath + "request";
        logger.debug("Request pairing code with json: {}", requestCodeJson);
        PairingDTO pairingDTO = OBJECT_MAPPER
                .readValue(connectionManager.doHttpsPost(requestPairingCodePath, requestCodeJson), PairingDTO.class);

        authTimestamp = pairingDTO.getTimestamp();
        authKey = pairingDTO.getAuthKey();

        logger.info("The pairing code is valid for {} seconds.", pairingDTO.getTimeout());
    }

    public void finishPairingWithTv(String pairingCode, PhilipsTVConnectionManager handler, HttpHost target)
            throws NoSuchAlgorithmException, InvalidKeyException, IOException, KeyStoreException,
            KeyManagementException {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setAuthAppId("1");
        authDTO.setAuthSignature(calculateRFC2104HMAC(authTimestamp + pairingCode));
        authDTO.setAuthTimestamp(authTimestamp);
        authDTO.setPin(pairingCode);

        FinishPairingDTO finishPairingDTO = new FinishPairingDTO(createDeviceSpecification(), authDTO);
        String grantPairingJson = OBJECT_MAPPER.writeValueAsString(finishPairingDTO);

        Header challengeHeader = null;
        try (CloseableHttpClient httpClient = ConnectionManagerUtil.createSharedHttpClient(target, EMPTY, EMPTY)) {
            CloseableHttpResponse response = httpClient
                    .execute(new HttpGet(target.toURI() + pairingBasePath + "grant"));
            challengeHeader = response.getFirstHeader("WWW-Authenticate");
        } catch (IOException e) {
            logger.debug("finishPairingWithTv: {}", e.getMessage());
            throw e;
        }

        try (CloseableHttpClient client = ConnectionManagerUtil.createSharedHttpClient(target, deviceId, authKey)) {
            logger.debug("{} and device id: {} and auth_key: {}", grantPairingJson, deviceId, authKey);

            String grantPairingCodePath = pairingBasePath + "grant";
            HttpPost httpPost = new HttpPost(grantPairingCodePath);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity(grantPairingJson));

            DigestScheme digestAuth = new DigestScheme();
            digestAuth.processChallenge(challengeHeader);

            AuthCache authCache = new BasicAuthCache();
            authCache.put(target, digestAuth);

            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            try (CloseableHttpResponse response = client.execute(target, httpPost, localContext)) {
                String jsonContent = EntityUtils.toString(response.getEntity());
                logger.debug("----------------------------------------");
                logger.debug("{}", response.getStatusLine());
                logger.debug("{}", jsonContent);
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException("Pairing grant failed");
                }
                if (jsonContent.contains("INVALID_PIN")) {
                    throw new IOException("Invalid PIN");
                }
            }
            handler.setCreds(deviceId, authKey);
        } catch (MalformedChallengeException e) {
            logger.debug("finishPairingWithTv: {}", e.getMessage());
            throw new IOException(e.getMessage());
        }
    }

    private String createDeviceId() {
        StringBuilder deviceIdBuilder = new StringBuilder();
        String chars = "abcdefghkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ123456789";
        for (int i = 0; i < 16; i++) {
            int index = (int) Math.floor(Math.random() * chars.length());
            deviceIdBuilder.append(chars.charAt(index));
        }
        return deviceIdBuilder.toString();
    }

    private DeviceDTO createDeviceSpecification() {
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setAppName("openHAB");
        deviceDTO.setAppId("app.id");
        deviceDTO.setDeviceName("heliotrope");
        deviceDTO.setDeviceOs("Android");
        deviceDTO.setType("native");
        if (deviceId.isEmpty()) {
            deviceId = createDeviceId();
        }
        deviceDTO.setId(deviceId);
        return deviceDTO;
    }

    private String toHexString(byte[] bytes) {
        try (Formatter formatter = new Formatter()) {
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }

            return formatter.toString();
        }
    }

    private String calculateRFC2104HMAC(String data)
            throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        String hmacSHA1 = "HmacSHA1";
        // Key used for generated the HMAC signature
        String secretKey = "ZmVay1EQVFOaZhwQ4Kv81ypLAZNczV9sG4KkseXWn1NEk6cXmPKO/MCa9sryslvLCFMnNe4Z4CPXzToowvhHvA==";
        Key signingKey = new SecretKeySpec(Base64.getDecoder().decode(secretKey), hmacSHA1);
        Mac mac = Mac.getInstance(hmacSHA1);
        mac.init(signingKey);
        return Base64.getEncoder().encodeToString(toHexString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8.name())))
                .getBytes(StandardCharsets.UTF_8.name()));
    }
}
