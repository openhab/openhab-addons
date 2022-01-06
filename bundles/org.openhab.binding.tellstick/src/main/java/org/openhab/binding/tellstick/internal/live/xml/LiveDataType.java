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
package org.openhab.binding.tellstick.internal.live.xml;

/**
 * This enum is used to describe the value types in the Live API from telldus.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public enum LiveDataType {
    HUMIDITY("humidity"),
    TEMPERATURE("temp"),
    WINDAVERAGE("wavg"),
    WINDDIRECTION("wdir"),
    WINDGUST("wgust"),
    RAINRATE("rrate"),
    RAINTOTAL("rtot"),
    WATT("watt"),
    LUMINATION("lum"),
    UNKOWN("unkown");

    private String name;

    LiveDataType(String name) {
        this.name = name;
    }

    public static LiveDataType fromName(String name) {
        LiveDataType result = LiveDataType.UNKOWN;
        for (LiveDataType m : LiveDataType.values()) {
            if (m.name.equals(name)) {
                result = m;
                break;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
