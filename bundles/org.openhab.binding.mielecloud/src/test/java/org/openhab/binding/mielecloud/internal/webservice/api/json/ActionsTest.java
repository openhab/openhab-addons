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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class ActionsTest {
    @Test
    public void testNullProcessActionInJsonIsConvertedToEmptyList() throws IOException {
        // given:
        String json = "{ \"processAction\": null, \"light\": [1], \"startTime\": [ [0, 0],[23,59] ] }";

        // when:
        Actions actions = new Gson().fromJson(json, Actions.class);

        // then:
        assertNotNull(actions.getProcessAction());
        assertTrue(actions.getProcessAction().isEmpty());
    }

    @Test
    public void testNullLightInJsonIsConvertedToEmptyList() throws IOException {
        // given:
        String json = "{ \"processAction\": [1], \"light\": null, \"startTime\": [ [0, 0],[23,59] ] }";

        // when:
        Actions actions = new Gson().fromJson(json, Actions.class);

        // then:
        assertNotNull(actions.getLight());
        assertTrue(actions.getLight().isEmpty());
    }

    @Test
    public void testNullStartTimeInJsonIsReturnedAsNull() throws IOException {
        // given:
        String json = "{ \"processAction\": [1], \"light\": [1], \"startTime\": null }";

        // when:
        Actions actions = new Gson().fromJson(json, Actions.class);

        // then:
        assertFalse(actions.getStartTime().isPresent());
    }

    @Test
    public void testIdListIsEmptyWhenProgramIdFieldIsMissing() {
        // given:
        String json = "{ \"processAction\": [1] }";

        // when:
        Actions actions = new Gson().fromJson(json, Actions.class);

        // then:
        assertTrue(actions.getProgramId().isEmpty());
    }

    @Test
    public void testIdListIsEmptyWhenProgramIdFieldIsNull() {
        // given:
        String json = "{ \"programId\": null }";

        // when:
        Actions actions = new Gson().fromJson(json, Actions.class);

        // then:
        assertTrue(actions.getProgramId().isEmpty());
    }

    @Test
    public void testIdListContainsEntriesWhenProgramIdFieldIsPresent() {
        // given:
        String json = "{ \"programId\": [1,2,3,4] }";

        // when:
        Actions actions = new Gson().fromJson(json, Actions.class);

        // then:
        assertEquals(Arrays.asList(1, 2, 3, 4), actions.getProgramId());
    }

    @Test
    public void processActionContainsSingleEntryWhenThereIsOneProcessAction() {
        // given:
        String json = "{ \"processAction\": [1] }";

        // when:
        Actions actions = new Gson().fromJson(json, Actions.class);

        // then:
        assertEquals(List.of(ProcessAction.START), actions.getProcessAction());
    }

    @Test
    public void processActionContainsTwoEntriesWhenThereAreTwoProcessActions() {
        // given:
        String json = "{ \"processAction\": [1,2] }";

        // when:
        Actions actions = new Gson().fromJson(json, Actions.class);

        // then:
        assertEquals(List.of(ProcessAction.START, ProcessAction.STOP), actions.getProcessAction());
    }
}
