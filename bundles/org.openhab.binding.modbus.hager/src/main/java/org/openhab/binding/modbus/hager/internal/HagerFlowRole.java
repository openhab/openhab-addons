/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.hager.internal;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.hager.internal.modbus.HagerFlowEmsRegisterMap;
import org.openhab.binding.modbus.hager.internal.modbus.HagerFlowEvseRegisterMap;
import org.openhab.binding.modbus.hager.internal.modbus.HagerFlowPowermeterRegisterMap;
import org.openhab.binding.modbus.hager.internal.modbus.HagerFlowSgReadyRegisterMap;
import org.openhab.binding.modbus.hager.internal.modbus.HagerFlowStorageRegisterMap;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HagerFlowRole} is one logical device of a Hager Flow system. The Hager Flow XEM470 exposes them as
 * separate Modbus unit ids, which is why every role is a Thing of its own with its own slave id.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public enum HagerFlowRole {

    EMS(HagerFlowBindingConstants.THING_TYPE_EMS, HagerFlowEmsRegisterMap.ALL, 0, 247),
    EVSE(HagerFlowBindingConstants.THING_TYPE_EVSE, HagerFlowEvseRegisterMap.ALL, 1, 8),
    STORAGE(HagerFlowBindingConstants.THING_TYPE_STORAGE, HagerFlowStorageRegisterMap.ALL, 0, 247),
    POWERMETER(HagerFlowBindingConstants.THING_TYPE_POWERMETER, HagerFlowPowermeterRegisterMap.ALL, 30, 37),
    SGREADY(HagerFlowBindingConstants.THING_TYPE_SGREADY, HagerFlowSgReadyRegisterMap.ALL, 50, 59);

    private static final Map<ThingTypeUID, HagerFlowRole> BY_THING_TYPE = Stream.of(values())
            .collect(Collectors.toUnmodifiableMap(HagerFlowRole::thingType, role -> role));

    private final ThingTypeUID thingType;
    private final List<Register> registers;
    private final int minSlaveId;
    private final int maxSlaveId;

    HagerFlowRole(ThingTypeUID thingType, List<Register> registers, int minSlaveId, int maxSlaveId) {
        this.thingType = thingType;
        this.registers = registers;
        this.minSlaveId = minSlaveId;
        this.maxSlaveId = maxSlaveId;
    }

    public ThingTypeUID thingType() {
        return thingType;
    }

    public List<Register> registers() {
        return registers;
    }

    /**
     * Looks up the role of a Thing type.
     *
     * @param thingType the Thing type to look up
     * @return the role, or empty if the Thing type does not belong to this binding
     */
    public static Optional<HagerFlowRole> of(ThingTypeUID thingType) {
        return Optional.ofNullable(BY_THING_TYPE.get(thingType));
    }

    /**
     * Validates the Thing configuration against the value range of this role.
     *
     * @param slaveId configured Modbus unit id
     * @param refreshMillis configured poll interval in milliseconds
     * @return a description of the problem, or empty if the configuration is valid
     */
    public Optional<String> validate(int slaveId, int refreshMillis) {
        if (slaveId < minSlaveId || slaveId > maxSlaveId) {
            return Optional.of(String.format("Slave ID must be between %d and %d", minSlaveId, maxSlaveId));
        }
        if (refreshMillis < HagerFlowBindingConstants.MIN_REFRESH_MS) {
            return Optional.of(
                    String.format("Refresh interval must be at least %d ms", HagerFlowBindingConstants.MIN_REFRESH_MS));
        }
        return Optional.empty();
    }
}
