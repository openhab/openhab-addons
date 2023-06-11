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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mynice.internal.xml.dto.CommandType;
import org.openhab.binding.mynice.internal.xml.dto.Device;
import org.openhab.binding.mynice.internal.xml.dto.T4Command;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
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
    private static final String OPENING = "opening";
    private static final String CLOSING = "closing";

    private final Logger logger = LoggerFactory.getLogger(GateHandler.class);

    private String id = "";

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
        if (command instanceof RefreshType) {
            return;
        } else {
            handleCommand(channelUID.getId(), command.toString());
        }
    }

    private void handleCommand(String channelId, String command) {
        if (DOOR_COMMAND.equals(channelId)) {
            getBridgeHandler().ifPresent(handler -> handler.sendCommand(id, command));
        } else if (DOOR_T4_COMMAND.equals(channelId)) {
            String allowed = thing.getProperties().get(ALLOWED_T4);
            if (allowed != null && allowed.contains(command)) {
                getBridgeHandler().ifPresent(handler -> {
                    try {
                        T4Command t4 = T4Command.fromCode(command);
                        handler.sendCommand(id, t4);
                    } catch (IllegalArgumentException e) {
                        logger.warn("{} is not a valid T4 command", command);
                    }
                });
            } else {
                logger.warn("This thing does not accept the T4 command '{}'", command);
            }
        }
    }

    @Override
    public void onDataFetched(List<Device> devices) {
        devices.stream().filter(d -> id.equals(d.id)).findFirst().map(device -> {
            updateStatus(ThingStatus.ONLINE);
            if (thing.getProperties().isEmpty()) {
                int value = Integer.parseInt(device.properties.t4allowed.values, 16);
                List<String> t4Allowed = T4Command.fromBitmask(value).stream().map(Enum::name).toList();
                updateProperties(Map.of(PROPERTY_VENDOR, device.manuf, PROPERTY_MODEL_ID, device.prod,
                        PROPERTY_SERIAL_NUMBER, device.serialNr, PROPERTY_HARDWARE_VERSION, device.versionHW,
                        PROPERTY_FIRMWARE_VERSION, device.versionFW, ALLOWED_T4, String.join(",", t4Allowed)));
            }
            if (device.prod != null) {
                getBridgeHandler().ifPresent(h -> h.sendCommand(CommandType.STATUS));
            } else {
                String status = device.properties.doorStatus;
                updateState(DOOR_STATUS, new StringType(status));
                updateState(DOOR_OBSTRUCTED, OnOffType.from("1".equals(device.properties.obstruct)));
                updateState(DOOR_MOVING, OnOffType.from(status.equals(CLOSING) || status.equals(OPENING)));
            }
            return true;
        });
    }
}
