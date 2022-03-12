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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import static org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus.Type.LEVEL;
import static org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus.Type.LEVEL_OR_DC;
import static org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus.Type.LOW_HIGH;
import static org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus.Type.VOLTAGE_BROAD_STEPS;
import static org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus.Type.VOLTAGE_FINE_STEPS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus;

/**
 * The Sensors supported by the gateway.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public enum Sensor {
    /**
     * wh24 + wh65 share the same id, they are distinguished by user set flag, see also {@link Command#CMD_READ_SSSS}
     */
    WH24((byte) 0, LOW_HIGH),
    WH65((byte) 0, LOW_HIGH),
    // also wh69
    WH68((byte) 1, VOLTAGE_FINE_STEPS),
    WH80((byte) 2, VOLTAGE_FINE_STEPS),
    WH40((byte) 3, VOLTAGE_BROAD_STEPS),
    WH25((byte) 4, LOW_HIGH),
    WH26((byte) 5, LOW_HIGH),
    WH31_CH1((byte) 6, LOW_HIGH),
    WH31_CH2((byte) 7, LOW_HIGH),
    WH31_CH3((byte) 8, LOW_HIGH),
    WH31_CH4((byte) 9, LOW_HIGH),
    WH31_CH5((byte) 10, LOW_HIGH),
    WH31_CH6((byte) 11, LOW_HIGH),
    WH31_CH7((byte) 12, LOW_HIGH),
    WH31_CH8((byte) 13, LOW_HIGH),
    WH51_CH1((byte) 14, VOLTAGE_BROAD_STEPS),
    WH51_CH2((byte) 15, VOLTAGE_BROAD_STEPS),
    WH51_CH3((byte) 16, VOLTAGE_BROAD_STEPS),
    WH51_CH4((byte) 17, VOLTAGE_BROAD_STEPS),
    WH51_CH5((byte) 18, VOLTAGE_BROAD_STEPS),
    WH51_CH6((byte) 19, VOLTAGE_BROAD_STEPS),
    WH51_CH7((byte) 20, VOLTAGE_BROAD_STEPS),
    WH51_CH8((byte) 21, VOLTAGE_BROAD_STEPS),
    WH41_CH1((byte) 22, LEVEL_OR_DC),
    WH41_CH2((byte) 23, LEVEL_OR_DC),
    WH41_CH3((byte) 24, LEVEL_OR_DC),
    WH41_CH4((byte) 25, LEVEL_OR_DC),
    WH57((byte) 26, LEVEL),
    WH55_CH1((byte) 27, LEVEL),
    WH55_CH2((byte) 28, LEVEL),
    WH55_CH3((byte) 29, LEVEL),
    WH55_CH4((byte) 30, LEVEL),
    WH34_CH1((byte) 31, VOLTAGE_FINE_STEPS),
    WH34_CH2((byte) 32, VOLTAGE_FINE_STEPS),
    WH34_CH3((byte) 33, VOLTAGE_FINE_STEPS),
    WH34_CH4((byte) 34, VOLTAGE_FINE_STEPS),
    WH34_CH5((byte) 35, VOLTAGE_FINE_STEPS),
    WH34_CH6((byte) 36, VOLTAGE_FINE_STEPS),
    WH34_CH7((byte) 37, VOLTAGE_FINE_STEPS),
    WH34_CH8((byte) 38, VOLTAGE_FINE_STEPS),
    WH45((byte) 39, LEVEL_OR_DC),
    WH35_CH1((byte) 40, VOLTAGE_FINE_STEPS),
    WH35_CH2((byte) 41, VOLTAGE_FINE_STEPS),
    WH35_CH3((byte) 42, VOLTAGE_FINE_STEPS),
    WH35_CH4((byte) 43, VOLTAGE_FINE_STEPS),
    WH35_CH5((byte) 44, VOLTAGE_FINE_STEPS),
    WH35_CH6((byte) 45, VOLTAGE_FINE_STEPS),
    WH35_CH7((byte) 46, VOLTAGE_FINE_STEPS),
    WH35_CH8((byte) 47, VOLTAGE_FINE_STEPS),
    WH90((byte) 48, VOLTAGE_FINE_STEPS);

    private static final Map<Byte, List<Sensor>> SENSOR_LOOKUP = new HashMap<>();

    static {
        for (Sensor sensor : values()) {
            SENSOR_LOOKUP.computeIfAbsent(sensor.id, ArrayList::new).add(sensor);
        }
    }

    private final BatteryStatus.Type batteryStatusTpe;
    private final byte id;

    Sensor(byte id, BatteryStatus.Type batteryStatusTpe) {
        this.batteryStatusTpe = batteryStatusTpe;
        this.id = id;
    }

    public static @Nullable List<Sensor> forIndex(byte idx) {
        return SENSOR_LOOKUP.get(idx);
    }

    public BatteryStatus getBatteryStatus(byte data) {
        return new BatteryStatus(batteryStatusTpe, data);
    }
}
