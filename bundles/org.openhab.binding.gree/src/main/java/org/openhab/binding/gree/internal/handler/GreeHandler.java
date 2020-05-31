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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
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

import tec.uom.se.unit.Units;

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
        config = getConfigAs(GreeConfiguration.class);
        logger.debug("Config for {} is {}", thing.getUID(), config.toString());
        if (config.ipAddress.isEmpty() || (config.refresh < 0)) {
            logger.warn("Config of {} is invalid. Check configuration", thing.getUID());
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
                    updateStatus(ThingStatus.ONLINE);

                    // Start the automatic refresh cycles
                    startAutomaticRefresh();
                    return;
                }
            }
            logger.debug("GREE unit is not responding");
        } catch (GreeException e) {
            logger.warn("Initialization failed: {}", messages.get("thinginit.exception", e.toString()));
        } catch (IOException | RuntimeException e) {
            logger.debug("Exception on initialization", e);
        }

        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // The thing is updated by the scheduled automatic refresh so do nothing here.
        } else {
            if (!clientSocket.isPresent()) {
                logger.warn("Thing not properly initialized, abort command");
                return;
            }

            DatagramSocket socket = clientSocket.get();
            logger.debug("Issue command {} to channe {}", command, channelUID.getIdWithoutGroup());
            String channelId = channelUID.getIdWithoutGroup();
            try {
                switch (channelId) {
                    case MODE_CHANNEL:
                        handleModeCommand(command, socket);
                        break;
                    case POWER_CHANNEL:
                        device.setDevicePower(socket, getOnOff(command));
                        break;
                    case TURBO_CHANNEL:
                        device.setDeviceTurbo(socket, getOnOff(command));
                        break;
                    case LIGHT_CHANNEL:
                        device.setDeviceLight(socket, getOnOff(command));
                        break;
                    case TEMP_CHANNEL:
                        device.setDeviceTempSet(socket, getNumber(command));
                        break;
                    case SWINGUD_CHANNEL:
                        device.setDeviceSwingUpDown(socket, getNumber(command));
                        break;
                    case SWINGLR_CHANNEL:
                        device.setDeviceSwingLeftRight(socket, getNumber(command));
                        break;
                    case WINDSPEED_CHANNEL:
                        device.setDeviceWindspeed(socket, getNumber(command));
                        break;
                    case QUIET_CHANNEL:
                        device.setQuietMode(socket, getNumber(command));
                        break;
                    case AIR_CHANNEL:
                        device.setDeviceAir(socket, getOnOff(command));
                        break;
                    case DRY_CHANNEL:
                        device.setDeviceDry(socket, getOnOff(command));
                        break;
                    case HEALTH_CHANNEL:
                        device.setDeviceHealth(socket, getOnOff(command));
                        break;
                    case PWRSAV_CHANNEL:
                        device.setDevicePwrSaving(socket, getOnOff(command));
                        break;
                }
            } catch (GreeException e) {
                logger.debug("Unable to execute command {} for channel {}: {}", command, channelId, e.toString());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid command value {} for channel {}", command, channelUID.getId());
            } catch (RuntimeException e) {
                logger.debug("Unable to execute command {} for channel {}", command, channelId, e);
            }
        }
    }

    private int getOnOff(Command command) {
        if (command instanceof OnOffType) {
            return ((OnOffType) command) == OnOffType.ON ? 1 : 0;
        }
        if ((command instanceof DecimalType) && (((DecimalType) command).intValue() <= 2)) {
            return ((DecimalType) command).intValue();
        }
        throw new IllegalArgumentException("Invalid OonOffType");
    }

    private int getNumber(Command command) {
        if (command instanceof DecimalType) {
            return ((DecimalType) command).intValue();
        }
        if (command instanceof QuantityType) {
            QuantityType<?> q = (QuantityType<?>) command;
            if (q.getUnit() == Units.CELSIUS) {
                return q.intValue();
            }
        }
        if (command instanceof StringType) {
            String temp = ((StringType) command).toString();
            temp = temp.replace("°C", "");
            temp = temp.replace(" ", "");
            return Integer.parseInt(temp);
        }
        throw new IllegalArgumentException("Invalud Number type");
    }

    private void handleModeCommand(Command command, DatagramSocket socket) throws GreeException {
        int mode = -1;
        String modeStr = "";
        boolean isNumber = false;
        if (command instanceof DecimalType) {
            // backward compatibility when channel was Number
            mode = ((DecimalType) command).intValue();
        } else if (command instanceof OnOffType) {
            // Switch
            logger.debug("Send Power-{}", command);
            device.setDevicePower(socket, getOnOff(command));
        } else /* String */ {
            isNumber = StringUtils.isNumeric(command.toString());
            modeStr = command.toString().toLowerCase();
            switch (modeStr) {
                case MODE_AUTO:
                    mode = GREE_MODE_AUTO;
                    break;
                case MODE_COOL:
                    mode = GREE_MODE_COOL;
                    break;
                case MODE_HEAT:
                    mode = GREE_MODE_HEAT;
                    break;
                case MODE_DRY:
                    mode = GREE_MODE_DRY;
                    break;
                case MODE_FAN:
                case MODE_FAN2:
                    mode = GREE_MODE_FAN;
                    break;
                case MODE_ECO:
                    // power saving will be set after the uinit was turned on
                    mode = GREE_MODE_COOL;
                    break;
                case MODE_ON:
                case MODE_OFF:
                    device.setDevicePower(socket, modeStr.equals(MODE_ON) ? 1 : 0);
                    break;
                default:
                    if (isNumber) {
                        // Support selecting the mode by number, maybe specific models support
                        // additional modes
                        mode = Integer.parseInt(modeStr);
                    } else {
                        logger.debug("Invalid mode requested: {}", command);
                    }
                    break;
            }
            logger.debug("Mode {} mapped to {}", modeStr, mode);
        }

        if (mode == -1) {
            throw new IllegalArgumentException("Invalid Mode selection");
        }

        // Turn on the unit if currently off
        if (!isNumber && (device.getIntStatusVal("Pow") == 0)) {
            logger.debug("Send Auto-ON for mode {}", mode);
            device.setDevicePower(socket, 1);
        }

        // Select mode
        logger.debug("Select mode {}", mode);
        device.SetDeviceMode(socket, mode);

        // Check for secondary action
        switch (modeStr) {
            case MODE_ECO:
                // Turn on power saving for eco mode
                logger.debug("Turn on Power-Saving");
                device.setDevicePwrSaving(socket, 1);
                break;
            case MODE_TURBO:
                device.setDeviceTurbo(socket, 1);
                break;
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
                        publishChannel(channel.getUID());
                    }
                } catch (GreeException e) {
                    if (!e.isTimeout()) {
                        logger.warn("Unable to perform auto-update: {}", e.toString());
                    }
                } catch (RuntimeException e) {
                    logger.debug("Unable to perform auto-update", e);
                }
            }
        };

        refreshTask = scheduler.scheduleWithFixedDelay(refresher, 0, config.refresh, TimeUnit.SECONDS);
        logger.debug("Automatic refresh started ({} second interval)", config.refresh);
    }

    private void publishChannel(ChannelUID channelUID) throws GreeException {
        try {
            String channelID = channelUID.getId();
            Optional<State> state = Optional.empty();
            switch (channelUID.getIdWithoutGroup()) {
                case POWER_CHANNEL:
                    state = updateOnOff("Pow");
                    break;
                case MODE_CHANNEL:
                    state = updateMode();
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
                case SWINGUD_CHANNEL:
                    state = updateNumber("SwUpDn");
                    break;
                case SWINGLR_CHANNEL:
                    state = updateNumber("SwingLfRig");
                    break;
                case WINDSPEED_CHANNEL:
                    state = updateNumber("WdSpd");
                    break;
                case QUIET_CHANNEL:
                    state = updateNumber("Quiet");
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
        } catch (GreeException e) {
            logger.warn("Exception on channel update: {}", e.toString());
        } catch (RuntimeException e) {
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

    private Optional<State> updateMode() throws GreeException {
        if (device.hasStatusValChanged("Mod")) {
            int mode = device.getIntStatusVal("Mod");
            String modeStr = "";
            switch (mode) {
                case GREE_MODE_AUTO:
                    modeStr = MODE_AUTO;
                    break;
                case GREE_MODE_COOL:
                    boolean powerSave = device.getIntStatusVal("SvSt") == 1;
                    modeStr = !powerSave ? MODE_COOL : MODE_ECO;
                    break;
                case GREE_MODE_DRY:
                    modeStr = MODE_DRY;
                    break;
                case GREE_MODE_FAN:
                    boolean turbo = device.getIntStatusVal("Tur") == 1;
                    modeStr = !turbo ? MODE_FAN : MODE_TURBO;
                    break;
                case GREE_MODE_HEAT:
                    modeStr = MODE_HEAT;
                    break;
                default:
                    modeStr = String.valueOf(mode);

            }
            if (!modeStr.isEmpty()) {
                logger.debug("Updading mode channel with {}/{}", mode, modeStr);
                return Optional.of(new StringType(modeStr));
            }
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
