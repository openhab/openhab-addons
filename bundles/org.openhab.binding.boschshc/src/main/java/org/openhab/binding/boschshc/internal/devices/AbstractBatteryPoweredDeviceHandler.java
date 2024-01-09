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
package org.openhab.binding.boschshc.internal.devices;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.batterylevel.BatteryLevel;
import org.openhab.binding.boschshc.internal.services.batterylevel.BatteryLevelService;
import org.openhab.core.thing.Thing;

/**
 * Abstract implementation for battery-powered devices.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public abstract class AbstractBatteryPoweredDeviceHandler extends BoschSHCDeviceHandler {

    /**
     * Service to monitor the battery level of the device
     */
    private final BatteryLevelService batteryLevelService;

    protected AbstractBatteryPoweredDeviceHandler(Thing thing) {
        super(thing);
        this.batteryLevelService = new BatteryLevelService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        registerService(batteryLevelService, this::updateChannels, List.of(CHANNEL_BATTERY_LEVEL, CHANNEL_LOW_BATTERY),
                true);
    }

    private void updateChannels(DeviceServiceData deviceServiceData) {
        BatteryLevel batteryLevel = BatteryLevel.fromDeviceServiceData(deviceServiceData);
        super.updateState(CHANNEL_BATTERY_LEVEL, batteryLevel.toState());
        super.updateState(CHANNEL_LOW_BATTERY, batteryLevel.toLowBatteryState());
    }
}
