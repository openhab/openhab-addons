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
package org.openhab.binding.tapocontrol.internal.devices.wifi.socket;

import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TypeUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoBaseDeviceData;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoChildDeviceData;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoChildList;
import org.openhab.binding.tapocontrol.internal.devices.wifi.TapoBaseDeviceHandler;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TAPO Smart-Plug-Device.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoSocketStripHandler extends TapoBaseDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(TapoSocketStripHandler.class);
    private TapoChildList tapoChildList = new TapoChildList();
    private TapoSocketData socketData = new TapoSocketData();

    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    public TapoSocketStripHandler(Thing thing) {
        super(thing);
    }

    /**
     * Function called by {@link org.openhab.binding.tapocontrol.internal.api.TapoDeviceConnector} if new data were
     * received
     * 
     * @param queryCommand command where new data belong to
     */
    @Override
    public void newDataResult(String queryCommand) {
        super.newDataResult(queryCommand);
        switch (queryCommand) {
            case DEVICE_CMD_GETINFO:
                baseDeviceData = connector.getResponseData(TapoBaseDeviceData.class);
                updateChannels(baseDeviceData);
                break;
            case DEVICE_CMD_GETCHILDDEVICELIST:
                tapoChildList = connector.getResponseData(TapoChildList.class);
                updateChildDevices(tapoChildList);
                break;
            default:
                logger.warn("({}) unhandled queryCommand '{}'", uid, queryCommand);
        }
    }

    /**
     * query device Properties
     */
    @Override
    public void queryDeviceData() {
        deviceError.reset();
        if (isLoggedIn(LOGIN_RETRIES)) {
            List<TapoRequest> requests = new ArrayList<>();
            requests.add(new TapoRequest(DEVICE_CMD_GETINFO));
            requests.add(new TapoRequest(DEVICE_CMD_GETCHILDDEVICELIST));
            connector.sendMultipleRequest(requests);
        }
    }

    /**
     * Get ChildData by position (index)
     */
    private Optional<TapoChildDeviceData> getChildData(int position) {
        return tapoChildList.getChildDeviceList().stream().filter(child -> child.getPosition() == position).findFirst();
    }

    /*****************************
     * HANDLE COMMANDS
     *****************************/

    /**
     * handle command sent to device
     *
     * @param channelUID channelUID command is sent to
     * @param command command to be sent
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /* perform actions */
        if (command instanceof RefreshType) {
            queryDeviceData();
        } else if (command instanceof OnOffType) {
            handleOnOffCommand(channelUID, command);
        } else {
            logger.warn("({}) command type '{}' not supported for channel '{}'", uid, command, channelUID.getId());
        }
    }

    private void handleOnOffCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getIdWithoutGroup();
        Boolean targetState = command == OnOffType.ON ? Boolean.TRUE : Boolean.FALSE;
        if (id.startsWith(CHANNEL_OUTPUT)) { // Command is sent to a child's device output
            Integer index = Integer.valueOf(id.replace(CHANNEL_OUTPUT, ""));
            switchOnOff(targetState, index);
        }
    }

    /*****************************
     * SEND COMMANDS
     *****************************/

    /**
     * Switch child On or Off
     * 
     * @param on if true device will switch on. Otherwise switch off
     * @param index index of child device
     */
    protected void switchOnOff(boolean on, int index) {
        socketData.switchOnOff(on);

        getChildData(index).ifPresent(child -> {
            child.setDeviceOn(on);
            connector.sendChildCommand(child, true);
        });
    }

    /*****************************
     * UPDATE CHANNELS
     *****************************/

    /**
     * Set all device Childs data to device
     */
    public void updateChildDevices(TapoChildList hostData) {
        hostData.getChildDeviceList().forEach(child -> {
            updateChildDevice(child.getPosition(), child);
        });
    }

    /**
     * Set data to child with index (position) x
     */
    public void updateChildDevice(int index, TapoChildDeviceData child) {
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_OUTPUT + index), getOnOffType(child.isOn()));
    }

    /**
     * Update Channels
     */
    protected void updateChannels(TapoBaseDeviceData deviceData) {
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_WIFI_STRENGTH),
                getDecimalType(deviceData.getSignalLevel()));
    }
}
