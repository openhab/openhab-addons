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
package org.openhab.binding.intesis.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IntesisHomeModeEnum} contains informations for translating device modes into internally used numbers.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public enum IntesisHomeModeEnum {
    AUTO(0),
    HEAT(1),
    DRY(2),
    FAN(3),
    COOL(4);

    private final int mode;

    private IntesisHomeModeEnum(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }
}
