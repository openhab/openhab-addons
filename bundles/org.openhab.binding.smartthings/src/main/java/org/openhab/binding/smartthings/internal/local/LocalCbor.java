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
package org.openhab.binding.smartthings.internal.local;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Bounded CBOR representations for the appliance's OIC 1.1 interface.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
final class LocalCbor {
    static final int MAX_BODY_SIZE = 65536;
    private static final ObjectMapper MAPPER = new ObjectMapper(
            CBORFactory.builder().streamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(64)
                    .maxStringLength(MAX_BODY_SIZE).maxNumberLength(100).build()).build());
    private static final ObjectMapper JSON = new ObjectMapper();

    private LocalCbor() {
    }

    static JsonElement decode(byte[] bytes) throws IOException {
        if (bytes.length == 0 || bytes.length > MAX_BODY_SIZE) {
            throw new IOException("Invalid CBOR representation size");
        }
        try (JsonParser parser = MAPPER.createParser(bytes)) {
            JsonNode value = MAPPER.readTree(parser);
            if (value == null || parser.nextToken() != null) {
                throw new IOException("Expected one CBOR representation");
            }
            try {
                return com.google.gson.JsonParser.parseString(value.toString());
            } catch (JsonParseException e) {
                throw new IOException("Invalid CBOR representation");
            }
        } catch (IOException e) {
            throw new IOException("Invalid CBOR representation");
        }
    }

    static byte[] encode(JsonElement value) throws IOException {
        try {
            byte[] result = MAPPER.writeValueAsBytes(JSON.readTree(value.toString()));
            if (result.length > MAX_BODY_SIZE) {
                throw new IOException("CBOR representation is too large");
            }
            return result;
        } catch (IOException e) {
            throw new IOException("Cannot encode appliance CBOR representation");
        }
    }
}
