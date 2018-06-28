/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.model;

/**
 * Definition of the Homematic types.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public enum HmValueType {
    BOOL,
    ACTION,
    FLOAT,
    INTEGER,
    ENUM,
    STRING,
    UNKNOWN,
    DATETIME;

    /**
     * Parses the string and returns the HmType object.
     */
    public static HmValueType parse(String type) {
        if (type == null) {
            return UNKNOWN;
        } else if (BOOL.toString().equals(type)) {
            return BOOL;
        } else if (ACTION.toString().equals(type)) {
            return ACTION;
        } else if (FLOAT.toString().equals(type)) {
            return FLOAT;
        } else if (INTEGER.toString().equals(type)) {
            return INTEGER;
        } else if (ENUM.toString().equals(type)) {
            return ENUM;
        } else if (STRING.toString().equals(type)) {
            return STRING;
        } else if (DATETIME.toString().equals(type)) {
            return DATETIME;
        } else {
            return UNKNOWN;
        }
    }

}
