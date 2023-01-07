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
package org.openhab.binding.lutron.internal.protocol.lip;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Target device type enum. Used to annotate LutronCommand objects so the LEAP bridge can translate them.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public enum TargetType {
    BLIND,
    BRIDGE,
    CCO,
    DIMMER,
    FAN,
    GREENMODE,
    GROUP,
    KEYPAD,
    SHADE,
    SWITCH,
    SYSVAR,
    TIMECLOCK,
    VIRTUALKEYPAD;
}
