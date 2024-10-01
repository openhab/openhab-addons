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
package org.openhab.binding.boschshc.internal.devices.waterleakage;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_ACOUSTIC_SIGNALS_ON_MOVE;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_PUSH_NOTIFICATIONS_ON_MOVE;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_SENSOR_MOVED;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_WATER_LEAKAGE;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_WATER_LEAKAGE_SENSOR_CHECK;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Message;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.communicationquality.CommunicationQualityService;
import org.openhab.binding.boschshc.internal.services.communicationquality.dto.CommunicationQualityServiceState;
import org.openhab.binding.boschshc.internal.services.dto.EnabledDisabledState;
import org.openhab.binding.boschshc.internal.services.waterleakagesensor.WaterLeakageSensorService;
import org.openhab.binding.boschshc.internal.services.waterleakagesensor.dto.WaterLeakageSensorServiceState;
import org.openhab.binding.boschshc.internal.services.waterleakagesensorcheck.WaterLeakageSensorCheckService;
import org.openhab.binding.boschshc.internal.services.waterleakagesensorcheck.dto.WaterLeakageSensorCheckServiceState;
import org.openhab.binding.boschshc.internal.services.waterleakagesensortilt.WaterLeakageSensorTiltService;
import org.openhab.binding.boschshc.internal.services.waterleakagesensortilt.dto.WaterLeakageSensorTiltServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for water leakage sensors.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class WaterLeakageSensorHandler extends AbstractBatteryPoweredDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(WaterLeakageSensorHandler.class);

    /**
     * Message code indicating that the water leakage sensor was moved
     */
    public static final String MESSAGE_CODE_TILT_DETECTED = "TILT_DETECTED";

    private WaterLeakageSensorTiltService waterLeakageSensorTiltService;
    private WaterLeakageSensorCheckService waterLeakageSensorCheckService;

    @Nullable
    private WaterLeakageSensorTiltServiceState currentWaterSensorTiltServiceState;

    public WaterLeakageSensorHandler(Thing thing) {
        super(thing);

        this.waterLeakageSensorTiltService = new WaterLeakageSensorTiltService();
        this.waterLeakageSensorCheckService = new WaterLeakageSensorCheckService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.createService(WaterLeakageSensorService::new, this::updateChannels, List.of(CHANNEL_WATER_LEAKAGE), true);
        this.registerService(waterLeakageSensorTiltService, this::updateChannels,
                List.of(CHANNEL_PUSH_NOTIFICATIONS_ON_MOVE, CHANNEL_ACOUSTIC_SIGNALS_ON_MOVE), true);
        this.registerService(waterLeakageSensorCheckService, this::updateChannels, List.of());

        this.createService(CommunicationQualityService::new, this::updateChannels, List.of(CHANNEL_SIGNAL_STRENGTH),
                true);
    }

    private void updateChannels(WaterLeakageSensorServiceState waterLeakageSensorServiceState) {
        updateState(CHANNEL_WATER_LEAKAGE, waterLeakageSensorServiceState.state.toOnOffType());
    }

    private void updateChannels(WaterLeakageSensorTiltServiceState waterLeakageSensorTiltServiceState) {
        currentWaterSensorTiltServiceState = waterLeakageSensorTiltServiceState;
        updateState(CHANNEL_PUSH_NOTIFICATIONS_ON_MOVE,
                waterLeakageSensorTiltServiceState.pushNotificationState.toOnOffType());
        updateState(CHANNEL_ACOUSTIC_SIGNALS_ON_MOVE,
                waterLeakageSensorTiltServiceState.acousticSignalState.toOnOffType());
    }

    private void updateChannels(WaterLeakageSensorCheckServiceState waterLeakageSensorCheckServiceState) {
        updateState(CHANNEL_WATER_LEAKAGE_SENSOR_CHECK, new StringType(waterLeakageSensorCheckServiceState.result));
    }

    private void updateChannels(CommunicationQualityServiceState communicationQualityServiceState) {
        updateState(CHANNEL_SIGNAL_STRENGTH, communicationQualityServiceState.quality.toSystemSignalStrength());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_PUSH_NOTIFICATIONS_ON_MOVE.equals(channelUID.getId())
                && command instanceof OnOffType onOffCommand) {
            updatePushNotificationState(onOffCommand);
        } else if (CHANNEL_ACOUSTIC_SIGNALS_ON_MOVE.equals(channelUID.getId())
                && command instanceof OnOffType onOffCommand) {
            updateAcousticSignalState(onOffCommand);
        }
    }

    private void updatePushNotificationState(OnOffType onOffCommand) {
        WaterLeakageSensorTiltServiceState newState = cloneCurrentWaterLeakageSensorTiltServiceState();
        if (newState != null) {
            newState.pushNotificationState = EnabledDisabledState.from(onOffCommand);
            this.currentWaterSensorTiltServiceState = newState;
            updateServiceState(waterLeakageSensorTiltService, newState);
        }
    }

    private void updateAcousticSignalState(OnOffType onOffCommand) {
        WaterLeakageSensorTiltServiceState newState = cloneCurrentWaterLeakageSensorTiltServiceState();
        if (newState != null) {
            newState.acousticSignalState = EnabledDisabledState.from(onOffCommand);
            this.currentWaterSensorTiltServiceState = newState;
            updateServiceState(waterLeakageSensorTiltService, newState);
        }
    }

    @Nullable
    private WaterLeakageSensorTiltServiceState cloneCurrentWaterLeakageSensorTiltServiceState() {
        if (currentWaterSensorTiltServiceState != null) {
            WaterLeakageSensorTiltServiceState clonedState = new WaterLeakageSensorTiltServiceState();
            clonedState.acousticSignalState = currentWaterSensorTiltServiceState.acousticSignalState;
            clonedState.pushNotificationState = currentWaterSensorTiltServiceState.pushNotificationState;
            return clonedState;
        } else {
            logger.warn("Could not obtain current water leakage detector tilt state, command will not be processed.");
        }
        return null;
    }

    @Override
    public void processMessage(Message message) {
        super.processMessage(message);

        if (message.messageCode != null && MESSAGE_CODE_TILT_DETECTED.equals(message.messageCode.name)) {
            triggerChannel(CHANNEL_SENSOR_MOVED);
        }
    }
}
