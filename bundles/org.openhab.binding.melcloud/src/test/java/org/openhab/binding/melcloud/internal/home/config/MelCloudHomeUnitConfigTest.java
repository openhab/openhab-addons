/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.melcloud.internal.home.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MelCloudHomeUnitConfig#toString()}: {@code unitId} must never appear unmasked.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class MelCloudHomeUnitConfigTest {

    @Test
    void whenUnitIdIsSetThenToStringMasksIt() {
        // Arrange
        MelCloudHomeUnitConfig config = new MelCloudHomeUnitConfig();
        config.unitId = "298f815e-79cc-45dc-81ff-5add6771ac62";

        // Act
        String result = config.toString();

        // Assert
        assertFalse(result.contains("298f815e-79cc-45dc-81ff-5add6771ac62"));
        assertEquals("[unitId=...ac62]", result);
    }

    @Test
    void whenUnitIdIsEmptyThenToStringFullyRedacts() {
        // Arrange
        MelCloudHomeUnitConfig config = new MelCloudHomeUnitConfig();

        // Act
        String result = config.toString();

        // Assert
        assertEquals("[unitId=***]", result);
    }
}
