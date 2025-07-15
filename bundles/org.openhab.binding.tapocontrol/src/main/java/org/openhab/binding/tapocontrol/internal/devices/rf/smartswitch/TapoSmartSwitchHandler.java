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
package org.openhab.binding.tapocontrol.internal.devices.rf.smartswitch;

import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TypeUtils.getOnOffType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoChildDeviceData;
import org.openhab.binding.tapocontrol.internal.devices.rf.TapoChildDeviceHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TAPO Smart-Contact-Device.
 *
 * @author Manfred Krämer - Initial contribution
 */

@NonNullByDefault
public class TapoSmartSwitchHandler extends TapoChildDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(TapoSmartSwitchHandler.class);

    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    public TapoSmartSwitchHandler(Thing thing) {
        super(thing);
    }

    /**
     * Update properties
     */
    @Override
    protected void devicePropertiesChanged(TapoChildDeviceData deviceInfo) {
        super.devicePropertiesChanged(deviceInfo);
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_OUTPUT), getOnOffType(deviceInfo.isOn()));
    }

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
            setDeviceData();
        } else if (command instanceof OnOffType onOffCommand) {
            deviceInfo.setDeviceOn(OnOffType.ON.equals(onOffCommand));

            hub.sendCommandToChild(deviceInfo);
        } else {
            logger.warn("({}) command type '{}' not supported for channel '{}'", uid, command, channelUID.getId());
        }
    }
}
