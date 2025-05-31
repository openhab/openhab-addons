/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.listener.internal;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.measure.MetricPrefix;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ListenerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author VitaTucek - Initial contribution
 */
@NonNullByDefault
public class ListenerHandler extends BeaconBluetoothHandler {
    private final Logger logger = LoggerFactory.getLogger(ListenerHandler.class);
    private @Nullable ListenerConfiguration config;
    private final AtomicBoolean receivedStatus = new AtomicBoolean();
    private @NonNullByDefault({}) ScheduledFuture<?> heartbeatFuture;

    public ListenerHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        super.initialize();
        config = getConfigAs(ListenerConfiguration.class);
        // is OFFLINE -> configuration error -> no heartbeat needed
        if (getThing().getStatus() != ThingStatus.OFFLINE) {
            heartbeatFuture = scheduler.scheduleWithFixedDelay(this::heartbeat, 0, config.dataTimeout,
                    TimeUnit.MINUTES);
        }
    }

    /**
     * Check device connection timeout
     */
    @SuppressWarnings("null")
    private void heartbeat() {
        synchronized (receivedStatus) {
            if (!receivedStatus.getAndSet(false) && getThing().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("No data received for %d minute", config.dataTimeout));
                scanTime = 0;
            }
        }
    }

    @Override
    public void dispose() {
        try {
            super.dispose();
        } finally {
            if (heartbeatFuture != null) {
                heartbeatFuture.cancel(true);
                heartbeatFuture = null;
            }
        }
    }

    long scanTime = 0;

    @SuppressWarnings("null")
    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        synchronized (receivedStatus) {
            receivedStatus.set(true);
            super.onScanRecordReceived(scanNotification);
            // RSSI boundaries - not all notifications report RSSI value
            if (scanNotification.getRssi() > Integer.MIN_VALUE) {
                if (scanTime > 0) {
                    long period = System.currentTimeMillis() - scanTime;
                    updateProperty(ListenerBindingConstants.PROPERTY_ADVERTISING_INTERVAL, String.valueOf(period));
                    QuantityType<Time> quantity = new QuantityType<Time>(period,
                            Units.SECOND.prefix(MetricPrefix.MILLI));
                    updateState(ListenerBindingConstants.CHANNEL_TYPE_INTERVAL, quantity);
                }
                scanTime = System.currentTimeMillis();
            }
            // manufacturer data
            if (scanNotification.getManufacturerData().length != 0) {
                // assign manufacturer data
                byte[] data = scanNotification.getManufacturerData();

                for (var channel : getThing().getChannels()) {
                    ChannelTypeUID channelType = channel.getChannelTypeUID();
                    if (channelType == null) {
                        continue;
                    }
                    // looking for service channel
                    if (!channelType.getId().equals(ListenerBindingConstants.CHANNEL_TYPE_MANUFACTURER_NUMBER)
                            && !channelType.getId().equals(ListenerBindingConstants.CHANNEL_TYPE_MANUFACTURER_RAW)) {
                        continue;
                    }
                    // get channel properties
                    var properties = channel.getConfiguration().getProperties();
                    // retrieve properties values
                    var index = properties.get(ListenerBindingConstants.PARAMETER_DATA_BEGIN);
                    var datalength = properties.get(ListenerBindingConstants.PARAMETER_DATA_LENGTH);
                    var multiplicator = properties.get(ListenerBindingConstants.PARAMETER_MULTIPLICATOR);
                    var payloadLength = properties.get(ListenerBindingConstants.PARAMETER_PAYLOAD_LENGTH);
                    int indexInt = (index == null) ? 0 : ((BigDecimal) index).intValue();
                    int lengthInt = (datalength == null) ? 2 : ((BigDecimal) datalength).intValue();
                    float multiplicatorFloat = (multiplicator == null) ? 1.0f
                            : ((BigDecimal) multiplicator).floatValue();
                    int payloadLengthInt = (payloadLength == null) ? 0 : ((BigDecimal) payloadLength).intValue();
                    // get data for number configured channel
                    if (channelType.getId().equals(ListenerBindingConstants.CHANNEL_TYPE_MANUFACTURER_NUMBER)) {
                        if (lengthInt != 1 && lengthInt != 2 && lengthInt != 4 && lengthInt != 8) {
                            logger.warn("Thing {}: Channel '{}' has set unsupported data length to {}",
                                    getThing().getLabel(), channel.getUID().getId(), lengthInt);
                            continue;
                        }
                        if (payloadLengthInt > 0 && payloadLengthInt != data.length) {
                            logger.debug(
                                    "Thing {}, Channel {}: Manufacturer data payload length {}B is different from expected lenght {}B",
                                    getThing().getLabel(), channel.getUID().getId(), data.length, payloadLengthInt);
                            continue;
                        }
                        DecimalType value = getDecimalValue(data, indexInt, lengthInt, multiplicatorFloat,
                                config.changeByteOrder);
                        if (value != null) {
                            updateState(channel.getUID(), value);
                        }
                        // get raw data for configured channel
                    } else if (channelType.getId().equals(ListenerBindingConstants.CHANNEL_TYPE_MANUFACTURER_RAW)) {
                        StringType value = getRawValue(data, indexInt, lengthInt);
                        updateState(channel.getUID(), value);
                    }
                }
                if (config != null && config.autoChannelCreation) {
                    ChannelUID channelUID = new ChannelUID(getThing().getUID(), "manufacturer-data");
                    ThingBuilder builder = editThing();
                    if (getThing().getChannel(channelUID) == null) {
                        Configuration configuration = new Configuration();
                        configuration.put(ListenerBindingConstants.PARAMETER_DATA_BEGIN, 0);
                        configuration.put(ListenerBindingConstants.PARAMETER_DATA_LENGTH, 0);
                        var channel = ChannelBuilder.create(channelUID, "String")
                                .withType(new ChannelTypeUID(BluetoothBindingConstants.BINDING_ID,
                                        ListenerBindingConstants.CHANNEL_TYPE_MANUFACTURER_RAW))
                                .withLabel("Manufacturer raw data").withConfiguration(configuration).build();
                        builder.withChannel(channel);
                        updateThing(builder.build());
                    }
                    updateState(channelUID,
                            new StringType("0x" + HexUtils.bytesToHex(scanNotification.getManufacturerData())));
                }
            }
            // services
            for (var service : scanNotification.getServiceData().entrySet()) {
                logger.debug("Thing {}: Service notification UUID/value={}/0x{}", getThing().getLabel(),
                        service.getKey(), HexUtils.bytesToHex(service.getValue()));

                String uuid = service.getKey();
                byte[] data = service.getValue();

                if (uuid.endsWith("-0000-1000-8000-00805f9b34fb")) {
                    if (uuid.startsWith("0000")) {
                        uuid = uuid.substring(4, 8);
                    } else {
                        uuid = uuid.substring(0, 8);
                    }
                } else if (uuid.endsWith("-8000-00805f9b34fb")) {
                    uuid = uuid.substring(0, 18);
                }
                for (var channel : getThing().getChannels()) {
                    ChannelTypeUID channelType = channel.getChannelTypeUID();
                    if (channelType == null) {
                        continue;
                    }
                    // looking for service channel
                    if (!channelType.getId().equals(ListenerBindingConstants.CHANNEL_TYPE_SERVICE_NUMBER)
                            && !channelType.getId().equals(ListenerBindingConstants.CHANNEL_TYPE_SERVICE_RAW)) {
                        continue;
                    }
                    // get channel properties
                    var properties = channel.getConfiguration().getProperties();
                    // get UUID property
                    var channelUuid = properties.get(ListenerBindingConstants.PARAMETER_DATA_UUID);
                    // looking for channel with advertised UUID
                    if (!channelUuid.toString().equalsIgnoreCase(uuid)) {
                        continue;
                    }
                    // retrieve rest of properties
                    var index = properties.get(ListenerBindingConstants.PARAMETER_DATA_BEGIN);
                    var datalength = properties.get(ListenerBindingConstants.PARAMETER_DATA_LENGTH);
                    var multiplicator = properties.get(ListenerBindingConstants.PARAMETER_MULTIPLICATOR);
                    var payloadLength = properties.get(ListenerBindingConstants.PARAMETER_PAYLOAD_LENGTH);
                    int indexInt = (index == null) ? 0 : ((BigDecimal) index).intValue();
                    int lengthInt = (datalength == null) ? 2 : ((BigDecimal) datalength).intValue();
                    float multiplicatorFloat = (multiplicator == null) ? 1.0f
                            : ((BigDecimal) multiplicator).floatValue();
                    int payloadLengthInt = (payloadLength == null) ? 0 : ((BigDecimal) payloadLength).intValue();
                    // get data for number configured channel
                    if (channelType.getId().equals(ListenerBindingConstants.CHANNEL_TYPE_SERVICE_NUMBER)) {
                        if (lengthInt != 1 && lengthInt != 2 && lengthInt != 4 && lengthInt != 8) {
                            logger.warn("Thing {}: Channel '{}' has set unsupported data length to {}",
                                    getThing().getLabel(), channel.getUID().getId(), lengthInt);
                            continue;
                        }
                        if (payloadLengthInt > 0 && payloadLengthInt != data.length) {
                            logger.debug(
                                    "Thing {}, Channel {}: Service payload length {}B is different from expected lenght {}B",
                                    getThing().getLabel(), channel.getUID().getId(), data.length, payloadLengthInt);
                            continue;
                        }
                        DecimalType value = getDecimalValue(data, indexInt, lengthInt, multiplicatorFloat,
                                config.changeByteOrder);
                        if (value != null) {
                            updateState(channel.getUID(), value);
                        }
                        // get raw data for configured channel
                    } else if (channelType.getId().equals(ListenerBindingConstants.CHANNEL_TYPE_SERVICE_RAW)) {
                        StringType value = getRawValue(data, indexInt, lengthInt);
                        updateState(channel.getUID(), value);
                    }
                }
                // create channel if creation is enabled
                if (config != null && config.autoChannelCreation) {
                    ChannelUID channelUID = new ChannelUID(getThing().getUID(), "service-".concat(uuid));
                    ThingBuilder builder = editThing();
                    if (getThing().getChannel(channelUID) == null) {
                        Configuration configuration = new Configuration();
                        configuration.put(ListenerBindingConstants.PARAMETER_DATA_UUID, uuid);
                        configuration.put(ListenerBindingConstants.PARAMETER_DATA_BEGIN, 0);
                        configuration.put(ListenerBindingConstants.PARAMETER_DATA_LENGTH, 0);
                        var channel = ChannelBuilder.create(channelUID, "String")
                                .withType(new ChannelTypeUID(BluetoothBindingConstants.BINDING_ID,
                                        ListenerBindingConstants.CHANNEL_TYPE_SERVICE_RAW))
                                .withLabel("UUID " + uuid).withDescription("Service raw data")
                                .withConfiguration(configuration).build();
                        builder.withChannel(channel);
                        updateThing(builder.build());
                        updateState(channelUID, new StringType("0x" + HexUtils.bytesToHex(data)));
                    }
                }
            }
        }
    }

    /**
     * Extract decimal value from byte array
     *
     * @param data source array
     * @param index start index
     * @param length length of data
     * @param multiplicator result correct factor
     * @param binEndian byte order
     * @return decimal value
     */
    private @Nullable DecimalType getDecimalValue(byte[] data, int index, int length, float multiplicator,
            boolean binEndian) {
        if (data.length < index + length) {
            return null;
        }

        long value = 0;
        var buffer = ByteBuffer.wrap(data, index, length);
        if (binEndian) {
            buffer = buffer.order(ByteOrder.BIG_ENDIAN);
        } else {
            buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        if (length == 1) {
            value = buffer.get(index);
        } else if (length == 2) {
            value = buffer.getShort(index);
        } else if (length == 4) {
            value = buffer.getInt(index);
        } else if (length == 8) {
            value = buffer.getLong(index);
        }

        if (multiplicator != 1.0f) {
            return new DecimalType(value * multiplicator);
        } else {
            return new DecimalType(value);
        }
    }

    /**
     * Convert byte array into string
     *
     * @param data source array
     * @param index start index
     * @param length length of data
     * @return string representation of data
     */
    private StringType getRawValue(byte[] data, int index, int length) {
        int indexTo = data.length;
        if (length > 0) {
            indexTo = index + length;
        }
        return new StringType("0x" + HexUtils.bytesToHex(Arrays.copyOfRange(data, index, indexTo)));
    }

    @Override
    public void onServicesDiscovered() {
        super.onServicesDiscovered();
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic, byte[] value) {
        super.onCharacteristicUpdate(characteristic, value);
    }
}
