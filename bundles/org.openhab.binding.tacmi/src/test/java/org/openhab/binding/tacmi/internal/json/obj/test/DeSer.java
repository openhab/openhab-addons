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
package org.openhab.binding.tacmi.internal.json.obj.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tacmi.internal.json.obj.IO;
import org.openhab.binding.tacmi.internal.json.obj.JsonResponse;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

/**
 * Test the GSON object mapper
 *
 * @author Moritz 'Morty' Strübe - Initial contribution
 *
 */
@NonNullByDefault
class DeSer {
    @Test
    void test() {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        JsonReader reader;
        reader = assertDoesNotThrow(() -> {
            return new JsonReader(new FileReader("src/test/resources/ex.json"));
        });

        JsonResponse resp = gson.fromJson(reader, JsonResponse.class);

        assertEquals(0, resp.statusCode);
        assertEquals(5, resp.header.version);
        var inp = resp.data.inputs.toArray(new IO[0]);
        assertEquals(50.2, inp[0].value.value);
    }
}
