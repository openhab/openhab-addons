/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SwingPortalHandler extends BaseThingHandler implements MyNiceDataListener {
    private String id = "";

    public SwingPortalHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        id = (String) getConfig().get("id");
        getBridgeHandler().ifPresent(h -> h.registerDataListener(this));
    }

    private Optional<It4WifiHandler> getBridgeHandler() {
        BridgeHandler bridge = getBridge().getHandler();
        return bridge instanceof It4WifiHandler ? Optional.of((It4WifiHandler) bridge) : Optional.empty();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        } else {
            // if (DOOR_STATUS.equals(channelUID.getId()) && command instanceof OpenCloseCommand) {
            // sendCommand(getGateCommand(command.toString().toLowerCase()));
            // }
        }
    }

    @Override
    public void onDataFetched(ThingUID bridge, List<Device> devices) {
        devices.stream().filter(d -> id.equals(d.id)).findFirst().map(d -> consumeData(d));
    }

    public boolean consumeData(Device device) {
        updateStatus(ThingStatus.ONLINE);
        if (thing.getProperties().isEmpty()) {
            Map<String, String> properties = Map.of(PROPERTY_VENDOR, device.manuf, PROPERTY_MODEL_ID, device.prod,
                    PROPERTY_SERIAL_NUMBER, device.serialNr, PROPERTY_HARDWARE_VERSION, device.versionHW,
                    PROPERTY_FIRMWARE_VERSION, device.versionFW);
            updateProperties(properties);
        }
        if (device.prod != null) {
            getBridgeHandler().ifPresent(h -> h.request(CommandType.STATUS));
        } else {
            String status = device.properties.doorStatus;
            updateState(DOOR_STATUS, new StringType(status));
            updateState(DOOR_OBSTRUCTED, new StringType(device.properties.obstruct));
            updateState(DOOR_MOVING, OnOffType.from(status.endsWith("ing")));
        }
        return true;
    }
}
