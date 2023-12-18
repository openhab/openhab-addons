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
package org.openhab.binding.modbus.sungrow.internal;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Definition of Sungrow system state codes.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public enum SystemState {

    STOP("Stop", 0x0002),
    STANDBY("Standby", 0x0008),
    INIT_STANDBY("Initial standby", 0x0010),
    STARTUP("Startup", 0x0020),
    RUNNING("Running", 0x0040),
    FAULT("Fault", 0x0100),
    MAINTAIN_MODE("Running in maintain mode", 0x0400),
    FORCED_MODE("Running in forced mode", 0x0800),
    OFF_GRID("Running in off-grid mode", 0x1000),
    RESTARTING("Restarting", 0x2501),
    EXTERNAL_EMS("Running in External EMS mode", 0x4000);

    private static final Map<Integer, SystemState> CODE_MAPPING = initMapping();

    private static Map<Integer, SystemState> initMapping() {
        return Arrays.stream(SystemState.values())
                .collect(Collectors.toMap(SystemState::getStateCode, Function.identity()));
    }

    /**
     * Returns the {@link SystemState} for the given state code.
     */
    @Nullable
    public static SystemState getByStateCode(int stateCode) {
        return CODE_MAPPING.get(stateCode);
    }

    private final int stateCode;
    private final String description;

    SystemState(String description, int stateCode) {
        this.stateCode = stateCode;
        this.description = description;
    }

    public int getStateCode() {
        return stateCode;
    }

    public String getDescription() {
        return description;
    }
}
