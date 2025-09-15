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
package org.openhab.binding.homekit.internal.hap_services;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.session.SecureSession;
import org.openhab.binding.homekit.internal.transport.HttpTransport;

/**
 * HTTP client methods for reading and writing HomeKit accessory characteristics over a secure session.
 * It handles encryption and decryption of requests and responses.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class CharacteristicReadWriteService {

    private static final String JSON_TEMPLATE = "{\"%s\":[{\"aid\":%s,\"iid\":%s,\"value\":%s}]}";

    private final SecureSession session;
    private final HttpTransport httpTransport;
    private final String baseUrl;

    public CharacteristicReadWriteService(HttpTransport httpTransport, SecureSession session, String baseUrl) {
        this.httpTransport = httpTransport;
        this.session = session;
        this.baseUrl = baseUrl;
    }

    /**
     * Reads a characteristic from the accessory.
     *
     * @param aid Accessory ID
     * @param iid Instance ID
     * @return JSON response as String
     * @throws Exception on communication or encryption errors
     */
    public String readCharacteristic(String aid, String iid) throws Exception {
        String endpoint = "%s?id=%s.%s".formatted(ENDPOINT_CHARACTERISTICS, aid, iid);
        byte[] encrypted = httpTransport.get(baseUrl, endpoint, CONTENT_TYPE_HAP);
        byte[] decrypted = session.decrypt(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * Writes a characteristic to the accessory.
     *
     * @param aid Accessory ID
     * @param iid Instance ID
     * @param value Value to write (String, Number, Boolean)
     * @throws Exception on communication or encryption errors
     */
    public void writeCharacteristic(String aid, String iid, Object value) throws Exception {
        String json = JSON_TEMPLATE.formatted(ENDPOINT_CHARACTERISTICS, aid, iid, formatValue(value));
        byte[] encrypted = session.encrypt(json.getBytes());
        httpTransport.put(baseUrl, ENDPOINT_CHARACTERISTICS, CONTENT_TYPE_HAP, encrypted);
    }

    /*
     * Formats the value for JSON. Strings are quoted, numbers and booleans are not.
     */
    private String formatValue(Object value) {
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }
        return "\"" + value.toString() + "\"";
    }
}
