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
package org.openhab.binding.boschshc.internal.services.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.serialization.GsonUtils;
import org.openhab.binding.boschshc.internal.services.userstate.dto.UserStateServiceState;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Test class
 *
 * @author Christian Oeing - Initial contribution
 */
class TestState extends BoschSHCServiceState {
    public TestState() {
        super("testState");
    }
}

/**
 * Test class
 *
 * @author Christian Oeing - Initial contribution
 */
class TestState2 extends BoschSHCServiceState {
    public TestState2() {
        super("testState2");
    }
}

/**
 * Unit tests for BoschSHCServiceStateTest
 *
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
class BoschSHCServiceStateTest {

    @Test
    void fromJsonNullStateForDifferentType() {
        var state = BoschSHCServiceState.fromJson(
                GsonUtils.DEFAULT_GSON_INSTANCE.fromJson("{\"@type\":\"differentState\"}", JsonObject.class),
                TestState.class);
        assertEquals(null, state);
    }

    @Test
    void fromJsonStateObjectForValidJson() {
        var state = BoschSHCServiceState.fromJson(
                GsonUtils.DEFAULT_GSON_INSTANCE.fromJson("{\"@type\":\"testState\"}", JsonObject.class),
                TestState.class);
        assertNotEquals(null, state);
    }

    /**
     * This checks for a bug we had where the expected type stayed the same for different state classes
     */
    @Test
    void fromJsonStateObjectForValidJsonAfterOtherState() {
        BoschSHCServiceState.fromJson(
                GsonUtils.DEFAULT_GSON_INSTANCE.fromJson("{\"@type\":\"testState\"}", JsonObject.class),
                TestState.class);
        var state2 = BoschSHCServiceState.fromJson(
                GsonUtils.DEFAULT_GSON_INSTANCE.fromJson("{\"@type\":\"testState2\"}", JsonObject.class),
                TestState2.class);
        assertNotEquals(null, state2);
    }

    @Test
    void fromJsonReturnsUserStateServiceStateForValidJson() {
        var state = BoschSHCServiceState.fromJson(new JsonPrimitive("false"), UserStateServiceState.class);
        assertNotEquals(null, state);
        assertTrue(state.getClass().isAssignableFrom(UserStateServiceState.class));
        assertFalse(state.isState());
    }
}
