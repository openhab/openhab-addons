/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mynice.internal.handler;

import static org.openhab.binding.mynice.internal.MyNiceBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mynice.internal.config.CourtesyConfiguration;
import org.openhab.binding.mynice.internal.xml.dto.CommandType;
import org.openhab.binding.mynice.internal.xml.dto.Device;
import org.openhab.binding.mynice.internal.xml.dto.Properties.DoorStatus;
import org.openhab.binding.mynice.internal.xml.dto.Property;
import org.openhab.binding.mynice.internal.xml.dto.T4Command;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class GateHandler extends BaseThingHandler implements MyNiceDataListener {

    private final Logger logger = LoggerFactory.getLogger(GateHandler.class);

    private String id = "";
    private Optional<DoorStatus> gateStatus = Optional.empty();
    private List<T4Command> t4Allowed = List.of();

    public GateHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        id = (String) getConfig().get(DEVICE_ID);
        getBridgeHandler().ifPresent(h -> h.registerDataListener(this));
    }

    @Override
    public void dispose() {
        id = "";
        gateStatus = Optional.empty();
        t4Allowed = List.of();
        getBridgeHandler().ifPresent(h -> h.unregisterDataListener(this));
    }

    private Optional<It4WifiHandler> getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof It4WifiHandler it4Handler) {
                return Optional.of(it4Handler);
            }
        }
        return Optional.empty();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();

        if (command instanceof RefreshType) {
            getBridgeHandler().ifPresent(handler -> handler.sendCommand(CommandType.INFO));
        } else if (CHANNEL_COURTESY.equals(channelId) && command instanceof OnOffType) {
            handleT4Command(T4Command.MDEy);
        } else if (CHANNEL_STATUS.equals(channelId)) {
            gateStatus.ifPresentOrElse(status -> {
                if (command instanceof StopMoveType stopMoveCommand) {
                    handleStopMove(status, stopMoveCommand);
                } else {
                    try {
                        handleStopMove(status, StopMoveType.valueOf(command.toString()));
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid StopMoveType command received : {}", command);
                    }
                }
            }, () -> logger.info("Current status of the gate unknown, can not send {} command", command));
        } else if (CHANNEL_COMMAND.equals(channelId)) {
            getBridgeHandler().ifPresent(handler -> handler.sendCommand(id, command.toString()));
        } else if (CHANNEL_T4_COMMAND.equals(channelId)) {
            try {
                T4Command t4 = T4Command.fromCode(command.toString());
                handleT4Command(t4);
            } catch (IllegalArgumentException e) {
                logger.warn("{} is not a valid T4 command", command);
            }
        } else {
            logger.warn("Unable to handle command {} on channel {}", command, channelId);
        }
    }

    private void handleStopMove(DoorStatus status, StopMoveType stopMoveCommand) {
        if (stopMoveCommand == StopMoveType.STOP) {
            if (status == DoorStatus.STOPPED) {
                logger.info("The gate is already stopped.");
            } else {
                handleT4Command(T4Command.MDAy);
            }
            return;
        }

        // It's a move Command
        if (status == DoorStatus.OPEN) {
            handleT4Command(T4Command.MDA0);
        } else if (status == DoorStatus.CLOSED) {
            handleT4Command(T4Command.MDAz);
        } else if (status.moving) {
            logger.info("The gate is already currently moving.");
        } else { // it is closed
            handleT4Command(T4Command.MDAx);
        }
    }

    private void handleT4Command(T4Command t4Command) {
        if (t4Allowed.contains(t4Command)) {
            getBridgeHandler().ifPresent(handler -> handler.sendCommand(id, t4Command));
        } else {
            logger.warn("This gate does not accept the T4 command '{}'", t4Command);
        }
    }

    @Override
    public void onDataFetched(List<Device> devices) {
        devices.stream().filter(d -> id.equals(d.id)).findFirst().map(device -> {
            updateStatus(ThingStatus.ONLINE);
            Property t4list = device.properties.t4allowed;
            if (t4Allowed.isEmpty() && t4list != null) {
                int value = Integer.parseInt(t4list.values, 16);
                t4Allowed = T4Command.fromBitmask(value).stream().toList();
                if (thing.getProperties().isEmpty()) {
                    updateProperties(Map.of(PROPERTY_VENDOR, device.manuf, PROPERTY_MODEL_ID, device.prod,
                            PROPERTY_SERIAL_NUMBER, device.serialNr, PROPERTY_HARDWARE_VERSION, device.versionHW,
                            PROPERTY_FIRMWARE_VERSION, device.versionFW, ALLOWED_T4,
                            String.join(",", t4Allowed.stream().map(Enum::name).toList())));
                }
            }
            if (device.prod != null) {
                getBridgeHandler().ifPresent(h -> h.sendCommand(CommandType.STATUS));
            } else {
                DoorStatus status = device.properties.status();

                updateState(CHANNEL_STATUS, new StringType(status.name()));
                updateState(CHANNEL_OBSTRUCTED, OnOffType.from(device.properties.obstructed()));
                updateState(CHANNEL_MOVING, OnOffType.from(status.moving));
                if (status.moving && isLinked(CHANNEL_COURTESY)) {
                    Channel courtesy = getThing().getChannel(CHANNEL_COURTESY);
                    if (courtesy != null) {
                        updateState(CHANNEL_COURTESY, OnOffType.ON);
                        CourtesyConfiguration config = courtesy.getConfiguration().as(CourtesyConfiguration.class);
                        scheduler.schedule(() -> updateState(CHANNEL_COURTESY, OnOffType.OFF), config.duration,
                                TimeUnit.SECONDS);
                    }
                }
                gateStatus = Optional.of(status);
            }
            return true;
        });
    }
}
