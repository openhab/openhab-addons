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
package org.openhab.binding.miio.internal.basic;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Various types of parameters to be send
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum CommandParameterType {
    NONE("none"),
    EMPTY("empty"),
    ONOFF("onoff"),
    ONOFFPARA("onoffpara"),
    ONOFFBOOL("onoffbool"),
    ONOFFBOOLSTRING("onoffboolstring"),
    ONOFFNUMBER("onoffnumber"),
    OPENCLOSE("openclose"),
    OPENCLOSENUMBER("openclosenumber"),
    OPENCLOSESWITCH("opencloseswitch"),
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
