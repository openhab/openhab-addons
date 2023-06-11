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
package org.openhab.binding.dmx.internal;

import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;

/**
 * The {@link DmxThingHandler} is an abstract class with base functions
 * for DMX Things
 *
 * @author Jan N. Klug - Initial contribution
 */

public abstract class DmxThingHandler extends BaseThingHandler {

    protected ThingStatusDetail dmxHandlerStatus = ThingStatusDetail.HANDLER_CONFIGURATION_PENDING;

    public DmxThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * updates the switch state (if any)
     *
     * @param channelUID channelUID provided in channel registration
     * @param state (ON / OFF)
     */
    public void updateSwitchState(ChannelUID channelUID, State state) {
        updateState(channelUID, state);
    }

    /**
     * updates the internal values from the DMX channels
     *
     * @param channelUID channelUID provided in channel registration
     * @param value (0-255)
     */
    public void updateChannelValue(ChannelUID channelUID, int value) {
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (ThingStatus.ONLINE.equals(bridgeStatusInfo.getStatus())
                && ThingStatus.OFFLINE.equals(getThing().getStatusInfo().getStatus())
                && ThingStatusDetail.BRIDGE_OFFLINE.equals(getThing().getStatusInfo().getStatusDetail())) {
            if (ThingStatusDetail.NONE.equals(dmxHandlerStatus)) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            } else {
                updateStatus(ThingStatus.OFFLINE, dmxHandlerStatus);
            }
        }
    }
}
