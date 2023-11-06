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
package org.openhab.binding.myq.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myq.internal.MyQBindingConstants;
import org.openhab.binding.myq.internal.config.MyQDeviceConfiguration;
import org.openhab.binding.myq.internal.dto.DeviceDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link MyQLampHandler} is responsible for handling commands for a lamp thing, which are
 * sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MyQLampHandler extends BaseThingHandler implements MyQDeviceHandler {
    private @Nullable DeviceDTO device;
    private String serialNumber;

    public MyQLampHandler(Thing thing) {
        super(thing);
        serialNumber = getConfigAs(MyQDeviceConfiguration.class).serialNumber;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState();
            return;
        }

        if (command instanceof OnOffType) {
            Bridge bridge = getBridge();
            final DeviceDTO localDevice = device;
            if (bridge != null && localDevice != null) {
                BridgeHandler handler = bridge.getHandler();
                if (handler != null) {
                    ((MyQAccountHandler) handler).sendLampAction(localDevice, command == OnOffType.ON ? "on" : "off");
                }
            }
        }
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    protected void updateState() {
        final DeviceDTO localDevice = device;
        if (localDevice != null) {
            String lampState = localDevice.state.lampState;
            updateState("switch", "on".equals(lampState) ? OnOffType.ON : OnOffType.OFF);
        }
    }

    @Override
    public void handleDeviceUpdate(DeviceDTO device) {
        if (!MyQBindingConstants.THING_TYPE_LAMP.getId().equals(device.deviceFamily)) {
            return;
        }
        this.device = device;
        if (device.state.online) {
            updateStatus(ThingStatus.ONLINE);
            updateState();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device reports as offline");
        }
    }
}
