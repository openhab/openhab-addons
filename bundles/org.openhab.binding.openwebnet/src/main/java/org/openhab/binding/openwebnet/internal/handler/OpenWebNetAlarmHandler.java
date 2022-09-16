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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.Alarm;
import org.openwebnet4j.message.Alarm.WhatAlarm;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereAlarm;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetAlarmHandler} is responsible for handling commands/messages for Alarm Central Unit and zones. It
 * extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
public class OpenWebNetAlarmHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetAlarmHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.ALARM_SUPPORTED_THING_TYPES;

    private static long lastAllDevicesRefreshTS = 0; // ts when last all device refresh was sent for this handler

    public OpenWebNetAlarmHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void handleChannelCommand(@NonNull ChannelUID channel, @NonNull Command command) {
        logger.warn("handleChannelCommand() Read only channel, unsupported command {}", command);
    }

    @Override
    protected void requestChannelState(@NonNull ChannelUID channel) {
        super.requestChannelState(channel);
        Where w = deviceWhere;
        ThingTypeUID thingType = thing.getThingTypeUID();
        try {
            if (THING_TYPE_BUS_ALARM_CENTRAL_UNIT.equals(thingType)) {
                send(Alarm.requestSystemStatus());
                lastAllDevicesRefreshTS = System.currentTimeMillis();
            } else {
                send(Alarm.requestZoneStatus(w.value()));
            }
        } catch (OWNException e) {
            logger.debug("Exception while requesting state for channel {}: {} ", channel, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    protected long getRefreshAllLastTS() {
        return lastAllDevicesRefreshTS;
    };

    @Override
    protected void refreshDevice(boolean refreshAll) {
        if (refreshAll) {
            logger.debug("--- refreshDevice() : refreshing via ALARM CENTRAL UNIT... ({})", thing.getUID());
            try {
                send(Alarm.requestSystemStatus());
                lastAllDevicesRefreshTS = System.currentTimeMillis();
            } catch (OWNException e) {
                logger.warn("Excpetion while requesting alarm system status: {}", e.getMessage());
            }
        } else {
            logger.debug("--- refreshDevice() : refreshing SINGLE... ({})", thing.getUID());
            requestChannelState(new ChannelUID(thing.getUID(), CHANNEL_ALARM));
        }
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        logger.debug("handleMessage({}) for thing: {}", msg, thing.getUID());
        super.handleMessage(msg);
        ThingTypeUID thingType = thing.getThingTypeUID();
        if (THING_TYPE_BUS_ALARM_CENTRAL_UNIT.equals(thingType)) {
            updateCU((Alarm) msg);
        } else {
            updateZone((Alarm) msg);
        }
    }

    private void updateCU(Alarm msg) {
        WhatAlarm w = (WhatAlarm) msg.getWhat();
        switch (w) {
            case START_PROGRAMMING:
            case STOP_PROGRAMMING:
                break;
            case SYSTEM_ACTIVE:
            case SYSTEM_INACTIVE:
            case SYSTEM_MAINTENANCE:
                break;
            case SYSTEM_BATTERY_FAULT:
                break;
            case SYSTEM_BATTERY_OK:
            case SYSTEM_BATTERY_UNLOADED:
                break;
            case SYSTEM_DISENGAGED:
            case SYSTEM_ENGAGED:
                break;
            case SYSTEM_NETWORK_ERROR:
            case SYSTEM_NETWORK_OK:
                break;
            default:
                break;
        }
    }

    private void updateZone(Alarm msg) {
        // TODO Auto-generated method stub
    }

    @Override
    protected @NonNull Where buildBusWhere(@NonNull String wStr) throws IllegalArgumentException {
        return new WhereAlarm(wStr);
    }

    @Override
    protected @NonNull String ownIdPrefix() {
        return Who.BURGLAR_ALARM.value().toString();
    }
}
