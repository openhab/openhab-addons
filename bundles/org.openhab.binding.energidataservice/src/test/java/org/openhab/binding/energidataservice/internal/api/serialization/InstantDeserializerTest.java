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
package org.openhab.binding.energidataservice.internal.api.serialization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Tests for {@link InstantDeserializer}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class InstantDeserializerTest {

    private final Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();

    @Test
    void instantWhenInvalidShouldThrowJsonParseException() {
        assertThrows(JsonParseException.class, () -> {
            gson.fromJson("\"invalid\"", Instant.class);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"2023-04-17T20:38:01Z\"", "\"2023-04-17T20:38:01\"" })
    void instantWhenValidShouldParse(String input) {
        assertThat((@Nullable Instant) gson.fromJson(input, Instant.class),
                is(equalTo(Instant.ofEpochSecond(1681763881))));
    }
}
