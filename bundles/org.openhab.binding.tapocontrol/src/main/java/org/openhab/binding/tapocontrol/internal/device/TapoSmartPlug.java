/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
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
public class TapoSmartPlug extends TapoDevice {
    private final Logger logger = LoggerFactory.getLogger(TapoSmartPlug.class);

    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    public TapoSmartPlug(Thing thing, TapoCredentials credentials) {
        super(thing, credentials);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("({}) handleCommand '{}' for channelUID {}", uid, command.toString(), channelUID.getId());
        Boolean refreshInfo = false;

        /* perform actions */
        if (command instanceof RefreshType) {
            refreshInfo = true;
        } else if (command == OnOffType.ON) {
            connector.setDeviceInfo("device_on", true);
            refreshInfo = true;
        } else if (command == OnOffType.OFF) {
            connector.setDeviceInfo("device_on", false);
            refreshInfo = true;
        }

        /* refreshInfo */
        if (refreshInfo) {
            queryDeviceInfo();
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
        publishState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_ONTIME), getDecimalType(deviceInfo.getOnTime()));
        publishState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_OVERHEAT),
                getDecimalType(deviceInfo.isOverheated() ? 1 : 0));
    }
}
