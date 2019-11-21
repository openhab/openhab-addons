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
package org.openhab.binding.caddx.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Used to map thing types from the binding string to a ENUM value.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public enum CaddxThingType {
    PANEL("panel"),
    PARTITION("partition"),
    ZONE("zone"),
    KEYPAD("keypad");

    private String label;

    /**
     * Lookup map to get a CaddxThingType from its label.
     */
    private static Map<String, CaddxThingType> labelToThingType;

    /**
     * Constructor.
     *
     * @param label
     */
    private CaddxThingType(String label) {
        this.label = label;
    }

    /**
     * Creates a HashMap that maps the string label to a CaddxThingType enum value.
     */
    static {
        labelToThingType = new HashMap<>();
        for (CaddxThingType s : values()) {
            labelToThingType.put(s.label, s);
        }
    }

    /**
     * Returns the label of the Caddx AlarmItemType Values enumeration.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Lookup function based on the binding type label. Returns null if the binding type is not found.
     *
     * @param label
     * @return enum value
     */
    public static CaddxThingType getCaddxThingType(String label) {
        return labelToThingType.get(label);
    }
}
