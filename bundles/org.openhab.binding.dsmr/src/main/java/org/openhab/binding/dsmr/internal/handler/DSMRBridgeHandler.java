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
package org.openhab.binding.dsmr.internal.handler;

import static org.openhab.binding.dsmr.internal.DSMRBindingConstants.CONFIGURATION_ADDITIONAL_KEY;
import static org.openhab.binding.dsmr.internal.DSMRBindingConstants.CONFIGURATION_DECRYPTION_KEY;
import static org.openhab.binding.dsmr.internal.DSMRBindingConstants.THING_TYPE_SMARTY_BRIDGE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dsmr.internal.device.DSMRDevice;
import org.openhab.binding.dsmr.internal.device.DSMRDeviceConfiguration;
import org.openhab.binding.dsmr.internal.device.DSMRDeviceRunnable;
import org.openhab.binding.dsmr.internal.device.DSMRFixedConfigDevice;
import org.openhab.binding.dsmr.internal.device.DSMRSerialAutoDevice;
import org.openhab.binding.dsmr.internal.device.DSMRTelegramListener;
import org.openhab.binding.dsmr.internal.device.connector.DSMRErrorStatus;
import org.openhab.binding.dsmr.internal.device.connector.DSMRSerialSettings;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener;
import org.openhab.binding.dsmr.internal.discovery.DSMRMeterDiscoveryService;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DSMRBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored way messages are forwarded to meters. Removed availableMeters dependency.
 */
@NonNullByDefault
public class DSMRBridgeHandler extends BaseBridgeHandler implements P1TelegramListener {

    /**
     * Factor that will be multiplied with {@link #receivedTimeoutNanos} to get the timeout factor after which the
     * device is set off line.
     */
    private static final int OFFLINE_TIMEOUT_FACTOR = 10;

    private final Logger logger = LoggerFactory.getLogger(DSMRBridgeHandler.class);

    /**
     * Additional meter listeners to get received meter values.
     */
    private final List<P1TelegramListener> meterListeners = new ArrayList<>();

    /**
     * Serial Port Manager.
     */
    private final SerialPortManager serialPortManager;

    /**
     * The dsmrDevice managing the connection and handling telegrams.
     */
    private @NonNullByDefault({}) DSMRDevice dsmrDevice;

    /**
     * Long running process that controls the DSMR device connection.
     */
    private @NonNullByDefault({}) DSMRDeviceRunnable dsmrDeviceRunnable;

    /**
     * Thread for {@link DSMRDeviceRunnable}. A thread is used because the {@link DSMRDeviceRunnable} is a blocking
     * process that runs as long as the thing is not disposed.
     */
    private @NonNullByDefault({}) Thread dsmrDeviceThread;

    /**
     * Watchdog to check if messages received and restart if necessary.
     */
    private @NonNullByDefault({}) ScheduledFuture<?> watchdog;

    /**
     * Number of nanoseconds after which a timeout is triggered when no messages received.
     */
    private long receivedTimeoutNanos;

    /**
     * Timestamp in nanoseconds of last P1 telegram received
     */
    private volatile long telegramReceivedTimeNanos;

    private final boolean smartyMeter;

    private @Nullable String lastKnownReadErrorMessage;

    /**
     * Constructor
     *
     * @param bridge the Bridge ThingType
     * @param serialPortManager The Serial port manager
     */
    public DSMRBridgeHandler(final Bridge bridge, final SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
        smartyMeter = THING_TYPE_SMARTY_BRIDGE.equals(bridge.getThingTypeUID());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(DSMRMeterDiscoveryService.class);
    }

