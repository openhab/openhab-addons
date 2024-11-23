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
package org.openhab.binding.netatmo.internal.handler.channelhelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.netatmo.internal.api.data.EventType;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class EventCameraChannelHelperTest {

    private @NonNullByDefault({}) EventCameraChannelHelper helper;

    @BeforeEach
    public void before() {
        helper = new EventCameraChannelHelper(Collections.emptySet());
    }

    @Test
    public void testInternalGetHomeEventGroupSubEvent() {
        State state = helper.internalGetHomeEvent(CHANNEL_EVENT_TYPE, GROUP_SUB_EVENT, new HomeEvent());
        assertTrue(state instanceof StringType);
        assertEquals(EventType.UNKNOWN.name(), state.toString());
    }

    @Test
    public void testInternalGetHomeEventGroupDoorbellSubEvent() {
        State state = helper.internalGetHomeEvent(CHANNEL_EVENT_TYPE, GROUP_DOORBELL_SUB_EVENT, new HomeEvent());
        assertTrue(state instanceof StringType);
        assertEquals(EventType.UNKNOWN.name(), state.toString());
    }

    @Test
    public void testInternalGetHomeEventGroupDoorbellStatus() {
        State state = helper.internalGetHomeEvent(CHANNEL_EVENT_TYPE, GROUP_DOORBELL_STATUS, new HomeEvent());
        // Only sub-event groups are handled by EventCameraChannelHelper. GROUP_DOORBELL_STATUS isn't a sub-event group.
        assertNull(state);
    }
}
