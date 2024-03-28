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
package org.openhab.binding.thekeys.internal.api;

import java.io.EOFException;
import java.net.ConnectException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.thekeys.internal.api.model.GatewayInfosDTO;
import org.openhab.binding.thekeys.internal.api.model.LockerDTO;
import org.openhab.binding.thekeys.internal.api.model.LockerStatusDTO;
import org.openhab.binding.thekeys.internal.api.model.LockersDTO;
import org.openhab.binding.thekeys.internal.api.model.OpenCloseDTO;
import org.openhab.binding.thekeys.internal.api.model.SynchronizeDTO;
import org.openhab.binding.thekeys.internal.gateway.TheKeysGatewayConfiguration;
import org.openhab.binding.thekeys.internal.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement the communication with the Gateway via HTTP
 *
 * @author Jordan Martin - Initial contribution
 */
@NonNullByDefault
public class GatewayService {
    private static final String API_SCHEME = "http://";
    private final Logger logger = LoggerFactory.getLogger(GatewayService.class);
    private final TheKeysGatewayConfiguration configuration;
    private final TheKeysHttpClient httpClient;

    public GatewayService(TheKeysGatewayConfiguration configuration, TheKeysHttpClient httpClient) {
        this.configuration = configuration;
        this.httpClient = httpClient;
    }

    /**
     * Get infos of the gateway
     *
     * @return The gateway response
     * @throws TheKeysException If the request failed
     */
    public GatewayInfosDTO getGatewayInfos() throws TheKeysException {
        return get("/", GatewayInfosDTO.class);
    }

    /**
     * Get available locks
     *
     * @return The list of the lock
     * @throws TheKeysException If the request failed
     */
    public List<LockerDTO> getLocks() throws TheKeysException {
        return get("/lockers", LockersDTO.class).getDevices();
    }

    /**
     * Get state of a lock
     *
     * @param lockId The lockId
     * @return The status of the specified lock
     * @throws TheKeysException If the request failed
     */
    public LockerStatusDTO getLockStatus(int lockId) throws TheKeysException {
        return post("/locker_status", LockerStatusDTO.class, lockId);
    }

    /**
     * Synchronize the gateway
     *
     * @return The result status of the synchronization
     * @throws TheKeysException If the request failed
     */
    public SynchronizeDTO synchronizeGateway() throws TheKeysException {
        return get("/synchronize", SynchronizeDTO.class);
    }

    /**
     * Synchronize the lock
     *
     * @return The result status of the lock synchronization
     * @throws TheKeysException If the request failed
     */
    public SynchronizeDTO synchronizeLock() throws TheKeysException {
        return get("/locker/synchronize", SynchronizeDTO.class);
    }

    /**
     * Open operation of the lock
     *
     * @param lockId Identifier of the lock
     * @return The gateway response
     * @throws TheKeysException If the request failed
     */
    public OpenCloseDTO open(int lockId) throws TheKeysException {
        return post("/open", OpenCloseDTO.class, lockId);
    }

    /**
     * Close operation of the lock
     *
     * @param lockId Identifier of the lock
     * @return The gateway response
     * @throws TheKeysException If the request failed
     */
    public OpenCloseDTO close(int lockId) throws TheKeysException {
        return post("/close", OpenCloseDTO.class, lockId);
    }

    /**
     * Make a GET request to the gateway
     *
     * @param path Path of the request
     * @param responseType Response type
     * @return The deserialized response
     * @throws TheKeysException If the request failed
     */
    private <T> T get(String path, Class<T> responseType) throws TheKeysException {
        return get(path, responseType, 3);
    }

    /**
     * Make a GET request to the gateway
     *
     * @param path Path of the request
     * @param responseType Response type
     * @param remainingRetryCount Number of retries in case of request failed
     * @return The deserialized response
     * @throws TheKeysException If the request failed
     */
    private synchronized <T> T get(String path, Class<T> responseType, int remainingRetryCount)
            throws TheKeysException {
        String url = API_SCHEME + configuration.host + path;
        try {
            logger.debug("Call the gateway : GET {}", url);
            return httpClient.get(url, configuration.apiTimeout * 1000, responseType);
        } catch (TheKeysException e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            // Retry the same request because the gateway failed sometimes to handle the request
            if ((remainingRetryCount > 0
                    && (rootCause instanceof ConnectException || rootCause instanceof EOFException))) {
                String rootCauseClass = rootCause == null ? "" : rootCause.getClass().getSimpleName();
                logger.debug("Request failed. {}, retrying {} more times", rootCauseClass, remainingRetryCount);
                return get(path, responseType, --remainingRetryCount);
            }
            throw e;
        }
    }

    /**
     * Make an authenticated POST request to the gateway
     *
     * @param path Path of the request
     * @param responseType Response type
     * @param lockId The lockId
     * @return The deserialized response
     * @throws TheKeysException If the request failed
     */
    private synchronized <T> T post(String path, Class<T> responseType, int lockId) throws TheKeysException {
        String url = API_SCHEME + configuration.host + path;
        logger.debug("Call the gateway : POST {}", url);
        return httpClient.post(url, createRequestBody(lockId), configuration.apiTimeout * 1000, responseType);
    }

    /**
     * Create the request body for a lock request
     *
     * @param lockId The lockId
     * @return The request body as inputstream
     * @throws TheKeysException If the hmac cannot be computed
     */
    private String createRequestBody(int lockId) throws TheKeysException {
        String timestamp = String.valueOf(Instant.now().toEpochMilli() / 1000);
        return computeRequestBodyWithHash(timestamp, lockId);
    }

    /**
     * Compute the request body with the authenticated hash
     *
     * @param timestamp Timestamp of the request in seconds
     * @param lockId The lockId
     * @return The request body
     * @throws TheKeysException If the hmac cannot be computed
     */
    String computeRequestBodyWithHash(String timestamp, int lockId) throws TheKeysException {
        String hash = hmacSha256(timestamp, configuration.code);
        return "identifier=%s&ts=%s&hash=%s".formatted(lockId, timestamp, hash);
    }

    /**
     * Compute the hmac-sha256 of the string
     *
     * @param data The string to encode
     * @param key The private key
     * @return The encoded string
     * @throws TheKeysException If the hmac cannot be computed
     */
    static String hmacSha256(String data, String key) throws TheKeysException {
        try {
            String algorithm = "HmacSHA256";
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(secretKeySpec);
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException e) {
            throw new TheKeysException("Failed to generate auth data", e);
        }
    }
}
