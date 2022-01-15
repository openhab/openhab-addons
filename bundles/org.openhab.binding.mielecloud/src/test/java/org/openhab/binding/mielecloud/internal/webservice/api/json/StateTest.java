/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class StateTest {
    @Test
    public void testNullRemainingTimeInJsonCausesRemainingTimeListToBeNull() throws IOException {
        // given:
        String json = "{ \"remainingTime\": null, \"startTime\": [0, 0], \"targetTemperature\": [{}], \"temperature\": [{}], \"elapsedTime\": [0, 0] }";

        // when:
        State state = new Gson().fromJson(json, State.class);

        // then:
        assertFalse(state.getRemainingTime().isPresent());
    }

    @Test
    public void testNullStartTimeInJsonCausesStartTimeListToBeNull() throws IOException {
        // given:
        String json = "{ \"remainingTime\": [0, 0], \"startTime\": null, \"targetTemperature\": [{}], \"temperature\": [{}], \"elapsedTime\": [0, 0] }";

        // when:
        State state = new Gson().fromJson(json, State.class);

        // then:
        assertFalse(state.getStartTime().isPresent());
    }

    @Test
    public void testNullElapsedTimeInJsonCausesElapsedTimeListToBeNull() throws IOException {
        // given:
        String json = "{ \"remainingTime\": [0, 0], \"startTime\": [0, 0], \"targetTemperature\": [{}], \"temperature\": [{}], \"elapsedTime\": null }";

        // when:
        State state = new Gson().fromJson(json, State.class);

        // then:
        assertFalse(state.getElapsedTime().isPresent());
    }

    @Test
    public void testNullTargetTemperatureInJsonIsConvertedToEmptyList() throws IOException {
        // given:
        String json = "{ \"remainingTime\": [0, 0], \"startTime\": [0, 0], \"targetTemperature\": null, \"temperature\": [{}], \"elapsedTime\": [0, 0] }";

        // when:
        State state = new Gson().fromJson(json, State.class);

        // then:
        assertNotNull(state.getTargetTemperature());
        assertTrue(state.getTargetTemperature().isEmpty());
    }

    @Test
    public void testNullTemperatureInJsonIsConvertedToEmptyList() throws IOException {
        // given:
        String json = "{ \"remainingTime\": [0, 0], \"startTime\": [0, 0], \"targetTemperature\": [{}], \"temperature\": null, \"elapsedTime\": [0, 0] }";

        // when:
        State state = new Gson().fromJson(json, State.class);

        // then:
        assertNotNull(state.getTemperature());
        assertTrue(state.getTemperature().isEmpty());
    }
}
