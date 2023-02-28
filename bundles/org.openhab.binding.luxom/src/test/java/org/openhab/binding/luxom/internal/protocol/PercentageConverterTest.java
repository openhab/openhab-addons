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
package org.openhab.binding.luxom.internal.protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.luxom.internal.handler.util.PercentageConverter;

/**
 *
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
class PercentageConverterTest {
    @Test
    void hexToPercentage() {
        assertEquals(34, PercentageConverter.getPercentage("057"));
    }

    @Test
    void hexToPercentage100() {
        assertEquals(100, PercentageConverter.getPercentage("0FF"));
    }

    @Test
    void percentageToHex() {
        assertEquals("57", PercentageConverter.getHexRepresentation(34));
    }
}
