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
package org.openhab.binding.tapocontrol.internal.discovery;

import static org.openhab.binding.tapocontrol.internal.api.protocol.TapoProtocolEnum.SECUREPASSTROUGH;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.discovery.dto.TapoDiscoveryResult;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Decodes discovery replies from legacy Kasa devices that use the TP-Link XOR protocol.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
final class KasaXorDiscovery {
    private static final int INITIAL_KEY = 0xAB;
    private static final String SYSINFO_REQUEST = "{\"system\":{\"get_sysinfo\":{}}}";

    private KasaXorDiscovery() {
    }

    static byte[] discoveryRequest() {
        byte[] plainText = SYSINFO_REQUEST.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = new byte[plainText.length];
        int key = INITIAL_KEY;
        for (int i = 0; i < plainText.length; i++) {
            encrypted[i] = (byte) (plainText[i] ^ key);
            key = Byte.toUnsignedInt(encrypted[i]);
        }
        return encrypted;
    }

    static Optional<TapoDiscoveryResult> decode(byte[] encrypted, int length, String ipAddress) {
        if (length <= 0 || length > encrypted.length) {
            return Optional.empty();
        }

        byte[] plainText = new byte[length];
        int key = INITIAL_KEY;
        for (int i = 0; i < length; i++) {
            int nextKey = Byte.toUnsignedInt(encrypted[i]);
            plainText[i] = (byte) (nextKey ^ key);
            key = nextKey;
        }

        try {
            JsonObject root = JsonParser.parseString(new String(plainText, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject system = root.getAsJsonObject("system");
            JsonObject sysinfo = system == null ? null : system.getAsJsonObject("get_sysinfo");
            if (sysinfo == null) {
                return Optional.empty();
            }

            String mac = stringValue(sysinfo, "mac", "mic_mac", "ethernet_mac");
            String model = stringValue(sysinfo, "model");
            if (mac.isBlank() || model.isBlank()) {
                return Optional.empty();
            }

            var encryption = new TapoDiscoveryResult.EncryptionSchema(false, SECUREPASSTROUGH.toString(), 80, 0);
            return Optional.of(new TapoDiscoveryResult(false, true, false, encryption, 0, 0,
                    stringValue(sysinfo, "alias"), "", stringValue(sysinfo, "hw_ver"),
                    stringValue(sysinfo, "deviceId", "device_id"), mac, model, stringValue(sysinfo, "dev_name"), "",
                    stringValue(sysinfo, "type", "mic_type"), stringValue(sysinfo, "fwId", "fw_id"),
                    stringValue(sysinfo, "sw_ver"), stringValue(sysinfo, "hwId", "hw_id"), ipAddress, "",
                    stringValue(sysinfo, "oemId", "oem_id")));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static String stringValue(JsonObject object, String... names) {
        for (String name : names) {
            if (object.has(name) && !object.get(name).isJsonNull()) {
                return object.get(name).getAsString();
            }
        }
        return "";
    }
}
