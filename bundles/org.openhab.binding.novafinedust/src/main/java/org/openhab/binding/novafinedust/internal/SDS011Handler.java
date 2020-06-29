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
package org.openhab.binding.novafinedust.internal;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.dimension.Density;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.novafinedust.internal.sds011protocol.SDS011Communicator;
import org.openhab.binding.novafinedust.internal.sds011protocol.WorkMode;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.SensorMeasuredDataReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SDS011Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stefan Triller - Initial contribution
 */
@NonNullByDefault
public class SDS011Handler extends BaseThingHandler {
    private static final Duration CONNECTION_MONITOR_START_DELAY_OFFSET = Duration.ofSeconds(10);

    private final Logger logger = LoggerFactory.getLogger(SDS011Handler.class);
    private final SerialPortManager serialPortManager;

    private NovaFineDustConfiguration config = new NovaFineDustConfiguration();
    private @Nullable SDS011Communicator communicator;

    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> connectionMonitor;

    private ZonedDateTime lastCommunication = ZonedDateTime.now();

    // initialize timeBetweenDataShouldArrive with a large number
    private Duration timeBetweenDataShouldArrive = Duration.ofDays(1);
    private final Duration dataCanBeLateTolerance = Duration.ofSeconds(5);

    // cached values for refresh command
    private State statePM10 = UnDefType.UNDEF;
    private State statePM25 = UnDefType.UNDEF;

    public SDS011Handler(Thing thing, SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // refresh channels with last received values from cache
        if (RefreshType.REFRESH.equals(command)) {
            if (NovaFineDustBindingConstants.CHANNEL_PM25.equals(channelUID.getId()) && statePM25 != UnDefType.UNDEF) {
                updateState(NovaFineDustBindingConstants.CHANNEL_PM25, statePM25);
            }
            if (NovaFineDustBindingConstants.CHANNEL_PM10.equals(channelUID.getId()) && statePM10 != UnDefType.UNDEF) {
                updateState(NovaFineDustBindingConstants.CHANNEL_PM10, statePM10);
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        config = getConfigAs(NovaFineDustConfiguration.class);

        if (!validateConfiguration()) {
            return;
        }

        // parse ports and if the port is found, initialize the reader
        SerialPortIdentifier portId = serialPortManager.getIdentifier(config.port);
        if (portId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port is not known!");
            return;
        }

        this.communicator = new SDS011Communicator(this, portId);

        if (config.reporting) {
            timeBetweenDataShouldArrive = Duration.ofMinutes(config.reportingInterval);
            scheduler.submit(() -> initializeCommunicator(WorkMode.REPORTING, timeBetweenDataShouldArrive));
        } else {
            timeBetweenDataShouldArrive = Duration.ofSeconds(config.pollingInterval);
            scheduler.submit(() -> initializeCommunicator(WorkMode.POLLING, timeBetweenDataShouldArrive));
        }

        Duration connectionMonitorStartDelay = timeBetweenDataShouldArrive.plus(CONNECTION_MONITOR_START_DELAY_OFFSET);
        connectionMonitor = scheduler.scheduleWithFixedDelay(this::verifyIfStillConnected,
                connectionMonitorStartDelay.getSeconds(), timeBetweenDataShouldArrive.getSeconds(), TimeUnit.SECONDS);
    }

    private void initializeCommunicator(WorkMode mode, Duration interval) {
        SDS011Communicator localCommunicator = communicator;
        if (localCommunicator == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not create communicator instance");
            return;
        }

        boolean initSuccessful = false;
        try {
            initSuccessful = localCommunicator.initialize(mode, interval);
        } catch (final IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "I/O error!");
            return;
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Port is in use!");
            return;
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Cannot attach listener to port!");
            return;
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Cannot set serial port parameters");
            return;
        }

        if (initSuccessful) {
            lastCommunication = ZonedDateTime.now();
            updateStatus(ThingStatus.ONLINE);

            if (mode == WorkMode.POLLING) {
                pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                    try {
                        localCommunicator.requestSensorData();
                    } catch (IOException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                "Cannot query data from device");
                    }
                }, 2, config.pollingInterval, TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Commands and replies from the device don't seem to match");
            logger.debug("Could not configure sensor -> setting Thing to OFFLINE and disposing the handler");
            dispose();
        }
    }

    private boolean validateConfiguration() {
        if (config.port.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set!");
            return false;
        }
        if (config.reporting) {
            if (config.reportingInterval < 0 || config.reportingInterval > 30) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Reporting interval has to be between 0 and 30 minutes");
                return false;
            }
        } else {
            if (config.pollingInterval < 3 || config.pollingInterval > 3600) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Polling interval has to be between 3 and 3600 seconds");
                return false;
            }
        }
        return true;
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localPollingJob = this.pollingJob;
        if (localPollingJob != null) {
            localPollingJob.cancel(true);
            this.pollingJob = null;
        }

        ScheduledFuture<?> localConnectionMonitor = this.connectionMonitor;
        if (localConnectionMonitor != null) {
            localConnectionMonitor.cancel(true);
            this.connectionMonitor = null;
        }

        SDS011Communicator localCommunicator = this.communicator;
        if (localCommunicator != null) {
            localCommunicator.dispose();
        }

        this.statePM10 = UnDefType.UNDEF;
        this.statePM25 = UnDefType.UNDEF;
    }

    /**
     * Pass the data from the device to the Thing channels
     *
     * @param sensorData the parsed data from the sensor
     */
    public void updateChannels(SensorMeasuredDataReply sensorData) {
        if (sensorData.isValidData()) {
            logger.debug("Updating channels with data: {}", sensorData);

            QuantityType<Density> statePM10 = new QuantityType<>(sensorData.getPm10(),
                    SmartHomeUnits.MICROGRAM_PER_CUBICMETRE);
            updateState(NovaFineDustBindingConstants.CHANNEL_PM10, statePM10);
            this.statePM10 = statePM10;

            QuantityType<Density> statePM25 = new QuantityType<>(sensorData.getPm25(),
                    SmartHomeUnits.MICROGRAM_PER_CUBICMETRE);
            updateState(NovaFineDustBindingConstants.CHANNEL_PM25, statePM25);
            this.statePM25 = statePM25;

            updateStatus(ThingStatus.ONLINE);
        }
        // there was a communication, even if the data was not valid, thus resetting the value here
        lastCommunication = ZonedDateTime.now();
    }

    private void verifyIfStillConnected() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime lastData = lastCommunication.plus(timeBetweenDataShouldArrive).plus(dataCanBeLateTolerance);
        if (now.isAfter(lastData)) {
            logger.debug("Check Alive timer: Timeout: lastCommunication={}, interval={}, tollerance={}",
                    lastCommunication, timeBetweenDataShouldArrive, dataCanBeLateTolerance);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Check connection cable and afterwards disable and enable this thing to make it work again");
            // in case someone has pulled the plug, we dispose ourselves and the user has to deactivate/activate the
            // thing once the cable is plugged in again
            dispose();
        } else {
            logger.trace("Check Alive timer: All OK: lastCommunication={}, interval={}, tollerance={}",
                    lastCommunication, timeBetweenDataShouldArrive, dataCanBeLateTolerance);
        }
    }

    /**
     * Set the firmware property on the Thing
     *
     * @param firmwareVersion the firmware version as a String
     */
    public void setFirmware(String firmwareVersion) {
        updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
    }
}
