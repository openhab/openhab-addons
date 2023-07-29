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
package org.openhab.binding.boschindego.internal.handler;

import static org.openhab.binding.boschindego.internal.BoschIndegoBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.boschindego.internal.BoschIndegoTranslationProvider;
import org.openhab.binding.boschindego.internal.DeviceStatus;
import org.openhab.binding.boschindego.internal.IndegoController;
import org.openhab.binding.boschindego.internal.config.BoschIndegoConfiguration;
import org.openhab.binding.boschindego.internal.dto.DeviceCommand;
import org.openhab.binding.boschindego.internal.dto.response.DeviceStateResponse;
import org.openhab.binding.boschindego.internal.dto.response.OperatingDataResponse;
import org.openhab.binding.boschindego.internal.exceptions.IndegoAuthenticationException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoInvalidCommandException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoTimeoutException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BoschIndegoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Fleck - Initial contribution
 * @author Jacob Laursen - Refactoring, bugfixing and removal of dependency towards abandoned library
 */
@NonNullByDefault
public class BoschIndegoHandler extends BaseThingHandler {

    private static final String MAP_POSITION_STROKE_COLOR = "#8c8b6d";
    private static final String MAP_POSITION_FILL_COLOR = "#fff701";
    private static final int MAP_POSITION_RADIUS = 10;

    private static final Duration MAP_REFRESH_INTERVAL = Duration.ofDays(1);
    private static final Duration OPERATING_DATA_INACTIVE_REFRESH_INTERVAL = Duration.ofHours(6);
    private static final Duration OPERATING_DATA_OFFLINE_REFRESH_INTERVAL = Duration.ofMinutes(30);
    private static final Duration OPERATING_DATA_ACTIVE_REFRESH_INTERVAL = Duration.ofMinutes(2);
    private static final Duration MAP_REFRESH_SESSION_DURATION = Duration.ofMinutes(5);
    private static final Duration COMMAND_STATE_REFRESH_TIMEOUT = Duration.ofSeconds(10);

    private final Logger logger = LoggerFactory.getLogger(BoschIndegoHandler.class);
    private final HttpClient httpClient;
    private final BoschIndegoTranslationProvider translationProvider;
    private final TimeZoneProvider timeZoneProvider;

    private @NonNullByDefault({}) IndegoController controller;
    private @Nullable ScheduledFuture<?> statePollFuture;
    private @Nullable ScheduledFuture<?> cuttingTimePollFuture;
    private @Nullable ScheduledFuture<?> cuttingTimeFuture;
    private boolean propertiesInitialized;
    private Optional<Integer> previousStateCode = Optional.empty();
    private @Nullable RawType cachedMap;
    private Instant cachedMapTimestamp = Instant.MIN;
    private Instant operatingDataTimestamp = Instant.MIN;
    private Instant mapRefreshStartedTimestamp = Instant.MIN;
    private ThingStatus lastOperatingDataStatus = ThingStatus.UNINITIALIZED;
    private int stateInactiveRefreshIntervalSeconds;
    private int stateActiveRefreshIntervalSeconds;
    private int currentRefreshIntervalSeconds;

    public BoschIndegoHandler(Thing thing, HttpClient httpClient, BoschIndegoTranslationProvider translationProvider,
            TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.translationProvider = translationProvider;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Indego handler");
        BoschIndegoConfiguration config = getConfigAs(BoschIndegoConfiguration.class);
        stateInactiveRefreshIntervalSeconds = (int) config.refresh;
        stateActiveRefreshIntervalSeconds = (int) config.stateActiveRefresh;
        String username = config.username;
        String password = config.password;

        if (username == null || username.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.missing-username");
            return;
        }
        if (password == null || password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.missing-password");
            return;
        }

        controller = new IndegoController(httpClient, username, password);

        updateStatus(ThingStatus.UNKNOWN);
        previousStateCode = Optional.empty();
        rescheduleStatePoll(0, stateInactiveRefreshIntervalSeconds);
        this.cuttingTimePollFuture = scheduler.scheduleWithFixedDelay(this::refreshCuttingTimesWithExceptionHandling, 0,
                config.cuttingTimeRefresh, TimeUnit.MINUTES);
    }

