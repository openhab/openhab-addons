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
package org.openhab.binding.tapocontrol.internal.device;

import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.structures.TapoDeviceInfo;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.unit.Units;
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
public class TapoSmartPlug extends TapoDevice {
    private final Logger logger = LoggerFactory.getLogger(TapoSmartPlug.class);

    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    public TapoSmartPlug(Thing thing) {
        super(thing);
    }

    /**
     * handle command sent to device
     *
     * @param channelUID channelUID command is sent to
     * @param command command to be sent
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        boolean refreshInfo = false;
        String id = channelUID.getIdWithoutGroup();

        /* perform actions */
        if (command instanceof RefreshType) {
            refreshInfo = true;
        } else if (command instanceof OnOffType) {
            Boolean targetState = command == OnOffType.ON ? Boolean.TRUE : Boolean.FALSE;
            if (CHANNEL_OUTPUT.equals(id)) { // Command is sent to the device output
                connector.sendDeviceCommand(DEVICE_PROPERTY_ON, targetState);
                refreshInfo = true;
            } else if (id.startsWith(CHANNEL_OUTPUT)) { // Command is sent to a child's device output
                Integer index = Integer.valueOf(id.replace(CHANNEL_OUTPUT, ""));
                connector.sendChildCommand(index, DEVICE_PROPERTY_ON, targetState);
                refreshInfo = true;
            }
        } else {
            logger.warn("({}) command type '{}' not supported for channel '{}'", uid, command, channelUID.getId());
        }

        /* refreshInfo */
        if (refreshInfo) {
            queryDeviceInfo(true);
        }
    }

    /**
     * UPDATE PROPERTIES
     *
     * @param TapoDeviceInfo
     */
    @Override
    protected void devicePropertiesChanged(TapoDeviceInfo deviceInfo) {
        super.devicePropertiesChanged(deviceInfo);
        publishState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_OUTPUT), getOnOffType(deviceInfo.isOn()));
        publishState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_WIFI_STRENGTH),
                getDecimalType(deviceInfo.getSignalLevel()));
        publishState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_ONTIME),
                getTimeType(deviceInfo.getOnTime(), Units.SECOND));
        publishState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_OVERHEAT), getOnOffType(deviceInfo.isOverheated()));
    }
}
