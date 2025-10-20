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
package org.openhab.binding.automower.internal.things;

import static org.openhab.binding.automower.internal.AutomowerBindingConstants.*;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.automower.internal.AutomowerBindingConstants;
import org.openhab.binding.automower.internal.actions.AutomowerActions;
import org.openhab.binding.automower.internal.bridge.AutomowerBridge;
import org.openhab.binding.automower.internal.bridge.AutomowerBridgeHandler;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Action;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Activity;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.CalendarTask;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Capabilities;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Headlight;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.HeadlightMode;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.InactiveReason;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Message;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Metadata;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mode;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mower;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerApp;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerMessages;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerStayOutZoneAttributes;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerWorkAreaAttributes;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Planner;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.RestrictedReason;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Settings;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.State;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.StayOutZone;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.StayOutZones;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.WorkArea;
import org.openhab.binding.automower.internal.rest.exceptions.AutomowerCommunicationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link AutomowerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Pfleger - Initial contribution
 * @author Marcin Czeczko - Added support for planner and calendar data
 * @author MikeTheTux - API Extension, WSS Support, Refactoring
 */
@NonNullByDefault
public class AutomowerHandler extends BaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_AUTOMOWER);
    private static final String NO_ID = "NO_ID";

    private final Logger logger = LoggerFactory.getLogger(AutomowerHandler.class);
    private final TimeZoneProvider timeZoneProvider;
    private ZoneId mowerZoneId;

    private AtomicReference<String> automowerId = new AtomicReference<>(NO_ID);
    private @Nullable ZonedDateTime lastQueryTime = null;

    private @Nullable Mower mowerState;
    private @Nullable MowerMessages mowerMessages;

    public AutomowerHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.mowerZoneId = timeZoneProvider.getTimeZone(); // default initializer
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        String groupId = channelUID.getGroupId();
        String channelId = channelUID.getIdWithoutGroup();
        if (groupId == null) {
            logger.warn("Invalid channelUID format: {}", channelUID);
            return;
        }

        /* all pre-conditions met ... */
        if (RefreshType.REFRESH == command) {
            if (GROUP_MESSAGE.startsWith(groupId)) {
                updateAutomowerMessages(); // refresh current state from cache
            } else {
                updateAutomowerState(); // refresh current state from cache
            }
        } else if (GROUP_CALENDARTASK.startsWith(groupId)) {
            String[] channelIDSplit = channelId.split("-", 2);
            int index = Integer.parseInt(channelIDSplit[0]) - 1;
            String param = channelIDSplit[1];
            sendAutomowerCalendarTask(command, index, null, param);
        } else if (GROUP_SETTING.startsWith(groupId)) {
            if (channelUID.getId().equals(CHANNEL_SETTING_CUTTING_HEIGHT)) {
                if (command instanceof DecimalType cmd) {
                    sendAutomowerSettingsCuttingHeight(cmd.byteValue());
                }
            } else if (channelUID.getId().equals(CHANNEL_SETTING_HEADLIGHT_MODE)) {
                if (command instanceof StringType cmd) {
                    sendAutomowerSettingsHeadlightMode(cmd.toString());
                }
            }
        } else if (GROUP_STATUS.startsWith(groupId)) {
            if (channelUID.getId().equals(CHANNEL_STATUS_ERROR_CODE)) {
                if (command instanceof DecimalType cmd) {
                    if (cmd.equals(new DecimalType(0))) {
                        sendAutomowerConfirmError();
                    }
                }
            } else if (channelUID.getId().equals(CHANNEL_STATUS_POLL_UPDATE)) {
                if (command instanceof OnOffType cmd) {
                    if (cmd == OnOffType.ON) {
                        poll();
                        updateState(CHANNEL_STATUS_POLL_UPDATE, OnOffType.OFF);
                    }
                }
            }
        } else if (GROUP_STATISTIC.startsWith(groupId)) {
            if (channelUID.getId().equals(CHANNEL_STATISTIC_CUTTING_BLADE_USAGE_TIME)) {
                if (command instanceof DecimalType cmd) {
                    if (cmd.equals(new DecimalType(0))) {
                        sendAutomowerResetCuttingBladeUsageTime();
                    }
                } else if (command instanceof QuantityType cmd) {
                    if (cmd.intValue() == 0) {
                        sendAutomowerResetCuttingBladeUsageTime();
                    }
                }
            }
        } else if (GROUP_COMMAND.startsWith(groupId)) {
            AutomowerCommand.fromChannelUID(channelUID).ifPresent(commandName -> {
                logger.debug("Sending command '{}'", commandName);
                getCommandValue(command).ifPresentOrElse(param -> {
                    if (commandName == AutomowerCommand.START_IN_WORK_AREA) {
                        sendAutomowerCommand(commandName, param, null);
                    } else {
                        sendAutomowerCommand(commandName, param);
                    }
                }, () -> sendAutomowerCommand(commandName));

                updateState(channelUID, OnOffType.OFF);
            });
        } else {
            logger.warn("Command {} not supported for channel {}", command, channelUID);
        }
    }

    private Optional<Long> getCommandValue(Type type) {
        if (type instanceof DecimalType command) {
            return Optional.of(command.longValue());
        }
        return Optional.empty();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(AutomowerActions.class);
    }

    public ZoneId getMowerZoneId() {
        return this.mowerZoneId;
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            AutomowerConfiguration currentConfig = getConfigAs(AutomowerConfiguration.class);
            final String configMowerZoneId = currentConfig.getMowerZoneId();
            if (configMowerZoneId != null && !configMowerZoneId.isBlank()) {
                try {
                    mowerZoneId = ZoneId.of(configMowerZoneId);
                } catch (DateTimeException e) {
                    logger.warn("Invalid configuration mowerZoneId: {}, Error: {}", mowerZoneId, e.getMessage());
                    mowerZoneId = timeZoneProvider.getTimeZone(); // wrong config, use System TimeZone
                }
            } else {
                mowerZoneId = timeZoneProvider.getTimeZone(); // not configured, use System TimeZone
            }

            final String mowerId = this.getThing().getUID().getId();
            automowerId.set(mowerId);
            // Adding handler to map of handlers
            AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
            if (automowerBridgeHandler != null) {
                automowerBridgeHandler.registerAutomowerHandler(mowerId, this);
            }

            updateStatus(ThingStatus.UNKNOWN); // Set to UNKNOWN initially

            scheduler.execute(() -> completeInitAsync());

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    private void completeInitAsync() {
        String mowerId = automowerId.get();
        if (!mowerId.equals(NO_ID)) {
            // initial poll to get the current state of the mower
            poll();
            // update messages once via polling of REST API and later event based via WebSocket only
            initializeMessages(mowerId);
        }
    }

    private void initializeMessages(String mowerId) {
        AutomowerBridge automowerBridge = getAutomowerBridge();
        try {
            if (automowerBridge != null) {
                logger.debug("Querying automower messages for: {}", mowerId);
                mowerMessages = automowerBridge.getAutomowerMessages(mowerId);
                updateMessagesChannelState(mowerMessages);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/conf-error-no-bridge");
            }
        } catch (AutomowerCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error-query-mower-failed");
            logger.warn("Unable to query automower messages for: {}. Error: {}", mowerId, e.getMessage());
        }
    }

    @Nullable
    private AutomowerBridge getAutomowerBridge() {
        if (getBridge() instanceof Bridge bridge) {
            if (bridge.getHandler() instanceof AutomowerBridgeHandler bridgeHandler) {
                return bridgeHandler.getAutomowerBridge();
            }
        }
        return null;
    }

    @Nullable
    private AutomowerBridgeHandler getAutomowerBridgeHandler() {
        if (getBridge() instanceof Bridge bridge) {
            if (bridge.getHandler() instanceof AutomowerBridgeHandler bridgeHandler) {
                return bridgeHandler;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
        if (automowerBridgeHandler != null) {
            automowerBridgeHandler.unregisterAutomowerHandler(this.getThing().getUID().getId());
        }

        if (!automowerId.get().equals(NO_ID)) {
            automowerId.set(NO_ID);
        }
    }

    private boolean isValidResult(@Nullable Mower mower) {
        return (mower != null && mower.getAttributes() != null && mower.getAttributes().getMetadata() != null
                && mower.getAttributes().getBattery() != null && mower.getAttributes().getSystem() != null
                && mower.getAttributes().getCalendar() != null && mower.getAttributes().getCalendar().getTasks() != null
                && mower.getAttributes().getCapabilities() != null && mower.getAttributes().getMower() != null
                && mower.getAttributes().getPlanner() != null
                && mower.getAttributes().getPlanner().getOverride() != null
                && mower.getAttributes().getSettings() != null && mower.getAttributes().getStatistics() != null);
    }

    private boolean isConnected(@Nullable Mower mower) {
        return mower != null && mower.getAttributes() != null && mower.getAttributes().getMetadata() != null
                && mower.getAttributes().getMetadata().isConnected();
    }

    public void updateAutomowerStateViaREST(Mower mower) {
        this.lastQueryTime = ZonedDateTime.now(timeZoneProvider.getTimeZone());
        updateAutomowerState(mower);
    }

    public void updateAutomowerState() {
        Mower mower = this.mowerState;
        if (mower != null) {
            updateAutomowerState(mower);
        }
    }

    private void updateAutomowerMessages() {
        MowerMessages mowerMessages = this.mowerMessages;
        if (mowerMessages != null) {
            updateMessagesChannelState(mowerMessages);
        }
    }

    private void updateAutomowerState(Mower mower) {
        this.mowerState = mower;
        if (isValidResult(this.mowerState)) {
            initializeProperties(mower);
            updateMowerChannelState(mower);
            if (isConnected(mower)) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/comm-error-mower-not-connected-to-cloud");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error-query-mower-failed");
        }
    }

    public void poll() {
        AutomowerBridge automowerBridge = getAutomowerBridge();
        AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
        if (automowerBridgeHandler != null && automowerBridge != null) {
            automowerBridgeHandler.pollAutomowers(automowerBridge);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/conf-error-no-bridge");
        }
    }

    /**
     * Sends a command to the automower that requires no parameter
     *
     * @param command The command that should be sent
     */
    public void sendAutomowerCommand(AutomowerCommand command) {
        sendAutomowerCommand(command, null, null);
    }

    /**
     * Sends a command to the automower that requires a duration
     *
     * @param command The command that should be sent
     * @param commandDurationMinutes The duration of the command in minutes. This is only evaluated for "Start",
     *            "StartInWorkArea" and "Park" commands
     */
    public void sendAutomowerCommand(AutomowerCommand command, long commandDurationMinutes) {
        sendAutomowerCommand(command, null, commandDurationMinutes);
    }

    /**
     * Sends a command to the automower with the given duration
     *
     * @param command The command that should be sent. Valid values are: "Start", "StartInWorkArea", "ResumeSchedule",
     *            "Pause", "Park", "ParkUntilNextSchedule", "ParkUntilFurtherNotice"
     * @param commandWorkAreaId The work area id to be used for the command. This is only evaluated for
     *            "StartInWorkArea" command
     * @param commandDurationMinutes The duration of the command in minutes. This is only evaluated for "Start",
     *            "StartInWorkArea" and "Park" commands
     */
    public void sendAutomowerCommand(AutomowerCommand command, @Nullable Long commandWorkAreaId,
            @Nullable Long commandDurationMinutes) {
        logger.debug("Sending command '{} {} {}'", command.getCommand(), commandWorkAreaId, commandDurationMinutes);
        String id = automowerId.get();
        try {
            AutomowerBridge automowerBridge = getAutomowerBridge();
            if (automowerBridge != null) {
                automowerBridge.sendAutomowerCommand(id, command, commandWorkAreaId, commandDurationMinutes);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/conf-error-no-bridge");
            }
        } catch (AutomowerCommunicationException e) {
            logger.warn("Unable to send Command to automower: {}, Error: {}", id, e.getMessage());
        }

        // Update of the mower state after sending the command is not required as resulting state updates will be
        // received via WebSocket events
    }

    /**
     * Sends CalendarTasks of an WorkArea to the automower
     *
     * @param workAreaId The work area id to be used for the calendar tasks
     * @param start Array of start times of the calendar task in minutes after midnight
     * @param duration Array of durations of the calendar task in minutes
     * @param monday Array of calendar task active on Monday
     * @param tuesday Array of calendar task active on Tuesday
     * @param wednesday Array of calendar task active on Wednesday
     * @param thursday Array of calendar task active on Thursday
     * @param friday Array of calendar task active on Friday
     * @param saturday Array of calendar task active on Saturday
     * @param sunday Array of calendar task active on Sunday
     */
    public synchronized void sendAutomowerCalendarTask(@Nullable Long workAreaId, short[] start, short[] duration,
            boolean[] monday, boolean[] tuesday, boolean[] wednesday, boolean[] thursday, boolean[] friday,
            boolean[] saturday, boolean[] sunday) {
        Mower mower = this.mowerState;
        if (mower != null && isValidResult(mower)) {
            List<CalendarTask> calendarTaskArray = new ArrayList<>();

            for (int i = 0; (i < start.length) && (i < duration.length) && (i < monday.length) && (i < tuesday.length)
                    && (i < wednesday.length) && (i < thursday.length) && (i < friday.length) && (i < saturday.length)
                    && (i < sunday.length); i++) {
                CalendarTask calendarTask = new CalendarTask();
                calendarTask.setStart(start[i]);
                calendarTask.setDuration(duration[i]);
                calendarTask.setMonday(monday[i]);
                calendarTask.setTuesday(tuesday[i]);
                calendarTask.setWednesday(wednesday[i]);
                calendarTask.setThursday(thursday[i]);
                calendarTask.setFriday(friday[i]);
                calendarTask.setSaturday(saturday[i]);
                calendarTask.setSunday(sunday[i]);
                if (workAreaId != null) {
                    calendarTask.setWorkAreaId(workAreaId);
                }
                calendarTaskArray.add(calendarTask);
            }

            mower.getAttributes().getCalendar().setTasks(calendarTaskArray);

            String id = automowerId.get();
            try {
                AutomowerBridge automowerBridge = getAutomowerBridge();
                if (automowerBridge != null) {
                    automowerBridge.sendAutomowerCalendarTask(id,
                            mower.getAttributes().getCapabilities().hasWorkAreas(), workAreaId, calendarTaskArray);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/conf-error-no-bridge");
                }
            } catch (AutomowerCommunicationException e) {
                logger.warn("Unable to send CalendarTask to automower: {}, Error: {}", id, e.getMessage());
            }

            // Update of the mower state after sending the update is not required as resulting state updates will be
            // received via WebSocket events
        }
    }

    /**
     * Sends a CalendarTask to the automower
     *
     * @param command The command that should be sent. E.g. a duration in min for the Start channel
     * @param index The index of the calendar task
     * @param areaId Id of WorkArea the index belongs to, or null if no WorkAreas are supported
     * @param param The channel that shall be updated
     */
    public void sendAutomowerCalendarTask(Command command, int index, @Nullable String areaId, String param) {
        logger.debug("Sending CalendarTask: index '{}', areaId '{}', param '{}', command '{}'", index, areaId, param,
                command.toString());

        Mower mower = this.mowerState;
        if (mower != null && isValidResult(mower)) {
            List<CalendarTask> calendarTasksFiltered;
            List<CalendarTask> calendarTasksAll = mower.getAttributes().getCalendar().getTasks();
            if (mower.getAttributes().getCapabilities().hasWorkAreas()) {
                // only set the Tasks of the current WorkArea
                calendarTasksFiltered = new ArrayList<>();
                for (CalendarTask calendarTask : calendarTasksAll) {
                    if (String.valueOf(calendarTask.getWorkAreaId()).equals(areaId)) {
                        calendarTasksFiltered.add(calendarTask);
                    }
                }
            } else {
                calendarTasksFiltered = calendarTasksAll;
            }

            CalendarTask calendarTask = calendarTasksFiltered.get(index);

            if (command instanceof DecimalType cmd) {
                if (CHANNEL_CALENDARTASK_START.equals(param)) {
                    calendarTask.setStart(cmd.shortValue());
                } else if (CHANNEL_CALENDARTASK_DURATION.equals(param)) {
                    calendarTask.setDuration(cmd.shortValue());
                }
            } else if (command instanceof QuantityType cmd) {
                cmd = cmd.toUnit("min");
                if (cmd != null) {
                    if (CHANNEL_CALENDARTASK_START.equals(param)) {
                        calendarTask.setStart(cmd.shortValue());
                    } else if (CHANNEL_CALENDARTASK_DURATION.equals(param)) {
                        calendarTask.setDuration(cmd.shortValue());
                    }
                }
            } else if (command instanceof OnOffType cmd) {
                boolean day = ((cmd == OnOffType.ON) ? true : false);

                if (CHANNEL_CALENDARTASK_MONDAY.equals(param)) {
                    calendarTask.setMonday(day);
                } else if (CHANNEL_CALENDARTASK_TUEDAY.equals(param)) {
                    calendarTask.setTuesday(day);
                } else if (CHANNEL_CALENDARTASK_WEDNESDAY.equals(param)) {
                    calendarTask.setWednesday(day);
                } else if (CHANNEL_CALENDARTASK_THURSRAY.equals(param)) {
                    calendarTask.setThursday(day);
                } else if (CHANNEL_CALENDARTASK_FRIDAY.equals(param)) {
                    calendarTask.setFriday(day);
                } else if (CHANNEL_CALENDARTASK_SATURDAY.equals(param)) {
                    calendarTask.setSaturday(day);
                } else if (CHANNEL_CALENDARTASK_SUNDAY.equals(param)) {
                    calendarTask.setSunday(day);
                }
            }

            String id = automowerId.get();
            try {
                AutomowerBridge automowerBridge = getAutomowerBridge();
                if (automowerBridge != null) {
                    automowerBridge.sendAutomowerCalendarTask(id,
                            mower.getAttributes().getCapabilities().hasWorkAreas(), calendarTask.getWorkAreaId(),
                            calendarTasksFiltered);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/conf-error-no-bridge");
                }
            } catch (AutomowerCommunicationException e) {
                logger.warn("Unable to send CalendarTask to automower: {}, Error: {}", id, e.getMessage());
            }
        }

        // Update of the mower state after sending the update is not required as resulting state updates will be
        // received via WebSocket events
    }

    /**
     * Sends StayOutZone Setting to the automower
     *
     * @param zoneId Id of zone
     * @param enable Zone enabled or disabled
     */
    public void sendAutomowerStayOutZone(String zoneId, boolean enable) {
        logger.debug("Sending StayOutZone: zoneId {}, enable {}", zoneId, enable);
        Mower mower = this.mowerState;
        if (mower != null && isValidResult(mower)) {
            MowerStayOutZoneAttributes attributes = new MowerStayOutZoneAttributes();
            attributes.setEnable(enable);
            mower.getAttributes().getStayOutZones().getZones().stream().filter(zone -> zone.getId().equals(zoneId))
                    .findFirst().ifPresent(zone -> {
                        zone.setEnabled(enable);
                    });

            String id = automowerId.get();
            try {
                AutomowerBridge automowerBridge = getAutomowerBridge();
                if (automowerBridge != null) {
                    automowerBridge.sendAutomowerStayOutZone(id, zoneId, attributes);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/conf-error-no-bridge");
                }
            } catch (AutomowerCommunicationException e) {
                logger.warn("Unable to send StayOutZone to automower: {}, Error: {}", id, e.getMessage());
            }

            // Update the mower state as this part is not updated via WebSocket events
            updateAutomowerState();
        }
    }

    /**
     * Sends WorkArea enable Setting to the automower
     *
     * @param areaId Id of WorkArea
     * @param enable WorkArea enabled or disabled
     * 
     */
    public void sendAutomowerWorkAreaEnable(String areaId, boolean enable) {
        Mower mower = this.mowerState;
        if (mower != null && isValidResult(mower)) {
            WorkArea workArea = mower.getAttributes().getWorkAreas().stream()
                    .filter(area -> String.valueOf(area.getWorkAreaId()).equals(areaId)).findFirst().orElse(null);
            if (workArea != null) {
                sendAutomowerWorkArea(workArea.getWorkAreaId(), enable, workArea.getCuttingHeight());
            }
        }
    }

    /**
     * Sends WorkArea CuttingHeight Setting to the automower
     * 
     * @param areaId Id of WorkArea
     * @param cuttingHeight CuttingHeight of the WorkArea
     * 
     */
    public void sendAutomowerWorkAreaCuttingHeight(String areaId, byte cuttingHeight) {
        Mower mower = this.mowerState;
        if (mower != null && isValidResult(mower)) {
            WorkArea workArea = mower.getAttributes().getWorkAreas().stream()
                    .filter(area -> String.valueOf(area.getWorkAreaId()).equals(areaId)).findFirst().orElse(null);
            if (workArea != null) {
                sendAutomowerWorkArea(workArea.getWorkAreaId(), workArea.isEnabled(), cuttingHeight);
            }
        }
    }

    /**
     * Sends WorkArea Settings to the automower
     *
     * @param workAreaId Id of WorkArea
     * @param enable Work area enable or disabled
     * @param cuttingHeight CuttingHeight of the WorkArea
     */
    public void sendAutomowerWorkArea(long workAreaId, boolean enable, byte cuttingHeight) {
        logger.debug("Sending WorkArea: workAreaId {}, enable {}, cuttingHeight {}", workAreaId, enable, cuttingHeight);
        Mower mower = this.mowerState;
        if (mower != null && isValidResult(mower)) {
            MowerWorkAreaAttributes workAreaAttributes = new MowerWorkAreaAttributes();
            workAreaAttributes.setEnable(enable);
            workAreaAttributes.setCuttingHeight(cuttingHeight);
            mower.getAttributes().getWorkAreas().stream().filter(workArea -> workArea.getWorkAreaId() == workAreaId)
                    .findFirst().ifPresent(workArea -> {
                        workArea.setEnabled(enable);
                        workArea.setCuttingHeight(cuttingHeight);
                    });

            String id = automowerId.get();
            try {
                AutomowerBridge automowerBridge = getAutomowerBridge();
                if (automowerBridge != null) {
                    automowerBridge.sendAutomowerWorkArea(id, workAreaId, workAreaAttributes);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/conf-error-no-bridge");
                }
            } catch (AutomowerCommunicationException e) {
                logger.warn("Unable to send WorkArea to automower: {}, Error: {}", id, e.getMessage());
            }

            // Update the mower state as this part is not updated via WebSocket events
            updateAutomowerState();
        }
    }

    /**
     * Sends CuttingHeight Setting to the automower
     *
     * @param cuttingHeight The cuttingHeight to be sent
     */
    public void sendAutomowerSettingsCuttingHeight(byte cuttingHeight) {
        if (isValidResult(mowerState)) {
            sendAutomowerSettings(cuttingHeight, null);
        }
    }

    /**
     * Sends HeadlightMode Setting to the automower
     *
     * @param headlightMode Headlight mode as string to be sent
     */
    public void sendAutomowerSettingsHeadlightMode(String headlightMode) {
        if (isValidResult(mowerState)) {
            try {
                sendAutomowerSettings(null, HeadlightMode.valueOf(headlightMode));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid HeadlightMode: {}, Error: {}", headlightMode, e.getMessage());
            }
        }
    }

    /**
     * Sends a set of Settings to the automower
     *
     * @param cuttingHeight The cuttingHeight to be sent
     * @param headlightMode Headlight mode as string to be sent
     */
    public void sendAutomowerSettings(@Nullable Byte cuttingHeight, @Nullable HeadlightMode headlightMode) {
        logger.debug("Sending Settings: cuttingHeight {}, headlightMode {}", cuttingHeight,
                ((headlightMode != null) ? headlightMode.toString() : "null"));
        Mower mower = this.mowerState;
        if (mower != null && isValidResult(mower)) {
            // Create a new Settings object and set the values
            // that are not null. This allows to only update the values that are changed.
            Settings settingsRequest = new Settings();
            // Update the settings object with the new values
            Settings settings = mower.getAttributes().getSettings();
            if (cuttingHeight != null) {
                settingsRequest.setCuttingHeight(cuttingHeight);
                settings.setCuttingHeight(cuttingHeight);
            }
            if (headlightMode != null) {
                Headlight headlight = new Headlight();
                headlight.setHeadlightMode(headlightMode);
                settingsRequest.setHeadlight(headlight);
                settings.getHeadlight().setHeadlightMode(headlightMode);
            }

            String id = automowerId.get();
            try {
                AutomowerBridge automowerBridge = getAutomowerBridge();
                if (automowerBridge != null) {
                    automowerBridge.sendAutomowerSettings(id, settingsRequest);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/conf-error-no-bridge");
                }
            } catch (AutomowerCommunicationException e) {
                logger.warn("Unable to send SettingCuttingHeight to automower: {}, Error: {}", id, e.getMessage());
            }

            // Update of the mower state after sending the update is not required as resulting state updates will be
            // received via WebSocket events
        }
    }

    /**
     * Confirm current non fatal error on the mower
     */
    public void sendAutomowerConfirmError() {
        logger.debug("Sending ConfirmError");
        Mower mower = this.mowerState;
        if (mower != null && isValidResult(mower) && mower.getAttributes().getCapabilities().canConfirmError()
                && mower.getAttributes().getMower().getIsErrorConfirmable()) {
            String id = automowerId.get();
            try {
                AutomowerBridge automowerBridge = getAutomowerBridge();
                if (automowerBridge != null) {
                    automowerBridge.sendAutomowerConfirmError(id);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/conf-error-no-bridge");
                }
            } catch (AutomowerCommunicationException e) {
                logger.warn("Unable to send ConfirmError to automower: {}, Error: {}", id, e.getMessage());
            }

            // Update of the mower state after sending the update is not required as resulting state updates will be
            // received via WebSocket events
        }
    }

    /**
     * Reset the cutting blade usage time
     */
    public void sendAutomowerResetCuttingBladeUsageTime() {
        logger.debug("Sending ResetCuttingBladeUsageTime");
        Mower mower = this.mowerState;
        if (mower != null && isValidResult(mower)) {
            mower.getAttributes().getStatistics().setCuttingBladeUsageTime(0);

            String id = automowerId.get();
            try {
                AutomowerBridge automowerBridge = getAutomowerBridge();
                if (automowerBridge != null) {
                    automowerBridge.sendAutomowerResetCuttingBladeUsageTime(id);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/conf-error-no-bridge");
                }
            } catch (AutomowerCommunicationException e) {
                logger.warn("Unable to send ResetCuttingBladeUsageTime to automower: {}, Error: {}", id,
                        e.getMessage());
            }

            // Update the mower state as this part is not updated via WebSocket events
            updateAutomowerState();
        }
    }

    private String restrictedState(@Nullable RestrictedReason reason) {
        String restrictedReason = "RESTRICTED";
        if (reason != null) {
            restrictedReason += "_" + reason.name();
        }
        return restrictedReason;
    }

    private @Nullable String getErrorMessage(int errorCode) {
        return ERROR.get(errorCode);
    }

    private @Nullable WorkArea getWorkAreaById(@Nullable Mower mower, long workAreaId) {
        if (mower != null) {
            List<WorkArea> workAreas = mower.getAttributes().getWorkAreas();
            for (WorkArea workArea : workAreas) {
                if (workArea.getWorkAreaId() == workAreaId) {
                    return workArea;
                }
            }
        }
        return null;
    }

    private void updateMowerChannelState(@Nullable Mower mower) {
        if (mower != null && isValidResult(mower)) {
            Capabilities capabilities = mower.getAttributes().getCapabilities();
            List<CalendarTask> calendarTasks = mower.getAttributes().getCalendar().getTasks();

            /*
             * Update channels based on the received data
             */
            addRemoveDynamicChannels(mower, capabilities, calendarTasks);

            /*
             * Now update the state of the channels
             */

            /* Update Status channels */
            updateStatusChannels(mower, capabilities);
            /* Update Settings channels */
            updateSettingChannels(mower, capabilities);
            /* Update Statistics channels */
            updateStatisticChannels(mower);
            /* Update CalendarTasks channels */
            updateCalendarTaskChannels(mower, capabilities, calendarTasks);
            /* Update StayOutZones channels */
            updateStayOutZonesChannels(mower, capabilities);
            /* Update WorkAreas channels */
            updateWorkAreasChannels(mower, capabilities, calendarTasks);
        }
    }

    private synchronized void addRemoveDynamicChannels(Mower mower, Capabilities capabilities,
            List<CalendarTask> calendarTasks) {
        // make sure that static channels are present
        List<Channel> channelAdd = new ArrayList<>();

        for (String channelID : AUTOMOWER_STATIC_CHANNEL_IDS) {
            Channel channel = thing.getChannel(channelID);
            if (channel == null) {
                logger.warn("Static Channel '{}' is not present: remove and re-add Thing", channelID);
            } else {
                channelAdd.add(channel);
            }
        }

        if (capabilities.hasWorkAreas()) {
            createChannel(CHANNEL_STATUS_WORK_AREA_ID, CHANNEL_TYPE_STATUS_WORK_AREA_ID, CoreItemFactory.NUMBER,
                    channelAdd, thing);
            createChannel(CHANNEL_STATUS_WORK_AREA, CHANNEL_TYPE_STATUS_WORK_AREA, CoreItemFactory.STRING, channelAdd,
                    thing);
        }
        if (capabilities.canConfirmError()) {
            createChannel(CHANNEL_STATUS_ERROR_CONFIRMABLE, CHANNEL_TYPE_STATUS_ERROR_CONFIRMABLE,
                    CoreItemFactory.SWITCH, channelAdd, thing);
        }
        if (capabilities.hasPosition()) {
            createChannel(CHANNEL_STATUS_POSITION, CHANNEL_TYPE_STATUS_POSITION, CoreItemFactory.LOCATION, channelAdd,
                    thing);
        }
        if (capabilities.hasHeadlights()) {
            createChannel(CHANNEL_SETTING_HEADLIGHT_MODE, CHANNEL_TYPE_SETTING_HEADLIGHT_MODE, CoreItemFactory.STRING,
                    channelAdd, thing);
        }

        int i;
        if (!capabilities.hasWorkAreas()) {
            for (i = 0; i < calendarTasks.size(); i++) {
                int j = 0;
                createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                        CHANNEL_TYPE_CALENDARTASK.get(j++), "Number:Time", channelAdd, thing);
                createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                        CHANNEL_TYPE_CALENDARTASK.get(j++), "Number:Time", channelAdd, thing);
                createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                        CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
                createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                        CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
                createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                        CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
                createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                        CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
                createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                        CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
                createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                        CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
                createIndexedChannel(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j),
                        CHANNEL_TYPE_CALENDARTASK.get(j++), CoreItemFactory.SWITCH, channelAdd, thing);
            }
        }

        if (capabilities.hasStayOutZones()) {
            createChannel(CHANNEL_STAYOUTZONE_DIRTY, CHANNEL_TYPE_STAYOUTZONES_DIRTY, CoreItemFactory.SWITCH,
                    channelAdd, thing);
        }

        // remove channels that are now longer required and add new once
        updateThing(editThing().withChannels(channelAdd).build());
    }

    private void updateStatusChannels(Mower mower, Capabilities capabilities) {
        updateState(CHANNEL_STATUS_NAME, new StringType(mower.getAttributes().getSystem().getName()));
        updateState(CHANNEL_STATUS_MODE, new StringType(mower.getAttributes().getMower().getMode().name()));
        updateState(CHANNEL_STATUS_ACTIVITY, new StringType(mower.getAttributes().getMower().getActivity().name()));
        updateState(CHANNEL_STATUS_INACTIVE_REASON,
                new StringType(mower.getAttributes().getMower().getInactiveReason().name()));

        if (mower.getAttributes().getMower().getState() != State.RESTRICTED) {
            updateState(CHANNEL_STATUS_STATE, new StringType(mower.getAttributes().getMower().getState().name()));
        } else {
            updateState(CHANNEL_STATUS_STATE,
                    new StringType(restrictedState(mower.getAttributes().getPlanner().getRestrictedReason())));
        }

        if (capabilities.hasWorkAreas()) {
            Long workAreaId = mower.getAttributes().getMower().getWorkAreaId();
            if (workAreaId != null) {
                updateState(CHANNEL_STATUS_WORK_AREA_ID, new DecimalType(workAreaId));
                WorkArea workArea = getWorkAreaById(mower, workAreaId);
                if (workArea != null && workArea.getName() != null) {
                    if (workAreaId.equals(0L) && workArea.getName().isBlank()) {
                        updateState(CHANNEL_STATUS_WORK_AREA, new StringType("main area"));
                    } else {
                        updateState(CHANNEL_STATUS_WORK_AREA, new StringType(workArea.getName()));
                    }
                } else {
                    updateState(CHANNEL_STATUS_WORK_AREA, UnDefType.NULL);
                }
            } else {
                updateState(CHANNEL_STATUS_WORK_AREA_ID, UnDefType.NULL);
            }
        }

        updateState(CHANNEL_STATUS_LAST_UPDATE, new DateTimeType(
                toZonedDateTime(mower.getAttributes().getMetadata().getStatusTimestamp(), ZoneId.of("UTC"))));
        ZonedDateTime lastQuery = this.lastQueryTime;
        if (lastQuery != null) {
            updateState(CHANNEL_STATUS_LAST_POLL_UPDATE, new DateTimeType(lastQuery));
        } else {
            updateState(CHANNEL_STATUS_LAST_POLL_UPDATE, UnDefType.NULL);
        }
        updateState(CHANNEL_STATUS_BATTERY,
                new QuantityType<>(mower.getAttributes().getBattery().getBatteryPercent(), Units.PERCENT));

        int errorCode = mower.getAttributes().getMower().getErrorCode();
        updateState(CHANNEL_STATUS_ERROR_CODE, new DecimalType(errorCode));
        String errorMessage = getErrorMessage(errorCode);
        if (errorMessage != null) {
            updateState(CHANNEL_STATUS_ERROR_MESSAGE, new StringType(errorMessage));
        } else {
            updateState(CHANNEL_STATUS_ERROR_MESSAGE, UnDefType.NULL);
        }

        long errorCodeTimestamp = mower.getAttributes().getMower().getErrorCodeTimestamp();
        if (errorCodeTimestamp == 0L) {
            updateState(CHANNEL_STATUS_ERROR_TIMESTAMP, UnDefType.NULL);
        } else {
            updateState(CHANNEL_STATUS_ERROR_TIMESTAMP,
                    new DateTimeType(toZonedDateTime(errorCodeTimestamp, mowerZoneId)));
        }

        if (capabilities.canConfirmError()) {
            updateState(CHANNEL_STATUS_ERROR_CONFIRMABLE,
                    OnOffType.from(mower.getAttributes().getMower().getIsErrorConfirmable()));
        }

        long nextStartTimestamp = mower.getAttributes().getPlanner().getNextStartTimestamp();
        // If next start timestamp is 0 it means the mower should start now
        if (nextStartTimestamp == 0L) {
            updateState(CHANNEL_STATUS_NEXT_START, UnDefType.NULL);
        } else {
            updateState(CHANNEL_STATUS_NEXT_START, new DateTimeType(toZonedDateTime(nextStartTimestamp, mowerZoneId)));
        }
        updateState(CHANNEL_STATUS_OVERRIDE_ACTION,
                new StringType(mower.getAttributes().getPlanner().getOverride().getAction().name()));
        RestrictedReason restrictedReason = mower.getAttributes().getPlanner().getRestrictedReason();
        if (restrictedReason != null) {
            updateState(CHANNEL_STATUS_RESTRICTED_REASON, new StringType(restrictedReason.name()));
        } else {
            updateState(CHANNEL_STATUS_RESTRICTED_REASON, UnDefType.NULL);
        }

        updateState(CHANNEL_STATUS_EXTERNAL_REASON,
                new DecimalType(mower.getAttributes().getPlanner().getExternalReason()));

        updateState(CHANNEL_SETTING_CUTTING_HEIGHT,
                new DecimalType(mower.getAttributes().getSettings().getCuttingHeight()));

        if (capabilities.hasPosition()) {
            updateState(CHANNEL_STATUS_POSITION,
                    new PointType(new DecimalType(mower.getAttributes().getLastPosition().getLatitude()),
                            new DecimalType(mower.getAttributes().getLastPosition().getLongitude())));
        }
    }

    private void updateSettingChannels(Mower mower, Capabilities capabilities) {
        if (capabilities.hasHeadlights()) {
            Headlight headlight = mower.getAttributes().getSettings().getHeadlight();
            if (headlight != null) {
                updateState(CHANNEL_SETTING_HEADLIGHT_MODE, new StringType(headlight.getHeadlightMode().name()));
            } else {
                updateState(CHANNEL_SETTING_HEADLIGHT_MODE, UnDefType.NULL);
            }
        }
    }

    private void updateStatisticChannels(Mower mower) {
        updateState(CHANNEL_STATISTIC_CUTTING_BLADE_USAGE_TIME,
                new QuantityType<>(mower.getAttributes().getStatistics().getCuttingBladeUsageTime(), Units.SECOND));
        updateState(CHANNEL_STATISTIC_DOWN_TIME,
                new QuantityType<>(mower.getAttributes().getStatistics().getDownTime(), Units.SECOND));
        updateState(CHANNEL_STATISTIC_NUMBER_OF_CHARGING_CYCLES,
                new DecimalType(mower.getAttributes().getStatistics().getNumberOfChargingCycles()));
        updateState(CHANNEL_STATISTIC_NUMBER_OF_COLLISIONS,
                new DecimalType(mower.getAttributes().getStatistics().getNumberOfCollisions()));
        updateState(CHANNEL_STATISTIC_TOTAL_CHARGING_TIME,
                new QuantityType<>(mower.getAttributes().getStatistics().getTotalChargingTime(), Units.SECOND));
        updateState(CHANNEL_STATISTIC_TOTAL_CUTTING_TIME,
                new QuantityType<>(mower.getAttributes().getStatistics().getTotalCuttingTime(), Units.SECOND));
        updateState(CHANNEL_STATISTIC_TOTAL_DRIVE_DISTANCE,
                new QuantityType<>(mower.getAttributes().getStatistics().getTotalDriveDistance(), SIUnits.METRE));
        updateState(CHANNEL_STATISTIC_TOTAL_RUNNING_TIME,
                new QuantityType<>(mower.getAttributes().getStatistics().getTotalRunningTime(), Units.SECOND));
        updateState(CHANNEL_STATISTIC_TOTAL_SEARCHING_TIME,
                new QuantityType<>(mower.getAttributes().getStatistics().getTotalSearchingTime(), Units.SECOND));

        if (mower.getAttributes().getStatistics().getTotalRunningTime() != 0) {
            updateState(CHANNEL_STATISTIC_TOTAL_CUTTING_PERCENT,
                    new QuantityType<>(
                            (float) mower.getAttributes().getStatistics().getTotalCuttingTime()
                                    / (float) mower.getAttributes().getStatistics().getTotalRunningTime() * 100.0,
                            Units.PERCENT));
            updateState(CHANNEL_STATISTIC_TOTAL_SEARCHING_PERCENT,
                    new QuantityType<>(
                            (float) mower.getAttributes().getStatistics().getTotalSearchingTime()
                                    / (float) mower.getAttributes().getStatistics().getTotalRunningTime() * 100.0,
                            Units.PERCENT));
        } else {
            updateState(CHANNEL_STATISTIC_TOTAL_CUTTING_PERCENT, new QuantityType<>(0, Units.PERCENT));
            updateState(CHANNEL_STATISTIC_TOTAL_SEARCHING_PERCENT, new QuantityType<>(0, Units.PERCENT));
        }
        updateState(CHANNEL_STATISTIC_UP_TIME,
                new QuantityType<>(mower.getAttributes().getStatistics().getUpTime(), Units.SECOND));
    }

    private void updateCalendarTaskChannels(Mower mower, Capabilities capabilities, List<CalendarTask> calendarTasks) {
        // Only update the calendar tasks if no work areas are supported as in this case the tasks are managed
        // via the work area thing handlers
        if (!capabilities.hasWorkAreas()) {
            int i = 0;
            for (; i < calendarTasks.size(); i++) {
                int j = 0;
                updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                        new QuantityType<>(calendarTasks.get(i).getStart(), Units.MINUTE));
                updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                        new QuantityType<>(calendarTasks.get(i).getDuration(), Units.MINUTE));
                updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                        OnOffType.from(calendarTasks.get(i).getMonday()));
                updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                        OnOffType.from(calendarTasks.get(i).getTuesday()));
                updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                        OnOffType.from(calendarTasks.get(i).getWednesday()));
                updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                        OnOffType.from(calendarTasks.get(i).getThursday()));
                updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                        OnOffType.from(calendarTasks.get(i).getFriday()));
                updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                        OnOffType.from(calendarTasks.get(i).getSaturday()));
                updateIndexedState(GROUP_CALENDARTASK, i + 1, CHANNEL_CALENDARTASK.get(j++),
                        OnOffType.from(calendarTasks.get(i).getSunday()));
            }
        }
    }

    private void updateStayOutZonesChannels(Mower mower, Capabilities capabilities) {
        if (capabilities.hasStayOutZones()) {
            StayOutZones stayOutZones = mower.getAttributes().getStayOutZones();
            if (stayOutZones != null) {
                updateState(CHANNEL_STAYOUTZONE_DIRTY, OnOffType.from(stayOutZones.isDirty()));

                List<StayOutZone> stayOutZoneList = stayOutZones.getZones();
                if (stayOutZoneList != null) {
                    AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
                    if (automowerBridgeHandler != null) {
                        for (StayOutZone zone : stayOutZoneList) {
                            AutomowerStayoutZoneHandler zoneHandler = automowerBridgeHandler
                                    .getAutomowerStayoutZoneHandlerByThingId(zone.getId());
                            if (zoneHandler != null) {
                                zoneHandler.updateStayOutZoneChannels(zone);
                            } else {
                                logger.trace("No AutomowerStayoutZoneHandler found for zoneId: {}", zone.getId());
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateWorkAreasChannels(Mower mower, Capabilities capabilities, List<CalendarTask> calendarTasks) {
        if (capabilities.hasWorkAreas()) {
            List<WorkArea> workAreas = mower.getAttributes().getWorkAreas();
            if (workAreas != null) {
                AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
                if (automowerBridgeHandler != null) {
                    for (WorkArea area : workAreas) {
                        AutomowerWorkAreaHandler areaHandler = automowerBridgeHandler
                                .getAutomowerWorkAreaHandlerByThingId(
                                        mower.getId() + "-" + String.valueOf(area.getWorkAreaId()));
                        if (areaHandler != null) {
                            areaHandler.updateChannels(area, calendarTasks, this);
                        } else {
                            logger.trace("No AutomowerWorkAreaHandler found for areaId: {}", area.getWorkAreaId());
                        }
                    }
                }
            }
        }
    }

    private void updateMessagesChannelState(@Nullable MowerMessages mowerMessages) {
        if (mowerMessages != null) {
            Message message = mowerMessages.getAttributes().getMessages().get(0);
            updateState(CHANNEL_MESSAGE_TIMESTAMP,
                    new DateTimeType(toZonedDateTime(message.getTime() * 1000, mowerZoneId)));

            int code = message.getCode();
            updateState(CHANNEL_MESSAGE_CODE, new DecimalType(code));

            String errorMessage = getErrorMessage(code);
            if (errorMessage != null) {
                updateState(CHANNEL_MESSAGE_TEXT, new StringType(errorMessage));
            } else {
                updateState(CHANNEL_MESSAGE_TEXT, UnDefType.NULL);
            }

            updateState(CHANNEL_MESSAGE_SEVERITY, new StringType(message.getSeverity()));
            Double latitude = message.getLatitude();
            Double longitude = message.getLongitude();
            if (latitude != null && longitude != null) {
                updateState(CHANNEL_MESSAGE_GPS_POSITION,
                        new PointType(new DecimalType(latitude), new DecimalType(longitude)));
            } else {
                updateState(CHANNEL_MESSAGE_GPS_POSITION, UnDefType.NULL);
            }
        }
    }

    private void initializeProperties(@Nullable Mower mower) {
        if (mower != null && isValidResult(mower)) {
            Map<String, String> properties = editProperties();
            properties.put(AutomowerBindingConstants.AUTOMOWER_ID, mower.getId());

            properties.put(AutomowerBindingConstants.AUTOMOWER_SERIAL_NUMBER,
                    mower.getAttributes().getSystem().getSerialNumber());
            properties.put(AutomowerBindingConstants.AUTOMOWER_MODEL, mower.getAttributes().getSystem().getModel());
            properties.put(AutomowerBindingConstants.AUTOMOWER_NAME, mower.getAttributes().getSystem().getName());

            Capabilities capabilities = mower.getAttributes().getCapabilities();
            properties.put(AutomowerBindingConstants.AUTOMOWER_CAN_CONFIRM_ERROR,
                    (capabilities.canConfirmError() ? "yes" : "no"));
            properties.put(AutomowerBindingConstants.AUTOMOWER_HAS_HEADLIGHTS,
                    (capabilities.hasHeadlights() ? "yes" : "no"));
            properties.put(AutomowerBindingConstants.AUTOMOWER_HAS_POSITION,
                    (capabilities.hasPosition() ? "yes" : "no"));
            properties.put(AutomowerBindingConstants.AUTOMOWER_HAS_STAY_OUT_ZONES,
                    (capabilities.hasStayOutZones() ? "yes" : "no"));
            properties.put(AutomowerBindingConstants.AUTOMOWER_HAS_WORK_AREAS,
                    (capabilities.hasWorkAreas() ? "yes" : "no"));

            updateProperties(properties);
        }
    }

    /**
     * Converts timestamp returned by the Automower API into ZonedDateTime of the specified timezone.
     * Timestamp returned by the API is a mix of timezone specified via zoneId and UTC timezone.
     * Method builds a valid ZonedDateTime object according to the provided zoneId parameter.
     *
     * @param timestamp - Automower API timestamp
     * @param zoneId - Intended timezone of the timestamp
     * @return ZonedDateTime using provided timezone
     */
    public ZonedDateTime toZonedDateTime(long timestamp, ZoneId zoneId) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC")).withZoneSameLocal(zoneId);
    }

    static String toCamelCase(String input) {
        char delimiter = ' ';

        StringBuilder builder = new StringBuilder();
        boolean nextCharLow = false;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (delimiter == currentChar) {
                nextCharLow = false;
            } else if (nextCharLow) {
                builder.append(Character.toLowerCase(currentChar));
            } else {
                builder.append(Character.toUpperCase(currentChar));
                nextCharLow = true;
            }
        }
        return builder.toString();
    }

    static String getLabel(String channel) {
        return toCamelCase(channel.replace(" ", "-"));
    }

    static String getIndexedChannel(String ChannelGroup, int id, String channel) {
        // 2 digits as default and all digits for the unrealistic case of more than 99 channels
        String format = ((id >= 100) ? "%d" : "%02d");
        return ChannelGroup + String.format(format, id) + (channel.isBlank() ? "" : "-" + channel);
    }

    static String getIndexedLabel(int id, String channel) {
        // 2 digits as default and all digits for the unrealistic case of more than 99 channels
        String format = ((id >= 100) ? "%d" : "%02d");
        channel = getLabel(channel);
        return (channel.isBlank() ? "" : channel + " #") + String.format(format, id);
    }

    private void updateIndexedState(String ChannelGroup, int id, String channel, org.openhab.core.types.State state) {
        String indexedChannel = getIndexedChannel(ChannelGroup, id, channel);
        updateState(indexedChannel, state);
    }

    static void createIndexedChannel(String ChannelGroup, int id, String channel, ChannelTypeUID channelTypeUID,
            String itemType, List<Channel> channelAdd, Thing thing) {
        String indexedChannel = getIndexedChannel(ChannelGroup, id, channel);
        String indexedLabel = getIndexedLabel(id, channel);
        createChannel(indexedLabel, indexedChannel, channelTypeUID, itemType, channelAdd, thing);
    }

    static void createChannel(String channel, ChannelTypeUID channelTypeUID, String itemType, List<Channel> channelAdd,
            Thing thing) {
        createChannel(null, channel, channelTypeUID, itemType, channelAdd, thing);
    }

    static void createChannel(@Nullable String label, String channel, ChannelTypeUID channelTypeUID, String itemType,
            List<Channel> channelAdd, Thing thing) {
        ChannelUID channelUid = new ChannelUID(thing.getUID(), channel);
        Channel chn = thing.getChannel(channelUid);
        if (chn == null) {
            // Channel does not yet exist - create it
            Channel newChannel;
            if (label == null) {
                newChannel = ChannelBuilder.create(channelUid, itemType).withType(channelTypeUID).build();
            } else {
                newChannel = ChannelBuilder.create(channelUid, itemType).withType(channelTypeUID).withLabel(label)
                        .build();
            }
            channelAdd.add(newChannel);
        } else {
            // Channel already exists
            channelAdd.add(chn);
        }
    }

    /*
     * Process WebSocket messages according to
     * https://developer.husqvarnagroup.cloud/apis/automower-connect-api?tab=websocket%20api
     */
    public void processWebSocketMessage(JsonObject event) {
        Mower mower = this.mowerState;
        MowerMessages mowerMessages = this.mowerMessages;
        if (mower != null && (mowerMessages != null)) {
            try {
                String type = event.has("type") ? event.get("type").getAsString() : null;
                if (type != null && event.has("attributes") && event.get("attributes").isJsonObject()) {
                    JsonObject attributes = event.getAsJsonObject("attributes");
                    Metadata metaData = mower.getAttributes().getMetadata();
                    long nowMs = ZonedDateTime.now(mowerZoneId).toInstant().toEpochMilli();
                    switch (type) {
                        case "battery-event-v2":
                            handleBatteryEventV2(attributes, mower, metaData, nowMs);
                            break;
                        case "calendar-event-v2":
                            handleCalendarEventV2(attributes, mower, metaData, nowMs);
                            break;
                        case "cuttingHeight-event-v2":
                            handleCuttingHeightEventV2(attributes, mower, metaData, nowMs);
                            break;
                        case "headlights-event-v2":
                            handleHeadlightsEventV2(attributes, mower, metaData, nowMs);
                            break;
                        case "message-event-v2":
                            handleMessageEventV2(attributes, mowerMessages);
                            break;
                        case "mower-event-v2":
                            handleMowerEventV2(attributes, mower, metaData, nowMs);
                            break;
                        case "planner-event-v2":
                            handlePlannerEventV2(attributes, mower, metaData, nowMs);
                            break;
                        case "position-event-v2":
                            handlePositionEventV2(attributes, mower, metaData, nowMs);
                            break;
                        default:
                            logger.debug("Unhandled WebSocket event type: {}", type);
                            break;
                    }
                } else {
                    logger.warn("Received WebSocket event without type or attributes: {}", event);
                }
            } catch (Exception e) {
                logger.error("Error processing WebSocket event: {}", e.getMessage());
            }
        } else {
            logger.debug("Channels not yet intialized via REST - ignoring WebSocket message: {}", event);
        }
    }

    private void handleBatteryEventV2(JsonObject attributes, Mower mower, Metadata metaData, long nowMs) {
        try {
            if (attributes.has("battery") && attributes.get("battery").isJsonObject()) {
                JsonObject batteryObj = attributes.getAsJsonObject("battery");
                if (batteryObj.has("batteryPercent")) {
                    byte batteryPercent = batteryObj.get("batteryPercent").getAsByte();
                    logger.debug("Received battery update: {}%", batteryPercent);
                    mower.getAttributes().getBattery().setBatteryPercent(batteryPercent);
                    metaData.setStatusTimestamp(nowMs);
                    updateMowerChannelState(mower);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing battery-event-v2: {}", e.getMessage());
        }
    }

    private void handleCalendarEventV2(JsonObject attributes, Mower mower, Metadata metaData, long nowMs) {
        try {
            if (attributes.has("calendar") && attributes.get("calendar").isJsonObject()) {
                JsonObject calendarObj = attributes.getAsJsonObject("calendar");
                if (calendarObj.has("tasks")) {
                    List<CalendarTask> calendarTasks = new ArrayList<>();
                    JsonArray tasks = calendarObj.getAsJsonArray("tasks");
                    for (int i = 0; i < tasks.size(); i++) {
                        JsonObject taskObj = tasks.get(i).getAsJsonObject();
                        CalendarTask task = new CalendarTask();
                        task.setStart(taskObj.get("start").getAsShort());
                        task.setDuration(taskObj.get("duration").getAsShort());
                        task.setMonday(taskObj.get("monday").getAsBoolean());
                        task.setTuesday(taskObj.get("tuesday").getAsBoolean());
                        task.setWednesday(taskObj.get("wednesday").getAsBoolean());
                        task.setThursday(taskObj.get("thursday").getAsBoolean());
                        task.setFriday(taskObj.get("friday").getAsBoolean());
                        task.setSaturday(taskObj.get("saturday").getAsBoolean());
                        task.setSunday(taskObj.get("sunday").getAsBoolean());
                        if (taskObj.has("workAreaId")) {
                            task.setWorkAreaId(taskObj.get("workAreaId").getAsLong());
                        }
                        logger.debug(
                                "Received calendar task: start={}, duration={}, monday={}, tuesday={}, wednesday={}, thursday={}, friday={}, saturday={}, sunday={}, workAreaId={}",
                                task.getStart(), task.getDuration(), task.getMonday(), task.getTuesday(),
                                task.getWednesday(), task.getThursday(), task.getFriday(), task.getSaturday(),
                                task.getSunday(), task.getWorkAreaId());
                        calendarTasks.add(task);
                    }
                    mower.getAttributes().getCalendar().setTasks(calendarTasks);
                    metaData.setStatusTimestamp(nowMs);
                    updateMowerChannelState(mower);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing calendar-event-v2: {}", e.getMessage());
        }
    }

    private void handleCuttingHeightEventV2(JsonObject attributes, Mower mower, Metadata metaData, long nowMs) {
        try {
            if (attributes.has("cuttingHeight") && attributes.get("cuttingHeight").isJsonObject()) {
                JsonObject cuttingHeightObj = attributes.getAsJsonObject("cuttingHeight");
                if (cuttingHeightObj.has("height")) {
                    mower.getAttributes().getSettings().setCuttingHeight(cuttingHeightObj.get("height").getAsByte());
                    logger.debug("Received cutting height update: {}", cuttingHeightObj.get("height").getAsByte());
                    metaData.setStatusTimestamp(nowMs);
                    updateMowerChannelState(mower);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing cuttingHeight-event-v2: {}", e.getMessage());
        }
    }

    private void handleHeadlightsEventV2(JsonObject attributes, Mower mower, Metadata metaData, long nowMs) {
        try {
            if (attributes.has("headlight") && attributes.get("headlight").isJsonObject()) {
                JsonObject headlightObj = attributes.getAsJsonObject("headlight");
                if (headlightObj.has("mode")) {
                    mower.getAttributes().getSettings().getHeadlight()
                            .setHeadlightMode(HeadlightMode.valueOf(headlightObj.get("mode").getAsString()));
                    logger.debug("Received headlight mode update: {}", headlightObj.get("mode").getAsString());
                    metaData.setStatusTimestamp(nowMs);
                    updateMowerChannelState(mower);
                }
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid headlight mode received: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing headlight-event-v2: {}", e.getMessage());
        }
    }

    private void handleMessageEventV2(JsonObject attributes, MowerMessages mowerMessages) {
        try {
            if (attributes.has("message") && attributes.get("message").isJsonObject()) {
                JsonObject msgObj = attributes.getAsJsonObject("message");
                long time = msgObj.has("time") ? msgObj.get("time").getAsLong() : 0L;
                int code = msgObj.has("code") ? msgObj.get("code").getAsInt() : 0;
                String severity = msgObj.has("severity") ? msgObj.get("severity").getAsString() : "";
                Double latitude = msgObj.has("latitude") ? msgObj.get("latitude").getAsDouble() : null;
                Double longitude = msgObj.has("longitude") ? msgObj.get("longitude").getAsDouble() : null;
                logger.debug("Received mower message: time={}, code={}, severity={}, lat={}, lon={}", time, code,
                        severity, latitude, longitude);

                Message message = mowerMessages.getAttributes().getMessages().get(0);
                if (message != null) {
                    message.setTime(time);
                    message.setCode(code);
                    message.setSeverity(severity);
                    message.setLatitude(latitude);
                    message.setLongitude(longitude);
                    updateMessagesChannelState(mowerMessages);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing message-event-v2: {}", e.getMessage());
        }
    }

    private void handleMowerEventV2(JsonObject attributes, Mower mower, Metadata metaData, long nowMs) {
        try {
            if (attributes.has("mower") && attributes.get("mower").isJsonObject()) {
                JsonObject mowerObj = attributes.getAsJsonObject("mower");
                MowerApp mowerApp = mower.getAttributes().getMower();
                if (mowerObj.has("mode")) {
                    mowerApp.setMode(Mode.valueOf(mowerObj.get("mode").getAsString()));
                }
                if (mowerObj.has("activity")) {
                    mowerApp.setActivity(Activity.valueOf(mowerObj.get("activity").getAsString()));
                }
                if (mowerObj.has("inactiveReason")) {
                    mowerApp.setInactiveReason(InactiveReason.valueOf(mowerObj.get("inactiveReason").getAsString()));
                }
                if (mowerObj.has("state")) {
                    mowerApp.setState(State.valueOf(mowerObj.get("state").getAsString()));
                }
                if (mowerObj.has("errorCode")) {
                    mowerApp.setErrorCode(mowerObj.get("errorCode").getAsInt());
                }
                if (mowerObj.has("isErrorConfirmable")) {
                    mowerApp.setIsErrorConfirmable(mowerObj.get("isErrorConfirmable").getAsBoolean());
                }
                if (mowerObj.has("errorCodeTimestamp")) {
                    mowerApp.setErrorCodeTimestamp(mowerObj.get("errorCodeTimestamp").getAsLong());
                }
                if (mowerObj.has("workAreaId")) {
                    mowerApp.setWorkAreaId(mowerObj.get("workAreaId").getAsLong());
                }

                logger.debug(
                        "Received mower event: mode={}, activity={}, inactiveReason={}, state={}, errorCode={}, isErrorConfirmable={}, errorCodeTimestamp={}, workAreaId={}",
                        mowerObj.has("mode") ? mowerObj.get("mode").getAsString() : null,
                        mowerObj.has("activity") ? mowerObj.get("activity").getAsString() : null,
                        mowerObj.has("inactiveReason") ? mowerObj.get("inactiveReason").getAsString() : null,
                        mowerObj.has("state") ? mowerObj.get("state").getAsString() : null,
                        mowerObj.has("errorCode") ? mowerObj.get("errorCode").getAsInt() : null,
                        mowerObj.has("isErrorConfirmable") ? mowerObj.get("isErrorConfirmable").getAsBoolean() : null,
                        mowerObj.has("errorCodeTimestamp") ? mowerObj.get("errorCodeTimestamp").getAsLong() : null,
                        mowerObj.has("workAreaId") ? mowerObj.get("workAreaId").getAsLong() : null);
                metaData.setStatusTimestamp(nowMs);
                updateMowerChannelState(mower);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid value received in mower event: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing mower-event-v2: {}", e.getMessage());
        }
    }

    private void handlePlannerEventV2(JsonObject attributes, Mower mower, Metadata metaData, long nowMs) {
        try {
            if (attributes.has("planner") && attributes.get("planner").isJsonObject()) {
                JsonObject plannerObj = attributes.getAsJsonObject("planner");
                Planner planner = mower.getAttributes().getPlanner();
                if (plannerObj.has("nextStartTimestamp")) {
                    planner.setNextStartTimestamp(plannerObj.get("nextStartTimestamp").getAsLong());
                }
                if (plannerObj.has("override") && plannerObj.get("override").isJsonObject()) {
                    JsonObject overrideObj = plannerObj.getAsJsonObject("override");
                    if (overrideObj.has("action")) {
                        planner.getOverride().setAction(Action.valueOf(overrideObj.get("action").getAsString()));
                    }
                }
                if (plannerObj.has("restrictedReason")) {
                    planner.setRestrictedReason(
                            RestrictedReason.valueOf(plannerObj.get("restrictedReason").getAsString()));
                }
                if (plannerObj.has("externalReason")) {
                    planner.setExternalReason(plannerObj.get("externalReason").getAsInt());
                }
                logger.debug(
                        "Received planner event: nextStartTimestamp={}, override.action={}, restrictedReason={}, externalReason={}",
                        plannerObj.has("nextStartTimestamp") ? plannerObj.get("nextStartTimestamp").getAsLong() : null,
                        plannerObj.has("override") && plannerObj.get("override").isJsonObject()
                                && plannerObj.getAsJsonObject("override").has("action")
                                        ? plannerObj.getAsJsonObject("override").get("action").getAsString()
                                        : null,
                        plannerObj.has("restrictedReason") ? plannerObj.get("restrictedReason").getAsString() : null,
                        plannerObj.has("externalReason") ? plannerObj.get("externalReason").getAsInt() : null);
                metaData.setStatusTimestamp(nowMs);
                updateMowerChannelState(mower);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid value received in planner event: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing planner-event-v2: {}", e.getMessage());
        }
    }

    private void handlePositionEventV2(JsonObject attributes, Mower mower, Metadata metaData, long nowMs) {
        try {
            if (attributes.has("position")) {
                JsonObject position = attributes.getAsJsonObject("position");
                if (position.has("latitude") && position.has("longitude")) {
                    double latitude = position.get("latitude").getAsDouble();
                    double longitude = position.get("longitude").getAsDouble();
                    logger.debug("Received position update: lat={}, lon={}", latitude, longitude);
                    mower.getAttributes().getLastPosition().setLatitude(latitude);
                    mower.getAttributes().getLastPosition().setLongitude(longitude);
                    metaData.setStatusTimestamp(nowMs);
                    updateMowerChannelState(mower);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing position-event-v2: {}", e.getMessage());
        }
    }
}
