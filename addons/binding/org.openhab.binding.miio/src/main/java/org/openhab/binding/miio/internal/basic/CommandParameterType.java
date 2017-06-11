/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal.basic;

/**
 * Various types of parameters to be send
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public enum CommandParameterType {
    NONE("none"),
    EMPTY("empty"),
    ONOFF("onoff"),
    STRING("string"),
    CUSTOMSTRING("customstring"),
    NUMBER("number"),
    COLOR("color"),
    UNKNOWN("unknown");

    private String text;

    CommandParameterType(String text) {
        this.text = text;
    }

    public static CommandParameterType fromString(String text) {
        for (CommandParameterType param : CommandParameterType.values()) {
            if (param.text.equalsIgnoreCase(text)) {
                return param;
            }
        }
        return UNKNOWN;
    }
}
