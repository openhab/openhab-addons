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
package org.openhab.binding.nobohub.internal.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.time.Month;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit test for ModelHelper class.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class ModelHelperTest {

    @Test
    public void testParseJavaStringNoSpace() {
        assertEquals("NoSpace", ModelHelper.toJavaString("NoSpace"));
    }

    @Test
    public void testParseJavaStringNormalSpace() {
        assertEquals("Contains Space", ModelHelper.toJavaString("Contains Space"));
    }

    @Test
    public void testParseJavaStringNoBreakSpace() {
        assertEquals("Contains NoBreak Space", ModelHelper.toJavaString("Contains" + (char) 160 + "NoBreak Space"));
    }

    @Test
    public void testGenerateNoboStringNoSpace() {
        assertEquals("NoSpace", ModelHelper.toHubString("NoSpace"));
    }

    @Test
    public void testGenerateNoboStringNormalSpace() {
        assertEquals("Contains" + (char) 160 + "NoBreak", ModelHelper.toHubString("Contains" + (char) 160 + "NoBreak"));
    }

    @Test
    public void testGenerateNoboStringNoBreakSpace() {
        assertEquals("Contains" + (char) 160 + "NoBreak" + (char) 160 + "Space",
                ModelHelper.toHubString("Contains NoBreak Space"));
    }

    @Test
    public void testParseNull() throws NoboDataException {
        assertNull(ModelHelper.toJavaDate("-1"));
    }

    @Test
    public void testParseDate() throws NoboDataException {
        LocalDateTime date = LocalDateTime.of(2020, Month.JANUARY, 22, 19, 30);
        assertEquals(date, ModelHelper.toJavaDate("202001221930"));
    }

    @Test()
    public void testParseIllegalDate() {
        assertThrows(NoboDataException.class, () -> ModelHelper.toJavaDate("20201322h1930"));
    }

    @Test
    public void testGenerateNoboDate() {
        LocalDateTime date = LocalDateTime.of(2020, Month.JANUARY, 22, 19, 30);
        assertEquals("202001221930", ModelHelper.toHubDateMinutes(date));
    }
}
