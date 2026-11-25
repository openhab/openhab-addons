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
package org.openhab.binding.tplinksmarthome.internal;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.CRC32;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Discovery of Kasa devices on firmware that secures the legacy protocol. They no longer answer the port 9999
 * broadcast, but do answer TP-Link's discovery request on UDP port 20002, as the Kasa and Tapo apps send it.
 * <p>
 * The request is a 16 byte header followed by JSON with an RSA public key. The reply has the same header followed by
 * JSON with the model, the protocol ({@code IOT.*} for the Kasa JSON this binding speaks, {@code SMART.*} for the newer
 * protocol of Tapo devices) and the encryption scheme. The reply's {@code device_id} is the MD5 hash of the device id
 * that {@code get_sysinfo} reports, not the id itself.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
final class KlapDiscovery {

    static final int PORT = 20002;

    private static final int HEADER_LENGTH = 16;
    private static final byte[] HEADER_START = { 0x02, 0x00, 0x00, 0x01, 0x00, 0x00, 0x11, 0x00 };
    private static final int HEADER_CHECKSUM_SEED = 0x5A6B7C8D;
    private static final int RSA_KEY_SIZE = 2048;
    private static final String IOT_PREFIX = "IOT.";
    private static final String KLAP = "KLAP";

    /**
     * A Kasa device that speaks the Kasa JSON over KLAP.
     *
     * @param model model as the device reports it, such as {@code EP10(US)}
     * @param hashedDeviceId upper case MD5 hash of the device id
     * @param mac MAC address, or empty if not reported
     * @param hardwareVersion hardware version, or empty if not reported
     */
    record Device(String model, String hashedDeviceId, String mac, String hardwareVersion) {
    }

    private KlapDiscovery() {
    }

    /**
     * @return a discovery request with a new RSA public key. The devices only use the key to encrypt replies that this
     *         binding doesn't need, so the private key is discarded.
     */
    static byte[] request() throws GeneralSecurityException {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(RSA_KEY_SIZE);
        final String publicKey = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(
                        generator.generateKeyPair().getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----\n";
        final JsonObject params = new JsonObject();
        params.addProperty("rsa_key", publicKey);
        final JsonObject message = new JsonObject();
        message.add("params", params);
        return packet(message.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Header layout as the Tapo app builds it: start bytes, payload length, a random id, and a CRC32 over the whole
     * packet computed while the checksum field holds a fixed seed.
     */
    static byte[] packet(final byte[] payload) {
        final ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH + payload.length);
        buffer.put(HEADER_START);
        buffer.putShort(4, (short) payload.length);
        buffer.putInt(ThreadLocalRandom.current().nextInt(0x10000000));
        buffer.putInt(HEADER_CHECKSUM_SEED);
        buffer.put(payload);
        final CRC32 crc = new CRC32();
        crc.update(buffer.array());
        buffer.putInt(12, (int) crc.getValue());
        return buffer.array();
    }

    /**
     * @return the device if the reply comes from a Kasa device that speaks the Kasa JSON over KLAP. Devices on the
     *         newer SMART protocol and devices that still use the legacy protocol are left out.
     */
    static Optional<Device> parse(final byte[] data, final int length) {
        if (length <= HEADER_LENGTH) {
            return Optional.empty();
        }
        try {
            final JsonElement reply = JsonParser
                    .parseString(new String(data, HEADER_LENGTH, length - HEADER_LENGTH, StandardCharsets.UTF_8));
            if (!reply.isJsonObject() || !(reply.getAsJsonObject().get("result") instanceof JsonObject result)) {
                return Optional.empty();
            }
            final String deviceType = string(result, "device_type");
            final String model = string(result, "device_model");
            final String deviceId = string(result, "device_id");
            final String encryptType = result.get("mgt_encrypt_schm") instanceof JsonObject scheme
                    ? string(scheme, "encrypt_type")
                    : "";
            if (!deviceType.startsWith(IOT_PREFIX) || !KLAP.equals(encryptType) || model.isEmpty()
                    || deviceId.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Device(model, deviceId.toUpperCase(Locale.ROOT), string(result, "mac"),
                    string(result, "hw_ver")));
        } catch (JsonParseException e) {
            return Optional.empty();
        }
    }

    /**
     * @param deviceId device id as {@code get_sysinfo} reports it
     * @return the upper case hash that the discovery reply carries for this device
     */
    static String hashDeviceId(final String deviceId) {
        try {
            return HexFormat.of().withUpperCase()
                    .formatHex(MessageDigest.getInstance("MD5").digest(deviceId.getBytes(StandardCharsets.UTF_8)));
        } catch (final GeneralSecurityException e) {
            // Every Java runtime has to provide MD5
            throw new IllegalStateException(e);
        }
    }

    private static String string(final JsonObject object, final String member) {
        final @Nullable JsonElement value = object.get(member);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
    }
}
