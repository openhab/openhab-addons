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
package org.openhab.binding.groheondus.internal.handler;

import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.*;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.grohe.ondus.api.OndusService;
import org.grohe.ondus.api.model.BaseApplianceCommand;
import org.grohe.ondus.api.model.blue.Appliance;
import org.grohe.ondus.api.model.blue.ApplianceCommand;
import org.grohe.ondus.api.model.blue.TapType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Schmidt - Initial contribution
 */
@NonNullByDefault
public class GroheOndusBlueHandler extends GroheOndusBaseHandler<Appliance, ApplianceCommand> {

    private static final int DEFAULT_POLLING_INTERVAL = 900;

    private final Logger logger = LoggerFactory.getLogger(GroheOndusBlueHandler.class);

    public GroheOndusBlueHandler(Thing thing) {
        super(thing, Appliance.TYPE);
    }

    @Override
    protected int getPollingInterval(Appliance appliance) {
        if (config.pollingInterval > 0) {
            return config.pollingInterval;
        }
        return DEFAULT_POLLING_INTERVAL;
    }

    @Override
    protected void updateChannel(ChannelUID channelUID, Appliance appliance, ApplianceCommand command) {
        String channelId = channelUID.getIdWithoutGroup();
        State newState;
        switch (channelId) {
            case CHANNEL_NAME:
                newState = new StringType(appliance.getName());
                break;
            case CHANNEL_TAP:
                if (command.getCommand().getTapType() == 0) {
                    newState = OnOffType.OFF;
                } else {
                    newState = OnOffType.ON;
                }
                break;
            default:
                throw new IllegalArgumentException("Channel " + channelUID + " not supported.");
        }
        updateState(channelUID, newState);
    }

    @Override
    protected ApplianceCommand getLastDataPoint(Appliance appliance) {
        ApplianceCommand command = findApplianceCommand(appliance);
        if (command == null) {
            return new ApplianceCommand();
        }
        return command;
    }

    private @Nullable ApplianceCommand findApplianceCommand(Appliance appliance) {
        OndusService service = getOndusService();
        if (service == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "No initialized OndusService available from bridge.");
            return null;
        }

        Optional<BaseApplianceCommand> applianceDataOptional;
        try {
            applianceDataOptional = service.applianceCommand(appliance);
            if (!applianceDataOptional.isPresent() || applianceDataOptional.get().getType() != Appliance.TYPE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not load command from API.");
                return null;
            }
            return (ApplianceCommand) applianceDataOptional.get();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not load command from API because of error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!CHANNEL_TAP.equals(channelUID.getIdWithoutGroup())) {
            return;
        }
        if (!(command instanceof OnOffType)) {
            logger.debug("Invalid command received for channel. Expected OnOffType, received {}.",
                    command.getClass().getName());
            return;
        }
        OnOffType openClosedCommand = (OnOffType) command;
        if (openClosedCommand == OnOffType.OFF) {
            logger.debug("The GROHE Blue channel tap only supports turning on the tap.");
            return;
        }

        OndusService service = getOndusService();
        if (service == null) {
            return;
        }
        Appliance appliance = getAppliance(service);
        if (appliance == null) {
            return;
        }
        try {
            ApplianceCommand applianceCommand = findApplianceCommand(appliance);
            if (applianceCommand == null) {
                return;
            }
            applianceCommand.turnTapOn(TapType.CARBONATED, 100);
            service.sendCommand(applianceCommand);
            updateChannels();
        } catch (IOException e) {
            logger.debug("Could not update valve open state", e);
        }
    }
}
