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
package org.openhab.binding.digiplex.internal.communication;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OpenClosedType;

/**
 * Zone status, as received from the alarm system
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public enum ZoneStatus {
    CLOSED('C'),
    OPEN('O'),
    TAMPERED('T'),
    FIRE_LOOP_TROUBLE('F'),
    UNKNOWN('u');

    private char indicator;

    ZoneStatus(char indicator) {
        this.indicator = indicator;
    }

    public OpenClosedType toOpenClosedType() {
        return this == CLOSED ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
    }

    public static ZoneStatus fromMessage(char indicator) {
        return Arrays.stream(ZoneStatus.values()).filter(type -> type.indicator == indicator).findFirst()
                .orElse(UNKNOWN);
    }
}
