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
package org.openhab.binding.verisure.internal.handler;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.dto.VerisureAlarmsDTO;
import org.openhab.binding.verisure.internal.dto.VerisureAlarmsDTO.ArmState;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Handler for the Alarm Device thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureAlarmThingHandler extends VerisureThingHandler<VerisureAlarmsDTO> {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_ALARM);

    private static final int REFRESH_DELAY_SECONDS = 10;

    public VerisureAlarmThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, channel: {}, command: {}", channelUID, command);
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else if (channelUID.getId().equals(CHANNEL_ALARM_STATUS)) {
            handleAlarmState(command);
            scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
        } else {
            logger.warn("Unknown command! {}", command);
        }
    }

    private void handleAlarmState(Command command) {
        String deviceId = config.getDeviceId();
        VerisureSession session = getSession();
        if (session != null) {
            VerisureAlarmsDTO alarm = session.getVerisureThing(deviceId, getVerisureThingClass());
            if (alarm != null) {
                BigDecimal installationId = alarm.getSiteId();
                String pinCode = session.getPinCode(installationId);

                if (pinCode != null) {
                    String url = START_GRAPHQL;
                    String operation, state = "";

                    switch (command.toString()) {
                        case "DISARMED":
                            operation = "disarm";
                            state = "armStateDisarm";
                            break;
                        case "ARMED_HOME":
                            operation = "armHome";
                            state = "armStateArmHome";
                            break;
                        case "ARMED_AWAY":
                            operation = "armAway";
                            state = "armStateArmAway";
                            break;
                        default:
                            logger.warn("Unknown alarm command: {}", command);
                            return;
                    }

                    ArrayList<AlarmDTO> list = new ArrayList<>();
                    AlarmDTO alarmJSON = new AlarmDTO();
                    VariablesDTO variables = new VariablesDTO();

                    variables.setCode(pinCode);
                    variables.setGiid(installationId.toString());
                    alarmJSON.setVariables(variables);
                    alarmJSON.setOperationName(operation);
                    String query = "mutation " + operation + "($giid: String!, $code: String!) {\n  " + state
                            + "(giid: $giid, code: $code)\n}\n";
                    alarmJSON.setQuery(query);
                    list.add(alarmJSON);

                    String queryQLAlarmSetState = gson.toJson(list);
                    logger.debug("Trying to set alarm state to {} with URL {} and data {}", operation, url,
                            queryQLAlarmSetState);

                    int httpResultCode = session.sendCommand(url, queryQLAlarmSetState, installationId);
                    if (httpResultCode == HttpStatus.OK_200) {
                        logger.debug("Alarm status successfully changed!");
                    } else {
                        logger.warn("Could not send command, HTTP result code: {}", httpResultCode);
                    }
                } else {
                    logger.warn("PIN code is not configured! Mandatory to control Alarm!");
                }
            }
        }
    }

    @Override
    public Class<VerisureAlarmsDTO> getVerisureThingClass() {
        return VerisureAlarmsDTO.class;
    }

    @Override
    public synchronized void update(VerisureAlarmsDTO thing) {
        updateAlarmState(thing);
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateAlarmState(VerisureAlarmsDTO alarmsJSON) {
        ArmState armState = alarmsJSON.getData().getInstallation().getArmState();
        String alarmStatus = armState.getStatusType();
        if (alarmStatus != null) {
            getThing().getChannels().stream().map(Channel::getUID)
                    .filter(channelUID -> isLinked(channelUID) && !channelUID.getId().equals("timestamp"))
                    .forEach(channelUID -> {
                        State state = getValue(channelUID.getId(), armState);
                        updateState(channelUID, state);
                    });
            updateTimeStamp(armState.getDate());
            updateInstallationChannels(alarmsJSON);
        } else {
            logger.warn("Alarm status is null!");
        }
    }

    public State getValue(String channelId, ArmState armState) {
        switch (channelId) {
            case CHANNEL_ALARM_STATUS:
                return new StringType(armState.getStatusType());
            case CHANNEL_CHANGED_BY_USER:
                return new StringType(armState.getName());
            case CHANNEL_CHANGED_VIA:
                return new StringType(armState.getChangedVia());
        }
        return UnDefType.UNDEF;
    }

    private static class AlarmDTO {

        @SuppressWarnings("unused")
        private @Nullable String operationName;
        @SuppressWarnings("unused")
        private VariablesDTO variables = new VariablesDTO();
        @SuppressWarnings("unused")
        private @Nullable String query;

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public void setVariables(VariablesDTO variables) {
            this.variables = variables;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

    private static class VariablesDTO {

        @SuppressWarnings("unused")
        private @Nullable String giid;
        @SuppressWarnings("unused")
        private @Nullable String code;

        public void setGiid(String giid) {
            this.giid = giid;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    @Override
    public void updateTriggerChannel(String event) {
        logger.debug("ThingHandler trigger event {}", event);
        triggerChannel(CHANNEL_SMARTLOCK_TRIGGER_CHANNEL, event);
    }
}