    private boolean rescheduleStatePoll(int delaySeconds, int refreshIntervalSeconds) {
        ScheduledFuture<?> statePollFuture = this.statePollFuture;
        if (statePollFuture != null) {
            if (refreshIntervalSeconds == currentRefreshIntervalSeconds) {
                // No change.
                return false;
            }
            statePollFuture.cancel(false);
        }
        logger.debug("Scheduling state refresh job with {}s interval and {}s delay", refreshIntervalSeconds,
                delaySeconds);
        this.statePollFuture = scheduler.scheduleWithFixedDelay(this::refreshStateWithExceptionHandling, delaySeconds,
                refreshIntervalSeconds, TimeUnit.SECONDS);
        currentRefreshIntervalSeconds = refreshIntervalSeconds;

        return true;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Indego handler");
        ScheduledFuture<?> pollFuture = this.statePollFuture;
        if (pollFuture != null) {
            pollFuture.cancel(true);
        }
        this.statePollFuture = null;
        pollFuture = this.cuttingTimePollFuture;
        if (pollFuture != null) {
            pollFuture.cancel(true);
        }
        this.cuttingTimePollFuture = null;
        pollFuture = this.cuttingTimeFuture;
        if (pollFuture != null) {
            pollFuture.cancel(true);
        }
        this.cuttingTimeFuture = null;

        scheduler.execute(() -> {
            try {
                controller.deauthenticate();
            } catch (IndegoException e) {
                logger.debug("Deauthentication failed", e);
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand {} for channel {}", command, channelUID);
        try {
            if (command == RefreshType.REFRESH) {
                handleRefreshCommand(channelUID.getId());
                return;
            }
            if (command instanceof DecimalType && channelUID.getId().equals(STATE)) {
                sendCommand(((DecimalType) command).intValue());
            }
        } catch (IndegoAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.authentication-failure");
        } catch (IndegoTimeoutException e) {
            updateStatus(lastOperatingDataStatus = ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.unreachable");
        } catch (IndegoInvalidCommandException e) {
            logger.warn("Invalid command: {}", e.getMessage());
            if (e.hasErrorCode()) {
                updateState(ERRORCODE, new DecimalType(e.getErrorCode()));
            }
        } catch (IndegoException e) {
            logger.warn("Command failed: {}", e.getMessage());
        }
    }

    private void handleRefreshCommand(String channelId)
            throws IndegoAuthenticationException, IndegoTimeoutException, IndegoException {
        switch (channelId) {
            case GARDEN_MAP:
                // Force map refresh and fall through to state update.
                cachedMapTimestamp = Instant.MIN;
            case STATE:
            case TEXTUAL_STATE:
            case MOWED:
            case ERRORCODE:
            case STATECODE:
            case READY:
                refreshState();
                break;
            case LAST_CUTTING:
                refreshLastCuttingTime();
                break;
            case NEXT_CUTTING:
                refreshNextCuttingTime();
                break;
            case BATTERY_LEVEL:
            case LOW_BATTERY:
            case BATTERY_VOLTAGE:
            case BATTERY_TEMPERATURE:
            case GARDEN_SIZE:
                refreshOperatingData();
                break;
        }
    }

    private void sendCommand(int commandInt) throws IndegoException {
        DeviceCommand command;
        switch (commandInt) {
            case 1:
                command = DeviceCommand.MOW;
                break;
            case 2:
                command = DeviceCommand.RETURN;
                break;
            case 3:
                command = DeviceCommand.PAUSE;
                break;
            default:
                logger.warn("Invalid command {}", commandInt);
                return;
        }

        DeviceStateResponse state = controller.getState();
        DeviceStatus deviceStatus = DeviceStatus.fromCode(state.state);
        if (!verifyCommand(command, deviceStatus, state.error)) {
            return;
        }
        logger.debug("Sending command {}", command);
        controller.sendCommand(command);

        // State is not updated immediately, so await new state for some seconds.
        // For command MOW, state will shortly be updated to 262 (docked, loading map).
        // This is considered "active", so after this state change, polling frequency will
        // be increased for faster updates.
        DeviceStateResponse stateResponse = controller.getState(COMMAND_STATE_REFRESH_TIMEOUT);
        if (stateResponse.state != 0) {
            updateState(stateResponse);
            deviceStatus = DeviceStatus.fromCode(stateResponse.state);
            rescheduleStatePollAccordingToState(deviceStatus);
        }
    }

    private void refreshStateWithExceptionHandling() {
        try {
            refreshState();
        } catch (IndegoAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.authentication-failure");
        } catch (IndegoTimeoutException e) {
            updateStatus(lastOperatingDataStatus = ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.unreachable");
        } catch (IndegoException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void refreshState() throws IndegoAuthenticationException, IndegoException {
        if (!propertiesInitialized) {
            getThing().setProperty(Thing.PROPERTY_SERIAL_NUMBER, controller.getSerialNumber());
            propertiesInitialized = true;
        }

        DeviceStateResponse state = controller.getState();
        DeviceStatus deviceStatus = DeviceStatus.fromCode(state.state);
        updateState(state);

        // Update map and start tracking positions if mower is active.
        if (state.mapUpdateAvailable) {
            cachedMapTimestamp = Instant.MIN;
        }
        refreshMap(state.svgXPos, state.svgYPos);
        if (deviceStatus.isActive()) {
            trackPosition();
        }

        int previousState;
        DeviceStatus previousDeviceStatus;
        if (previousStateCode.isPresent()) {
            previousState = previousStateCode.get();
            previousDeviceStatus = DeviceStatus.fromCode(previousState);
            if (state.state != previousState
                    && ((!previousDeviceStatus.isDocked() && deviceStatus.isDocked()) || deviceStatus.isCompleted())) {
                // When returning to dock or on its way after completing lawn, refresh last cutting time immediately.
                // We cannot fully rely on completed lawn state since active polling refresh interval is configurable
                // and we might miss the state if mower returns before next poll.
                refreshLastCuttingTime();
            }
        } else {
            previousState = state.state;
            previousDeviceStatus = DeviceStatus.fromCode(previousState);
        }
        previousStateCode = Optional.of(state.state);

        refreshOperatingDataConditionally(
                previousDeviceStatus.isCharging() || deviceStatus.isCharging() || deviceStatus.isActive());

        if (lastOperatingDataStatus == ThingStatus.ONLINE && thing.getStatus() != ThingStatus.ONLINE) {
            // Revert temporary offline status caused by disruptions other than unreachable device.
            updateStatus(ThingStatus.ONLINE);
        } else if (lastOperatingDataStatus == ThingStatus.OFFLINE) {
            // Update description to reflect why thing is still offline.
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.unreachable");
        }

        rescheduleStatePollAccordingToState(deviceStatus);
    }

    private void rescheduleStatePollAccordingToState(DeviceStatus deviceStatus) {
        int refreshIntervalSeconds;
        if (deviceStatus.isActive()) {
            refreshIntervalSeconds = stateActiveRefreshIntervalSeconds;
        } else if (deviceStatus.isCharging()) {
            refreshIntervalSeconds = (int) OPERATING_DATA_ACTIVE_REFRESH_INTERVAL.getSeconds();
        } else {
            refreshIntervalSeconds = stateInactiveRefreshIntervalSeconds;
        }
        if (rescheduleStatePoll(refreshIntervalSeconds, refreshIntervalSeconds)) {
            // After job has been rescheduled, request operating data one last time on next poll.
            // This is needed to update battery values after a charging cycle has completed.
            operatingDataTimestamp = Instant.MIN;
        }
    }

    private void refreshOperatingDataConditionally(boolean isActive)
            throws IndegoAuthenticationException, IndegoTimeoutException, IndegoException {
        // Refresh operating data only occationally or when robot is active/charging.
        // This will contact the robot directly through cellular network and wake it up
        // when sleeping. Additionally, refresh more often after being offline to try to get
        // back online as soon as possible without putting too much stress on the service.
        if ((isActive && operatingDataTimestamp.isBefore(Instant.now().minus(OPERATING_DATA_ACTIVE_REFRESH_INTERVAL)))
                || (lastOperatingDataStatus != ThingStatus.ONLINE && operatingDataTimestamp
                        .isBefore(Instant.now().minus(OPERATING_DATA_OFFLINE_REFRESH_INTERVAL)))
                || operatingDataTimestamp.isBefore(Instant.now().minus(OPERATING_DATA_INACTIVE_REFRESH_INTERVAL))) {
            refreshOperatingData();
        }
    }

    private void refreshOperatingData() throws IndegoAuthenticationException, IndegoTimeoutException, IndegoException {
        updateOperatingData(controller.getOperatingData());
        operatingDataTimestamp = Instant.now();
        updateStatus(lastOperatingDataStatus = ThingStatus.ONLINE);
    }

    private void refreshCuttingTimesWithExceptionHandling() {
        try {
            refreshLastCuttingTime();
            refreshNextCuttingTime();
        } catch (IndegoAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.authentication-failure");
        } catch (IndegoException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void refreshLastCuttingTime() throws IndegoAuthenticationException, IndegoException {
        if (isLinked(LAST_CUTTING)) {
            Instant lastCutting = controller.getPredictiveLastCutting();
            if (lastCutting != null) {
                updateState(LAST_CUTTING,
                        new DateTimeType(ZonedDateTime.ofInstant(lastCutting, timeZoneProvider.getTimeZone())));
            } else {
                updateState(LAST_CUTTING, UnDefType.UNDEF);
            }
        }
    }

    private void refreshNextCuttingTimeWithExceptionHandling() {
        try {
            refreshNextCuttingTime();
        } catch (IndegoAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.authentication-failure");
        } catch (IndegoException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void refreshNextCuttingTime() throws IndegoAuthenticationException, IndegoException {
        cancelCuttingTimeRefresh();
        if (isLinked(NEXT_CUTTING)) {
            Instant nextCutting = controller.getPredictiveNextCutting();
            if (nextCutting != null) {
                updateState(NEXT_CUTTING,
                        new DateTimeType(ZonedDateTime.ofInstant(nextCutting, timeZoneProvider.getTimeZone())));
                scheduleCuttingTimesRefresh(nextCutting);
            } else {
                updateState(NEXT_CUTTING, UnDefType.UNDEF);
            }
        }
    }

    private void cancelCuttingTimeRefresh() {
        ScheduledFuture<?> cuttingTimeFuture = this.cuttingTimeFuture;
        if (cuttingTimeFuture != null) {
            // Do not interrupt as we might be running within that job.
            cuttingTimeFuture.cancel(false);
            this.cuttingTimeFuture = null;
        }
    }

    private void scheduleCuttingTimesRefresh(Instant nextCutting) {
        // Schedule additional update right after next planned cutting. This ensures a faster update.
        long secondsUntilNextCutting = Instant.now().until(nextCutting, ChronoUnit.SECONDS) + 2;
        if (secondsUntilNextCutting > 0) {
            logger.debug("Scheduling fetching of next cutting time in {} seconds", secondsUntilNextCutting);
            this.cuttingTimeFuture = scheduler.schedule(this::refreshNextCuttingTimeWithExceptionHandling,
                    secondsUntilNextCutting, TimeUnit.SECONDS);
        }
    }

    private void refreshMap(int xPos, int yPos) throws IndegoAuthenticationException, IndegoException {
        if (!isLinked(GARDEN_MAP)) {
            return;
        }
        RawType cachedMap = this.cachedMap;
        boolean mapRefreshed;
        if (cachedMap == null || cachedMapTimestamp.isBefore(Instant.now().minus(MAP_REFRESH_INTERVAL))) {
            this.cachedMap = cachedMap = controller.getMap();
            cachedMapTimestamp = Instant.now();
            mapRefreshed = true;
        } else {
            mapRefreshed = false;
        }
        String svgMap = new String(cachedMap.getBytes(), StandardCharsets.UTF_8);
        if (!svgMap.endsWith("</svg>")) {
            if (mapRefreshed) {
                logger.warn("Unexpected map format, unable to plot location");
                logger.trace("Received map: {}", svgMap);
                updateState(GARDEN_MAP, cachedMap);
            }
            return;
        }
        svgMap = svgMap.substring(0, svgMap.length() - 6) + "<circle cx=\"" + xPos + "\" cy=\"" + yPos + "\" r=\""
                + MAP_POSITION_RADIUS + "\" stroke=\"" + MAP_POSITION_STROKE_COLOR + "\" fill=\""
                + MAP_POSITION_FILL_COLOR + "\" />\n</svg>";
        updateState(GARDEN_MAP, new RawType(svgMap.getBytes(), cachedMap.getMimeType()));
    }

    private void trackPosition() throws IndegoAuthenticationException, IndegoException {
        if (!isLinked(GARDEN_MAP)) {
            return;
        }
        if (mapRefreshStartedTimestamp.isBefore(Instant.now().minus(MAP_REFRESH_SESSION_DURATION))) {
            int count = (int) MAP_REFRESH_SESSION_DURATION.getSeconds() / stateActiveRefreshIntervalSeconds + 1;
            logger.debug("Requesting position updates (count: {}; interval: {}s), previously triggered {}", count,
                    stateActiveRefreshIntervalSeconds, mapRefreshStartedTimestamp);
            controller.requestPosition(count, stateActiveRefreshIntervalSeconds);
            mapRefreshStartedTimestamp = Instant.now();
        }
    }

    private void updateState(DeviceStateResponse state) {
        DeviceStatus deviceStatus = DeviceStatus.fromCode(state.state);
        DeviceCommand associatedCommand = deviceStatus.getAssociatedCommand();
        int status = associatedCommand != null ? getStatusFromCommand(associatedCommand) : 0;
        int mowed = state.mowed;
        int error = state.error;
        int statecode = state.state;
        boolean ready = isReadyToMow(deviceStatus, state.error);

        updateState(STATECODE, new DecimalType(statecode));
        updateState(READY, new DecimalType(ready ? 1 : 0));
        updateState(ERRORCODE, new DecimalType(error));
        updateState(MOWED, new PercentType(mowed));
        updateState(STATE, new DecimalType(status));
        updateState(TEXTUAL_STATE, new StringType(deviceStatus.getMessage(translationProvider)));
    }

    private void updateOperatingData(OperatingDataResponse operatingData) {
        updateState(BATTERY_VOLTAGE, new QuantityType<>(operatingData.battery.voltage, Units.VOLT));
        updateState(BATTERY_LEVEL, new DecimalType(operatingData.battery.percent));
        updateState(LOW_BATTERY, OnOffType.from(operatingData.battery.percent < 20));
        updateState(BATTERY_TEMPERATURE, new QuantityType<>(operatingData.battery.batteryTemperature, SIUnits.CELSIUS));
        updateState(GARDEN_SIZE, new QuantityType<>(operatingData.garden.size, SIUnits.SQUARE_METRE));
    }

    private boolean isReadyToMow(DeviceStatus deviceStatus, int error) {
        return deviceStatus.isReadyToMow() && error == 0;
    }

    private boolean verifyCommand(DeviceCommand command, DeviceStatus deviceStatus, int errorCode) {
        // Mower reported an error
        if (errorCode != 0) {
            logger.warn("The mower reported an error.");
            return false;
        }

        // Command is equal to current state
        if (command == deviceStatus.getAssociatedCommand()) {
            logger.debug("Command is equal to state");
            return false;
        }
        // Can't pause while the mower is docked
        if (command == DeviceCommand.PAUSE && deviceStatus.getAssociatedCommand() == DeviceCommand.RETURN) {
            logger.info("Can't pause the mower while it's docked or docking");
            return false;
        }
        // Command means "MOW" but mower is not ready
        if (command == DeviceCommand.MOW && !isReadyToMow(deviceStatus, errorCode)) {
            logger.info("The mower is not ready to mow at the moment");
            return false;
        }
        return true;
    }

    private int getStatusFromCommand(DeviceCommand command) {
        switch (command) {
            case MOW:
                return 1;
            case RETURN:
                return 2;
            case PAUSE:
                return 3;
            default:
                return 0;
        }
    }
}
