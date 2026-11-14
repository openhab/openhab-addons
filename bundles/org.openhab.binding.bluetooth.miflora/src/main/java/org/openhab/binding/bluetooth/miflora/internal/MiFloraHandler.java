/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.miflora.internal;

import static org.openhab.binding.bluetooth.miflora.internal.MiFloraBindingConstants.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.miflora.internal.MiBeaconParser.Frame;
import org.openhab.binding.bluetooth.miflora.internal.MiBeaconParser.Reading;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.core.library.dimension.ElectricConductivity;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MiFloraHandler} decodes the readings a Mi Flora sensor broadcasts. It never connects to the sensor.
 *
 * @author Lukas Brinias - Initial contribution
 */
@NonNullByDefault
public class MiFloraHandler extends BeaconBluetoothHandler {

    private static final String SERVICE_UUID = "0000fe95-0000-1000-8000-00805f9b34fb";
    private static final String SERVICE_UUID_SHORT = "fe95";
    private static final Unit<ElectricConductivity> MICROSIEMENS_PER_CENTIMETRE = MetricPrefix.MICRO(Units.SIEMENS)
            .divide(MetricPrefix.CENTI(SIUnits.METRE)).asType(ElectricConductivity.class);
    private static final List<String> READING_CHANNELS = List.of(CHANNEL_ID_TEMPERATURE, CHANNEL_ID_ILLUMINANCE,
            CHANNEL_ID_MOISTURE, CHANNEL_ID_CONDUCTIVITY);

    private final Logger logger = LoggerFactory.getLogger(MiFloraHandler.class);

    private Duration dataTimeout = Duration.ofMinutes(10);
    private volatile Instant lastReading = Instant.now();
    // Each frame is repeated several times; the counter only changes with a new reading
    private final AtomicInteger lastFrameCounter = new AtomicInteger(-1);
    private @Nullable ScheduledFuture<?> timeoutJob;

    public MiFloraHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            return;
        }
        dataTimeout = Duration.ofMinutes(Math.max(1, getConfigAs(MiFloraConfiguration.class).dataTimeout));
        lastReading = Instant.now();
        lastFrameCounter.set(-1);
        timeoutJob = scheduler.scheduleWithFixedDelay(this::checkTimeout, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        try {
            super.dispose();
        } finally {
            ScheduledFuture<?> job = timeoutJob;
            if (job != null) {
                job.cancel(true);
                timeoutJob = null;
            }
        }
    }

    private void checkTimeout() {
        ThingStatus status = getThing().getStatus();
        // UNKNOWN too, so a sensor that is never heard after initialization goes offline as well
        if ((status == ThingStatus.ONLINE || status == ThingStatus.UNKNOWN)
                && Duration.between(lastReading, Instant.now()).compareTo(dataTimeout) > 0) {
            READING_CHANNELS.forEach(channel -> updateState(channel, UnDefType.UNDEF));
            lastFrameCounter.set(-1);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.no-readings");
        }
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        byte[] data = serviceData(scanNotification.getServiceData());
        if (data == null) {
            // RSSI updates and scan responses carry no MiBeacon data
            return;
        }
        Frame frame = MiBeaconParser.parse(data);
        if (frame == null) {
            logger.debug("Ignoring MiBeacon data from {} that is not a plain reading", address);
            return;
        }
        super.onScanRecordReceived(scanNotification);
        lastReading = Instant.now();
        if (lastFrameCounter.getAndSet(frame.frameCounter()) == frame.frameCounter()) {
            return;
        }
        for (Reading reading : frame.readings()) {
            Map.Entry<String, State> channelState = toChannelState(reading);
            if (channelState == null) {
                logger.debug("Ignoring MiBeacon object {} from {}", Integer.toHexString(reading.type()), address);
            } else if (isLinked(channelState.getKey())) {
                updateState(channelState.getKey(), channelState.getValue());
            }
        }
    }

    /**
     * Maps a reading to its channel and state.
     *
     * @param reading the decoded reading
     * @return the channel ID and its new state, or null for an object type a Mi Flora doesn't send
     */
    static Map.@Nullable Entry<String, State> toChannelState(Reading reading) {
        long value = reading.value();
        return switch (reading.type()) {
            case MiBeaconParser.OBJECT_TEMPERATURE ->
                Map.entry(CHANNEL_ID_TEMPERATURE, new QuantityType<>(value / 10.0, SIUnits.CELSIUS));
            case MiBeaconParser.OBJECT_ILLUMINANCE ->
                Map.entry(CHANNEL_ID_ILLUMINANCE, new QuantityType<>(value, Units.LUX));
            case MiBeaconParser.OBJECT_MOISTURE ->
                Map.entry(CHANNEL_ID_MOISTURE, new QuantityType<>(value, Units.PERCENT));
            case MiBeaconParser.OBJECT_CONDUCTIVITY ->
                Map.entry(CHANNEL_ID_CONDUCTIVITY, new QuantityType<>(value, MICROSIEMENS_PER_CENTIMETRE));
            default -> null;
        };
    }

    private static byte @Nullable [] serviceData(Map<String, byte[]> serviceData) {
        for (Map.Entry<String, byte[]> entry : serviceData.entrySet()) {
            String uuid = entry.getKey();
            if (SERVICE_UUID.equalsIgnoreCase(uuid) || SERVICE_UUID_SHORT.equalsIgnoreCase(uuid)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
