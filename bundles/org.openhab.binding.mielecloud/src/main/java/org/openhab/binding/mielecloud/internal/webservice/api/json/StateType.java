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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the Miele device state.
 *
 * @author Roland Edelhoff - Initial contribution
 */
@NonNullByDefault
public enum StateType {
    OFF(1),
    ON(2),
    PROGRAMMED(3),
    PROGRAMMED_WAITING_TO_START(4),
    RUNNING(5),
    PAUSE(6),
    END_PROGRAMMED(7),
    FAILURE(8),
    PROGRAMME_INTERRUPTED(9),
    IDLE(10),
    RINSE_HOLD(11),
    SERVICE(12),
    SUPERFREEZING(13),
    SUPERCOOLING(14),
    SUPERHEATING(15),
    SUPERCOOLING_SUPERFREEZING(146),
    NOT_CONNECTED(255);

    private static final Map<Integer, StateType> STATE_TYPE_BY_CODE;

    static {
        Map<Integer, StateType> stateTypeByCode = new HashMap<>();
        for (StateType stateType : values()) {
            stateTypeByCode.put(stateType.code, stateType);
        }
        STATE_TYPE_BY_CODE = Collections.unmodifiableMap(stateTypeByCode);
    }

    private final int code;

    private StateType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Optional<StateType> fromCode(int code) {
        return Optional.ofNullable(STATE_TYPE_BY_CODE.get(code));
    }
}
