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
package org.openhab.binding.boschshc.internal.services.alarm.dto;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AlarmState}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class AlarmStateTest {
    @Test
    void testFromValidIdentifier() {
        assertSame(AlarmState.PRIMARY_ALARM, AlarmState.from("PRIMARY_ALARM"));
    }

    @Test
    void testFromInvalidIdentifier() {
        assertSame(AlarmState.IDLE_OFF, AlarmState.from("INVALID"));
    }
}
