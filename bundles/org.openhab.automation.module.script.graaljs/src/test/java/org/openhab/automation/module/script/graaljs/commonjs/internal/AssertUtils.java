/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.automation.module.script.graaljs.commonjs.internal;

import org.graalvm.polyglot.Value;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Assertion utilities for Graal objects.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public class AssertUtils {
    static void assertValueEquals(Value v1, Value v2) {
        assertEquals(v1.toString(), v2.toString());
    }

    static void assertAbsent(Optional<?> opt) {
        assertFalse(opt.isPresent());
    }

    static void assertPresent(Optional<?> opt) {
        assertTrue(opt.isPresent());
    }
}
