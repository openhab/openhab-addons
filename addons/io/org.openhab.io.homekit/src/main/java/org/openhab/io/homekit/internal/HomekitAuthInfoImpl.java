/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitAuthInfo;
import com.beowulfe.hap.HomekitServer;

/**
 * Provides a mechanism to store authenticated homekit client details inside the
 * ESH StorageService, by implementing HomekitAuthInfo.
 *
 * @author Andy Lintner
 */
public class HomekitAuthInfoImpl implements HomekitAuthInfo {
    private final Logger logger = LoggerFactory.getLogger(HomekitAuthInfoImpl.class);

    private final Storage<String> storage;
    private final String mac;
    private final BigInteger salt;
    private final byte[] privateKey;
    private final String pin;

    public HomekitAuthInfoImpl(StorageService storageService, String pin) throws InvalidAlgorithmParameterException {
        storage = storageService.getStorage("homekit");
        initializeStorage();
        this.pin = pin;
        mac = storage.get("mac");
        salt = new BigInteger(storage.get("salt"));
        privateKey = Base64.getDecoder().decode(storage.get("privateKey"));
    }

    @Override
    public void createUser(String username, byte[] publicKey) {
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
        String encodedKey = storage.get(createUserKey(username));
        if (encodedKey != null) {
            return Base64.getDecoder().decode(encodedKey);
        } else {
            return null;
        }
    }

    @Override
    public void removeUser(String username) {
        storage.remove(createUserKey(username));
    }

    @Override
    public boolean hasUser() {
        Collection<String> keys = storage.getKeys();
        return keys.stream().filter(k -> isUserKey(k)).count() > 0;
    }

    public void clear() {
        for (String key : new HashSet<>(storage.getKeys())) {
            if (isUserKey("user_")) {
                storage.remove(key);
            }
        }
    }

    private String createUserKey(String username) {
        return "user_" + username;
    }

    private boolean isUserKey(String key) {
        return key.startsWith("user_");
    }

    private void initializeStorage() throws InvalidAlgorithmParameterException {
        if (storage.get("mac") == null) {
            logger.warn(
                    "Could not find existing MAC in {}. Generating new MAC. This will require re-pairing of iOS devices.",
                    storage.getClass().getName());
            storage.put("mac", HomekitServer.generateMac());
        }
        if (storage.get("salt") == null) {
            storage.put("salt", HomekitServer.generateSalt().toString());
        }
        if (storage.get("privateKey") == null) {
            storage.put("privateKey", Base64.getEncoder().encodeToString(HomekitServer.generateKey()));
        }
    }

}
