/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;

/**
 * Tests property utility functions.
 * 
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class PropertyUtilsTest {
    @Test
    public void isPropertyEmptyTrueForEmptyMap() {
        Map<String, String> properties = new HashMap<String, String>();
        boolean result = PropertyUtils.isPropertyEmpty(properties, "foo");
        assertTrue(result);
    }

    @Test
    public void isPropertyEmptyTrueForMapWithMagicKeyValuePair() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("foo", PropertyUtils.EMPTY);
        boolean result = PropertyUtils.isPropertyEmpty(properties, "foo");
        assertTrue(result);
    }

    @Test
    public void isPropertyEmptyFalseForMapWithNonMatchingKey() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("foo", PropertyUtils.EMPTY);
        boolean result = PropertyUtils.isPropertyEmpty(properties, "bar");
        assertTrue(result);
    }

    @Test
    public void isPropertyEmptyFalseForMapWithKeyButOtherValue() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("foo", "bar");
        boolean result = PropertyUtils.isPropertyEmpty(properties, "foo");
        assertFalse(result);
    }

    @Test
    public void hasPropertyFalseForEmptyMap() {
        Map<String, String> properties = new HashMap<String, String>();
        boolean result = PropertyUtils.hasProperty(properties, "foo");
        assertFalse(result);
    }

    @Test
    public void hasPropertyFalseForMapWithMagicKeyValuePair() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("foo", PropertyUtils.EMPTY);
        boolean result = PropertyUtils.hasProperty(properties, "foo");
        assertFalse(result);
    }

    @Test
    public void hasPropertyFalseForMapWithNonMatchingKey() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("foo", PropertyUtils.EMPTY);
        boolean result = PropertyUtils.hasProperty(properties, "bar");
        assertFalse(result);
    }

    @Test
    public void hasPropertyTrueForMapWithKeyButOtherValue() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("foo", "bar");
        boolean result = PropertyUtils.hasProperty(properties, "foo");
        assertTrue(result);
    }
}
