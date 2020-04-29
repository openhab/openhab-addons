/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.gree.internal.handler;

import static org.openhab.binding.gree.internal.GreeBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.DatagramSocket;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.gree.internal.GreeConfiguration;
import org.openhab.binding.gree.internal.GreeException;
import org.openhab.binding.gree.internal.GreeTranslationProvider;
import org.openhab.binding.gree.internal.discovery.GreeAirDevice;
import org.openhab.binding.gree.internal.discovery.GreeDeviceFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GreeHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
@NonNullByDefault
public class GreeHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(GreeHandler.class);
    private final GreeTranslationProvider messages;
    private GreeConfiguration config = new GreeConfiguration();
    private GreeDeviceFinder deviceFinder = new GreeDeviceFinder();
    private GreeAirDevice device = new GreeAirDevice();
    private Optional<DatagramSocket> clientSocket = Optional.empty();

    private @Nullable ScheduledFuture<?> refreshTask;
    private long lastRefreshTime = 0;

    public GreeHandler(Thing thing, GreeTranslationProvider messages) {
        super(thing);
        this.messages = messages;
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(GreeConfiguration.class);
        logger.debug("Config for {} is {}", thing.getUID(), config.toString());
        if (config.ipAddress.isEmpty() || (config.refresh < 0)) {
            logger.debug("Config of {} is invalid. Check configuration", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid configuration. Check thing configuration.");
            return;
        }

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            initializeThing();
        });
    }

    private void initializeThing() {
        logger.debug("Thing {} is initializing", thing.getUID());

        try {
            // Create a new Datagram socket with a specified timeout
            clientSocket = Optional.of(new DatagramSocket());
            if (!clientSocket.isPresent()) {
                logger.debug("Unable to create datagram socket, discovery aborted!");
                return;
            }
            clientSocket.get().setSoTimeout(DATAGRAM_SOCKET_TIMEOUT);

            // Find the GREE device
            deviceFinder = new GreeDeviceFinder(config.ipAddress);
            deviceFinder.scan(clientSocket, false);
            logger.debug("{} units found matching IP address", deviceFinder.getScannedDeviceCount());

            // Now check that this one is amongst the air conditioners that responded.
            GreeAirDevice newDevice = deviceFinder.getDeviceByIPAddress(config.ipAddress);
            if (newDevice != null) {
                // Ok, our device responded, now let's Bind with it
                device = newDevice;
                device.bindWithDevice(clientSocket);
                if (device.getIsBound()) {
                    logger.debug("GREE AirConditioner {} bound successful", thing.getUID());
                    updateStatus(ThingStatus.ONLINE);

                    // Start the automatic refresh cycles
                    startAutomaticRefresh();
                    return;
                }
            }
            logger.debug("GREE unit is not responding");
        } catch (GreeException e) {
            logger.debug("Initialization failed: {}", messages.get("thinginit.exception", e.toString()));
        } catch (IOException | RuntimeException e) {
            logger.debug("Exception on inituialization", e);
        }

        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // The thing is updated by the scheduled automatic refresh so do nothing here.
        } else {
            if (!clientSocket.isPresent()) {
                logger.debug("Thing not properlyx initialized, abort command");
                return;
            }

            DatagramSocket socket = clientSocket.get();
            try {
                switch (channelUID.getIdWithoutGroup()) {
                    case POWER_CHANNEL:
                        device.setDevicePower(socket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                    case MODE_CHANNEL:
                        device.SetDeviceMode(socket, ((DecimalType) command).intValue());
                        break;
                    case TURBO_CHANNEL:
                        device.setDeviceTurbo(socket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                    case LIGHT_CHANNEL:
                        device.setDeviceLight(socket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                    case TEMP_CHANNEL:
                        device.setDeviceTempSet(socket, ((DecimalType) command).intValue());
                        break;
                    case SWINGV_CHANNEL:
                        device.setDeviceSwingVertical(socket, ((DecimalType) command).intValue());
                        break;
                    case WINDSPEED_CHANNEL:
                        device.setDeviceWindspeed(socket, ((DecimalType) command).intValue());
                        break;
                    case AIR_CHANNEL:
                        device.setDeviceAir(socket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                    case DRY_CHANNEL:
                        device.setDeviceDry(socket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                    case HEALTH_CHANNEL:
                        device.setDeviceHealth(socket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                    case PWRSAV_CHANNEL:
                        device.setDevicePwrSaving(socket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                }
            } catch (GreeException e) {
                logger.debug("Unable to execute command {} for channel {}: {}", command, channelUID.getId(),
                        e.toString());
            } catch (RuntimeException e) {
                logger.debug("Unable to execute command {} for channel {}", command, channelUID.getId(), e);
            }
        }
    }

    private boolean isMinimumRefreshTimeExceeded() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRefresh = currentTime - lastRefreshTime;
        if (timeSinceLastRefresh < MINIMUM_REFRESH_TIME_MS) {
            return false;
        }
        lastRefreshTime = currentTime;
        return true;
    }

    private void startAutomaticRefresh() {

        Runnable refresher = new Runnable() {
            @Override
            public void run() {

                try {
                    logger.debug("Executing automatic update of values");
                    // safeguard for multiple REFRESH commands
                    if (isMinimumRefreshTimeExceeded()) {
                        logger.debug("Fetching status values from device.");
                        // Get the current status from the Airconditioner
                        device.getDeviceStatus(clientSocket.get());
                    } else {
                        logger.trace(
                                "Skipped fetching status values from device because minimum refresh time not reached");
                    }

                    // Update All Channels
                    List<Channel> channels = getThing().getChannels();
                    for (Channel channel : channels) {
                        publishChannelIfLinked(channel.getUID());
                    }

                } catch (GreeException | RuntimeException e) {
                    logger.debug("Unable to perform auto-update", e);
                }
            }
        };

        refreshTask = scheduler.scheduleWithFixedDelay(refresher, 0, config.refresh, TimeUnit.SECONDS);
        logger.debug("Automatic refresh started ({} second interval)", config.refresh);
    }

    private void publishChannelIfLinked(ChannelUID channelUID) throws GreeException {
        try {
            String channelID = channelUID.getId();
            Optional<State> state = Optional.empty();
            switch (channelUID.getIdWithoutGroup()) {
                case POWER_CHANNEL:
                    state = updateOnOff("Pow");
                    break;
                case MODE_CHANNEL:
                    state = updateNumber("Mod");
                    break;
                case TURBO_CHANNEL:
                    state = updateOnOff("Tur");
                    break;
                case LIGHT_CHANNEL:
                    state = updateOnOff("Lig");
                    break;
                case TEMP_CHANNEL:
                    state = updateTemp("SetTem");
                    break;
                case SWINGV_CHANNEL:
                    state = updateNumber("SwUpDn");
                    break;
                case WINDSPEED_CHANNEL:
                    state = updateNumber("WdSpd");
                    break;
                case AIR_CHANNEL:
                    state = updateOnOff("Air");
                    break;
                case DRY_CHANNEL:
                    state = updateOnOff("Blo");
                    break;
                case HEALTH_CHANNEL:
                    state = updateOnOff("Health");
                    break;
                case PWRSAV_CHANNEL:
                    state = updateOnOff("SvSt");
                    break;
            }
            if (state.isPresent()) {
                logger.trace("Updating channel {} : {}", channelID, state.get());
                updateState(channelID, state.get());
            }
        } catch (GreeException | IllegalArgumentException e) {
            logger.debug("Exception on channel update", e);
        }
    }

    private Optional<State> updateOnOff(final String valueName) throws GreeException {
        if (device.hasStatusValChanged(valueName)) {
            return Optional.of(device.getIntStatusVal(valueName) == 1 ? OnOffType.ON : OnOffType.OFF);
        }
        return Optional.empty();
    }

    private Optional<State> updateNumber(final String valueName) throws GreeException {
        if (device.hasStatusValChanged(valueName)) {
            return Optional.of(new DecimalType(device.getIntStatusVal(valueName)));
        }
        return Optional.empty();
    }

    private Optional<State> updateTemp(final String valueName) throws GreeException {
        if (device.hasStatusValChanged(valueName)) {
            return Optional.of(toQuantityType(device.getIntStatusVal(valueName), DIGITS_TEMP, SIUnits.CELSIUS));
        }
        return Optional.empty();
    }

    public static State toQuantityType(int value, int digits, Unit<?> unit) {
        BigDecimal bd = new BigDecimal(value);
        return new QuantityType<>(bd.setScale(digits, BigDecimal.ROUND_HALF_UP), unit);
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} is disposing", thing.getUID());
        if (clientSocket.isPresent()) {
            clientSocket.get().close();
            clientSocket = Optional.empty();
        }
        if (refreshTask != null) {
            refreshTask.cancel(true);
            refreshTask = null;
        }
        lastRefreshTime = 0;
    }
}
