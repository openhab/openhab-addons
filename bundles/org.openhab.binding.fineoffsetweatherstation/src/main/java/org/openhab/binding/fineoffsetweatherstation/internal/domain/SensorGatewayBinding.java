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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus;

/**
 * The binding of a sensor to the gateway.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public enum SensorGatewayBinding {
    /**
     * wh24 + wh65 share the same id, they are distinguished by user set flag, see also {@link Command#CMD_READ_SSSS}
     */
    WH24((byte) 0, Sensor.WH24, null),
    WH65((byte) 0, Sensor.WH65, null),
    // also wh69
    WH68((byte) 1, Sensor.WH68, null),
    WH80((byte) 2, Sensor.WH80, null),
    WH40((byte) 3, Sensor.WH40, null),
    WH25((byte) 4, Sensor.WH25, null),
    WH26((byte) 5, Sensor.WH26, null),
    WH31_CH1((byte) 6, Sensor.WH31, 1),
    WH31_CH2((byte) 7, Sensor.WH31, 2),
    WH31_CH3((byte) 8, Sensor.WH31, 3),
    WH31_CH4((byte) 9, Sensor.WH31, 4),
    WH31_CH5((byte) 10, Sensor.WH31, 5),
    WH31_CH6((byte) 11, Sensor.WH31, 6),
    WH31_CH7((byte) 12, Sensor.WH31, 7),
    WH31_CH8((byte) 13, Sensor.WH31, 8),
    WH51_CH1((byte) 14, Sensor.WH51, 1),
    WH51_CH2((byte) 15, Sensor.WH51, 2),
    WH51_CH3((byte) 16, Sensor.WH51, 3),
    WH51_CH4((byte) 17, Sensor.WH51, 4),
    WH51_CH5((byte) 18, Sensor.WH51, 5),
    WH51_CH6((byte) 19, Sensor.WH51, 6),
    WH51_CH7((byte) 20, Sensor.WH51, 7),
    WH51_CH8((byte) 21, Sensor.WH51, 8),
    WH41_CH1((byte) 22, Sensor.WH41, 1),
    WH41_CH2((byte) 23, Sensor.WH41, 2),
    WH41_CH3((byte) 24, Sensor.WH41, 3),
    WH41_CH4((byte) 25, Sensor.WH41, 4),
    WH57((byte) 26, Sensor.WH57, null),
    WH55_CH1((byte) 27, Sensor.WH55, 1),
    WH55_CH2((byte) 28, Sensor.WH55, 2),
    WH55_CH3((byte) 29, Sensor.WH55, 3),
    WH55_CH4((byte) 30, Sensor.WH55, 4),
    WH34_CH1((byte) 31, Sensor.WH34, 1),
    WH34_CH2((byte) 32, Sensor.WH34, 2),
    WH34_CH3((byte) 33, Sensor.WH34, 3),
    WH34_CH4((byte) 34, Sensor.WH34, 4),
    WH34_CH5((byte) 35, Sensor.WH34, 5),
    WH34_CH6((byte) 36, Sensor.WH34, 6),
    WH34_CH7((byte) 37, Sensor.WH34, 7),
    WH34_CH8((byte) 38, Sensor.WH34, 8),
    WH45((byte) 39, Sensor.WH45, null),
    WH35_CH1((byte) 40, Sensor.WH35, 1),
    WH35_CH2((byte) 41, Sensor.WH35, 2),
    WH35_CH3((byte) 42, Sensor.WH35, 3),
    WH35_CH4((byte) 43, Sensor.WH35, 4),
    WH35_CH5((byte) 44, Sensor.WH35, 5),
    WH35_CH6((byte) 45, Sensor.WH35, 6),
    WH35_CH7((byte) 46, Sensor.WH35, 7),
    WH35_CH8((byte) 47, Sensor.WH35, 8),
    WH90((byte) 48, Sensor.WH90, null);

    private static final Map<Byte, List<SensorGatewayBinding>> SENSOR_LOOKUP = new HashMap<>();

    static {
        for (SensorGatewayBinding sensorGatewayBinding : values()) {
            List<SensorGatewayBinding> bindings = SENSOR_LOOKUP.computeIfAbsent(sensorGatewayBinding.id,
                    ArrayList::new);
            // noinspection ConstantConditions
            if (bindings != null) {
                bindings.add(sensorGatewayBinding);
            }
        }
    }

    private final byte id;
    private final Sensor sensor;
    private final @Nullable Integer channel;

    SensorGatewayBinding(byte id, Sensor sensor, @Nullable Integer channel) {
        this.id = id;
        this.sensor = sensor;
        this.channel = channel;
    }

    public static @Nullable List<SensorGatewayBinding> forIndex(byte idx) {
        return SENSOR_LOOKUP.get(idx);
    }

    public BatteryStatus getBatteryStatus(byte data) {
        return sensor.getBatteryStatus(data);
    }

    public Sensor getSensor() {
        return sensor;
    }

    public @Nullable Integer getChannel() {
        return channel;
    }
}
