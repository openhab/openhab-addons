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
package org.openhab.binding.ism8.server;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SubServiceType} contains all supported sub-service types
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public class SubServiceType {
    public static final byte SetDatapointValueReq = (byte) 0x06;
    public static final byte SetDatapointValueRes = (byte) 0x86;
    public static final byte DatapointValueWrite = (byte) 0xC1;
    public static final byte RequestAllDatapoints = (byte) 0xD0;
}
