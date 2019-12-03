/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.eclipse.jetty.http.HttpMethod.PUT;
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.eclipse.jetty.http.HttpStatus.TOO_MANY_REQUESTS_429;
import static org.eclipse.jetty.http.HttpStatus.UNAUTHORIZED_401;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierData;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDevice;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * Handles communication with Philips Air purifiers AC2729 and AC2889 and others
 *
 * @author Michał Boroński - Initial contribution
 *
 */
public class PhilipsAirAPIConnection {
    private final Logger logger = LoggerFactory.getLogger(PhilipsAirAPIConnection.class);
    private static final String BASE_UPNP_URL = "http://%HOST%/upnp/description.xml";
    private static final String STATUS_URL = "http://%HOST%/di/v1/products/1/air";
    private static final String DEVICE_URL = "http://%HOST%/di/v1/products/1/device";
    private static final String USERINFO_URL = "http://%HOST%/di/v1/products/0/userinfo";
    private static final String KEY_URL = "http://%HOST%/di/v1/products/0/security";
    private static final String FILTERS_URL = "http://%HOST%/di/v1/products/1/fltsts";
    private static final String FIRMWARE_URL = "http://%HOST%/di/v1/products/0/firmware";

    private final HttpClient httpClient;

    private final ExpiringCacheMap<String, String> cache;

    private final static Random rand = new Random();

    private final static BigInteger G = new BigInteger(
            "A4D1CBD5C3FD34126765A442EFB99905F8104DD258AC507FD6406CFF14266D31266FEA1E5C41564B777E690F5504F213160217B4B01B886A5E91547F9E2749F4D7FBD7D3B9A92EE1909D0D2263F80A76A6A24C087A091F531DBF0A0169B6A28AD662A4D18E73AFA32D779D5918D08BC8858F4DCEF97C2A24855E6EEB22B3B2E5",
            16);
    private final static BigInteger P = new BigInteger(
            "B10B8F96A080E01DDE92DE5EAE5D54EC52C99FBCFB06A3C69A6A9DCA52D23B616073E28675A23D189838EF1E2EE652C013ECB4AEA906112324975C3CD49B83BFACCBDD7D90C4BD7098488E9C219A73724EFFD6FAE5644738FAA31A4FF55BCCC0A151AF5F0DC8B4BD45BF37DF365C1A65E68CFDA76D4DA708DF1FB2BC2E4A4371",
            16);

    private final Gson gson = new Gson();
    private long cooldownTimer = 0;

    private Cipher decipher;
    private Cipher cipher;
    private PhilipsAirConfiguration config;

    public PhilipsAirAPIConnection(PhilipsAirConfiguration config, HttpClient httpClient) {
        this.httpClient = httpClient;
        cache = new ExpiringCacheMap<>(TimeUnit.SECONDS.toMillis(config.getRefreshInterval()));
        this.config = config;
        initCipher();
    }

