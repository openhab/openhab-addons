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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.verisure.internal.model.VerisureAlarmsJSON;
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

    public VerisureAlarmThingHandler(Thing thing) {
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
            VerisureAlarmsJSON alarm = (VerisureAlarmsJSON) session
                    .getVerisureThing(config.deviceId.replaceAll("[^a-zA-Z0-9]+", ""));
            if (alarm != null) {
            	BigDecimal installationId = alarm.getSiteId();
            	BigDecimal pinCode = session.getPinCode(installationId);
            	String deviceId = config.deviceId;
            	if (deviceId != null && pinCode != null && installationId != null) {
            		StringBuilder sb = new StringBuilder(deviceId);
            		sb.insert(4, " ");
            		String url = START_GRAPHQL;
            		String queryQLAlarmSetState;
                    if (command.toString().equals("0")) {
                    	queryQLAlarmSetState = "[{\"operationName\":\"disarm\",\"variables\":{\"giid\":\"" + installationId + "\",\"code\":\"" + pinCode + "\"},\"query\":\"mutation disarm($giid: String!, $code: String!) {\\n  armStateDisarm(giid: $giid, code: $code)\\n}\\n\"}]\n" + 
                    			"";
                        logger.debug("Trying to set alarm state to DISARMED with URL {} and data {}", url, queryQLAlarmSetState);
                    } else if (command.toString().equals("1")) {
                    	queryQLAlarmSetState = "[{\"operationName\":\"armHome\",\"variables\":{\"giid\":\"" + installationId + "\",\"code\":\"" + pinCode + "\"},\"query\":\"mutation armHome($giid: String!, $code: String!) {\\n  armStateArmHome(giid: $giid, code: $code)\\n}\\n\"}]\n" + 
                    			"";
                        logger.debug("Trying to set alarm state to ARMED_HOME with URL {} and data {}", url, queryQLAlarmSetState);
                    } else if (command.toString().equals("2")) {
                    	queryQLAlarmSetState = "[{\"operationName\":\"armAway\",\"variables\":{\"giid\":\"" + installationId + "\",\"code\":\"" + pinCode + "\"},\"query\":\"mutation armAway($giid: String!, $code: String!) {\\n  armStateArmAway(giid: $giid, code: $code)\\n}\\n\"}]\n" + 
                        		"";
                        logger.debug("Trying to set alarm state to ARMED_AWAY with URL {} and data {}", url, queryQLAlarmSetState);
                    } else {
                        logger.debug("Unknown command! {}", command);
                        return;
                    }
                    int httpResultCode = session.sendCommand(url, queryQLAlarmSetState, installationId);
					if (httpResultCode == HttpStatus.OK_200) {
						ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_ALARM_STATUS);
						updateState(cuid, new StringType("PENDING"));
					} else {
						logger.warn("Could not send command, HTTP result code: {}", httpResultCode);
					}
                } else if (pinCode == null) {
                    logger.warn("PIN code is not configured! Mandatory to control Alarm!");
                }
            }
        }
    }

    @Override
    public synchronized void update(@Nullable VerisureThingJSON thing) {
        logger.debug("update on thing: {}", thing);
        updateStatus(ThingStatus.ONLINE);
        if (getThing().getThingTypeUID().equals(THING_TYPE_ALARM)) {
            VerisureAlarmsJSON obj = (VerisureAlarmsJSON) thing;
            if (obj != null) {
                updateAlarmState(obj);
            }
        } else {
            logger.warn("Can't handle this thing typeuid: {}", getThing().getThingTypeUID());
        }
    }

    private void updateAlarmState(VerisureAlarmsJSON alarmsJSON) {
        String alarmStatus = alarmsJSON.getData().getInstallation().getArmState().getStatusType();
        if (alarmStatus != null) {
        	ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_ALARM_STATUS);
            updateState(cuid, new StringType(alarmStatus));
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_NUMERIC_STATUS);
            DecimalType val = new DecimalType(0);
            if (alarmStatus.equals("DISARMED")) {
                val = new DecimalType(0);
            } else if (alarmStatus.equals("ARMED_HOME")) {
                val = new DecimalType(1);
            } else if (alarmStatus.equals("ARMED_AWAY")) {
                val = new DecimalType(2);
            } else if (alarmStatus.equals("PENDING")) {
                // Schedule another refresh.
            	logger.debug("Issuing another immediate refresh since statis is stii PENDING ...");
                this.scheduleImmediateRefresh();
            } else {
                logger.warn("Unknown alarmstatus: {}", alarmStatus);
            }
            updateState(cuid, val);
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_SET_ALARM_STATUS);
            updateState(cuid, val);
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_CHANGED_BY_USER);
            updateState(cuid, new StringType(alarmsJSON.getData().getInstallation().getArmState().getName()));
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_CHANGED_VIA);
            updateState(cuid, new StringType(alarmsJSON.getData().getInstallation().getArmState().getChangedVia())); 
            updateTimeStamp(alarmsJSON.getData().getInstallation().getArmState().getDate());
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_INSTALLATION_ID);
            BigDecimal siteId = alarmsJSON.getSiteId();
            if (siteId != null) {
                updateState(cuid, new DecimalType(siteId.longValue()));
            }
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_INSTALLATION_NAME);
            updateState(cuid, new StringType(alarmsJSON.getSiteName()));
        } else {
        	logger.warn("Alarm status is null!");
        }
    }

}
