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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.automower.internal.AutomowerBindingConstants;
import org.openhab.binding.automower.internal.actions.AutomowerActions;
import org.openhab.binding.automower.internal.bridge.AutomowerBridge;
import org.openhab.binding.automower.internal.bridge.AutomowerBridgeHandler;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.CalendarTask;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Capabilities;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Headlight;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.HeadlightMode;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mower;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerStayOutZoneAttributes;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerWorkAreaAttributes;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Position;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.RestrictedReason;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Settings;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.State;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.StayOutZone;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.WorkArea;
import org.openhab.binding.automower.internal.rest.exceptions.AutomowerCommunicationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AutomowerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Pfleger - Initial contribution
 * @author Marcin Czeczko - Added support for planner and calendar data
 */
@NonNullByDefault
public class AutomowerHandler extends BaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_AUTOMOWER);
    private static final String NO_ID = "NO_ID";
    private static final long DEFAULT_COMMAND_DURATION_MIN = 60;
    private static final long DEFAULT_POLLING_INTERVAL_S = TimeUnit.MINUTES.toSeconds(10);

    private final Logger logger = LoggerFactory.getLogger(AutomowerHandler.class);
    private final TimeZoneProvider timeZoneProvider;
    private ZoneId mowerZoneId;

    private AtomicReference<String> automowerId = new AtomicReference<>(NO_ID);
    private long lastQueryTimeMs = 0L;

    private @Nullable ScheduledFuture<?> automowerPollingJob;
    private long maxQueryFrequencyNanos = TimeUnit.MINUTES.toNanos(1);

    private @Nullable Mower mowerState;

    private Runnable automowerPollingRunnable = () -> {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            updateAutomowerState();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    };

    public AutomowerHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.mowerZoneId = timeZoneProvider.getTimeZone(); // default initializer
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing channel '{}'", channelUID);
            refreshChannels(channelUID);
        } else if (CHANNEL_CALENDARTASKS.contains(channelUID.getId())) {
            sendAutomowerCalendarTask(command, channelUID.getId());
        } else if (CHANNEL_STAYOUTZONES.contains(channelUID.getId())) {
            String[] channelIDSplit = channelUID.getId().split("-");
            int index = Integer.parseInt(channelIDSplit[0].substring("zone".length())) - 1;
            if ("enabled".equals(channelIDSplit[0])) {
                if (command instanceof OnOffType cmd) {
                    sendAutomowerStayOutZone(index, ((cmd == OnOffType.ON) ? true : false));
                }
            }
        } else if (CHANNEL_WORKAREAS.contains(channelUID.getId())) {
            String[] channelIDSplit = channelUID.getId().split("-");
            int index = Integer.parseInt(channelIDSplit[0].substring("workarea".length())) - 1;
            if ("enabled".equals(channelIDSplit[0])) {
                if (command instanceof OnOffType cmd) {
                    sendAutomowerWorkAreaEnable(index, ((cmd == OnOffType.ON) ? true : false));
                }
            } else if ("cutting-height".equals(channelIDSplit[0])) {
                if (command instanceof QuantityType cmd) {
                    cmd = cmd.toUnit("%");
                    if (cmd != null) {
                        sendAutomowerWorkAreaCuttingHeight(index, cmd.byteValue());
                    }
                } else if (command instanceof DecimalType cmd) {
                    sendAutomowerWorkAreaCuttingHeight(index, cmd.byteValue());
                }
            }
        } else if (channelUID.getId().equals(CHANNEL_SETTING_CUTTING_HEIGHT)) {
            if (command instanceof DecimalType cmd) {
                sendAutomowerSettingsCuttingHeight(cmd.byteValue());
            }
        } else if (channelUID.getId().equals(CHANNEL_SETTING_HEADLIGHT_MODE)) {
            if (command instanceof StringType cmd) {
                sendAutomowerSettingsHeadlightMode(cmd.toString());
            }
        } else if (channelUID.getId().equals(CHANNEL_COMMAND_CONFIRM_ERROR)) {
            if (command instanceof OnOffType cmd) {
                if (cmd == OnOffType.ON) {
                    sendAutomowerConfirmError();
                    updateState(CHANNEL_COMMAND_CONFIRM_ERROR, OnOffType.OFF);
                }
            }
        } else {
            AutomowerCommand.fromChannelUID(channelUID).ifPresent(commandName -> {
                logger.debug("Sending command '{}'", commandName);
                getCommandValue(command).ifPresentOrElse(duration -> sendAutomowerCommand(commandName, duration),
                        () -> sendAutomowerCommand(commandName));
            });
        }
    }

    private Optional<Integer> getCommandValue(Type type) {
        if (type instanceof DecimalType command) {
            return Optional.of(command.intValue());
        }
        return Optional.empty();
    }

    private void refreshChannels(ChannelUID channelUID) {
        updateAutomowerState();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(AutomowerActions.class);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            AutomowerConfiguration currentConfig = getConfigAs(AutomowerConfiguration.class);
            final String configMowerId = currentConfig.getMowerId();
            final Integer pollingIntervalS = currentConfig.getPollingInterval();
            final String configMowerZoneId = currentConfig.getMowerZoneId();
            if ((configMowerZoneId != null) && !configMowerZoneId.isBlank()) {
                try {
                    mowerZoneId = ZoneId.of(configMowerZoneId);
                } catch (DateTimeException e) {
                    logger.warn("Invalid configuration mowerZoneId: {}, Error: {}", mowerZoneId, e.getMessage());
                    mowerZoneId = timeZoneProvider.getTimeZone(); // wrong config, use System TimeZone
                }
            } else {
                mowerZoneId = timeZoneProvider.getTimeZone(); // not configured, use System TimeZone
            }

            if (configMowerId == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/conf-error-no-mower-id");
            } else if (pollingIntervalS != null && pollingIntervalS < 1) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/conf-error-invalid-polling-interval");
            } else {
                automowerId.set(configMowerId);
                startAutomowerPolling(pollingIntervalS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    @Nullable
    private AutomowerBridge getAutomowerBridge() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof AutomowerBridgeHandler bridgeHandler) {
                return bridgeHandler.getAutomowerBridge();
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        if (!automowerId.get().equals(NO_ID)) {
            stopAutomowerPolling();
            automowerId.set(NO_ID);
        }
    }

    private void startAutomowerPolling(@Nullable Integer pollingIntervalS) {
        if (automowerPollingJob == null) {
            final long pollingIntervalToUse = pollingIntervalS == null ? DEFAULT_POLLING_INTERVAL_S : pollingIntervalS;
            automowerPollingJob = scheduler.scheduleWithFixedDelay(automowerPollingRunnable, 1, pollingIntervalToUse,
                    TimeUnit.SECONDS);
        }
    }

    private void stopAutomowerPolling() {
        if (automowerPollingJob != null) {
            automowerPollingJob.cancel(true);
            automowerPollingJob = null;
        }
    }

    private boolean isValidResult(@Nullable Mower mower) {
        return ((mower != null && mower.getAttributes() != null) && (mower.getAttributes().getMetadata() != null)
                && (mower.getAttributes().getBattery() != null) && (mower.getAttributes().getSystem() != null)
                && (mower.getAttributes().getCalendar() != null)
                && (mower.getAttributes().getCalendar().getTasks() != null)
                && (mower.getAttributes().getCapabilities() != null) && (mower.getAttributes().getMower() != null)
                && (mower.getAttributes().getPlanner() != null) && (mower.getAttributes().getPositions() != null)
                && (mower.getAttributes().getSettings() != null) && (mower.getAttributes().getStatistics() != null)
                && (mower.getAttributes().getSystem() != null));
    }

    private boolean isConnected(@Nullable Mower mower) {
        return mower != null && mower.getAttributes() != null && mower.getAttributes().getMetadata() != null
                && mower.getAttributes().getMetadata().isConnected();
    }

    private synchronized void updateAutomowerState() {
        String id = automowerId.get();
        try {
            AutomowerBridge automowerBridge = getAutomowerBridge();
            if (automowerBridge != null) {
                if (mowerState == null || (System.nanoTime() - lastQueryTimeMs > maxQueryFrequencyNanos)) {
                    lastQueryTimeMs = System.nanoTime();
                    mowerState = automowerBridge.getAutomowerStatus(id);
                }
                if (isValidResult(mowerState)) {
                    initializeProperties(mowerState);

                    updateChannelState(mowerState);

                    if (isConnected(mowerState)) {
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/comm-error-mower-not-connected-to-cloud");
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/comm-error-query-mower-failed");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/conf-error-no-bridge");
            }
        } catch (AutomowerCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error-query-mower-failed");
            logger.warn("Unable to query automower status for:  {}. Error: {}", id, e.getMessage());
        }
    }

    /**
     * Sends a command to the automower with the default duration of 60min
     *
     * @param command The command that should be sent. Valid values are: "Start", "ResumeSchedule", "Pause", "Park",
     *            "ParkUntilNextSchedule", "ParkUntilFurtherNotice"
     */
    public void sendAutomowerCommand(AutomowerCommand command) {
        sendAutomowerCommand(command, DEFAULT_COMMAND_DURATION_MIN);
    }

    /**
     * Sends a command to the automower with the given duration
     *
     * @param command The command that should be sent. Valid values are: "Start", "ResumeSchedule", "Pause", "Park",
     *            "ParkUntilNextSchedule", "ParkUntilFurtherNotice"
     * @param commandDurationMinutes The duration of the command in minutes. This is only evaluated for "Start" and
     *            "Park" commands
     */
    public void sendAutomowerCommand(AutomowerCommand command, long commandDurationMinutes) {
        logger.debug("Sending command '{} {}'", command.getCommand(), commandDurationMinutes);
        String id = automowerId.get();
        try {
            AutomowerBridge automowerBridge = getAutomowerBridge();
            if (automowerBridge != null) {
                automowerBridge.sendAutomowerCommand(id, command, commandDurationMinutes);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/conf-error-no-bridge");
            }
        } catch (AutomowerCommunicationException e) {
            logger.warn("Unable to send Command to automower: {}, Error: {}", id, e.getMessage());
        }
        updateAutomowerState();
    }

    /**
     * Sends a CalendarTask to the automower
     *
     * @param command The command that should be sent. E.g. a duration in min for the Start channel
     * @param channelID The triggering channel
     */
    public void sendAutomowerCalendarTask(Long workAreaId, short[] start, short[] duration, boolean[] monday,
            boolean[] tuesday, boolean[] wednesday, boolean[] thursday, boolean[] friday, boolean[] saturday,
            boolean[] sunday) {
        if (isValidResult(mowerState)) {

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

                calendarTaskArray.add(calendarTask);
            }

            String id = automowerId.get();
            try {
                AutomowerBridge automowerBridge = getAutomowerBridge();
                if (automowerBridge != null) {
                    automowerBridge.sendAutomowerCalendarTask(id,
                            mowerState.getAttributes().getCapabilities().hasWorkAreas(), workAreaId, calendarTaskArray);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/conf-error-no-bridge");
                }
            } catch (AutomowerCommunicationException e) {
                logger.warn("Unable to send CalendarTask to automower: {}, Error: {}", id, e.getMessage());
            }
        }
    }

    /**
     * Sends a CalendarTask to the automower
     *
     * @param command The command that should be sent. E.g. a duration in min for the Start channel
     * @param channelID The triggering channel
     */
    public void sendAutomowerCalendarTask(Command command, String channelID) {
        String[] channelIDSplit = channelID.split("-");
        int index = Integer.parseInt(channelIDSplit[0].substring("calendartasks".length())) - 1;
        String param = channelIDSplit[1];
        logger.debug("Sending CalendarTask '{}', index '{}', param '{}', command '{}'", channelID, index, param,
                command.toString());

        if (isValidResult(mowerState)) {
            List<CalendarTask> calendarTasksFiltered;
            List<CalendarTask> calendarTasksAll = mowerState.getAttributes().getCalendar().getTasks();
            if (mowerState.getAttributes().getCapabilities().hasWorkAreas()) {
                // only set the Tasks of the current WorkArea
                calendarTasksFiltered = new ArrayList<>();
                for (CalendarTask calendarTask : calendarTasksAll) {
                    if (calendarTask.getWorkAreaId().equals(calendarTasksAll.get(index).getWorkAreaId())) {
                        calendarTasksFiltered.add(calendarTask);
                    }
                }
            } else {
                calendarTasksFiltered = calendarTasksAll;
            }

            if (calendarTasksFiltered.get(index) != null) {
                CalendarTask calendarTask = calendarTasksAll.get(index); // possible race condition: cache is update in
                                                                         // the background via cyclic tasks before the
                                                                         // new request is sent out - deep clone of
                                                                         // object required
                                                                         // Different handling required - hasWorkAreas
                                                                         // ON/OFF
                if (command instanceof DecimalType cmd) {
                    if ("start".equals(param)) {
                        calendarTask.setStart(cmd.shortValue());
                    } else if ("duration".equals(param)) {
                        calendarTask.setDuration(cmd.shortValue());
                    }
                } else if (command instanceof QuantityType cmd) {
                    cmd = cmd.toUnit("min");
                    if (cmd != null) {
                        if ("start".equals(param)) {
                            calendarTask.setStart(cmd.shortValue());
                        } else if ("duration".equals(param)) {
                            calendarTask.setDuration(cmd.shortValue());
                        }
                    }
                } else if (command instanceof OnOffType cmd) {
                    boolean day = ((cmd == OnOffType.ON) ? true : false);

                    if ("monday".equals(param)) {
                        calendarTask.setMonday(day);
                    } else if ("tuesday".equals(param)) {
                        calendarTask.setTuesday(day);
                    } else if ("wednesday".equals(param)) {
                        calendarTask.setWednesday(day);
                    } else if ("thursday".equals(param)) {
                        calendarTask.setThursday(day);
                    } else if ("friday".equals(param)) {
                        calendarTask.setFriday(day);
                    } else if ("saturday".equals(param)) {
                        calendarTask.setSaturday(day);
                    } else if ("sunday".equals(param)) {
                        calendarTask.setSunday(day);
                    }
                }

                String id = automowerId.get();
                try {
                    AutomowerBridge automowerBridge = getAutomowerBridge();
                    if (automowerBridge != null) {
                        automowerBridge.sendAutomowerCalendarTask(id,
                                mowerState.getAttributes().getCapabilities().hasWorkAreas(),
                                calendarTask.getWorkAreaId(), calendarTasksFiltered);
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/conf-error-no-bridge");
                    }
                } catch (AutomowerCommunicationException e) {
                    logger.warn("Unable to send CalendarTask to automower: {}, Error: {}", id, e.getMessage());
                }
            }
        }
        updateAutomowerState();
    }

    /**
     * Sends StayOutZone Setting to the automower
     *
     * @param index Index of zone
     * @param enable Zone enabled or disabled
     */
    public void sendAutomowerStayOutZone(int index, boolean enable) {
        if (isValidResult(mowerState)) {
            sendAutomowerStayOutZone(mowerState.getAttributes().getStayOutZones().getZones().get(index).getId(),
                    enable);
        }
    }

    /**
     * Sends StayOutZone Setting to the automower
     *
     * @param zoneId Id of zone
     * @param enable Zone enabled or disabled
     */
    public void sendAutomowerStayOutZone(String zoneId, boolean enable) {
        logger.debug("Sending StayOutZone: zoneId {}, enable {}", zoneId, enable);
        if (isValidResult(mowerState)) {
            MowerStayOutZoneAttributes attributes = new MowerStayOutZoneAttributes();
            attributes.setEnable(enable);

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

            updateAutomowerState();
        }
    }

    /**
     * Sends WorkArea enable Setting to the automower
     *
     * @param index Index of WorkArea
     * @param enable WorkArea enabled or disabled
     * 
     */
    public void sendAutomowerWorkAreaEnable(int index, boolean enable) {
        if (isValidResult(mowerState)) {
            sendAutomowerWorkArea(mowerState.getAttributes().getWorkAreas().get(index).getWorkAreaId(), enable,
                    mowerState.getAttributes().getWorkAreas().get(index).getCuttingHeight());
        }
    }

    /**
     * Sends WorkArea CuttingHeight Setting to the automower
     * 
     * @param index Index of WorkArea
     * @param cuttingHeight CuttingHeight of the WorkArea
     * 
     */
    public void sendAutomowerWorkAreaCuttingHeight(int index, byte cuttingHeight) {
        if (isValidResult(mowerState)) {
            sendAutomowerWorkArea(mowerState.getAttributes().getWorkAreas().get(index).getWorkAreaId(),
                    mowerState.getAttributes().getWorkAreas().get(index).isEnabled(), cuttingHeight);
        }
    }

    /**
     * Sends WorkArea Settings to the automower
     *
     * @param workAreaId Id of WorkArea
     * @param enable CuttingHeight of the WorkArea
     * @param cuttingHeight CuttingHeight of the WorkArea
     */
    public void sendAutomowerWorkArea(long workAreaId, boolean enable, byte cuttingHeight) {
        logger.debug("Sending WorkArea: workAreaId {}, enable {}, cuttingHeight {}", workAreaId, enable, cuttingHeight);
        if (isValidResult(mowerState)) {
            // update local cache ...
            WorkArea workArea = getWorkAreaById(mowerState, workAreaId);
            workArea.setEnabled(enable);
            workArea.setCuttingHeight(cuttingHeight);
            // ... as well as request
            MowerWorkAreaAttributes workAreaAttributes = new MowerWorkAreaAttributes();
            workAreaAttributes.setEnable(enable);
            workAreaAttributes.setCuttingHeight(cuttingHeight);

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
            sendAutomowerSettings(cuttingHeight,
                    mowerState.getAttributes().getSettings().getHeadlight().getHeadlightMode());
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
                sendAutomowerSettings(mowerState.getAttributes().getSettings().getCuttingHeight(),
                        HeadlightMode.valueOf(headlightMode));
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
    public void sendAutomowerSettings(byte cuttingHeight, HeadlightMode headlightMode) {
        logger.debug("Sending Settings: cuttingHeight {}, headlightMode {}", cuttingHeight, headlightMode.toString());
        if (isValidResult(mowerState)) {
            Settings settings = new Settings();
            settings.setCuttingHeight(cuttingHeight);
            Headlight headlight = new Headlight();
            headlight.setHeadlightMode(headlightMode);

            // update local cache ...
            mowerState.getAttributes().getSettings().setCuttingHeight(cuttingHeight);
            mowerState.getAttributes().getSettings().getHeadlight().setHeadlightMode(headlightMode);

            String id = automowerId.get();
            try {
                AutomowerBridge automowerBridge = getAutomowerBridge();
                if (automowerBridge != null) {
                    automowerBridge.sendAutomowerSettings(id, settings);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/conf-error-no-bridge");
                }
            } catch (AutomowerCommunicationException e) {
                logger.warn("Unable to send SettingCuttingHeight to automower: {}, Error: {}", id, e.getMessage());
            }

            updateAutomowerState();
        }
    }

    /**
     * Confirm current non fatal error on the mower
     */
    public void sendAutomowerConfirmError() {
        logger.debug("Sending ConfirmError");
        if (isValidResult(mowerState) && (mowerState.getAttributes().getCapabilities().canConfirmError())
                && (mowerState.getAttributes().getMower().getIsErrorConfirmable())) {
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
            updateAutomowerState();
        }
    }

    private String restrictedState(RestrictedReason reason) {
        return "RESTRICTED_" + reason.name();
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

    private void updateChannelState(@Nullable Mower mower) {
        if (isValidResult(mower)) {
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

            Long workAreaId = mower.getAttributes().getMower().getWorkAreaId();
            if (workAreaId != null) {
                updateState(CHANNEL_STATUS_WORK_AREA_ID, new DecimalType(workAreaId));
                WorkArea workArea = getWorkAreaById(mower, workAreaId);
                if ((workArea != null) && (workArea.getName() != null)) {
                    if ((workAreaId.equals(0L)) && workArea.getName().isBlank()) {
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

            updateState(CHANNEL_STATUS_LAST_UPDATE, new DateTimeType(
                    toZonedDateTime(mower.getAttributes().getMetadata().getStatusTimestamp(), ZoneId.of("UTC"))));
            updateState(CHANNEL_STATUS_BATTERY,
                    new QuantityType<>(mower.getAttributes().getBattery().getBatteryPercent(), Units.PERCENT));

            updateState(CHANNEL_STATUS_ERROR_CODE, new DecimalType(mower.getAttributes().getMower().getErrorCode()));

            long errorCodeTimestamp = mower.getAttributes().getMower().getErrorCodeTimestamp();
            if (errorCodeTimestamp == 0L) {
                updateState(CHANNEL_STATUS_ERROR_TIMESTAMP, UnDefType.NULL);
            } else {
                updateState(CHANNEL_STATUS_ERROR_TIMESTAMP,
                        new DateTimeType(toZonedDateTime(errorCodeTimestamp, mowerZoneId)));
            }

            updateState(CHANNEL_STATUS_ERROR_CONFIRMABLE,
                    OnOffType.from(mower.getAttributes().getMower().getIsErrorConfirmable()));

            long nextStartTimestamp = mower.getAttributes().getPlanner().getNextStartTimestamp();
            // If next start timestamp is 0 it means the mower should start now, so using current timestamp
            if (nextStartTimestamp == 0L) {
                updateState(CHANNEL_PLANNER_NEXT_START, UnDefType.NULL);
            } else {
                updateState(CHANNEL_PLANNER_NEXT_START,
                        new DateTimeType(toZonedDateTime(nextStartTimestamp, mowerZoneId)));
            }
            updateState(CHANNEL_PLANNER_OVERRIDE_ACTION,
                    new StringType(mower.getAttributes().getPlanner().getOverride().getAction().name()));
            updateState(CHANNEL_PLANNER_RESTRICTED_REASON,
                    new StringType(mower.getAttributes().getPlanner().getRestrictedReason().name()));
            updateState(CHANNEL_PLANNER_EXTERNAL_REASON,
                    new DecimalType(mower.getAttributes().getPlanner().getExternalReason()));

            updateState(CHANNEL_SETTING_CUTTING_HEIGHT,
                    new DecimalType(mower.getAttributes().getSettings().getCuttingHeight()));
            updateState(CHANNEL_SETTING_HEADLIGHT_MODE,
                    new StringType(mower.getAttributes().getSettings().getHeadlight().getHeadlightMode().name()));

            updateState(CHANNEL_STATISTIC_CUTTING_BLADE_USAGE_TIME,
                    new QuantityType<>(mower.getAttributes().getStatistics().getCuttingBladeUsageTime(), Units.SECOND));
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

            updateState(LAST_POSITION,
                    new PointType(new DecimalType(mower.getAttributes().getLastPosition().getLatitude()),
                            new DecimalType(mower.getAttributes().getLastPosition().getLongitude())));
            List<Position> positions = mower.getAttributes().getPositions();
            for (int i = 0; i < positions.size(); i++) {
                updateState(CHANNEL_POSITIONS.get(i), new PointType(new DecimalType(positions.get(i).getLatitude()),
                        new DecimalType(positions.get(i).getLongitude())));
            }

            updateState(CHANNEL_STAYOUTZONES_DIRTY, OnOffType.from(mower.getAttributes().getStayOutZones().isDirty()));

            List<CalendarTask> calendarTasks = mower.getAttributes().getCalendar().getTasks();
            int j = 0;
            for (int i = 0; i < calendarTasks.size() && j < CHANNEL_CALENDARTASKS.size(); i++) {
                updateState(CHANNEL_CALENDARTASKS.get(j++),
                        new QuantityType<>(calendarTasks.get(i).getStart(), Units.MINUTE));
                updateState(CHANNEL_CALENDARTASKS.get(j++),
                        new QuantityType<>(calendarTasks.get(i).getDuration(), Units.MINUTE));
                updateState(CHANNEL_CALENDARTASKS.get(j++), OnOffType.from(calendarTasks.get(i).getMonday()));
                updateState(CHANNEL_CALENDARTASKS.get(j++), OnOffType.from(calendarTasks.get(i).getTuesday()));
                updateState(CHANNEL_CALENDARTASKS.get(j++), OnOffType.from(calendarTasks.get(i).getWednesday()));
                updateState(CHANNEL_CALENDARTASKS.get(j++), OnOffType.from(calendarTasks.get(i).getThursday()));
                updateState(CHANNEL_CALENDARTASKS.get(j++), OnOffType.from(calendarTasks.get(i).getFriday()));
                updateState(CHANNEL_CALENDARTASKS.get(j++), OnOffType.from(calendarTasks.get(i).getSaturday()));
                updateState(CHANNEL_CALENDARTASKS.get(j++), OnOffType.from(calendarTasks.get(i).getSunday()));
                if (calendarTasks.get(i).getWorkAreaId() == null) {
                    updateState(CHANNEL_CALENDARTASKS.get(j++), UnDefType.NULL);
                    updateState(CHANNEL_CALENDARTASKS.get(j++), UnDefType.NULL);
                } else {
                    updateState(CHANNEL_CALENDARTASKS.get(j++), new DecimalType(calendarTasks.get(i).getWorkAreaId()));
                    WorkArea workArea = getWorkAreaById(mower, calendarTasks.get(i).getWorkAreaId());
                    if (workArea != null) {
                        if ((calendarTasks.get(i).getWorkAreaId().equals(0L)) && workArea.getName().isBlank()) {
                            updateState(CHANNEL_CALENDARTASKS.get(j++), new StringType("main area"));
                        } else {
                            updateState(CHANNEL_CALENDARTASKS.get(j++), new StringType(workArea.getName()));
                        }
                    } else {
                        updateState(CHANNEL_CALENDARTASKS.get(j++), UnDefType.NULL);
                    }
                }
            }
            // clear remaining channels
            for (; j < CHANNEL_CALENDARTASKS.size(); j++) {
                updateState(CHANNEL_CALENDARTASKS.get(j), UnDefType.NULL);
            }

            List<StayOutZone> stayOutZones = mower.getAttributes().getStayOutZones().getZones();
            j = 0;
            for (int i = 0; i < stayOutZones.size() && j < CHANNEL_STAYOUTZONES.size(); i++) {
                updateState(CHANNEL_STAYOUTZONES.get(j++), new StringType(stayOutZones.get(i).getId()));
                updateState(CHANNEL_STAYOUTZONES.get(j++), new StringType(stayOutZones.get(i).getName()));
                updateState(CHANNEL_STAYOUTZONES.get(j++), OnOffType.from(stayOutZones.get(i).isEnabled()));
            }
            // clear remaining channels
            for (; j < CHANNEL_STAYOUTZONES.size(); j++) {
                updateState(CHANNEL_STAYOUTZONES.get(j), UnDefType.NULL);
            }

            List<WorkArea> workAreas = mower.getAttributes().getWorkAreas();
            j = 0;
            for (int i = 0; i < workAreas.size() && j < CHANNEL_WORKAREAS.size(); i++) {
                WorkArea workArea = workAreas.get(i);
                updateState(CHANNEL_WORKAREAS.get(j++), new DecimalType(workArea.getWorkAreaId()));
                if ((workArea.getWorkAreaId() == 0L) && workArea.getName().isBlank()) {
                    updateState(CHANNEL_WORKAREAS.get(j++), new StringType("main area"));
                } else {
                    updateState(CHANNEL_WORKAREAS.get(j++), new StringType(workArea.getName()));
                }
                updateState(CHANNEL_WORKAREAS.get(j++), new QuantityType<>(workArea.getCuttingHeight(), Units.PERCENT));
                updateState(CHANNEL_WORKAREAS.get(j++), OnOffType.from(workArea.isEnabled()));
                if (workArea.getProgress() != null) {
                    updateState(CHANNEL_WORKAREAS.get(j++), new QuantityType<>(workArea.getProgress(), Units.PERCENT));
                } else {
                    updateState(CHANNEL_WORKAREAS.get(j++), UnDefType.NULL);
                }

                if ((workArea.getLastTimeCompleted() != null) && (workArea.getLastTimeCompleted() != 0)) {
                    updateState(CHANNEL_WORKAREAS.get(j++),
                            new DateTimeType(toZonedDateTime(workArea.getLastTimeCompleted(), mowerZoneId)));
                } else {
                    updateState(CHANNEL_WORKAREAS.get(j++), UnDefType.NULL);
                }
            }
            // clear remaining channels
            for (; j < CHANNEL_WORKAREAS.size(); j++) {
                updateState(CHANNEL_WORKAREAS.get(j), UnDefType.NULL);
            }
        }
    }

    private void initializeProperties(@Nullable Mower mower) {
        if (isValidResult(mower)) {
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
    private ZonedDateTime toZonedDateTime(long timestamp, ZoneId zoneId) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC")).withZoneSameLocal(zoneId);
    }
}
