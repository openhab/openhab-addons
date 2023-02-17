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

package org.openhab.binding.phc.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The {@link StringUtils} class defines some static string utility methods
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class StringUtilsTest {

    @Test
    public void padLeft() {
        assertEquals("000000", StringUtils.padLeft("", 6, "0"));
        assertEquals("000000", StringUtils.padLeft(null, 6, "0"));
        assertEquals("000teststr", StringUtils.padLeft("teststr", 10, "0"));
        assertEquals("AAAAAAp3RF@CT", StringUtils.padLeft("p3RF@CT", 13, "A"));
        assertEquals("nopaddingshouldhappen", StringUtils.padLeft("nopaddingshouldhappen", 21, "x"));
        assertEquals("LongerStringThenMinSize", StringUtils.padLeft("LongerStringThenMinSize", 10, "x"));
    }
}
