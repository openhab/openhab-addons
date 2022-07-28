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
import org.openhab.binding.boschindego.internal.exceptions.IndegoUnreachableException;
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
    private static final int MAP_REFRESH_INTERVAL_DAYS = 1;

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
        this.statePollFuture = scheduler.scheduleWithFixedDelay(this::refreshStateAndOperatingDataWithExceptionHandling,
                0, config.refresh, TimeUnit.SECONDS);
        this.cuttingTimePollFuture = scheduler.scheduleWithFixedDelay(this::refreshCuttingTimesWithExceptionHandling, 0,
                config.cuttingTimeRefresh, TimeUnit.MINUTES);
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
        } catch (IndegoUnreachableException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.unreachable");
        } catch (IndegoException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void handleRefreshCommand(String channelId)
            throws IndegoAuthenticationException, IndegoUnreachableException, IndegoException {
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
            case NEXT_CUTTING:
                refreshCuttingTimes();
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
        updateState(TEXTUAL_STATE, UnDefType.UNDEF);
        controller.sendCommand(command);
        refreshState();
    }

    private void refreshStateAndOperatingDataWithExceptionHandling() {
        try {
            refreshState();
            refreshOperatingData();
        } catch (IndegoAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.authentication-failure");
        } catch (IndegoUnreachableException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
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
        updateState(state);

        if (state.mapUpdateAvailable) {
            cachedMapTimestamp = Instant.MIN;
        }
        refreshMap(state.svgXPos, state.svgYPos);

        // When state code changed, refresh cutting times immediately.
        if (previousStateCode.isPresent() && state.state != previousStateCode.get()) {
            refreshCuttingTimes();

            // After learning lawn, trigger a forced map refresh on next poll.
            if (previousStateCode.get() == DeviceStatus.STATE_LEARNING_LAWN) {
                cachedMapTimestamp = Instant.MIN;
            }
        }
        previousStateCode = Optional.of(state.state);
    }

    private void refreshOperatingData()
            throws IndegoAuthenticationException, IndegoUnreachableException, IndegoException {
        updateOperatingData(controller.getOperatingData());
        updateStatus(ThingStatus.ONLINE);
    }

    private void refreshCuttingTimesWithExceptionHandling() {
        try {
            refreshCuttingTimes();
        } catch (IndegoAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.authentication-failure");
        } catch (IndegoException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void refreshCuttingTimes() throws IndegoAuthenticationException, IndegoException {
        if (isLinked(LAST_CUTTING)) {
            Instant lastCutting = controller.getPredictiveLastCutting();
            if (lastCutting != null) {
                updateState(LAST_CUTTING,
                        new DateTimeType(ZonedDateTime.ofInstant(lastCutting, timeZoneProvider.getTimeZone())));
            } else {
                updateState(LAST_CUTTING, UnDefType.UNDEF);
            }
        }

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
        // Schedule additional update right after next planned cutting. This ensures a faster update
        // in case the next cutting will be postponed (for example due to weather conditions).
        long secondsUntilNextCutting = Instant.now().until(nextCutting, ChronoUnit.SECONDS) + 2;
        if (secondsUntilNextCutting > 0) {
            logger.debug("Scheduling fetching of cutting times in {} seconds", secondsUntilNextCutting);
            this.cuttingTimeFuture = scheduler.schedule(this::refreshCuttingTimesWithExceptionHandling,
                    secondsUntilNextCutting, TimeUnit.SECONDS);
        }
    }

    private void refreshMap(int xPos, int yPos) throws IndegoAuthenticationException, IndegoException {
        if (!isLinked(GARDEN_MAP)) {
            return;
        }
        RawType cachedMap = this.cachedMap;
        boolean mapRefreshed;
        if (cachedMap == null
                || cachedMapTimestamp.isBefore(Instant.now().minus(Duration.ofDays(MAP_REFRESH_INTERVAL_DAYS)))) {
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

    private void updateState(DeviceStateResponse state) {
        DeviceStatus deviceStatus = DeviceStatus.fromCode(state.state);
        int status = getStatusFromCommand(deviceStatus.getAssociatedCommand());
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
            logger.error("The mower reported an error.");
            return false;
        }

        // Command is equal to current state
        if (command == deviceStatus.getAssociatedCommand()) {
            logger.debug("Command is equal to state");
            return false;
        }
        // Can't pause while the mower is docked
        if (command == DeviceCommand.PAUSE && deviceStatus.getAssociatedCommand() == DeviceCommand.RETURN) {
            logger.debug("Can't pause the mower while it's docked or docking");
            return false;
        }
        // Command means "MOW" but mower is not ready
        if (command == DeviceCommand.MOW && !isReadyToMow(deviceStatus, errorCode)) {
            logger.debug("The mower is not ready to mow at the moment");
            return false;
        }
        return true;
    }

    private int getStatusFromCommand(@Nullable DeviceCommand command) {
        if (command == null) {
            return 0;
        }
        int status;
        switch (command) {
            case MOW:
                status = 1;
                break;
            case RETURN:
                status = 2;
                break;
            case PAUSE:
                status = 3;
                break;
            default:
                status = 0;
        }
        return status;
    }
}