    private void initCipher() {
        try {
            if (StringUtils.isEmpty(config.getKey())) {
                exchangeKeys();
            }

            decipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(HexUtils.hexToBytes(config.getKey()), "AES"),
                    new IvParameterSpec(new byte[16]));
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(HexUtils.hexToBytes(config.getKey()), "AES"),
                    new IvParameterSpec(new byte[16]));
        } catch (GeneralSecurityException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("An exception occured", e);
            decipher = null;
        }

    }

    public synchronized @Nullable String getAirPurifierInfo(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(BASE_UPNP_URL, host));
    }

    public synchronized @Nullable PhilipsAirPurifierData getAirPurifierStatus(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return gson.fromJson(getResponseFromCache(buildURL(STATUS_URL, host)), PhilipsAirPurifierData.class);
    }

    public synchronized @Nullable String getAirPurifierKey(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(KEY_URL, host));
    }

    public synchronized @Nullable String getAirPurifierFirmware(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(FIRMWARE_URL, host));
    }

    public synchronized @Nullable PhilipsAirPurifierDevice getAirPurifierDevice(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return gson.fromJson(getResponseFromCache(buildURL(DEVICE_URL, host)), PhilipsAirPurifierDevice.class);
    }

    public synchronized @Nullable String getAirPurifierUserinfo(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(USERINFO_URL, host));
    }

    public synchronized @Nullable PhilipsAirPurifierFilters getAirPurifierFiltersStatus(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return gson.fromJson(getResponseFromCache(buildURL(FILTERS_URL, host)), PhilipsAirPurifierFilters.class);
    }

    private static String buildURL(String url, String host) {
        return url.replaceFirst("%HOST%", host);
    }

    private @Nullable String getResponseFromCache(String url) {
        return cache.putIfAbsentAndGet(url, () -> getResponse(url, HttpMethod.GET, null, true));
    }

    private String getResponse(String url, HttpMethod method, String content, boolean decode) {
        try {
            if (decode && decipher == null) {
                logger.error("Cipher not initialized");
                config.setKey("");
                initCipher();
            }

            if (cooldownTimer > System.currentTimeMillis()) {
                logger.debug(
                        "Cooldown period is active, waiting Philips Air Purifier device responded with status code");
                throw new PhilipsAirAPIException(
                        "Cooldown period is active, waiting Philips Air Purifier device responded with status code");
            }

            Request request = httpClient.newRequest(url).method(method);
            if (method == PUT && StringUtils.isNotEmpty(content)) {
                request.content(new StringContentProvider(content));
            }

            ContentResponse contentResponse = request.timeout(config.getRefreshInterval(), TimeUnit.SECONDS).send();
            int httpStatus = contentResponse.getStatus();
            String finalcontent = contentResponse.getContentAsString();
            if (decode) {
                try {
                    finalcontent = decode(finalcontent);
                } catch (BadPaddingException bexp) {
                    // retry for once with a new key
                    config.setKey("");
                    initCipher();
                    finalcontent = decode(finalcontent);
                }
            }

            logger.debug("Philips Air Purifier device response: status = {}, content = '{}'", httpStatus, finalcontent);
            switch (httpStatus) {
            case OK_200:
                return finalcontent;
            case BAD_REQUEST_400:
            case UNAUTHORIZED_401:
            case NOT_FOUND_404:
                logger.debug("Philips Air Purifier device responded with status code {}", httpStatus);
                throw new PhilipsAirAPIException(String.format("Error with status %d", httpStatus));
            case TOO_MANY_REQUESTS_429:
                cooldownTimer = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
            default:
                logger.debug("Philips Air Purifier device responded with status code {}", httpStatus);
                throw new PhilipsAirAPIException(String.format("Error with status %d", httpStatus));
            }
        } catch (ExecutionException e) {
            String errorMessage = e.getLocalizedMessage();
            logger.trace("Exception occurred during execution: {}", errorMessage, e);
            throw new PhilipsAirAPIException(errorMessage, e.getCause());
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new PhilipsAirAPIException(e.getLocalizedMessage(), e.getCause());
        } catch (Exception e) {
            logger.error("Unexpected exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new PhilipsAirAPIException(e.getLocalizedMessage(), e.getCause());
        }
    }

    private String decode(String encodedContent) throws IllegalBlockSizeException, BadPaddingException {
        byte[] decoded = decipher.doFinal(Base64.getDecoder().decode(encodedContent));
        byte[] unpaded = Arrays.copyOfRange(decoded, 2, decoded.length);
        return new String(unpaded);
    }

    public static BigInteger randomForBitsNonZero(int numBits, Random r) {
        BigInteger candidate = new BigInteger(numBits, r);
        while (candidate.equals(BigInteger.ZERO)) {
            candidate = new BigInteger(numBits, r);
        }
        return candidate;
    }

    public String exchangeKeys() throws GeneralSecurityException, InterruptedException, TimeoutException,
            ExecutionException, InvalidAlgorithmParameterException {
        String url = buildURL(KEY_URL, config.getHost());
        BigInteger a = randomForBitsNonZero(256, PhilipsAirAPIConnection.rand);
        BigInteger A = G.modPow(a, P);
        String data = "{\"diffie\":\"" + A.toString(16) + "\"}";

        String encodedContent = getResponse(url, PUT, data, false);
        JsonObject encodedJson = gson.fromJson(encodedContent, JsonObject.class);
        String key = encodedJson.get("key").getAsString();
        BigInteger B = new BigInteger(encodedJson.get("hellman").getAsString(), 16);
        BigInteger s = B.modPow(a, P);
        byte[] s_byte = s.toByteArray();
        // remove trailing 0
        if (s_byte.length > 128 && s_byte[0] == 0) {
            s_byte = Arrays.copyOfRange(s_byte, 1, 128);
        }

        byte[] s_byte_trunc = Arrays.copyOfRange(s_byte, 0, 16);
        byte[] hexKey = HexUtils.hexToBytes(key);

        Cipher ciph = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ciph.init(Cipher.DECRYPT_MODE, new SecretKeySpec(s_byte_trunc, "AES"), new IvParameterSpec(new byte[16]));

        byte[] keyDecoded = ciph.doFinal(hexKey);
        String aesKey = HexUtils.bytesToHex(keyDecoded).substring(0, 32);
        config.setKey(aesKey);
        return aesKey;
    }

    private String encrypt(String data, String key)
            throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        String encodedData = "AA" + data;

        byte[] encryptedBytes = cipher.doFinal(encodedData.getBytes("ascii"));

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public PhilipsAirPurifierData sendCommand(String parameter, Object value)
            throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        String commandValue = null;
        if (parameter.equals("om")) {
            commandValue = "{\"om\":\"" + value + "\",\"mode\":\"M\"}";
        } else if ((value instanceof String)) {
            commandValue = "{\"" + parameter + "\":\"" + value + "\"}";
        } else {
            commandValue = "{\"" + parameter + "\":" + value + "}";
        }

        logger.info("{}", commandValue.toString());
        commandValue = encrypt(commandValue.toString(), null);
        String response = getResponse(buildURL(STATUS_URL, config.getHost()), PUT, commandValue.toString(), true);
        logger.info("{}", response);
        return gson.fromJson(response, PhilipsAirPurifierData.class);
    }
}
