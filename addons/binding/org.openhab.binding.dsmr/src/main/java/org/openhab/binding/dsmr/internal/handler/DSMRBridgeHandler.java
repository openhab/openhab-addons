/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.dsmr.internal.device.DSMRDevice;
import org.openhab.binding.dsmr.internal.device.DSMRDeviceConfiguration;
import org.openhab.binding.dsmr.internal.device.DSMRDeviceRunnable;
import org.openhab.binding.dsmr.internal.device.DSMREventListener;
import org.openhab.binding.dsmr.internal.device.DSMRFixedConfigDevice;
import org.openhab.binding.dsmr.internal.device.DSMRSerialAutoDevice;
import org.openhab.binding.dsmr.internal.device.connector.DSMRConnectorErrorEvent;
import org.openhab.binding.dsmr.internal.device.connector.DSMRSerialSettings;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener;
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
public class DSMRBridgeHandler extends BaseBridgeHandler implements DSMREventListener {

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
    private @Nullable DSMRDevice dsmrDevice;

    /**
     * Long running process that controls the DSMR device connection.
     */
    private @Nullable DSMRDeviceRunnable dsmrDeviceRunnable;

    /**
     * Thread for {@link DSMRDeviceRunnable}. A thread is used because the {@link DSMRDeviceRunnable} is a blocking
     * process that runs as long as the thing is not disposed.
     */
    private @Nullable Thread dsmrDeviceThread;

    /**
     * Watchdog to check if messages received and restart if necessary.
     */
    private @Nullable ScheduledFuture<?> watchdog;

    /**
     * Number of nanoseconds after which a timeout is triggered when no messages received.
     */
    private long receivedTimeoutNanos;

    /**
     * Timestamp in nanoseconds of last P1 telegram received
     */
    private volatile long telegramReceivedTimeNanos;

    /**
     * Constructor
     *
     * @param bridge the Bridge ThingType
     * @param serialPortManager The Serial port manager
     */
    public DSMRBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    /**
     * The {@link DSMRBridgeHandler} does not support handling commands.
     *
     * @param channelUID the {@link ChannelUID} of the channel to which the command was sent
     * @param command the {@link Command}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
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
        DSMRDeviceConfiguration deviceConfig = getConfigAs(DSMRDeviceConfiguration.class);

        logger.trace("Using configuration {}", deviceConfig);
        updateStatus(ThingStatus.UNKNOWN);
        receivedTimeoutNanos = TimeUnit.SECONDS.toNanos(deviceConfig.receivedTimeout);
        try {
            DSMRDevice dsmrDevice = createDevice(deviceConfig);
            resetLastReceivedState();
            this.dsmrDevice = dsmrDevice; // otherwise Eclipse will give a null pointer error on the next line :-(
            dsmrDeviceRunnable = new DSMRDeviceRunnable(dsmrDevice, this);
            dsmrDeviceThread = new Thread(dsmrDeviceRunnable);
            dsmrDeviceThread.start();
            watchdog = scheduler.scheduleWithFixedDelay(this::alive, receivedTimeoutNanos, receivedTimeoutNanos,
                TimeUnit.NANOSECONDS);
        } catch (IllegalArgumentException e) {
            logger.debug("Incomplete configuration: {}", deviceConfig);

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/error.configuration.incomplete");
        }
    }

    /**
     * Creates the {@link DSMRDevice} that corresponds with the user specified configuration.
     *
     * @param deviceConfig device configuration
     * @return Specific {@link DSMRDevice} instance or throws an {@link IllegalArgumentException} if no valid
     *         configuration was set.
     */
    private DSMRDevice createDevice(DSMRDeviceConfiguration deviceConfig) {
        DSMRDevice dsmrDevice;

        if (deviceConfig.isSerialFixedSettings()) {
            dsmrDevice = new DSMRFixedConfigDevice(serialPortManager, deviceConfig.serialPort,
                DSMRSerialSettings.getPortSettingsFromConfiguration(deviceConfig), this);
        } else {
            dsmrDevice = new DSMRSerialAutoDevice(serialPortManager, deviceConfig.serialPort, this, scheduler,
                deviceConfig.receivedTimeout);
        }
        return dsmrDevice;
    }

    /**
     * Registers a meter listener.
     *
     * @param meterListener the meter discovery listener to add
     * @return true if listener is added, false otherwise
     */
    public boolean registerDSMRMeterListener(P1TelegramListener meterListener) {
        logger.trace("Register DSMRMeterListener");
        return meterListeners.add(meterListener);
    }

    /**
     * Unregisters a meter listener
     *
     * @param meterListener the meter discovery listener to remove
     * @return true is listener is removed, false otherwise
     */
    public boolean unregisterDSMRMeterListener(P1TelegramListener meterListener) {
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
        long deltaLastReceived = System.nanoTime() - telegramReceivedTimeNanos;

        if (deltaLastReceived > receivedTimeoutNanos) {
            logger.debug("No data received for {} seconds, restarting port if possible.",
                TimeUnit.NANOSECONDS.toSeconds(deltaLastReceived));
            if (dsmrDeviceRunnable != null) {
                dsmrDeviceRunnable.restart();
            }
            if (deltaLastReceived > receivedTimeoutNanos * OFFLINE_TIMEOUT_FACTOR) {
                logger.trace("Setting device offline if not yet done, and reset last received time.");
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    deviceOffline(ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.bridge.nodata");
                }
                resetLastReceivedState();
            }
        }
    }

    /**
     * Sets the last received time of messages to the current time.
     */
    private void resetLastReceivedState() {
        telegramReceivedTimeNanos = System.nanoTime();
        logger.trace("Telegram received time set: {}", telegramReceivedTimeNanos);
    }

    @Override
    public synchronized void handleTelegramReceived(P1Telegram telegram) {
        if (telegram.getCosemObjects().isEmpty()) {
            logger.debug("Parsing worked but something went wrong, so there were no CosemObjects:{}",
                telegram.getTelegramState().stateDetails);
            deviceOffline(ThingStatusDetail.COMMUNICATION_ERROR, telegram.getTelegramState().stateDetails);
        } else {
            resetLastReceivedState();
            meterValueReceived(telegram);
        }
    }

    @Override
    public void handleErrorEvent(DSMRConnectorErrorEvent portEvent) {
        if (portEvent != DSMRConnectorErrorEvent.READ_ERROR) {
            deviceOffline(ThingStatusDetail.CONFIGURATION_ERROR, portEvent.getEventDetails());
        }
    }

    /**
     * Method to forward the last received messages to the bound meters and to the meterListeners.
     *
     * @param telegram received meter values.
     */
    private void meterValueReceived(P1Telegram telegram) {
        updateStatus(ThingStatus.ONLINE);
        getThing().getThings().forEach(child -> {
            if (logger.isTraceEnabled()) {
                logger.trace("Update child:{} with {} objects", child.getThingTypeUID().getId(),
                    telegram.getCosemObjects().size());
            }
            DSMRMeterHandler dsmrMeterHandler = (DSMRMeterHandler) child.getHandler();

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
    public void setLenientMode(boolean lenientMode) {
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
    private void deviceOffline(ThingStatusDetail status, String details) {
        updateStatus(ThingStatus.OFFLINE, status, details);
    }
}
