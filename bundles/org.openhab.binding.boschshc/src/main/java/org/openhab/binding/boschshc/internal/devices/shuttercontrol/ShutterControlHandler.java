/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.shuttercontrol;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_LEVEL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.BoschSHCBridgeHandler;
import org.openhab.binding.boschshc.internal.devices.shuttercontrol.dto.ShutterControlState;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * Handler for a shutter control device
 * 
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class ShutterControlHandler extends BoschSHCHandler {
    /**
     * Utility functions to convert data between Bosch things and openHAB items
     */
    static final class DataConversion {
        public static int levelToOpenPercentage(double level) {
            return (int) Math.round((1 - level) * 100);
        }

        public static double openPercentageToLevel(double openPercentage) {
            return (100 - openPercentage) / 100.0;
        }
    }

    static final String ShutterControlServiceName = "ShutterControl";

    public ShutterControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            ShutterControlState state = this.getDeviceState();
            if (state == null) {
                logger.warn("Could not fetch device state, skipping refresh");
                return;
            }
            this.updateState(state);
        } else if (command instanceof UpDownType) {
            // Set full close/open as target state
            UpDownType upDownType = (UpDownType) command;
            ShutterControlState state = new ShutterControlState();
            if (upDownType == UpDownType.UP) {
                state.level = 1;
            } else if (upDownType == UpDownType.DOWN) {
                state.level = 0;
            } else {
                logger.warn("Received unknown UpDownType command: {}", upDownType);
                return;
            }
            this.setDeviceState(state);
        } else if (command instanceof StopMoveType) {
            // Set STOPPED operation state
            ShutterControlState state = new ShutterControlState();
            state.operationState = OperationState.STOPPED;
            this.setDeviceState(state);
        } else if (command instanceof PercentType) {
            // Set specific level
            PercentType percentType = (PercentType) command;
            double level = DataConversion.openPercentageToLevel(percentType.doubleValue());
            this.setDeviceState(new ShutterControlState(level));
        }
    }

    @Override
    public void processUpdate(String id, JsonElement state) {
        try {
            updateState(gson.fromJson(state, ShutterControlState.class));
        } catch (JsonSyntaxException e) {
            logger.warn("Received unknown update in Shutter Control: {}", state);
        }
    }

    private @Nullable ShutterControlState getDeviceState() {
        return this.getState(ShutterControlServiceName, ShutterControlState.class);
    }

    private void setDeviceState(ShutterControlState state) {
        BoschSHCBridgeHandler bridgeHandler;
        try {
            bridgeHandler = this.getBridgeHandler();
        } catch (BoschSHCException e) {
            return;
        }
        String deviceId = this.getBoschID();
        if (deviceId == null) {
            return;
        }
        bridgeHandler.putState(deviceId, ShutterControlServiceName, state);
    }

    private void updateState(ShutterControlState state) {
        // Convert level to open ratio
        int openPercentage = DataConversion.levelToOpenPercentage(state.level);
        updateState(CHANNEL_LEVEL, new PercentType(openPercentage));
    }
}
