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
package org.openhab.binding.boschshc.internal.services.alarm;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.alarm.dto.AlarmServiceState;
import org.openhab.binding.boschshc.internal.services.alarm.dto.AlarmState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;

/**
 * Unit tests for {@link AlarmService}.
 * 
 * @author David Pace - Initial contribution
 */
@NonNullByDefault
class AlarmServiceTest {

    private @NonNullByDefault({}) AlarmService fixture;

    @BeforeEach
    public void beforeEach() {
        fixture = new AlarmService();
    }

    @Test
    void testHandleCommandValidCommand() throws BoschSHCException {
        AlarmServiceState state = fixture.handleCommand(new StringType("IDLE_OFF"));
        assertNotNull(state);
        assertSame(AlarmState.IDLE_OFF, state.value);
    }

    @Test
    void testHandleCommandInvalidCommand() {
        assertThrows(BoschSHCException.class, () -> fixture.handleCommand(new DecimalType(0)));
    }
}
