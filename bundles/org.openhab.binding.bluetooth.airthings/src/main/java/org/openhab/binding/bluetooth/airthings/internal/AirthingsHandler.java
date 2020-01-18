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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirthingsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class AirthingsHandler extends BeaconBluetoothHandler {

    private static final String DATA_UUID = "b42e2a68-ade7-11e4-89d3-123b93f75cba";
    private static final int EXPECTED_DATA_LEN = 20;

    private final Logger logger = LoggerFactory.getLogger(AirthingsHandler.class);
    private final UUID uuid = UUID.fromString(DATA_UUID);

    private volatile Boolean servicesResolved = false;
    private @NonNullByDefault({}) AirthingsConfiguration configuration;
    private @NonNullByDefault({}) ScheduledFuture<?> scheduledTask;

    public AirthingsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        configuration = getConfigAs(AirthingsConfiguration.class);
        logger.debug("Using configuration: {}", configuration.toString());
        scheduledTask = scheduler.scheduleWithFixedDelay(this::connect, 30, configuration.refreshInterval,
                TimeUnit.SECONDS);
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
            disconnect();
            servicesResolved = false;
        }
    }

    private void connect() {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            logger.debug("Connect to device {}...", address);
            if (device.connect()) {
                logger.debug("Connection started to device {}", address);
            } else {
                logger.debug("Connection couldn't be started to device {}", address);
            }
        } else {
            logger.debug("Skip connect to device {} as it is offline", address);
        }
    }

    private void disconnect() {
        logger.debug("Disconnect from device {}...", address);
        if (device.disconnect()) {
            logger.debug("Disconnect started to device {}", address);
        } else {
            logger.debug("Disconnect couldn't be started to device {}", address);
        }
    }

    private void readDevice() {
        synchronized (servicesResolved) {
            if (!servicesResolved) {
                logger.debug("Discover services for device {}", address);
                if (!device.discoverServices()) {
                    logger.debug("Error while discovering services");
                    disconnect();
                }
            } else {
                logger.debug("Read data from device {}...", address);
                BluetoothCharacteristic characteristic = device.getCharacteristic(uuid);
                if (device.readCharacteristic(characteristic)) {
                    logger.debug("Read started for device {}", address);
                } else {
                    logger.debug("Read failed for device {}", address);
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Read failed");
                    disconnect();
                }
            }
        }
    }

    @Override
    public void onServicesDiscovered() {
        scheduler.submit(() -> {
            synchronized (servicesResolved) {
                if (!servicesResolved) {
                    servicesResolved = true;
                    logger.debug("Service discovery completed for device {}", address);
                }
            }
            for (BluetoothService service : device.getServices()) {
                logger.debug("Service '{}'", service);
            }
            readDevice();
        });
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        scheduler.submit(() -> {
            switch (connectionNotification.getConnectionState()) {
                case DISCOVERED:
                    logger.debug("Device {} DISCOVERED", address);
                    break;
                case CONNECTED:
                    logger.debug("Device {} CONNECTED", address);
                    readDevice();
                    break;
                case DISCONNECTED:
                    logger.debug("Device {} DISCONNECTED", address);
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public void onCharacteristicReadComplete(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status) {
        scheduler.submit(() -> {
            if (status == BluetoothCompletionStatus.SUCCESS) {
                logger.debug("Characteristic {} from {} has been read - value {}", characteristic.getUuid(), address,
                        characteristic.getValue());
                updateStatus(ThingStatus.ONLINE);
                parseDataAndUpdateChannels(characteristic.getValue());
            } else {
                logger.debug("Characteristic {} from {} has been read - ERROR", characteristic.getUuid(), address);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No response from device");
                return;
            }
            disconnect();
        });
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
        logger.debug("Characteristic {} from {} has been updated {}", characteristic.getUuid(), address,
                characteristic.getValue());
    }

    private void parseDataAndUpdateChannels(int[] rawData) {
        try {
            if (rawData.length == EXPECTED_DATA_LEN) {
                int sensor_version = rawData[0];
                if (sensor_version == 1) {
                    double humidity = rawData[1] / 2.0D;
                    int radon_short_term_avg = (rawData[5] & 0xFF) << 8 | (rawData[4] & 0xFF);
                    int radon_long_term_avg = (rawData[7] & 0xFF) << 8 | (rawData[6] & 0xFF);
                    double temperature = ((rawData[9] & 0xFF) << 8 | (rawData[8] & 0xFF)) / 100.0D;
                    double pressure = ((rawData[11] & 0xFF) << 8 | (rawData[10] & 0xFF)) / 50.0D;
                    int co2 = (rawData[13] & 0xFF) << 8 | (rawData[12] & 0xFF);
                    int voc = (rawData[15] & 0xFF) << 8 | (rawData[14] & 0xFF);
                    logger.debug(
                            "Data from {}: humidity={} %rH, radon_short_term_avg={} Bq/m3, radon_long_term_avg={} Bq/m3, temperature={} degC, pressure={} hPa, co2={} ppm, voc={} ppb",
                            address, humidity, radon_short_term_avg, radon_long_term_avg, temperature, pressure, co2,
                            voc);

                    updateState(CHANNEL_ID_HUMIDITY, new DecimalType(humidity));
                    updateState(CHANNEL_ID_TEMPERATURE, new DecimalType(temperature));
                    updateState(CHANNEL_ID_PRESSURE, new DecimalType(pressure));
                    updateState(CHANNEL_ID_CO2, new DecimalType(voc));
                    updateState(CHANNEL_ID_VOC, new DecimalType(humidity));
                    updateState(CHANNEL_ID_RADON_ST_AVG, new DecimalType(radon_short_term_avg));
                    updateState(CHANNEL_ID_RADON_LT_AVG, new DecimalType(radon_long_term_avg));
                } else {
                    logger.debug("Unknowm data structure version {} from {}", sensor_version, address);
                }
            } else {
                logger.debug("Illegal data structure len {} from {}", rawData.length, address);
            }
        } catch (RuntimeException e) {
            logger.debug("Error occured when parsing data from {}", address, e);
        }
    }
}
