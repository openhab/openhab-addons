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

import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Enumeration;

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

    private final Storage<String> storage;
    private final String controllerId;

    @Activate
    public HomekitKeyStore(@Reference StorageService storageService) {
        storage = storageService.getStorage(getClass().getName(), getClass().getClassLoader());
        controllerId = getMacAddress();
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

    public Ed25519PrivateKeyParameters getControllerKey() {
        String key = storage.get(controllerId);
        if (key != null) {
            return new Ed25519PrivateKeyParameters(decode(key), 0);
        }
        Ed25519PrivateKeyParameters newKey = new Ed25519PrivateKeyParameters(new SecureRandom());
        storage.put(controllerId, encode(newKey.getEncoded()));
        return newKey;
    }

    /**
     * Returns the MAC address of the first non-loopback network interface found.
     *
     * @return the MAC address as a String in the format "XX:XX:XX:XX:XX:XX"
     * @throws IllegalStateException if no suitable network interface is found
     */
    private String getMacAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback() && !ni.isVirtual() && ni.getHardwareAddress() instanceof byte[] mac) {
                    String macAddr = "";
                    for (int i = 0; i < mac.length; i++) {
                        macAddr += String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : "");
                    }
                    return macAddr;
                }
            }
        } catch (SocketException e) {
        }
        throw new IllegalStateException("No suitable network interface found for deriving MAC address");
    }
}
