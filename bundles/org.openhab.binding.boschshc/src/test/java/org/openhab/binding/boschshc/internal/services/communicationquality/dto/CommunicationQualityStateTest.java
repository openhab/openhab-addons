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
package org.openhab.binding.boschshc.internal.services.communicationquality.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;

/**
 * Unit tests for {@link CommunicationQualityState}.
 * 
 * @author David Pace - Initial contribution
 *
 */
class CommunicationQualityStateTest {

    @Test
    void testToSystemSignalStrength() {
        assertEquals(new DecimalType(0), CommunicationQualityState.UNKNOWN.toSystemSignalStrength());
        assertEquals(new DecimalType(0), CommunicationQualityState.FETCHING.toSystemSignalStrength());
        assertEquals(new DecimalType(1), CommunicationQualityState.BAD.toSystemSignalStrength());
        assertEquals(new DecimalType(2), CommunicationQualityState.MEDIUM.toSystemSignalStrength());
        assertEquals(new DecimalType(3), CommunicationQualityState.NORMAL.toSystemSignalStrength());
        assertEquals(new DecimalType(4), CommunicationQualityState.GOOD.toSystemSignalStrength());
    }
}
