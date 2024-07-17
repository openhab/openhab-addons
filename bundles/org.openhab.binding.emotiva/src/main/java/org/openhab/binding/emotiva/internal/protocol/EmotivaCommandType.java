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

/**
 * Enum types for commands to send to Emotiva devices. Used by {@link EmotivaControlRequest} to create correct
 * {@link org.openhab.binding.emotiva.internal.dto.EmotivaControlDTO} command message.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public enum EmotivaCommandType {

    CYCLE, // Cycles to multiple states
    NONE, // Unknown or not in use commands
    NUMBER,
    MENU_CONTROL,
    MODE, // Audio mode
    SET, // Sets a specific number or string value
    SOURCE_MAIN_ZONE, // Main Zone sources
    SOURCE_USER, // Source with possible user assigned name
    SOURCE_ZONE2, // Zone 2 sources
    SPEAKER_PRESET, // Speaker preset
    TOGGLE, // Two state toggle
    UP_DOWN_SINGLE, // +1/-1
    UP_DOWN_HALF // +0.5/-0.5

}
