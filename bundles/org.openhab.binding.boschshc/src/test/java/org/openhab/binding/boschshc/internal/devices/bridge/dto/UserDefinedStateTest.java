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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for UserDefinedStateTest
 *
 * @author Patrick Gell - Initial contribution
 */
@NonNullByDefault
public class UserDefinedStateTest {

    public static UserDefinedState createTestState(final String id) {
        UserDefinedState userState = new UserDefinedState();
        userState.setId(id);
        userState.setState(true);
        userState.setName("test user state");
        return userState;
    }

    private @NonNullByDefault({}) UserDefinedState fixture;
    private final String testId = UUID.randomUUID().toString();

    @BeforeEach
    void beforeEach() {
        fixture = createTestState(testId);
    }

    @Test
    void testIsValid() {
        assertTrue(UserDefinedState.isValid(fixture));
    }

    @Test
    void testToString() {
        assertEquals(
                String.format("UserDefinedState{id='%s', name='test user state', state=true, type='userDefinedState'}",
                        testId),
                fixture.toString());
    }
}
