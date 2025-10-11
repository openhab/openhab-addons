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
import org.openhab.binding.homekit.internal.transport.IpTransport;

/**
 * HTTP client methods for reading and writing HomeKit accessory characteristics over a secure session.
 * It handles encryption and decryption of requests and responses.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class CharacteristicReadWriteClient {

    private final IpTransport ipTransport;

    public CharacteristicReadWriteClient(IpTransport ipTransport) {
        this.ipTransport = ipTransport;
    }

    /**
     * Reads characteristic(s) from the accessory.
     *
     * @param query the query string e.g. "1.10,1.11" for aid 1 and iid 10 and 11
     * @return JSON response as String
     * @throws Exception on communication or encryption errors
     */
    public String readCharacteristic(String query) throws Exception {
        String endpoint = "%s?id=%s".formatted(ENDPOINT_CHARACTERISTICS, query);
        byte[] result = ipTransport.get(endpoint, CONTENT_TYPE_HAP);
        return new String(result, StandardCharsets.UTF_8);
    }

    /**
     * Writes a characteristic to the accessory.
     *
     * @param json the JSON string to write.
     * @throws Exception on communication or encryption errors
     */
    public void writeCharacteristic(String json) throws Exception {
        ipTransport.put(ENDPOINT_CHARACTERISTICS, CONTENT_TYPE_HAP, json.getBytes(StandardCharsets.UTF_8));
    }
}
