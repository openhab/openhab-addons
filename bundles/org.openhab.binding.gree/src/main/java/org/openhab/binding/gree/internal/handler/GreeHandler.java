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
package org.openhab.binding.gree.internal.handler;

import static org.openhab.binding.gree.internal.GreeBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.DatagramSocket;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gree.internal.GreeConfiguration;
import org.openhab.binding.gree.internal.GreeException;
import org.openhab.binding.gree.internal.GreeTranslationProvider;
import org.openhab.binding.gree.internal.discovery.GreeDeviceFinder;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
    private final GreeDeviceFinder deviceFinder;
    private final String thingId;
    private GreeConfiguration config = new GreeConfiguration();
    private GreeAirDevice device = new GreeAirDevice();
    private Optional<DatagramSocket> clientSocket = Optional.empty();
    private boolean forceRefresh = false;

    private @Nullable ScheduledFuture<?> refreshTask;
    private @Nullable Future<?> initializeFuture;
    private long lastRefreshTime = 0;
    private long apiRetries = 0;

    public GreeHandler(Thing thing, GreeTranslationProvider messages, GreeDeviceFinder deviceFinder) {
        super(thing);
        this.messages = messages;
        this.deviceFinder = deviceFinder;
        this.thingId = getThing().getUID().getId();
    }

    @Override
    public void initialize() {
        config = getConfigAs(GreeConfiguration.class);
        if (config.ipAddress.isEmpty() || (config.refresh < 0)) {
            String message = messages.get("thinginit.invconf");
            logger.warn("{}: {}", thingId, message);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
            return;
        }

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        updateStatus(ThingStatus.UNKNOWN);

        // Start the automatic refresh cycles
        startAutomaticRefresh();
        initializeFuture = scheduler.submit(this::initializeThing);
    }

    private void initializeThing() {
        String message = "";
        try {
            if (clientSocket.isEmpty()) {
                clientSocket = Optional.of(new DatagramSocket());
                clientSocket.get().setSoTimeout(DATAGRAM_SOCKET_TIMEOUT);
            }
            // Find the GREE device
            deviceFinder.scan(clientSocket.get(), config.ipAddress, false);
            GreeAirDevice newDevice = deviceFinder.getDeviceByIPAddress(config.ipAddress);
            if (newDevice != null) {
                // Ok, our device responded, now let's Bind with it
                device = newDevice;
                device.bindWithDevice(clientSocket.get());
                if (device.getIsBound()) {
                    updateStatus(ThingStatus.ONLINE);
                    return;
                }
            }

            message = messages.get("thinginit.failed");
            logger.info("{}: {}", thingId, message);
        } catch (GreeException e) {
            logger.info("{}: {}", thingId, messages.get("thinginit.exception", e.getMessageString()));
        } catch (IOException e) {
            logger.warn("{}: {}", thingId, messages.get("thinginit.exception", "I/O Error"), e);
        } catch (RuntimeException e) {
            logger.warn("{}: {}", thingId, messages.get("thinginit.exception", "RuntimeException"), e);
        }

        if (getThing().getStatus() != ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // The thing is updated by the scheduled automatic refresh so do nothing here.
        } else {
            logger.debug("{}: Issue command {} to channe {}", thingId, command, channelUID.getIdWithoutGroup());
            String channelId = channelUID.getIdWithoutGroup();
            logger.debug("{}: Handle command {} for channel {}, command class {}", thingId, command, channelId,
                    command.getClass());

            int retries = MAX_API_RETRIES;
            do {
                try {
                    sendRequest(channelId, command);
                    // force refresh on next status refresh cycle
                    forceRefresh = true;
                    apiRetries = 0;
                    return; // successful
                } catch (GreeException e) {
                    retries--;
                    if (retries > 0) {
                        logger.debug("{}: Command {} failed for channel {}, retry", thingId, command, channelId);
                    } else {
                        String message = logInfo(
                                messages.get("command.exception", command, channelId) + ": " + e.getMessageString());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
                    }
                } catch (IllegalArgumentException e) {
                    logInfo("command.invarg", command, channelId);
                    retries = 0;
                } catch (RuntimeException e) {
                    logger.warn("{}: {}", thingId, messages.get("command.exception", command, channelId), e);
                    retries = 0;
                }
            } while (retries > 0);
        }
    }

    private void sendRequest(String channelId, Command command) throws GreeException {
        DatagramSocket socket = clientSocket.get();
        switch (channelId) {
            case MODE_CHANNEL:
                handleModeCommand(socket, command);
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
            case TARGET_TEMP_CHANNEL:
                // Set value, read back effective one and update channel
                // e.g. 22.5C will result in 22.0, because the AC doesn't support half-steps for C
                device.setDeviceTempSet(socket, convertTemp(command));
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
                handleQuietCommand(socket, command);
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
    }

    private void handleModeCommand(DatagramSocket socket, Command command) throws GreeException {
        int mode = -1;
        String modeStr = "";
        boolean isNumber = false;
        if (command instanceof DecimalType decimalCommand) {
            // backward compatibility when channel was Number
            mode = decimalCommand.intValue();
        } else if (command instanceof OnOffType) {
            // Switch
            logger.debug("{}: Send Power-{}", thingId, command);
            device.setDevicePower(socket, getOnOff(command));
        } else /* String */ {
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
                    logger.debug("{}: Turn unit {}", thingId, modeStr);
                    device.setDevicePower(socket, modeStr.equals(MODE_ON) ? 1 : 0);
                    return;
                default:
                    // fallback: mode number, pass transparent
                    // if string is not parsable parseInt() throws an exception
                    mode = Integer.parseInt(modeStr);
                    isNumber = true;
                    break;
            }
            logger.debug("{}: Mode {} mapped to {}", thingId, modeStr, mode);
        }

        if (mode == -1) {
            throw new IllegalArgumentException("Invalid Mode selection");
        }

        // Turn on the unit if currently off
        if (!isNumber && (device.getIntStatusVal(GREE_PROP_POWER) == 0)) {
            logger.debug("{}: Send Auto-ON for mode {}", thingId, mode);
            device.setDevicePower(socket, 1);
        }

        // Select mode
        logger.debug("{}: Select mode {}", thingId, mode);
        device.setDeviceMode(socket, mode);

        // Check for secondary action
        switch (modeStr) {
            case MODE_ECO:
                // Turn on power saving for eco mode
                logger.debug("{}: Turn on Power-Saving", thingId);
                device.setDevicePwrSaving(socket, 1);
                break;
        }
    }

    private void handleQuietCommand(DatagramSocket socket, Command command) throws GreeException {
        int mode = -1;
        if (command instanceof DecimalType decimalCommand) {
            mode = decimalCommand.intValue();
        } else if (command instanceof StringType) {
            switch (command.toString().toLowerCase()) {
                case QUIET_OFF:
                    mode = GREE_QUIET_OFF;
                    break;
                case QUIET_AUTO:
                    mode = GREE_QUIET_AUTO;
                    break;
                case QUIET_QUIET:
                    mode = GREE_QUIET_QUIET;
                    break;
            }
        }
        if (mode != -1) {
            device.setQuietMode(socket, mode);
        } else {
            throw new IllegalArgumentException("Invalid QuietType");
        }
    }

    private int getOnOff(Command command) {
        if (command instanceof OnOffType) {
            return command == OnOffType.ON ? 1 : 0;
        }
        if (command instanceof DecimalType decimalCommand) {
            int value = decimalCommand.intValue();
            if ((value == 0) || (value == 1)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid OnOffType");
    }

    private int getNumber(Command command) {
        if (command instanceof DecimalType decimalCommand) {
            return decimalCommand.intValue();
        }
        throw new IllegalArgumentException("Invalid Number type");
    }

    private QuantityType<?> convertTemp(Command command) {
        if (command instanceof DecimalType temperature) {
            // The Number alone doesn't specify the temp unit
            // for this get current setting from the A/C unit
            int unit = device.getIntStatusVal(GREE_PROP_TEMPUNIT);
            return toQuantityType(temperature, DIGITS_TEMP,
                    unit == TEMP_UNIT_CELSIUS ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT);
        }
        if (command instanceof QuantityType quantityCommand) {
            return quantityCommand;
        }
        throw new IllegalArgumentException("Invalud Temp type");
    }

    private void startAutomaticRefresh() {
        Runnable refresher = () -> {
            try {
                // safeguard for multiple REFRESH commands
                if (isMinimumRefreshTimeExceeded()) {
                    // Get the current status from the Airconditioner

                    if (getThing().getStatus() == ThingStatus.OFFLINE) {
                        // try to re-initialize thing access
                        logger.debug("{}: Re-initialize device", thingId);
                        initializeThing();
                        return;
                    }

                    if (clientSocket.isPresent()) {
                        device.getDeviceStatus(clientSocket.get());
                        apiRetries = 0; // the call was successful without an exception
                        logger.debug("{}: Executing automatic update of values", thingId);
                        List<Channel> channels = getThing().getChannels();
                        for (Channel channel : channels) {
                            publishChannel(channel.getUID());
                        }
                    }
                }
            } catch (GreeException e) {
                String subcode = "";
                if (e.getCause() != null) {
                    subcode = " (" + e.getCause().getMessage() + ")";
                }
                String message = messages.get("update.exception", e.getMessageString() + subcode);
                if (getThing().getStatus() == ThingStatus.OFFLINE) {
                    logger.debug("{}: Thing still OFFLINE ({})", thingId, message);
                } else {
                    if (!e.isTimeout()) {
                        logger.info("{}: {}", thingId, message);
                    } else {
                        logger.debug("{}: {}", thingId, message);
                    }

                    apiRetries++;
                    if (apiRetries > MAX_API_RETRIES) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
                        apiRetries = 0;
                    }
                }
            } catch (RuntimeException e) {
                String message = messages.get("update.exception", "RuntimeException");
                logger.warn("{}: {}", thingId, message, e);
                apiRetries++;
            }
        };

        if (refreshTask == null) {
            refreshTask = scheduler.scheduleWithFixedDelay(refresher, 0, REFRESH_INTERVAL_SEC, TimeUnit.SECONDS);
            logger.debug("{}: Automatic refresh started ({} second interval)", thingId, config.refresh);
            forceRefresh = true;
        }
    }

    private boolean isMinimumRefreshTimeExceeded() {
        long currentTime = Instant.now().toEpochMilli();
        long timeSinceLastRefresh = currentTime - lastRefreshTime;
        if (!forceRefresh && (timeSinceLastRefresh < config.refresh * 1000)) {
            return false;
        }
        lastRefreshTime = currentTime;
        return true;
    }

    private void publishChannel(ChannelUID channelUID) {
        String channelID = channelUID.getId();
        try {
            State state = null;
            switch (channelUID.getIdWithoutGroup()) {
                case POWER_CHANNEL:
                    state = updateOnOff(GREE_PROP_POWER);
                    break;
                case MODE_CHANNEL:
                    state = updateMode();
                    break;
                case TURBO_CHANNEL:
                    state = updateOnOff(GREE_PROP_TURBO);
                    break;
                case LIGHT_CHANNEL:
                    state = updateOnOff(GREE_PROP_LIGHT);
                    break;
                case TARGET_TEMP_CHANNEL:
                    state = updateTargetTemp();
                    break;
                case CURRENT_TEMP_CHANNEL:
                    state = updateCurrentTemp();
                    break;
                case SWINGUD_CHANNEL:
                    state = updateNumber(GREE_PROP_SWINGUPDOWN);
                    break;
                case SWINGLR_CHANNEL:
                    state = updateNumber(GREE_PROP_SWINGLEFTRIGHT);
                    break;
                case WINDSPEED_CHANNEL:
                    state = updateNumber(GREE_PROP_WINDSPEED);
                    break;
                case QUIET_CHANNEL:
                    state = updateQuiet();
                    break;
                case AIR_CHANNEL:
                    state = updateOnOff(GREE_PROP_AIR);
                    break;
                case DRY_CHANNEL:
                    state = updateOnOff(GREE_PROP_DRY);
                    break;
                case HEALTH_CHANNEL:
                    state = updateOnOff(GREE_PROP_HEALTH);
                    break;
                case PWRSAV_CHANNEL:
                    state = updateOnOff(GREE_PROP_PWR_SAVING);
                    break;
            }
            if (state != null) {
                logger.debug("{}: Updating channel {} : {}", thingId, channelID, state);
                updateState(channelID, state);
            }
        } catch (GreeException e) {
            logger.info("{}: {}", thingId, messages.get("channel.exception", channelID, e.getMessageString()));
        } catch (RuntimeException e) {
            logger.warn("{}: {}", thingId, messages.get("channel.exception", "RuntimeException"), e);
        }
    }

    private @Nullable State updateOnOff(final String valueName) throws GreeException {
        if (device.hasStatusValChanged(valueName)) {
            return OnOffType.from(device.getIntStatusVal(valueName) == 1);
        }
        return null;
    }

    private @Nullable State updateNumber(final String valueName) throws GreeException {
        if (device.hasStatusValChanged(valueName)) {
            return new DecimalType(device.getIntStatusVal(valueName));
        }
        return null;
    }

    private @Nullable State updateMode() throws GreeException {
        if (device.hasStatusValChanged(GREE_PROP_MODE)) {
            int mode = device.getIntStatusVal(GREE_PROP_MODE);
            String modeStr = "";
            switch (mode) {
                case GREE_MODE_AUTO:
                    modeStr = MODE_AUTO;
                    break;
                case GREE_MODE_COOL:
                    boolean powerSave = device.getIntStatusVal(GREE_PROP_PWR_SAVING) == 1;
                    modeStr = !powerSave ? MODE_COOL : MODE_ECO;
                    break;
                case GREE_MODE_DRY:
                    modeStr = MODE_DRY;
                    break;
                case GREE_MODE_FAN:
                    modeStr = MODE_FAN;
                    break;
                case GREE_MODE_HEAT:
                    modeStr = MODE_HEAT;
                    break;
                default:
                    modeStr = String.valueOf(mode);

            }
            if (!modeStr.isEmpty()) {
                logger.debug("{}: Updading mode channel with {}/{}", thingId, mode, modeStr);
                return new StringType(modeStr);
            }
        }
        return null;
    }

    private @Nullable State updateQuiet() throws GreeException {
        if (device.hasStatusValChanged(GREE_PROP_QUIET)) {
            switch (device.getIntStatusVal(GREE_PROP_QUIET)) {
                case GREE_QUIET_OFF:
                    return new StringType(QUIET_OFF);
                case GREE_QUIET_AUTO:
                    return new StringType(QUIET_AUTO);
                case GREE_QUIET_QUIET:
                    return new StringType(QUIET_QUIET);
            }
        }
        return null;
    }

    private @Nullable State updateTargetTemp() throws GreeException {
        if (device.hasStatusValChanged(GREE_PROP_SETTEMP) || device.hasStatusValChanged(GREE_PROP_TEMPUNIT)) {
            int unit = device.getIntStatusVal(GREE_PROP_TEMPUNIT);
            return toQuantityType(device.getIntStatusVal(GREE_PROP_SETTEMP), DIGITS_TEMP,
                    unit == TEMP_UNIT_CELSIUS ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT);
        }
        return null;
    }

    private @Nullable State updateCurrentTemp() throws GreeException {
        if (device.hasStatusValChanged(GREE_PROP_CURRENT_TEMP_SENSOR)) {
            double temp = device.getIntStatusVal(GREE_PROP_CURRENT_TEMP_SENSOR);
            return temp != 0
                    ? new DecimalType(
                            temp + INTERNAL_TEMP_SENSOR_OFFSET + config.currentTemperatureOffset.doubleValue())
                    : UnDefType.UNDEF;
        }
        return null;
    }

    private String logInfo(String msgKey, Object... arg) {
        String message = messages.get(msgKey, arg);
        logger.info("{}: {}", thingId, message);
        return message;
    }

    public static QuantityType<?> toQuantityType(Number value, int digits, Unit<?> unit) {
        BigDecimal bd = new BigDecimal(value.doubleValue());
        return new QuantityType<>(bd.setScale(digits, RoundingMode.HALF_EVEN), unit);
    }

    private void stopRefreshTask() {
        forceRefresh = false;
        if (refreshTask == null) {
            return;
        }
        ScheduledFuture<?> task = refreshTask;
        if (task != null) {
            task.cancel(true);
        }
        refreshTask = null;
    }

    @Override
    public void dispose() {
        logger.debug("{}: Thing {} is disposing", thingId, thing.getUID());
        if (clientSocket.isPresent()) {
            clientSocket.get().close();
            clientSocket = Optional.empty();
        }
        stopRefreshTask();
        if (initializeFuture != null) {
            initializeFuture.cancel(true);
        }
    }
}
