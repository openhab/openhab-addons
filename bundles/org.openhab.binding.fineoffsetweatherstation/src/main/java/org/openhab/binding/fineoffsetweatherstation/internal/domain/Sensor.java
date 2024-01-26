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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import static org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus.Type.LEVEL;
import static org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus.Type.LEVEL_OR_DC;
import static org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus.Type.LOW_HIGH;
import static org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus.Type.VOLTAGE_BROAD_STEPS;
import static org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus.Type.VOLTAGE_FINE_STEPS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus;

/**
 * The Sensors supported by the gateway.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public enum Sensor {
    WH24(LOW_HIGH),
    WH25(LOW_HIGH),
    WH26(LOW_HIGH),
    WH31(LOW_HIGH),
    WH34(VOLTAGE_FINE_STEPS),
    WH35(VOLTAGE_FINE_STEPS),
    WH40(VOLTAGE_BROAD_STEPS),
    WH41(LEVEL_OR_DC),
    WH45(LEVEL_OR_DC),
    WH51(VOLTAGE_BROAD_STEPS),
    WH55(LEVEL),
    WH57(LEVEL),
    WH65(LOW_HIGH),
    WH68(VOLTAGE_FINE_STEPS),
    WH80(VOLTAGE_FINE_STEPS),
    WH90(VOLTAGE_FINE_STEPS);

    private final BatteryStatus.Type batteryStatusTpe;

    Sensor(BatteryStatus.Type batteryStatusTpe) {
        this.batteryStatusTpe = batteryStatusTpe;
    }

    public BatteryStatus getBatteryStatus(byte data) {
        return new BatteryStatus(batteryStatusTpe, data);
    }
}
