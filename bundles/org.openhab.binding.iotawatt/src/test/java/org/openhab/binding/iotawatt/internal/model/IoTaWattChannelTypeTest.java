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
package org.openhab.binding.iotawatt.internal.model;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
class IoTaWattChannelTypeTest {

    @Test
    void valueOf_whenUnknownValue_thenThrowException() {
        // given
        final String unknownValue = "unknownValue";

        // when/then
        // noinspection ResultOfMethodCallIgnored
        assertThrows(IllegalArgumentException.class, () -> IoTaWattChannelType.fromOutputUnits(unknownValue));
    }
}
