/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.io.homekit.internal;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.server.HomekitAuthInfo;
import io.github.hapjava.server.impl.HomekitServer;

/**
 * Provides a mechanism to store authenticated HomeKit client details inside the
 * ESH StorageService, by implementing HomekitAuthInfo.
 *
 * @author Andy Lintner - Initial contribution
 */
public class HomekitAuthInfoImpl implements HomekitAuthInfo {
    private final Logger logger = LoggerFactory.getLogger(HomekitAuthInfoImpl.class);
    public static final String STORAGE_KEY = "homekit";
    private static final String STORAGE_MAC = "mac";
    private static final String STORAGE_SALT = "salt";
    private static final String STORAGE_PRIVATE_KEY = "privateKey";
    private static final String STORAGE_USER_PREFIX = "user_";

    private final Storage<String> storage;
    private String mac;
    private BigInteger salt;
    private byte[] privateKey;
    private final String pin;

    public HomekitAuthInfoImpl(Storage<String> storage, String pin) throws InvalidAlgorithmParameterException {
        this.storage = storage;
        this.pin = pin;
        initializeStorage();
    }

    @Override
    public void createUser(String username, byte[] publicKey) {
        logger.trace("Create user {}", username);
        storage.put(createUserKey(username), Base64.getEncoder().encodeToString(publicKey));
    }

    @Override
    public String getMac() {
        return mac;
    }

    @Override
    public String getPin() {
        return pin;
    }

    @Override
    public byte[] getPrivateKey() {
        return privateKey;
    }

    @Override
    public BigInteger getSalt() {
        return salt;
    }

    @Override
    public byte[] getUserPublicKey(String username) {
        final String encodedKey = storage.get(createUserKey(username));
        if (encodedKey != null) {
            return Base64.getDecoder().decode(encodedKey);
        } else {
            return null;
        }
    }

    @Override
    public void removeUser(String username) {
        logger.trace("Remove user {}", username);
        storage.remove(createUserKey(username));
    }

    @Override
    public boolean hasUser() {
        Collection<String> keys = storage.getKeys();
        return keys.stream().anyMatch(this::isUserKey);
    }

    public void clear() {
        logger.trace("Clear all users");
        for (String key : new HashSet<>(storage.getKeys())) {
            if (isUserKey(key)) {
                storage.remove(key);
            }
        }
    }

    private String createUserKey(String username) {
        return STORAGE_USER_PREFIX + username;
    }

    private boolean isUserKey(String key) {
        return key.startsWith(STORAGE_USER_PREFIX);
    }

    private void initializeStorage() throws InvalidAlgorithmParameterException {
        mac = storage.get(STORAGE_MAC);
        final @Nullable Object saltConfig = storage.get(STORAGE_SALT);
        final @Nullable Object privateKeyConfig = storage.get(STORAGE_PRIVATE_KEY);
        if (mac == null) {
            logger.warn(
                    "Could not find existing MAC in {}. Generating new MAC. This will require re-pairing of iOS devices.",
                    storage.getClass().getName());
            mac = HomekitServer.generateMac();
            storage.put(STORAGE_MAC, mac);
        }
        if (saltConfig == null) {
            salt = HomekitServer.generateSalt();
            storage.put(STORAGE_SALT, salt.toString());
        } else {
            salt = new BigInteger(saltConfig.toString());
        }
        if (privateKeyConfig == null) {
            privateKey = HomekitServer.generateKey();
            storage.put(STORAGE_PRIVATE_KEY, Base64.getEncoder().encodeToString(privateKey));
        } else {
            privateKey = Base64.getDecoder().decode(privateKeyConfig.toString());
        }
    }
}
