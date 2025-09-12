/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of HomeKit characteristic data types.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum DataFormatType {
    BOOL,
    UINT8,
    UINT16,
    UINT32,
    UINT64,
    INT,
    FLOAT,
    STRING,
    TLV8,
    DATA;

    public static DataFormatType from(String dataFormat) throws IllegalArgumentException {
        return valueOf(dataFormat.toUpperCase());
    }
}
