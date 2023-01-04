/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.dscalarm.internal.handler;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to map thing types from the binding string to an ENUM value.
 *
 * @author Russell Stephens - Initial Contribution
 */
public enum DSCAlarmThingType {
    PANEL("panel"),
    PARTITION("partition"),
    ZONE("zone"),
    KEYPAD("keypad");

    private String label;

    /**
     * Lookup map to get a DSCAlarmDeviceType from its label.
     */
    private static Map<String, DSCAlarmThingType> labelToDSCAlarmThingType;

    /**
     * Constructor.
     *
     * @param label
     */
    private DSCAlarmThingType(String label) {
        this.label = label;
    }

    /**
     * Creates a HashMap that maps the string label to a DSCAlarmDeviceType enum value.
     */
    private static void initMapping() {
        labelToDSCAlarmThingType = new HashMap<>();
        for (DSCAlarmThingType s : values()) {
            labelToDSCAlarmThingType.put(s.label, s);
        }
    }

    /**
     * Returns the label of the DSCAlarmItemType Values enumeration.
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
    public static DSCAlarmThingType getDSCAlarmThingType(String label) {
        if (labelToDSCAlarmThingType == null) {
            initMapping();
        }
        return labelToDSCAlarmThingType.get(label);
    }
}
