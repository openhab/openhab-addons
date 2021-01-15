/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author Krzysztof Goworek - Initial contribution
 */
public class StringUtilsTest {

    @Test
    public void testIsEmpty() {
        assertFalse(StringUtils.isEmpty("foobar"));
        assertFalse(StringUtils.isEmpty("  "));
        assertTrue(StringUtils.isEmpty(""));
        assertTrue(StringUtils.isEmpty(null));
    }

    @Test
    public void testIsNotEmpty() {
        assertTrue(StringUtils.isNotEmpty("foobar"));
        assertTrue(StringUtils.isNotEmpty("  "));
        assertFalse(StringUtils.isNotEmpty(""));
        assertFalse(StringUtils.isNotEmpty(null));
    }

    @Test
    public void testIsBlank() {
        assertFalse(StringUtils.isBlank("foobar"));
        assertTrue(StringUtils.isBlank("  "));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(null));
    }

    @Test
    public void testIsNotBlank() {
        assertTrue(StringUtils.isNotBlank("foobar"));
        assertFalse(StringUtils.isNotBlank("  "));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank(null));
    }
}
