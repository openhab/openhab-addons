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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utilities for working with properties.
 * 
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class PropertyUtils {

    public static final String EMPTY = "<empty>";

    public static boolean isPropertyEmpty(Map<String, String> properties, String propName) {
        if (properties.containsKey(propName)) {
            return EMPTY.equals(properties.get(propName));
        }
        return true;
    }

    public static boolean hasProperty(Map<String, String> properties, String propName) {
        if (properties.containsKey(propName)) {
            return !EMPTY.equals(properties.get(propName));
        }
        return false;
    }
}
