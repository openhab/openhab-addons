/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.server.HomekitAuthInfo;
import io.github.hapjava.server.impl.HomekitServer;

/**
 * Provides a mechanism to store authenticated HomeKit client details inside the
 * StorageService, by implementing HomekitAuthInfo.
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

    private final Storage<Object> storage;
    private String mac;
    private BigInteger salt;
    private byte[] privateKey;
    private String pin;
    private String setupId;
    private boolean blockUserDeletion;

    public HomekitAuthInfoImpl(Storage<Object> storage, String pin, String setupId, boolean blockUserDeletion)
            throws InvalidAlgorithmParameterException {
        this.storage = storage;
        this.pin = pin;
        this.setupId = setupId;
        this.blockUserDeletion = blockUserDeletion;
        initializeStorage();
    }

    public void setBlockUserDeletion(boolean blockUserDeletion) {
        this.blockUserDeletion = blockUserDeletion;
    }

    @Override
    public void createUser(String username, byte[] publicKey, boolean isAdmin) {
        logger.trace("create user {}", username);
        final String userKey = createUserKey(username);
        final String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey);
        storage.put(userKey, encodedPublicKey);
        logger.trace("stored user key {} with value {}", userKey, encodedPublicKey);
    }

    @Override
    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    @Override
    public String getSetupId() {
        return setupId;
    }

    public void setSetupId(String setupId) {
        this.setupId = setupId;
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
        final String encodedKey = (String) storage.get(createUserKey(username));
        if (encodedKey != null) {
            return Base64.getDecoder().decode(encodedKey);
        } else {
            return null;
        }
    }

    @Override
    public void removeUser(String username) {
        logger.trace("remove user {}", username);
        if (!this.blockUserDeletion) {
            storage.remove(createUserKey(username));
        } else {
            logger.debug("deletion of the user was blocked by binding settings");
        }
    }

    @Override
    public boolean hasUser() {
        Collection<String> keys = storage.getKeys();
        return keys.stream().anyMatch(this::isUserKey);
    }

    @Override
    public Collection<String> listUsers() {
        Collection<String> keys = storage.getKeys();
        // don't forget to strip user_ prefix
        return keys.stream().filter(this::isUserKey).map(u -> u.substring(5)).collect(Collectors.toList());
    }

    @Override
    public boolean userIsAdmin(String username) {
        return true;
    }

    public void clear() {
        if (!this.blockUserDeletion) {
            for (String key : new HashSet<>(storage.getKeys())) {
                if (isUserKey(key)) {
                    storage.remove(key);
                }
            }
            mac = HomekitServer.generateMac();
            storage.put(STORAGE_MAC, mac);
            storage.remove(STORAGE_SALT);
            storage.remove(STORAGE_PRIVATE_KEY);
            try {
                initializeStorage();
                logger.info("All users cleared from HomeKit bridge; re-pairing required.");
            } catch (InvalidAlgorithmParameterException e) {
                logger.warn(
                        "Failed generating new encryption settings for HomeKit bridge; re-pairing required, but will likely fail.");
            }
        } else {
            logger.warn("Deletion of HomeKit users was blocked by addon settings.");
        }
    }

    private String createUserKey(String username) {
        return STORAGE_USER_PREFIX + username;
    }

    private boolean isUserKey(String key) {
        return key.startsWith(STORAGE_USER_PREFIX);
    }

    private void initializeStorage() throws InvalidAlgorithmParameterException {
        mac = (String) storage.get(STORAGE_MAC);
        final @Nullable Object saltConfig = storage.get(STORAGE_SALT);
        final @Nullable Object privateKeyConfig = storage.get(STORAGE_PRIVATE_KEY);
        if (mac == null) {
            logger.warn(
                    "could not find existing MAC in {}. Generating new MAC. This will require re-pairing of iOS devices.",
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
