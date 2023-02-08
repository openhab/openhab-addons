/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
