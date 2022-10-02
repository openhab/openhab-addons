/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sunspec.internal;

import java.util.Arrays;
import java.util.Optional;

/**
 * Possible values for an inverter's status field
 *
 * @author Nagy Attila Gábor - Initial contribution
 */
public enum InverterStatus {

    OFF(1),
    SLEEP(2),
    STARTING(3),
    ON(4),
    THROTTLED(5),
    SHUTTING_DOWN(6),
    FAULT(7),
    STANDBY(8),
    UNKNOWN(-1);

    private final int code;

    InverterStatus(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static InverterStatus getByCode(int code) {
        Optional<InverterStatus> status = Arrays.stream(InverterStatus.values()).filter(s -> s.code == code)
                .findFirst();
        return status.orElse(InverterStatus.UNKNOWN);
    }
}
