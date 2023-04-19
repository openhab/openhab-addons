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

package org.openhab.binding.sensibo.internal.util;

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
    public void splitByCharacterType() {
        assertArrayEquals(new String[0], StringUtils.splitByCharacterType(null));
        assertArrayEquals(new String[0], StringUtils.splitByCharacterType(""));
        assertArrayEquals(new String[] { "ab", " ", "de", " ", "fg" }, StringUtils.splitByCharacterType("ab de fg"));
        assertArrayEquals(new String[] { "ab", "   ", "de", " ", "fg" },
                StringUtils.splitByCharacterType("ab   de fg"));
        assertArrayEquals(new String[] { "ab", ":", "cd", ":", "ef" }, StringUtils.splitByCharacterType("ab:cd:ef"));
        assertArrayEquals(new String[] { "number", "5" }, StringUtils.splitByCharacterType("number5"));
        assertArrayEquals(new String[] { "foo", "Bar" }, StringUtils.splitByCharacterType("fooBar"));
        assertArrayEquals(new String[] { "foo", "200", "Bar" }, StringUtils.splitByCharacterType("foo200Bar"));
        assertArrayEquals(new String[] { "ASF", "Rules" }, StringUtils.splitByCharacterType("ASFRules"));
    }
}
