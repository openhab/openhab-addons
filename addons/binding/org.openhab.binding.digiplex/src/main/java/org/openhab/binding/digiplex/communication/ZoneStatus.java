/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.communication;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OpenClosedType;

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
