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
package org.openhab.binding.homekit.internal.persistence;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HomekitKeyStore} is responsible for persisting cryptographic keys.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(service = HomekitKeyStore.class)
public class HomekitKeyStore {

    private static final String CONTROLLER_ID = "controllerId";
    private static final String CONTROLLER_KEY_ID = "controller";

    private final Storage<String> storage;

    @Activate
    public HomekitKeyStore(@Reference StorageService storageService) {
        storage = storageService.getStorage(getClass().getName(), getClass().getClassLoader());
    }

    private String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] decode(String string) {
        return Base64.getDecoder().decode(string);
    }

    public @Nullable Ed25519PublicKeyParameters getAccessoryKey(String keyId) {
        return storage.get(keyId) instanceof String key ? new Ed25519PublicKeyParameters(decode(key), 0) : null;
    }

    public void setAccessoryKey(String keyId, @Nullable Ed25519PublicKeyParameters key) {
        if (key == null) {
            storage.remove(keyId);
        } else {
            storage.put(keyId, encode(key.getEncoded()));
        }
    }

    public byte[] getControllerId() {
        String controllerId = storage.get(CONTROLLER_ID);
        if (controllerId != null) {
            return decode(controllerId);
        }
        // create a new 16 byte controller ID
        ByteBuffer buf = ByteBuffer.allocate(16);
        UUID uuid = UUID.randomUUID();
        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());
        byte[] newControllerId = buf.array();
        storage.put(CONTROLLER_ID, encode(newControllerId));
        return newControllerId;
    }

    public Ed25519PrivateKeyParameters getControllerKey() {
        String controllerKey = storage.get(CONTROLLER_KEY_ID);
        if (controllerKey != null) {
            return new Ed25519PrivateKeyParameters(decode(controllerKey), 0);
        }
        Ed25519PrivateKeyParameters newControllerKey = new Ed25519PrivateKeyParameters(new SecureRandom());
        storage.put(CONTROLLER_KEY_ID, encode(newControllerKey.getEncoded()));
        return newControllerKey;
    }
}
