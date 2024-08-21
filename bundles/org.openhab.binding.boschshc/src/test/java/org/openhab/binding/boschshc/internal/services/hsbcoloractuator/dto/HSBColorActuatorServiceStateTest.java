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
package org.openhab.binding.boschshc.internal.services.hsbcoloractuator.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.HSBType;

/**
 * Unit tests for {@link HSBColorActuatorServiceState}.
 *
 * @author David Pace - Initial contribution
 *
 */
class HSBColorActuatorServiceStateTest {

    @Test
    void testToHSBType() {
        HSBColorActuatorServiceState hsbColorActuatorState = new HSBColorActuatorServiceState();
        hsbColorActuatorState.rgb = -12427; // r = 255, g = 207, b = 117
        assertEquals(HSBType.fromRGB(255, 207, 117), hsbColorActuatorState.toHSBType());
    }
}
