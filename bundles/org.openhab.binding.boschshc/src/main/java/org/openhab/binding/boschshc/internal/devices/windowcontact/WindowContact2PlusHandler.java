/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.windowcontact;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.vibrationsensor.VibrationSensorService;
import org.openhab.binding.boschshc.internal.services.vibrationsensor.dto.VibrationSensorSensitivity;
import org.openhab.binding.boschshc.internal.services.vibrationsensor.dto.VibrationSensorServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for Door/Window Contact II Plus.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class WindowContact2PlusHandler extends WindowContact2Handler {

    private final Logger logger = LoggerFactory.getLogger(WindowContact2PlusHandler.class);

    private final VibrationSensorService vibrationSensorService;

    @Nullable
    private VibrationSensorServiceState currentVibrationSensorState;

    public WindowContact2PlusHandler(Thing thing) {
        super(thing);
        this.vibrationSensorService = new VibrationSensorService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.registerService(vibrationSensorService, this::updateChannels, List.of(CHANNEL_VIBRATION_SENSOR_ENABLED,
                CHANNEL_VIBRATION_SENSOR_STATE, CHANNEL_VIBRATION_SENSOR_SENSITIVITY), true);
    }

    private void updateChannels(VibrationSensorServiceState state) {
        this.currentVibrationSensorState = state;
        updateState(CHANNEL_VIBRATION_SENSOR_ENABLED, OnOffType.from(state.enabled));
        updateState(CHANNEL_VIBRATION_SENSOR_STATE, new StringType(state.value.toString()));
        updateState(CHANNEL_VIBRATION_SENSOR_SENSITIVITY, new StringType(state.sensitivity.toString()));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        switch (channelUID.getId()) {
            case CHANNEL_VIBRATION_SENSOR_ENABLED:
                if (command instanceof OnOffType onOffCommand) {
                    updateVibrationSensorEnabled(onOffCommand);
                }
                break;
            case CHANNEL_VIBRATION_SENSOR_SENSITIVITY:
                if (command instanceof StringType stringCommand) {
                    updateVibrationSensorSensitivity(stringCommand.toFullString());
                }
                break;
        }
    }

    private void updateVibrationSensorSensitivity(String sensitivityValue) {
        VibrationSensorSensitivity newSensitivity = VibrationSensorSensitivity.from(sensitivityValue);

        if (newSensitivity == null) {
            logger.warn("Unsupported vibration sensor sensitivity value: {}", sensitivityValue);
            return;
        }

        VibrationSensorServiceState copy = copyCurrentState();
        if (copy == null) {
            return;
        }

        copy.sensitivity = newSensitivity;

        this.currentVibrationSensorState = copy;
        updateServiceState(vibrationSensorService, copy);
    }

    private void updateVibrationSensorEnabled(OnOffType enabled) {
        VibrationSensorServiceState copy = copyCurrentState();
        if (copy == null) {
            return;
        }

        copy.enabled = enabled == OnOffType.ON;

        this.currentVibrationSensorState = copy;
        updateServiceState(vibrationSensorService, copy);
    }

    @Nullable
    private VibrationSensorServiceState copyCurrentState() {
        if (currentVibrationSensorState != null) {
            return currentVibrationSensorState.copy();
        }

        logger.warn("Could not obtain current vibration sensor service state, command will not be processed.");
        return null;
    }
}
