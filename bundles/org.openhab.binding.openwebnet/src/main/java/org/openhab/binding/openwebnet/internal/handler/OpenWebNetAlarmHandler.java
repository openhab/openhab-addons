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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
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
 * The {@link OpenWebNetAlarmHandler} is responsible for handling
 * commands/messages for Alarm system and zones. It extends the abstract
 * {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
public class OpenWebNetAlarmHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetAlarmHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.ALARM_SUPPORTED_THING_TYPES;

    private static long lastAllDevicesRefreshTS = 0; // ts when last all device refresh was sent for this handler

    private static final String BATTERY_OK = "OK";
    private static final String BATTERY_FAULT = "FAULT";
    private static final String BATTERY_UNLOADED = "UNLOADED";

    private static final String SILENT = "SILENT";
    private static final String INTRUSION = "INTRUSION";
    private static final String ANTI_PANIC = "ANTI_PANIC";
    private static final String TAMPERING = "TAMPERING";

    public OpenWebNetAlarmHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        logger.warn("Alarm.handleChannelCommand() Read only channel, unsupported command {}", command);
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        super.requestChannelState(channel);
        Where w = deviceWhere;
        ThingTypeUID thingType = thing.getThingTypeUID();
        try {
            if (THING_TYPE_BUS_ALARM_SYSTEM.equals(thingType)) {
                send(Alarm.requestSystemStatus());
                lastAllDevicesRefreshTS = System.currentTimeMillis();
            } else {
                if (w != null) {
                    send(Alarm.requestZoneStatus("#" + w.value()));
                } else {
                    logger.debug("null where while requesting state for channel {}", channel);
                }
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
            logger.debug("--- refreshDevice() : refreshing all via ALARM CENTRAL UNIT... ({})", thing.getUID());
            try {
                send(Alarm.requestSystemStatus());
                lastAllDevicesRefreshTS = System.currentTimeMillis();
            } catch (OWNException e) {
                logger.warn("Excpetion while requesting alarm system status: {}", e.getMessage());
            }
        } else {
            logger.debug("--- refreshDevice() : refreshing SINGLE... ({})", thing.getUID());
            requestChannelState(new ChannelUID(thing.getUID(), CHANNEL_ALARM_SYSTEM_STATE));
        }
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        logger.debug("handleMessage({}) for thing: {}", msg, thing.getUID());
        super.handleMessage(msg);
        ThingTypeUID thingType = thing.getThingTypeUID();
        if (THING_TYPE_BUS_ALARM_SYSTEM.equals(thingType)) {
            updateSystem((Alarm) msg);
        } else {
            updateZone((Alarm) msg);
        }
    }

    private void updateSystem(Alarm msg) {
        WhatAlarm w = (WhatAlarm) msg.getWhat();
        if (w == null) {
            logger.debug("Alarm.updateSystem() WHAT is null. Frame={}", msg);
            return;
        }
        switch (w) {
            case SYSTEM_ACTIVE:
            case SYSTEM_INACTIVE:
            case SYSTEM_MAINTENANCE:
                updateAlarmSystemState(w);
                break;
            case SYSTEM_DISENGAGED:
            case SYSTEM_ENGAGED:
                updateAlarmSystemArmed(w);
                break;
            case SYSTEM_BATTERY_FAULT:
            case SYSTEM_BATTERY_OK:
            case SYSTEM_BATTERY_UNLOADED:
                updateBatteryState(w);
                break;
            case SYSTEM_NETWORK_ERROR:
            case SYSTEM_NETWORK_OK:
                updateNetworkState(w);
                break;
            case START_PROGRAMMING:
            case STOP_PROGRAMMING:
            case DELAY_END:
            case NO_CONNECTION_TO_DEVICE:
            default:
                logger.debug("Alarm.updateSystem() Ignoring unsupported WHAT {}. Frame={}", msg.getWhat(), msg);
        }
    }

    private void updateAlarmSystemState(WhatAlarm w) {
        updateState(CHANNEL_ALARM_SYSTEM_STATE, OnOffType.from(w == Alarm.WhatAlarm.SYSTEM_ACTIVE));
    }

    private void updateAlarmSystemArmed(WhatAlarm w) {
        updateState(CHANNEL_ALARM_SYSTEM_ARMED, OnOffType.from(w == Alarm.WhatAlarm.SYSTEM_ENGAGED));
    }

    private void updateNetworkState(WhatAlarm w) {
        updateState(CHANNEL_ALARM_SYSTEM_NETWORK, OnOffType.from(w == Alarm.WhatAlarm.SYSTEM_NETWORK_OK));
    }

    private void updateBatteryState(WhatAlarm w) {
        if (w == Alarm.WhatAlarm.SYSTEM_BATTERY_OK) {
            updateState(CHANNEL_ALARM_SYSTEM_BATTERY, new StringType(BATTERY_OK));
        } else if (w == Alarm.WhatAlarm.SYSTEM_BATTERY_UNLOADED) {
            updateState(CHANNEL_ALARM_SYSTEM_BATTERY, new StringType(BATTERY_UNLOADED));
        } else {
            updateState(CHANNEL_ALARM_SYSTEM_BATTERY, new StringType(BATTERY_FAULT));
        }
    }

    private void updateZone(Alarm msg) {
        WhatAlarm w = (WhatAlarm) msg.getWhat();
        if (w == null) {
            logger.debug("Alarm.updateZone() WHAT is null. Frame={}", msg);
            return;
        }
        switch (w) {
            case ZONE_DISENGAGED:
            case ZONE_ENGAGED:
                updateZoneState(w);
                break;
            case ZONE_ALARM_INTRUSION:
            case ZONE_ALARM_TAMPERING:
            case ZONE_ALARM_ANTI_PANIC:
            case ZONE_ALARM_SILENT:
                updateZoneAlarmState(w);
                break;
            case ZONE_ALARM_TECHNICAL:// not handled for now
            case ZONE_ALARM_TECHNICAL_RESET:
            default:
                logger.debug("Alarm.updateZone() Ignoring unsupported WHAT {}. Frame={}", msg.getWhat(), msg);
        }
    }

    private void updateZoneState(WhatAlarm w) {
        updateState(CHANNEL_ALARM_ZONE_STATE, OnOffType.from(w == Alarm.WhatAlarm.ZONE_ENGAGED));
    }

    private void updateZoneAlarmState(WhatAlarm w) {
        if (w == Alarm.WhatAlarm.ZONE_ALARM_SILENT) {
            updateState(CHANNEL_ALARM_ZONE_ALARM_STATE, new StringType(SILENT));
        } else if (w == Alarm.WhatAlarm.ZONE_ALARM_INTRUSION) {
            updateState(CHANNEL_ALARM_ZONE_ALARM_STATE, new StringType(INTRUSION));
        } else if (w == Alarm.WhatAlarm.ZONE_ALARM_ANTI_PANIC) {
            updateState(CHANNEL_ALARM_ZONE_ALARM_STATE, new StringType(ANTI_PANIC));
        } else {
            updateState(CHANNEL_ALARM_ZONE_ALARM_STATE, new StringType(TAMPERING));
        }
    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereAlarm(wStr);
    }

    @Override
    protected String ownIdPrefix() {
        return Who.BURGLAR_ALARM.value().toString();
    }
}
