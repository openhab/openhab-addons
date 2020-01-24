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
package org.openhab.binding.bluetooth.airthings.internal;

import static org.openhab.binding.bluetooth.airthings.internal.AirthingsBindingConstants.*;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirthingsWavePlusHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class AirthingsWavePlusHandler extends BeaconBluetoothHandler {

    private static final String DATA_UUID = "b42e2a68-ade7-11e4-89d3-123b93f75cba";

    private final Logger logger = LoggerFactory.getLogger(AirthingsWavePlusHandler.class);
    private final UUID uuid = UUID.fromString(DATA_UUID);

    private volatile Boolean servicesResolved = false;
    private @NonNullByDefault({}) AirthingsConfiguration configuration;
    private @NonNullByDefault({}) ScheduledFuture<?> scheduledTask;

    public AirthingsWavePlusHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        configuration = getConfigAs(AirthingsConfiguration.class);
        logger.debug("Using configuration: {}", configuration);
    }

    @Override
    public void dispose() {
        try {
            super.dispose();
        } finally {
            if (scheduledTask != null) {
                scheduledTask.cancel(true);
                scheduledTask = null;
            }
            servicesResolved = false;
        }
    }

    private void startScheduledTask() {
        if (scheduledTask == null) {
            logger.debug("Start scheduled task to read device in every {} seconds", configuration.refreshInterval);
            scheduledTask = scheduler.scheduleWithFixedDelay(this::execute, 10, configuration.refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    private void execute() {
        ConnectionState connectionState = device.getConnectionState();
        logger.debug("Device {} state is {}", address, connectionState);
        switch (connectionState) {
            case DISCONNECTED:
                connect();
                break;
            case CONNECTED:
                synchronized (servicesResolved) {
                    if (servicesResolved) {
                        read();
                    } else {
                        discoverServices();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void connect() {
        logger.debug("Connect to device {}...", address);
        if (!device.connect()) {
            logger.debug("Connecting to device {} failed", address);
        }
    }

    private void disconnect() {
        logger.debug("Disconnect from device {}...", address);
        if (!device.disconnect()) {
            logger.debug("Disconnect from device {} failed", address);
        }
    }

    private void discoverServices() {
        logger.debug("Discover services for device {}", address);
        if (!device.discoverServices()) {
            logger.debug("Discovering services failed");
            disconnect();
        }
    }

    private void read() {
        logger.debug("Read data from device {}...", address);
        BluetoothCharacteristic characteristic = device.getCharacteristic(uuid);
        if (!device.readCharacteristic(characteristic)) {
            logger.debug("Read data from device {} failed", address);
            disconnect();
        }
    }

    private void printServices() {
        if (logger.isDebugEnabled()) {
            for (BluetoothService service : device.getServices()) {
                logger.debug("Device {} Service '{}'", address, service);
            }
        }
    }

    @Override
    public void onServicesDiscovered() {
        synchronized (servicesResolved) {
            servicesResolved = true;
        }
        logger.debug("Service discovery completed for device {}", address);
        printServices();
        execute();
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        switch (connectionNotification.getConnectionState()) {
            case DISCOVERED:
                logger.debug("Device {} DISCOVERED", address);
                startScheduledTask();
                break;
            case CONNECTED:
                logger.debug("Device {} CONNECTED", address);
                execute();
                break;
            case DISCONNECTED:
                logger.debug("Device {} DISCONNECTED", address);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCharacteristicReadComplete(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status) {
        try {
            if (status == BluetoothCompletionStatus.SUCCESS) {
                logger.debug("Characteristic {} from device {}: {}", characteristic.getUuid(), address,
                        characteristic.getValue());
                updateStatus(ThingStatus.ONLINE);
                try {
                    updateChannels(new AirthingsWavePlusDataParser(characteristic.getValue()));
                } catch (AirthingsParserException e) {
                    logger.warn("Data parsing error occured, when parsing data from device {}, cause {}", address,
                            e.getMessage(), e);
                }
            } else {
                logger.debug("Characteristic {} from device {} failed", characteristic.getUuid(), address);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No response from device");
            }
        } finally {
            disconnect();
        }
    }

    private void updateChannels(AirthingsWavePlusDataParser parser) {
        logger.debug("Parsed data: {}", parser);
        updateState(CHANNEL_ID_HUMIDITY,
                QuantityType.valueOf(Double.valueOf(parser.getHumidity()), SmartHomeUnits.PERCENT));
        updateState(CHANNEL_ID_TEMPERATURE,
                QuantityType.valueOf(Double.valueOf(parser.getTemperature()), SIUnits.CELSIUS));
        updateState(CHANNEL_ID_PRESSURE,
                QuantityType.valueOf(Double.valueOf(parser.getPressure()), SmartHomeUnits.MILLIBAR));
        updateState(CHANNEL_ID_CO2,
                QuantityType.valueOf(Double.valueOf(parser.getCo2()), SmartHomeUnits.PARTS_PER_MILLION));
        updateState(CHANNEL_ID_TVOC, QuantityType.valueOf(Double.valueOf(parser.getTvoc()), PARTS_PER_BILLION));
        updateState(CHANNEL_ID_RADON_ST_AVG,
                QuantityType.valueOf(Double.valueOf(parser.getRadonShortTermAvg()), BECQUEREL_PER_CUBIC_METRE));
        updateState(CHANNEL_ID_RADON_LT_AVG,
                QuantityType.valueOf(Double.valueOf(parser.getRadonLongTermAvg()), BECQUEREL_PER_CUBIC_METRE));
    }
}
