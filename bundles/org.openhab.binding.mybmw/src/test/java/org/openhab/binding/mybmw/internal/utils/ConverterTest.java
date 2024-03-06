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
package org.openhab.binding.mybmw.internal.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * @author Martin Grassl - initial contribution
 */
@NonNullByDefault
public class ConverterTest {
    @Test
    void testToTitleCase() {
        assertEquals("Closed", Converter.toTitleCase("CLOSED"));
        assertEquals("Secured", Converter.toTitleCase("SECURED"));
        assertEquals("Undef", Converter.toTitleCase(null));
        assertEquals("Undef", Converter.toTitleCase(""));
        assertEquals("Secured", Converter.toTitleCase("SECURED"));
        assertEquals("Secured", Converter.toTitleCase("SECURED"));
        assertEquals("Test Data", Converter.toTitleCase("test_data"));
        assertEquals("Test-Data", Converter.toTitleCase("test-data"));
        assertEquals("Test Data", Converter.toTitleCase("test data"));
    }

    @Test
    void testDateConversion() {
        State state = Converter.zonedToLocalDateTime(null, ZoneId.systemDefault());
        assertTrue(state instanceof UnDefType);
        state = Converter.zonedToLocalDateTime("", ZoneId.systemDefault());
        assertTrue(state instanceof UnDefType);
        state = Converter.zonedToLocalDateTime("2023-01-18", ZoneId.systemDefault());
        assertTrue(state instanceof UnDefType);
        state = Converter.zonedToLocalDateTime("2023-01-18T18:07:59.076Z", ZoneId.systemDefault());
        assertTrue(state instanceof DateTimeType);
        state = Converter.zonedToLocalDateTime("2023-10-28T17:41:17Z", ZoneId.systemDefault());
        assertTrue(state instanceof DateTimeType);
    }
}
