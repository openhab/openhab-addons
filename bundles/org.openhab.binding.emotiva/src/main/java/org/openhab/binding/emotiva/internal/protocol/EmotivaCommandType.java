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
package org.openhab.binding.emotiva.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.emotiva.internal.dto.EmotivaControlDTO;

/**
 * Enum types for commands to send to Emotiva devices. Used by {@link EmotivaControlRequest} to create correct
 * {@link EmotivaControlDTO} command message.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public enum EmotivaCommandType {

    // Not in use
    NONE,
    // Sets a specific number or string value
    SET,
    // Two state toggle
    TOGGLE,
    // Cycles to multiple states
    CYCLE,
    // Speaker preset
    SPEAKER_PRESET,
    NUMBER,
    // Input sources
    SOURCE,
    // Audio mode
    MODE,
    // +1/-1
    UP_DOWN_SINGLE,
    // +0.5/-0.5
    UP_DOWN_HALF,
    // Source with possible user assigned name
    USER_SOURCE,
    ZONE2_SOURCE

}
