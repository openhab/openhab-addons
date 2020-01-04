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
 * This POJO represents the "params" of one WiZ Lighting Response "params" are
 * returned for sync and heartbeat packets
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
@NonNullByDefault
public class WizResponseContents {
    /*
     * Bulb metadata in the param of all incoming messages from the bulb
     */
    // The MAC address of the bulb
    public @Nullable String mac;
    // The source of a command
    // Possibilites: "udp" (in response to UDP command) "hb" (regular heartbeat)
    public @Nullable String src;
}
