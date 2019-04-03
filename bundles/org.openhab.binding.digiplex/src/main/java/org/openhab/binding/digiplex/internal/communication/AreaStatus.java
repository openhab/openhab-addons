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
package org.openhab.binding.digiplex.internal.communication;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;

/**
 * Area status, as received for the Area Status requests
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public enum AreaStatus {
    DISARMED('D'),
    ARMED('A'),
    ARMED_FORCE('F'),
    ARMED_STAY('S'),
    ARMED_INSTANT('I'),
    UNKNOWN('u');

    private char indicator;

    AreaStatus(char indicator) {
        this.indicator = indicator;
    }

    public OpenClosedType toOpenClosedType() {
        return this == DISARMED ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
    }

    public static AreaStatus fromMessage(char indicator) {
        return Arrays.stream(AreaStatus.values()).filter(type -> type.indicator == indicator).findFirst()
                .orElse(UNKNOWN);
    }

    public StringType toStringType() {
        return new StringType(this.toString());
    }

}
