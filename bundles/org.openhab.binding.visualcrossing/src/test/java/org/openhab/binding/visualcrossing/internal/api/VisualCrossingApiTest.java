/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    @DisplayName("should parse full JSON")
    void parseFullJson() throws VisualCrossingAuthException, VisualCrossingApiException, VisualCrossingRateException {
        // given
        // noinspection DataFlowIssue
        given(requireNonNull(restClient)
                .get("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"//
                        + "wroc%C5%82aw%2Cpoland"//
                        + "?key=xyz"//
                        + "&contentType=json"//
                        + "&unitGroup=metric"//
                        + "&lang=en"))
                .willReturn(FULL_JSON);

        // when
        var timeline = requireNonNull(api).timeline("wrocław,poland", METRIC, "en", null, null);

        // then
        assertThat(timeline).isEqualTo(FULL_JSON_RESPONSE);
    }
}
