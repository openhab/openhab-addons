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
package org.openhab.binding.visualcrossing.internal.api;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.openhab.binding.visualcrossing.internal.api.VisualCrossingApi.UnitGroup.METRIC;
import static org.openhab.binding.visualcrossing.internal.api.VisualCrossingApiTestConst.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.visualcrossing.internal.api.rest.RestClient;

import com.google.gson.Gson;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
@ExtendWith({ MockitoExtension.class })
class VisualCrossingApiTest {
    @Nullable
    VisualCrossingApi api;
    @Mock
    @Nullable
    RestClient restClient;

    @BeforeEach
    void setUp() {
        api = new VisualCrossingApi("https://weather.visualcrossing.com", "xyz", requireNonNull(restClient),
                new Gson());
    }

    @ParameterizedTest(name = "{index}: should parse full JSON for address {1}")
    @MethodSource
    void parse(String json, String address) throws Exception {
        // given
        given(requireNonNull(restClient)
                .get("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"//
                        + "wroc%C5%82aw%2Cpoland"//
                        + "?key=xyz"//
                        + "&contentType=json"//
                        + "&unitGroup=metric"//
                        + "&lang=en"))
                .willReturn(json);

        // when
        var timeline = requireNonNull(api).timeline("wrocław,poland", METRIC, "en", null, null);

        // then
        assertThat(timeline.address()).isEqualTo(address);
    }

    static Stream<Arguments> parse() throws IOException {
        return Stream.of(Arguments.of(readFullJson("weather-response.json"), "wrocław,poland"),
                Arguments.of(readFullJson("weather-response-251231.json"), "krakow,poland"));
    }

    @Test
    @DisplayName("should parse full JSON")
    void parseFullJson() throws Exception {
        // given
        given(requireNonNull(restClient)
                .get("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"//
                        + "wroc%C5%82aw%2Cpoland"//
                        + "?key=xyz"//
                        + "&contentType=json"//
                        + "&unitGroup=metric"//
                        + "&lang=en"))
                .willReturn(readFullJson("weather-response.json"));

        // when
        var timeline = requireNonNull(api).timeline("wrocław,poland", METRIC, "en", null, null);

        // then
        assertThat(timeline).isEqualTo(FULL_JSON_RESPONSE);
    }

    private static String readFullJson(String resourceName) throws IOException {
        try (var inputStream = VisualCrossingApiTestConst.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalStateException("Resource not found: " + resourceName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
