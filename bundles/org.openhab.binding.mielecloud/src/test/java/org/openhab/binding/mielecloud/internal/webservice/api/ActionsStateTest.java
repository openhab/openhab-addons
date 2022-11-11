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
package org.openhab.binding.mielecloud.internal.webservice.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Actions;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Light;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProcessAction;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class ActionsStateTest {
    private static final String DEVICE_IDENTIFIER = "003458276345";

    @Test
    public void testGetDeviceIdentifierReturnsDeviceIdentifier() {
        // given:
        Actions actions = mock(Actions.class);
        ActionsState actionsState = new ActionsState(DEVICE_IDENTIFIER, actions);

        // when:
        String deviceId = actionsState.getDeviceIdentifier();

        // then:
        assertEquals(DEVICE_IDENTIFIER, deviceId);
    }

    @Test
    public void testReturnValuesWhenActionsIsNull() {
        // given:
        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, null);

        // when:
        boolean canBeStarted = actionState.canBeStarted();
        boolean canBeStopped = actionState.canBeStopped();
        boolean canBePaused = actionState.canBePaused();
        boolean canStartSupercooling = actionState.canStartSupercooling();
        boolean canStopSupercooling = actionState.canStopSupercooling();
        boolean canContolSupercooling = actionState.canContolSupercooling();
        boolean canStartSuperfreezing = actionState.canStartSuperfreezing();
        boolean canStopSuperfreezing = actionState.canStopSuperfreezing();
        boolean canControlSuperfreezing = actionState.canControlSuperfreezing();
        boolean canEnableLight = actionState.canEnableLight();
        boolean canDisableLight = actionState.canDisableLight();

        // then:
        assertFalse(canBeStarted);
        assertFalse(canBeStopped);
        assertFalse(canBePaused);
        assertFalse(canStartSupercooling);
        assertFalse(canStopSupercooling);
        assertFalse(canContolSupercooling);
        assertFalse(canStartSuperfreezing);
        assertFalse(canStopSuperfreezing);
        assertFalse(canControlSuperfreezing);
        assertFalse(canEnableLight);
        assertFalse(canDisableLight);
    }

    @Test
    public void testReturnValuesWhenProcessActionIsEmpty() {
        // given:
        Actions actions = mock(Actions.class);
        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, null);
        when(actions.getProcessAction()).thenReturn(Collections.emptyList());

        // when:
        boolean canBeStarted = actionState.canBeStarted();
        boolean canBeStopped = actionState.canBeStopped();
        boolean canBePaused = actionState.canBePaused();
        boolean canStartSupercooling = actionState.canStartSupercooling();
        boolean canStopSupercooling = actionState.canStopSupercooling();
        boolean canStartSuperfreezing = actionState.canStartSuperfreezing();
        boolean canStopSuperfreezing = actionState.canStopSuperfreezing();

        // then:
        assertFalse(canBeStarted);
        assertFalse(canBeStopped);
        assertFalse(canBePaused);
        assertFalse(canStartSupercooling);
        assertFalse(canStopSupercooling);
        assertFalse(canStartSuperfreezing);
        assertFalse(canStopSuperfreezing);
    }

    @Test
    public void testReturnValuesWhenLightIsEmpty() {
        // given:
        Actions actions = mock(Actions.class);
        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, null);
        when(actions.getLight()).thenReturn(Collections.emptyList());

        // when:
        boolean canEnableLight = actionState.canEnableLight();
        boolean canDisableLight = actionState.canDisableLight();

        // then:
        assertFalse(canEnableLight);
        assertFalse(canDisableLight);
    }

    @Test
    public void testReturnValueWhenProcessActionStartIsAvailable() {
        // given:
        Actions actions = mock(Actions.class);
        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, actions);
        when(actions.getProcessAction()).thenReturn(Collections.singletonList(ProcessAction.START));

        // when:
        boolean canBeStarted = actionState.canBeStarted();
        boolean canBeStopped = actionState.canBeStopped();

        // then:
        assertTrue(canBeStarted);
        assertFalse(canBeStopped);
    }

    @Test
    public void testReturnValueWhenProcessActionStopIsAvailable() {
        // given:
        Actions actions = mock(Actions.class);
        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, actions);
        when(actions.getProcessAction()).thenReturn(Collections.singletonList(ProcessAction.STOP));

        // when:
        boolean canBeStarted = actionState.canBeStarted();
        boolean canBeStopped = actionState.canBeStopped();

        // then:
        assertFalse(canBeStarted);
        assertTrue(canBeStopped);
    }

    @Test
    public void testReturnValueWhenProcessActionStartAndStopAreAvailable() {
        // given:
        Actions actions = mock(Actions.class);
        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, actions);
        when(actions.getProcessAction()).thenReturn(List.of(ProcessAction.START, ProcessAction.STOP));

        // when:
        boolean canBeStarted = actionState.canBeStarted();
        boolean canBeStopped = actionState.canBeStopped();

        // then:
        assertTrue(canBeStarted);
        assertTrue(canBeStopped);
    }

    @Test
    public void testReturnValueWhenProcessActionStartSupercoolIsAvailable() {
        // given:
        Actions actions = mock(Actions.class);
        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, actions);
        when(actions.getProcessAction()).thenReturn(Collections.singletonList(ProcessAction.START_SUPERCOOLING));

        // when:
        boolean canStartSupercooling = actionState.canStartSupercooling();
        boolean canContolSupercooling = actionState.canContolSupercooling();

        // then:
        assertTrue(canStartSupercooling);
        assertTrue(canContolSupercooling);
    }

    @Test
    public void testReturnValueWhenProcessActionStartSuperfreezeIsAvailable() {
        // given:
        Actions actions = mock(Actions.class);
        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, actions);
        when(actions.getProcessAction()).thenReturn(Collections.singletonList(ProcessAction.START_SUPERFREEZING));

        // when:
        boolean canStartSuperfreezing = actionState.canStartSuperfreezing();
        boolean canControlSuperfreezing = actionState.canControlSuperfreezing();

        // then:
        assertTrue(canStartSuperfreezing);
        assertTrue(canControlSuperfreezing);
    }

    @Test
    public void testReturnValueWhenLightEnableIsAvailable() {
        // given:
        Actions actions = mock(Actions.class);
        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, actions);
        when(actions.getLight()).thenReturn(Collections.singletonList(Light.ENABLE));

        // when:
        boolean canEnableLight = actionState.canEnableLight();

        // then:
        assertTrue(canEnableLight);
    }

    @Test
    public void testReturnValueWhenLightDisableIsAvailable() {
        // given:
        Actions actions = mock(Actions.class);
        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, actions);
        when(actions.getLight()).thenReturn(Collections.singletonList(Light.DISABLE));

        // when:
        boolean canDisableLight = actionState.canDisableLight();

        // then:
        assertTrue(canDisableLight);
    }

    @Test
    public void testCanControlLightReturnsTrueWhenLightCanBeEnabled() {
        // given:
        Actions actions = mock(Actions.class);
        when(actions.getLight()).thenReturn(Collections.singletonList(Light.ENABLE));

        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, actions);

        // when:
        boolean canControlLight = actionState.canControlLight();

        // then:
        assertTrue(canControlLight);
    }

    @Test
    public void testCanControlLightReturnsTrueWhenLightCanBeDisabled() {
        // given:
        Actions actions = mock(Actions.class);
        when(actions.getLight()).thenReturn(Collections.singletonList(Light.DISABLE));

        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, actions);

        // when:
        boolean canControlLight = actionState.canControlLight();

        // then:
        assertTrue(canControlLight);
    }

    @Test
    public void testCanControlLightReturnsTrueWhenLightCanBeEnabledAndDisabled() {
        // given:
        Actions actions = mock(Actions.class);
        when(actions.getLight()).thenReturn(Arrays.asList(Light.ENABLE, Light.DISABLE));

        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, actions);

        // when:
        boolean canControlLight = actionState.canControlLight();

        // then:
        assertTrue(canControlLight);
    }

    @Test
    public void testCanControlLightReturnsFalseWhenNoLightOptionIsAvailable() {
        // given:
        Actions actions = mock(Actions.class);
        when(actions.getLight()).thenReturn(new LinkedList<Light>());

        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, actions);

        // when:
        boolean canControlLight = actionState.canControlLight();

        // then:
        assertFalse(canControlLight);
    }

    @Test
    public void testNoProgramCanBeSetWhenNoProgramIdIsPresent() {
        // given:
        Actions actions = mock(Actions.class);
        when(actions.getProgramId()).thenReturn(Collections.emptyList());

        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, actions);

        // when:
        boolean canSetActiveProgram = actionState.canSetActiveProgramId();

        // then:
        assertFalse(canSetActiveProgram);
    }

    @Test
    public void testProgramIdCanBeSetWhenProgramIdIsPresent() {
        // given:
        Actions actions = mock(Actions.class);
        when(actions.getProgramId()).thenReturn(Collections.singletonList(1));

        ActionsState actionState = new ActionsState(DEVICE_IDENTIFIER, actions);

        // when:
        boolean canSetActiveProgram = actionState.canSetActiveProgramId();

        // then:
        assertTrue(canSetActiveProgram);
    }
}