    /**
     * The {@link DSMRBridgeHandler} does not support handling commands.
     *
     * @param channelUID the {@link ChannelUID} of the channel to which the command was sent
     * @param command the {@link Command}
     */
    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // DSMRBridgeHandler does not support commands
    }

    /**
     * Initializes this {@link DSMRBridgeHandler}.
     *
     * This method will get the corresponding configuration and initialize and start the corresponding
     * {@link DSMRDevice}.
     */
    @Override
    public void initialize() {
        final DSMRDeviceConfiguration deviceConfig = getConfigAs(DSMRDeviceConfiguration.class);

        if (smartyMeter && !validateSmartyMeterConfiguration(deviceConfig)) {
            return;
        }

        logger.trace("Using configuration {}", deviceConfig);
        updateStatus(ThingStatus.UNKNOWN);
        receivedTimeoutNanos = TimeUnit.SECONDS.toNanos(deviceConfig.receivedTimeout);
        final DSMRDevice dsmrDevice = createDevice(deviceConfig);
        resetLastReceivedState();
        this.dsmrDevice = dsmrDevice; // otherwise Eclipse will give a null pointer error on the next line :-(
        dsmrDeviceRunnable = new DSMRDeviceRunnable(dsmrDevice, this);
        dsmrDeviceThread = new Thread(dsmrDeviceRunnable);
        dsmrDeviceThread.setName("OH-binding-" + getThing().getUID());
        dsmrDeviceThread.setDaemon(true);
        dsmrDeviceThread.start();
        watchdog = scheduler.scheduleWithFixedDelay(this::alive, receivedTimeoutNanos, receivedTimeoutNanos,
                TimeUnit.NANOSECONDS);
    }

    private boolean validateSmartyMeterConfiguration(final DSMRDeviceConfiguration deviceConfig) {
        final boolean valid;
        if (deviceConfig.decryptionKey == null || deviceConfig.decryptionKey.length() != 32) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/addon.dsmr.error.configuration.invalidsmartykey");
            valid = false;
        } else if (!validDecryptionKey(deviceConfig.decryptionKey, CONFIGURATION_DECRYPTION_KEY)
                || !validDecryptionKey(deviceConfig.additionalKey, CONFIGURATION_ADDITIONAL_KEY)) {
            valid = false;
        } else {
            valid = true;
        }
        return valid;
    }

    private boolean validDecryptionKey(final String key, final String message) {
        try {
            HexUtils.hexToBytes(key);
            return true;
        } catch (final IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/addon.dsmr.error.configuration.invalid." + message + " [" + e.getMessage() + "]");
        }
        return false;
    }

    /**
     * Creates the {@link DSMRDevice} that corresponds with the user specified configuration.
     *
     * @param deviceConfig device configuration
     * @return Specific {@link DSMRDevice} instance
     */
    private DSMRDevice createDevice(final DSMRDeviceConfiguration deviceConfig) {
        final DSMRDevice dsmrDevice;

        if (smartyMeter) {
            dsmrDevice = new DSMRFixedConfigDevice(serialPortManager, deviceConfig.serialPort,
                    DSMRSerialSettings.HIGH_SPEED_SETTINGS, this,
                    new DSMRTelegramListener(deviceConfig.decryptionKey, deviceConfig.additionalKey));
        } else {
            final DSMRTelegramListener telegramListener = new DSMRTelegramListener();

            if (deviceConfig.isSerialFixedSettings()) {
                dsmrDevice = new DSMRFixedConfigDevice(serialPortManager, deviceConfig.serialPort,
                        DSMRSerialSettings.getPortSettingsFromConfiguration(deviceConfig), this, telegramListener);
            } else {
                dsmrDevice = new DSMRSerialAutoDevice(serialPortManager, deviceConfig.serialPort, this,
                        telegramListener, scheduler, deviceConfig.receivedTimeout);
            }
        }
        return dsmrDevice;
    }

    /**
     * Registers a meter listener.
     *
     * @param meterListener the meter discovery listener to add
     * @return true if listener is added, false otherwise
     */
    public boolean registerDSMRMeterListener(final P1TelegramListener meterListener) {
        logger.trace("Register DSMRMeterListener");
        return meterListeners.add(meterListener);
    }

    /**
     * Unregisters a meter listener
     *
     * @param meterListener the meter discovery listener to remove
     * @return true is listener is removed, false otherwise
     */
    public boolean unregisterDSMRMeterListener(final P1TelegramListener meterListener) {
        logger.trace("Unregister DSMRMeterListener");
        return meterListeners.remove(meterListener);
    }

    /**
     * Watchdog method that is run with the scheduler and checks if meter values were received. If the timeout is
     * exceeded the device is restarted. If the off line timeout factor is exceeded the device is set off line. By not
     * setting the device on first exceed off line their is some slack in the system and it won't flip on and offline in
     * case of an unstable system.
     */
    private void alive() {
        logger.trace("Bridge alive check with #{} children.", getThing().getThings().size());
        final long deltaLastReceived = System.nanoTime() - telegramReceivedTimeNanos;

        if (deltaLastReceived > receivedTimeoutNanos) {
            logger.debug("No valid data received for {} seconds, restarting port if possible.",
                    TimeUnit.NANOSECONDS.toSeconds(deltaLastReceived));
            if (deltaLastReceived > receivedTimeoutNanos * OFFLINE_TIMEOUT_FACTOR) {
                logger.trace("Setting device offline if not yet done, and reset last received time.");
                if (isInitialized() && getThing().getStatus() != ThingStatus.OFFLINE) {
                    final String lkm = lastKnownReadErrorMessage;
                    final String message = lkm == null ? "@text/addon.dsmr.error.bridge.nodata" : lkm;

                    deviceOffline(ThingStatusDetail.COMMUNICATION_ERROR, message);
                }
                resetLastReceivedState();
            }
            if (dsmrDeviceRunnable != null) {
                dsmrDeviceRunnable.restart();
            }
        }
    }

    /**
     * Sets the last received time of messages to the current time.
     */
    private void resetLastReceivedState() {
        lastKnownReadErrorMessage = null;
        telegramReceivedTimeNanos = System.nanoTime();
        logger.trace("Telegram received time set: {}", telegramReceivedTimeNanos);
    }

    @Override
    public synchronized void telegramReceived(final P1Telegram telegram) {
        resetLastReceivedState();
        meterValueReceived(telegram);
    }

    @Override
    public void onError(final DSMRErrorStatus errorStatus, final String message) {
        if (errorStatus == DSMRErrorStatus.TELEGRAM_NO_DATA) {
            logger.debug("Parsing worked but something went wrong, so there were no CosemObjects:{}", message);
            lastKnownReadErrorMessage = errorStatus.getEventDetails();
        } else {
            final String errorMessage = errorStatus.getEventDetails() + ' ' + message;
            lastKnownReadErrorMessage = errorMessage;
            // if fatal set directly offline.
            if (errorStatus.isFatal()) {
                deviceOffline(ThingStatusDetail.CONFIGURATION_ERROR, errorMessage);
            }
        }
    }

    /**
     * Method to forward the last received messages to the bound meters and to the meterListeners.
     *
     * @param telegram received meter values.
     */
    private void meterValueReceived(final P1Telegram telegram) {
        if (isInitialized() && getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        getThing().getThings().forEach(child -> {
            if (logger.isTraceEnabled()) {
                logger.trace("Update child:{} with {} objects", child.getThingTypeUID().getId(),
                        telegram.getCosemObjects().size());
            }
            final DSMRMeterHandler dsmrMeterHandler = (DSMRMeterHandler) child.getHandler();

            if (dsmrMeterHandler instanceof DSMRMeterHandler) {
                dsmrMeterHandler.telegramReceived(telegram);
            }
        });
        meterListeners.forEach(m -> m.telegramReceived(telegram));
    }

    @Override
    public void dispose() {
        if (watchdog != null) {
            watchdog.cancel(true);
            watchdog = null;
        }
        if (dsmrDeviceRunnable != null) {
            dsmrDeviceRunnable.stop();
        }
    }

    /**
     * @param lenientMode the lenientMode to set
     */
    public void setLenientMode(final boolean lenientMode) {
        logger.trace("SetLenientMode: {}", lenientMode);
        if (dsmrDevice != null) {
            dsmrDevice.setLenientMode(lenientMode);
        }
    }

    /**
     * Convenience method to set device off line.
     *
     * @param status off line status
     * @param details off line detailed message
     */
    private void deviceOffline(final ThingStatusDetail status, final String details) {
        updateStatus(ThingStatus.OFFLINE, status, details);
    }
}
