/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.binding.nest.NestBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.SmokeDetector.BatteryHealth;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Thermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The smoke detector handler, it handles the data from Nest for the smoke detector.
 *
 * @author David Bennett - Initial Contribution
 */
public class NestSmokeDetectorHandler extends NestBaseHandler {
    private Logger logger = LoggerFactory.getLogger(NestSmokeDetectorHandler.class);

    public NestSmokeDetectorHandler(Thing thing) {
        super(thing);
    }

    /**
     * Handles any incoming command requests.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // There is nothing to update on the smoke detector.
    }

    @Override
    public void onNewNestThermostatData(Thermostat thermostat) {
        // ignore we are not a thermostat handler
    }

    @Override
    public void onNewNestCameraData(Camera camera) {
        // ignore we are not a camera handler
    }

    @Override
    public void onNewNestSmokeDetectorData(SmokeDetector smokeDetector) {
        if (isNotHandling(smokeDetector)) {
            return;
        }

        logger.debug("Updating smoke detector {}", smokeDetector.getDeviceId());
        updateState(CHANNEL_UI_COLOR_STATE, new StringType(smokeDetector.getUiColorState().toString()));
        updateState(CHANNEL_LOW_BATTERY,
                smokeDetector.getBatteryHealth() == BatteryHealth.OK ? OnOffType.OFF : OnOffType.ON);
        updateState(CHANNEL_CO_ALARM_STATE, new StringType(smokeDetector.getCoAlarmState().toString()));
        updateState(CHANNEL_SMOKE_ALARM_STATE, new StringType(smokeDetector.getSmokeAlarmState().toString()));
        updateState(CHANNEL_MANUAL_TEST_ACTIVE, smokeDetector.isManualTestActive() ? OnOffType.ON : OnOffType.OFF);

        updateStatus(smokeDetector.isOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

        // Setup the properties for this device.
        updateProperty(PROPERTY_FIRMWARE_VERSION, smokeDetector.getSoftwareVersion());
    }

    @Override
    public void onNewNestStructureData(Structure struct) {
        // ignore we are not a structure handler
    }

}
