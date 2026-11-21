/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.VerisureThingConfiguration;
import org.openhab.binding.verisure.internal.dto.VerisureAlarmsDTO;
import org.openhab.binding.verisure.internal.dto.VerisureAlarmsDTO.ArmState;
import org.openhab.binding.verisure.internal.dto.VerisureArmCheckDTO;
import org.openhab.binding.verisure.internal.dto.VerisureArmCheckDTO.Violation;
import org.openhab.binding.verisure.internal.dto.VerisureInstallationInfoDTO.VacationMode;
import org.openhab.binding.verisure.internal.dto.VerisureThingDTO;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * Handler for the Alarm Device thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureAlarmThingHandler extends VerisureThingHandler<VerisureAlarmsDTO> {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_ALARM);

    private static final int REFRESH_DELAY_SECONDS = 10;

    private static final String ARM_CHECK_OK = "OK";
    private static final String ARM_CHECK_FAILED = "FAILED";
    private static final String ARM_CHECK_TIMEOUT = "TIMEOUT";
    private static final String ARM_CHECK_THROTTLED = "THROTTLED";
    private static final int ARM_CHECK_POLL_SECONDS = 3;
    private static final int ARM_CHECK_MAX_POLLS = 40;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    // Channels that are not updated from the polled state in the generic loop
    private static final Set<String> SEPARATELY_UPDATED_CHANNELS = Set.of(CHANNEL_TIMESTAMP, CHANNEL_ARM_CHECK,
            CHANNEL_ARM_CHECK_RESULT, CHANNEL_VACATION_MODE_START, CHANNEL_VACATION_MODE_END);

    private @Nullable ScheduledFuture<?> armCheckJob;

    public VerisureAlarmThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = armCheckJob;
        if (job != null) {
            job.cancel(true);
            armCheckJob = null;
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, channel: {}, command: {}", channelUID, command);
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else if (channelUID.getId().equals(CHANNEL_ALARM_STATUS)) {
            handleAlarmState(command);
            scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
        } else if (channelUID.getId().equals(CHANNEL_ARM_CHECK)) {
            if (command == OnOffType.ON) {
                startArmCheck();
            }
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

    private synchronized void startArmCheck() {
        ScheduledFuture<?> job = armCheckJob;
        if (job != null && !job.isDone()) {
            logger.debug("Arm check is already running");
            return;
        }
        armCheckJob = scheduler.schedule(this::requestArmCheck, 0, TimeUnit.SECONDS);
    }

    private void requestArmCheck() {
        VerisureSession session = getSession();
        VerisureAlarmsDTO alarm = session != null
                ? session.getVerisureThing(config.getDeviceId(), getVerisureThingClass())
                : null;
        if (session == null || alarm == null) {
            finishArmCheck(ARM_CHECK_FAILED);
            return;
        }
        BigDecimal installationId = alarm.getSiteId();
        String operation = "ArmStateDryRun";
        JsonObject variables = new JsonObject();
        variables.addProperty("giid", installationId.toString());
        String query = "mutation " + operation + "($giid: String!) {\n armStateDryRun(giid: $giid)\n}\n";
        JsonObject response = session.postGraphQL(createOperationJSON(operation, query, variables), installationId);
        if (response != null && response.get("data") instanceof JsonObject data
                && data.get("armStateDryRun") instanceof JsonPrimitive transactionId) {
            scheduleArmCheckPoll(installationId, transactionId.getAsString(), 1);
        } else {
            logger.debug("Arm check could not be started: {}", response);
            finishArmCheck(isThrottled(response) ? ARM_CHECK_THROTTLED : ARM_CHECK_FAILED);
        }
    }

    private boolean isThrottled(@Nullable JsonObject response) {
        if (response != null && response.get("errors") instanceof JsonArray errors) {
            for (int i = 0; i < errors.size(); i++) {
                if (errors.get(i) instanceof JsonObject error && error.get("data") instanceof JsonObject data
                        && data.get("status") instanceof JsonPrimitive status && status.isNumber()
                        && status.getAsInt() == HTTP_TOO_MANY_REQUESTS) {
                    return true;
                }
            }
        }
        return false;
    }

    private void scheduleArmCheckPoll(BigDecimal installationId, String transactionId, int attempt) {
        armCheckJob = scheduler.schedule(() -> pollArmCheck(installationId, transactionId, attempt),
                ARM_CHECK_POLL_SECONDS, TimeUnit.SECONDS);
    }

    private void pollArmCheck(BigDecimal installationId, String transactionId, int attempt) {
        VerisureSession session = getSession();
        if (session == null) {
            finishArmCheck(ARM_CHECK_FAILED);
            return;
        }
        String operation = "ArmStateDryRunStatus";
        JsonObject variables = new JsonObject();
        variables.addProperty("giid", installationId.toString());
        variables.addProperty("transactionId", transactionId);
        String query = "query " + operation
                + "($giid: String!, $transactionId: String!) {\n installation(giid: $giid) {\n armState {\n dryRunStatus(transactionId: $transactionId) {\n status {\n status\n }\n result {\n deviceViolations {\n deviceLabel\n violation\n }\n }\n }\n }\n }\n}\n";
        JsonObject response = session.postGraphQL(createOperationJSON(operation, query, variables), installationId);
        VerisureArmCheckDTO armCheck = null;
        try {
            armCheck = response != null ? gson.fromJson(response, VerisureArmCheckDTO.class) : null;
        } catch (JsonSyntaxException e) {
            logger.debug("Failed to parse arm check status: {}", e.getMessage());
        }
        String status = armCheck != null ? armCheck.getStatus() : null;
        logger.debug("Arm check status {} after {} polls", status, attempt);
        if (armCheck != null && VerisureArmCheckDTO.STATUS_DONE.equals(status)) {
            finishArmCheck(formatViolations(session, armCheck.getViolations()));
        } else if (VerisureArmCheckDTO.STATUS_FAILED.equals(status)
                || VerisureArmCheckDTO.STATUS_STATE_CHANGE_FAILED.equals(status)) {
            finishArmCheck(ARM_CHECK_FAILED);
        } else if (attempt >= ARM_CHECK_MAX_POLLS) {
            finishArmCheck(ARM_CHECK_TIMEOUT);
        } else {
            scheduleArmCheckPoll(installationId, transactionId, attempt + 1);
        }
    }

    private String formatViolations(VerisureSession session, List<Violation> violations) {
        if (violations.isEmpty()) {
            return ARM_CHECK_OK;
        }
        return violations.stream().map(violation -> {
            String deviceLabel = violation.getDeviceLabel();
            String name = deviceLabel;
            if (deviceLabel != null) {
                VerisureThingDTO device = session
                        .getVerisureThing(VerisureThingConfiguration.normalizeDeviceId(deviceLabel));
                String location = device != null ? device.getLocation() : null;
                name = location != null ? location : deviceLabel;
            }
            return name + ": " + violation.getViolation();
        }).collect(Collectors.joining(", "));
    }

    private void finishArmCheck(String result) {
        logger.debug("Arm check result: {}", result);
        updateState(CHANNEL_ARM_CHECK_RESULT, new StringType(result));
        updateState(CHANNEL_ARM_CHECK, OnOffType.OFF);
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
            getThing().getChannels().stream().map(Channel::getUID).filter(
                    channelUID -> isLinked(channelUID) && !SEPARATELY_UPDATED_CHANNELS.contains(channelUID.getId()))
                    .forEach(channelUID -> {
                        State state = getValue(channelUID.getId(), armState, alarmsJSON);
                        updateState(channelUID, state);
                    });
            updateTimeStamp(armState.getDate());
            VacationMode vacationMode = alarmsJSON.getVacationMode();
            updateState(CHANNEL_VACATION_MODE_START,
                    toDateTime(vacationMode != null ? vacationMode.getFromDate() : null));
            updateState(CHANNEL_VACATION_MODE_END, toDateTime(vacationMode != null ? vacationMode.getToDate() : null));
            updateInstallationChannels(alarmsJSON);
        } else {
            logger.warn("Alarm status is null!");
        }
    }

    private State toDateTime(@Nullable String date) {
        if (date == null || date.isBlank()) {
            return UnDefType.UNDEF;
        }
        try {
            return new DateTimeType(ZonedDateTime.parse(date).withZoneSameInstant(ZoneId.systemDefault()));
        } catch (DateTimeException e) {
            // Not a date with time and zone
        }
        try {
            return new DateTimeType(LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()));
        } catch (DateTimeException e) {
            logger.debug("Unknown date format: {}", date);
            return UnDefType.UNDEF;
        }
    }

    public State getValue(String channelId, ArmState armState, VerisureAlarmsDTO alarmsJSON) {
        switch (channelId) {
            case CHANNEL_VACATION_MODE:
                VacationMode vacationMode = alarmsJSON.getVacationMode();
                return vacationMode != null ? OnOffType.from(vacationMode.isActive()) : UnDefType.UNDEF;
            case CHANNEL_SYSTEM_HEALTH:
                String systemHealth = alarmsJSON.getSystemHealth();
                return systemHealth != null ? new StringType(systemHealth) : UnDefType.UNDEF;
            case CHANNEL_SYSTEM_HEALTH_ALERTS:
                String systemHealthAlerts = alarmsJSON.getSystemHealthAlerts();
                return systemHealthAlerts != null ? new StringType(systemHealthAlerts) : UnDefType.UNDEF;
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
