/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ism8.server;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SubServiceType} contains all supported sub-service types
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public class SubServiceType {
    /**
     * Sub-Service: Set data-point value request.
     *
     */
    public static final byte SET_DATAPOINT_VALUE_REQUEST = (byte) 0x06;

    /**
     * Sub-Service: Set data-point value result.
     *
     */
    public static final byte SET_DATAPOINT_VALUE_RESULT = (byte) 0x86;

    /**
     * Sub-Service: Write data-point value.
     *
     */
    public static final byte DATAPOINT_VALUE_WRITE = (byte) 0xC1;

    /**
     * Sub-Service: Request all data-points.
     *
     */
    public static final byte REQUEST_ALL_DATAPOINTS = (byte) 0xD0;
}
