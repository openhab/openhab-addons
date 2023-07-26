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
package org.openhab.binding.exec.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test class for utility methods.
 *
 * @author Leo Siepel - Initial contribution
 */

public class UtilTests {

    @Test
    public void chompTest() {
        assertEquals("", Util.chomp(""));
        assertEquals(null, Util.chomp(null));
        assertEquals("abc ", Util.chomp("abc \r"));
        assertEquals("abc", Util.chomp("abc\n"));
        assertEquals("abc", Util.chomp("abc\r\n"));
        assertEquals("abc\r\n", Util.chomp("abc\r\n\r\n"));
        assertEquals("abc\n", Util.chomp("abc\n\r"));
        assertEquals("abc\n\rabc", Util.chomp("abc\n\rabc"));
        assertEquals("", Util.chomp("\r"));
        assertEquals("", Util.chomp("\n"));
        assertEquals("", Util.chomp("\r\n"));
    }
}
