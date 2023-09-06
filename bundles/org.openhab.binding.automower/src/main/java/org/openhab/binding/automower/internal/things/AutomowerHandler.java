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
package org.openhab.binding.automower.internal.things;

import static org.openhab.binding.automower.internal.AutomowerBindingConstants.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.automower.internal.AutomowerBindingConstants;
import org.openhab.binding.automower.internal.actions.AutomowerActions;
import org.openhab.binding.automower.internal.bridge.AutomowerBridge;
import org.openhab.binding.automower.internal.bridge.AutomowerBridgeHandler;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mower;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Position;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.RestrictedReason;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.State;
import org.openhab.binding.automower.internal.rest.exceptions.AutomowerCommunicationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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

import com.google.gson.Gson;

/**
 * The {@link AutomowerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Pfleger - Initial contribution
 * @author Marcin Czeczko - Added support for planner & calendar data
 */
@NonNullByDefault
public class AutomowerHandler extends BaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_AUTOMOWER);
    private static final String NO_ID = "NO_ID";
    private static final long DEFAULT_COMMAND_DURATION_MIN = 60;
    private static final long DEFAULT_POLLING_INTERVAL_S = TimeUnit.MINUTES.toSeconds(10);

    private final Logger logger = LoggerFactory.getLogger(AutomowerHandler.class);
    private final TimeZoneProvider timeZoneProvider;

    private AtomicReference<String> automowerId = new AtomicReference<String>(NO_ID);
    private long lastQueryTimeMs = 0L;

    private @Nullable ScheduledFuture<?> automowerPollingJob;
    private long maxQueryFrequencyNanos = TimeUnit.MINUTES.toNanos(1);

    private @Nullable Mower mowerState;

    private Gson gson = new Gson();

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
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing channel '{}'", channelUID);
            refreshChannels(channelUID);
        } else {
            AutomowerCommand.fromChannelUID(channelUID).ifPresent(commandName -> {
                logger.debug("Sending command '{}'", commandName);
                getCommandValue(command).ifPresentOrElse(duration -> sendAutomowerCommand(commandName, duration),
                        () -> sendAutomowerCommand(commandName));
            });
        }
    }

    private Optional<Integer> getCommandValue(Type type) {
        if (type instanceof DecimalType) {
            return Optional.of(((DecimalType) type).intValue());
        }
        return Optional.empty();
    }

    private void refreshChannels(ChannelUID channelUID) {
        updateAutomowerState();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(AutomowerActions.class);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            AutomowerConfiguration currentConfig = getConfigAs(AutomowerConfiguration.class);
            final String configMowerId = currentConfig.getMowerId();
            final Integer pollingIntervalS = currentConfig.getPollingInterval();

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
            if (handler instanceof AutomowerBridgeHandler) {
                AutomowerBridgeHandler bridgeHandler = (AutomowerBridgeHandler) handler;
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
        return mower != null && mower.getAttributes() != null && mower.getAttributes().getMetadata() != null
                && mower.getAttributes().getBattery() != null && mower.getAttributes().getSystem() != null;
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
            logger.warn("Unable to send command to automower: {}, Error: {}", id, e.getMessage());
        }

        updateAutomowerState();
    }

    private String restrictedState(RestrictedReason reason) {
        return "RESTRICTED_" + reason.name();
    }

    private void updateChannelState(@Nullable Mower mower) {
        if (isValidResult(mower)) {
            updateState(CHANNEL_STATUS_NAME, new StringType(mower.getAttributes().getSystem().getName()));
            updateState(CHANNEL_STATUS_MODE, new StringType(mower.getAttributes().getMower().getMode().name()));
            updateState(CHANNEL_STATUS_ACTIVITY, new StringType(mower.getAttributes().getMower().getActivity().name()));

            if (mower.getAttributes().getMower().getState() != State.RESTRICTED) {
                updateState(CHANNEL_STATUS_STATE, new StringType(mower.getAttributes().getMower().getState().name()));
            } else {
                updateState(CHANNEL_STATUS_STATE,
                        new StringType(restrictedState(mower.getAttributes().getPlanner().getRestrictedReason())));
            }

            updateState(CHANNEL_STATUS_LAST_UPDATE,
                    new DateTimeType(toZonedDateTime(mower.getAttributes().getMetadata().getStatusTimestamp())));
            updateState(CHANNEL_STATUS_BATTERY, new QuantityType<Dimensionless>(
                    mower.getAttributes().getBattery().getBatteryPercent(), Units.PERCENT));

            updateState(CHANNEL_STATUS_ERROR_CODE, new DecimalType(mower.getAttributes().getMower().getErrorCode()));

            long errorCodeTimestamp = mower.getAttributes().getMower().getErrorCodeTimestamp();
            if (errorCodeTimestamp == 0L) {
                updateState(CHANNEL_STATUS_ERROR_TIMESTAMP, UnDefType.NULL);
            } else {
                updateState(CHANNEL_STATUS_ERROR_TIMESTAMP, new DateTimeType(toZonedDateTime(errorCodeTimestamp)));
            }

            long nextStartTimestamp = mower.getAttributes().getPlanner().getNextStartTimestamp();
            // If next start timestamp is 0 it means the mower should start now, so using current timestamp
            if (nextStartTimestamp == 0L) {
                updateState(CHANNEL_PLANNER_NEXT_START, UnDefType.NULL);
            } else {
                updateState(CHANNEL_PLANNER_NEXT_START, new DateTimeType(toZonedDateTime(nextStartTimestamp)));
            }
            updateState(CHANNEL_PLANNER_OVERRIDE_ACTION,
                    new StringType(mower.getAttributes().getPlanner().getOverride().getAction()));

            updateState(CHANNEL_CALENDAR_TASKS,
                    new StringType(gson.toJson(mower.getAttributes().getCalendar().getTasks())));

            updateState(LAST_POSITION,
                    new PointType(new DecimalType(mower.getAttributes().getLastPosition().getLatitude()),
                            new DecimalType(mower.getAttributes().getLastPosition().getLongitude())));
            ArrayList<Position> positions = mower.getAttributes().getPositions();
            for (int i = 0; i < positions.size(); i++) {
                updateState(CHANNEL_POSITIONS.get(i), new PointType(new DecimalType(positions.get(i).getLatitude()),
                        new DecimalType(positions.get(i).getLongitude())));

            }
        }
    }

    private void initializeProperties(@Nullable Mower mower) {
        Map<String, String> properties = editProperties();

        properties.put(AutomowerBindingConstants.AUTOMOWER_ID, mower.getId());

        if (mower.getAttributes() != null && mower.getAttributes().getSystem() != null) {
            properties.put(AutomowerBindingConstants.AUTOMOWER_SERIAL_NUMBER,
                    mower.getAttributes().getSystem().getSerialNumber());
            properties.put(AutomowerBindingConstants.AUTOMOWER_MODEL, mower.getAttributes().getSystem().getModel());
            properties.put(AutomowerBindingConstants.AUTOMOWER_NAME, mower.getAttributes().getSystem().getName());
        }

        updateProperties(properties);
    }

    /**
     * Converts timestamp returned by the Automower API into local time-zone.
     * Timestamp returned by the API doesn't have offset and it always in the current time zone - it can be treated as
     * UTC.
     * Method builds a ZonedDateTime with same hour value but in the current system timezone.
     *
     * @param timestamp - Automower API timestamp
     * @return ZonedDateTime in system timezone
     */
    private ZonedDateTime toZonedDateTime(long timestamp) {
        Instant timestampInstant = Instant.ofEpochMilli(timestamp);
        return ZonedDateTime.ofInstant(timestampInstant, timeZoneProvider.getTimeZone());
    }
}
