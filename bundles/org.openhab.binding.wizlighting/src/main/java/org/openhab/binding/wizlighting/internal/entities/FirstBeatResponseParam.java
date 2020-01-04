/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This POJO represents the "params" returned in a "firstBeat"
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class FirstBeatResponseParam {
    // The MAC address the response is coming from
    public @Nullable String mac;
    // Home ID of the bulb
    public int homeId;
    // Firmware version of the bulb
    public String fwVersion = "1.15.2";
}
