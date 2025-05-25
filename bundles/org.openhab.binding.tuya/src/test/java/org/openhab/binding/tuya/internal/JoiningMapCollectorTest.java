/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tuya.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tuya.internal.util.JoiningMapCollector;

/**
 * The {@link JoiningMapCollectorTest} is a test class for the {@link JoiningMapCollector} class
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class JoiningMapCollectorTest {
    private static final Map<String, String> INPUT = Map.of( //
            "key1", "value1", //
            "key3", "value3", //
            "key2", "value2");

    @Test
    public void defaultTest() {
        String result = INPUT.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(JoiningMapCollector.joining());
        Assertions.assertEquals("key1value1key2value2key3value3", result);
    }

    @Test
    public void urlTest() {
        String result = INPUT.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(JoiningMapCollector.joining("=", "&"));
        Assertions.assertEquals("key1=value1&key2=value2&key3=value3", result);
    }
}
