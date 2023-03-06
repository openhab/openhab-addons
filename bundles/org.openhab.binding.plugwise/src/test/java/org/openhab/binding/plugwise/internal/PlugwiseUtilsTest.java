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

package org.openhab.binding.plugwise.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The {@link PlugwiseUtilsTest} class tests some static string utility methods
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class PlugwiseUtilsTest {

    @Test
    public void upperUnderscoreToLowerCamelBaseTest() {
        assertEquals("nodelimiterpresent", PlugwiseUtils.upperUnderscoreToLowerCamel("NoDelimiterPresent"));
        assertEquals("delimiterIsPresent", PlugwiseUtils.upperUnderscoreToLowerCamel("DeliMiter_iS_PreSent"));
        assertEquals("doubleDelimitersDouble", PlugwiseUtils.upperUnderscoreToLowerCamel("DOUBLE__DELIMITERS__DOUBLE"));
        assertEquals("helloWorld", PlugwiseUtils.upperUnderscoreToLowerCamel("HELLO_WORLD"));
        assertEquals("", PlugwiseUtils.upperUnderscoreToLowerCamel(""));
        assertEquals("", PlugwiseUtils.upperUnderscoreToLowerCamel("_"));
    }
}
