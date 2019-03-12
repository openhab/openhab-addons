/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal.handler;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.verisure.internal.model.VerisureAlarmJSON;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;

/**
 * Handler for the Alarm Device thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureAlarmThingHandler extends VerisureThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>();
    static {
        SUPPORTED_THING_TYPES.add(THING_TYPE_ALARM);
    }

    public VerisureAlarmThingHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, channel: {}, command: {}", channelUID, command);
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else if (channelUID.getId().equals(CHANNEL_SET_ALARM_STATUS)) {
            handleAlarmState(command);
            scheduleImmediateRefresh();
        } else {
            logger.warn("Unknown command! {}", command);
        }
    }

    private void handleAlarmState(Command command) {
        if (session != null && config.deviceId != null) {
            VerisureAlarmJSON alarm = (VerisureAlarmJSON) session.getVerisureThing(config.deviceId);
            if (alarm != null) {
                BigDecimal pinCode = session.getPinCode();
                String csrf = session.getCsrf();
                String siteName = alarm.getSiteName();
                if (pinCode != null && csrf != null && siteName != null) {
                    String data = null;
                    String url = ALARM_COMMAND;
                    if (command.toString().equals("0")) {
                        data = "code=" + pinCode + "&state=DISARMED&_csrf=" + csrf;
                        logger.debug("Trying to set alarm state to DISARMED with URL {} and data {}", url, data);
                    } else if (command.toString().equals("1")) {
                        data = "code=" + pinCode + "&state=ARMED_HOME&_csrf=" + csrf;
                        logger.debug("Trying to set alarm state to ARMED_HOME with URL {} and data {}", url, data);
                    } else if (command.toString().equals("2")) {
                        data = "code=" + pinCode + "&state=ARMED_AWAY&_csrf=" + csrf;
                        logger.debug("Trying to set alarm state to ARMED_AWAY with URL {} and data {}", url, data);
                    } else {
                        logger.debug("Unknown command! {}", command);
                        return;
                    }
                    session.sendCommand(siteName, url, data);
                    ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
                    updateState(cuid, new StringType("pending"));
                }
            }
        }
    }

    @Override
    public synchronized void update(@Nullable VerisureThingJSON thing) {
        logger.debug("update on thing: {}", thing);
        updateStatus(ThingStatus.ONLINE);
        if (getThing().getThingTypeUID().equals(THING_TYPE_ALARM)) {
            VerisureAlarmJSON obj = (VerisureAlarmJSON) thing;
            if (obj != null) {
                updateAlarmState(obj);
            }
        } else {
            logger.warn("Can't handle this thing typeuid: {}", getThing().getThingTypeUID());
        }
    }

    private void updateAlarmState(VerisureAlarmJSON status) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
        String alarmStatus = status.getStatus();
        if (alarmStatus != null) {
            updateState(cuid, new StringType(alarmStatus));
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_NUMERIC_STATUS);
            DecimalType val = new DecimalType(0);
            if (alarmStatus.equals("unarmed")) {
                val = new DecimalType(0);
            } else if (alarmStatus.equals("armedhome")) {
                val = new DecimalType(1);
            } else if (alarmStatus.equals("armedaway")) {
                val = new DecimalType(2);
            } else {
                logger.warn("Unknown alarmstatus: {}", alarmStatus);
            }
            updateState(cuid, val);
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_CHANGED_BY_USER);
            updateState(cuid, new StringType(status.getName()));
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_LASTUPDATE);
            updateState(cuid, new StringType(status.getDate()));
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_ALARM_STATUS);
            updateState(cuid, new StringType(status.getLabel()));
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_ID);
            BigDecimal siteId = status.getSiteId();
            if (siteId != null) {
                updateState(cuid, new DecimalType(status.getSiteId().intValue()));
            }
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_NAME);
            updateState(cuid, new StringType(status.getSiteName()));
        }
    }

}
