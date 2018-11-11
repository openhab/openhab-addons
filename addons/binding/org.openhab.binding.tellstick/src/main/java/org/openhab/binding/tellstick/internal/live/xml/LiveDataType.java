/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    WINDAVERAGE("windaverage"),
    WINDDIRECTION("winddirection"),
    WINDGUST("temp"),
    RAINRATE("rainrate"),
    RAINTOTAL("rainttotal"),
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
