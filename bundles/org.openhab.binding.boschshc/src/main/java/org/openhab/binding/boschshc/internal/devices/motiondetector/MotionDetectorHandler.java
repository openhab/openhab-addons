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
package org.openhab.binding.boschshc.internal.devices.motiondetector;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_ILLUMINANCE;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_LATEST_MOTION;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.illuminance.IlluminanceService;
import org.openhab.binding.boschshc.internal.services.illuminance.dto.IlluminanceServiceState;
import org.openhab.binding.boschshc.internal.services.latestmotion.LatestMotionService;
import org.openhab.binding.boschshc.internal.services.latestmotion.dto.LatestMotionServiceState;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Thing;

/**
 * Detects every movement through an intelligent combination of passive infra-red technology and an additional
 * temperature sensor.
 *
 * @author Stefan Kästle - Initial contribution
 * @author Christian Oeing - Use service instead of custom logic
 * @author David Pace - Added illuminance channel
 */
@NonNullByDefault
public class MotionDetectorHandler extends AbstractBatteryPoweredDeviceHandler {

    public MotionDetectorHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.createService(LatestMotionService::new, this::updateChannels, List.of(CHANNEL_LATEST_MOTION));
        this.createService(IlluminanceService::new, this::updateChannels, List.of(CHANNEL_ILLUMINANCE), true);
    }

    private void updateChannels(LatestMotionServiceState state) {
        DateTimeType date = new DateTimeType(state.latestMotionDetected);
        updateState(CHANNEL_LATEST_MOTION, date);
    }

    private void updateChannels(IlluminanceServiceState state) {
        DecimalType illuminance = new DecimalType(state.illuminance);
        updateState(CHANNEL_ILLUMINANCE, illuminance);
    }
}
