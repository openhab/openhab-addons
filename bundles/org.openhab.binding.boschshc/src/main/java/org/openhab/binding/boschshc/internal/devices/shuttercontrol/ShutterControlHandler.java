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
package org.openhab.binding.boschshc.internal.devices.shuttercontrol;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_LEVEL;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.BoschSHCDeviceHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.shuttercontrol.OperationState;
import org.openhab.binding.boschshc.internal.services.shuttercontrol.ShutterControlService;
import org.openhab.binding.boschshc.internal.services.shuttercontrol.dto.ShutterControlServiceState;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Control of your shutter to take any position you desire.
 *
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class ShutterControlHandler extends BoschSHCDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ShutterControlService shutterControlService;

    public ShutterControlHandler(Thing thing) {
        super(thing);
        this.shutterControlService = new ShutterControlService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.registerService(this.shutterControlService, this::updateChannels, List.of(CHANNEL_LEVEL));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (command instanceof UpDownType upDownCommand) {
            // Set full close/open as target state
            ShutterControlServiceState state = new ShutterControlServiceState();
            switch (upDownCommand) {
                case UpDownType.UP -> state.level = 1.0;
                case UpDownType.DOWN -> state.level = 0.0;
                default -> {
                    logger.warn("Received unknown UpDownType command: {}", upDownCommand);
                    return;
                }
            }
            this.updateServiceState(this.shutterControlService, state);
        } else if (command instanceof StopMoveType stopMoveCommand) {
            if (stopMoveCommand == StopMoveType.STOP) {
                // Set STOPPED operation state
                ShutterControlServiceState state = new ShutterControlServiceState();
                state.operationState = OperationState.STOPPED;
                this.updateServiceState(this.shutterControlService, state);
            }
        } else if (command instanceof PercentType percentCommand) {
            // Set specific level
            double level = openPercentageToLevel(percentCommand.doubleValue());
            this.updateServiceState(this.shutterControlService, new ShutterControlServiceState(level));
        }
    }

    private double openPercentageToLevel(double openPercentage) {
        return (100 - openPercentage) / 100.0;
    }

    private void updateChannels(ShutterControlServiceState state) {
        if (state.level != null) {
            // Convert level to open ratio
            int openPercentage = levelToOpenPercentage(state.level);
            updateState(CHANNEL_LEVEL, new PercentType(openPercentage));
        }
    }

    private int levelToOpenPercentage(double level) {
        return (int) Math.round((1 - level) * 100);
    }
}
