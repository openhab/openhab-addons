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
package org.openhab.binding.tapocontrol.internal.devices.rf.smartcontact;

import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TypeUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoChildDeviceData;
import org.openhab.binding.tapocontrol.internal.devices.rf.TapoChildDeviceHandler;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TAPO Smart-Contact-Device.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoSmartContactHandler extends TapoChildDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(TapoSmartContactHandler.class);

    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    public TapoSmartContactHandler(Thing thing) {
        super(thing);
    }

    /**
     * Update properties
     */
    @Override
    protected void devicePropertiesChanged(TapoChildDeviceData deviceInfo) {
        super.devicePropertiesChanged(deviceInfo);
        updateState(getChannelID(CHANNEL_GROUP_SENSOR, CHANNEL_IS_OPEN), getOnOffType(deviceInfo.isOpen()));
    }

    /**
     * Fires events on {@link TapoChildDeviceData} changes.
     */
    @Override
    protected void triggerEvents(TapoChildDeviceData deviceInfo) {
        super.triggerEvents(deviceInfo);
        if (checkForStateChange(CHANNEL_IS_OPEN, deviceInfo.isOpen())) {
            if (deviceInfo.isOpen()) {
                triggerChannel(getChannelID(CHANNEL_GROUP_SENSOR, EVENT_CONTACT_OPENED), EVENT_STATE_OPENED);
                logger.trace("({}) contact event fired '{}'", uid, EVENT_STATE_OPENED);
            } else {
                triggerChannel(getChannelID(CHANNEL_GROUP_SENSOR, EVENT_CONTACT_CLOSED), EVENT_STATE_CLOSED);
                logger.trace("({}) contact event fired '{}'", uid, EVENT_STATE_CLOSED);
            }
        }
    }
}
