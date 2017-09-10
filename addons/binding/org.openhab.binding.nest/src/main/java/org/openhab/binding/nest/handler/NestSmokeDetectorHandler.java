/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static org.openhab.binding.nest.NestBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.SmokeDetector.BatteryHealth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The smoke detector handler, it handles the data from nest for the smoke detector.
 *
 * @author David Bennett - Initial Contribution
 */
public class NestSmokeDetectorHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(NestSmokeDetectorHandler.class);
    private SmokeDetector lastData;

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

    /**
     * Updates the smoke detector on data from nest.
     *
     * @param smokeDetector The current smoke detector state
     */
    public void updateSmokeDetector(SmokeDetector smokeDetector) {
        logger.debug("Updating smoke detector {}", smokeDetector.getDeviceId());
        if (lastData == null || !lastData.equals(smokeDetector)) {
            updateState(CHANNEL_UI_COLOR_STATE, new StringType(smokeDetector.getUiColorState().toString()));
            updateState(CHANNEL_LOW_BATTERY,
                    smokeDetector.getBatteryHealth() == BatteryHealth.OK ? OnOffType.OFF : OnOffType.ON);
            updateState(CHANNEL_CO_ALARM_STATE, new StringType(smokeDetector.getCoAlarmState().toString()));
            updateState(CHANNEL_SMOKE_ALARM_STATE, new StringType(smokeDetector.getSmokeAlarmState().toString()));
            updateState(CHANNEL_MANUAL_TEST_ACTIVE, smokeDetector.isManualTestActive() ? OnOffType.ON : OnOffType.OFF);

            if (smokeDetector.isOnline()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }

            // Setup the properties for this device.
            updateProperty(PROPERTY_ID, smokeDetector.getDeviceId());
            updateProperty(PROPERTY_FIRMWARE_VERSION, smokeDetector.getSoftwareVersion());
        } else {
            logger.debug("Nothing to update, same as before.");
        }
    }

}
