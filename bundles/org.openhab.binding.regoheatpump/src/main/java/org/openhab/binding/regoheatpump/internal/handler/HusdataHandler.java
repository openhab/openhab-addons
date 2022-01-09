/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.regoheatpump.internal.handler;

import static org.openhab.binding.regoheatpump.internal.RegoHeatPumpBindingConstants.*;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.regoheatpump.internal.protocol.RegoConnection;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HusdataHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Boris Krivonog - Initial contribution
 */
abstract class HusdataHandler extends BaseThingHandler {

    private static final Map<Integer, String> MAPPINGS;
    private final Logger logger = LoggerFactory.getLogger(HusdataHandler.class);
    private RegoConnection connection;
    private ScheduledFuture<?> scheduledRefreshFuture;
    private BufferedReader bufferedReader;

    static {
        MAPPINGS = mappings();
    }

    protected HusdataHandler(Thing thing) {
        super(thing);
    }

    protected abstract RegoConnection createConnection();

    @Override
    public void initialize() {
        bufferedReader = null;
        connection = createConnection();

        updateStatus(ThingStatus.UNKNOWN);

        scheduledRefreshFuture = scheduler.scheduleWithFixedDelay(this::handleDataFromHusdataInterface, 2, 1,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        super.dispose();

        if (scheduledRefreshFuture != null) {
            scheduledRefreshFuture.cancel(true);
            scheduledRefreshFuture = null;
        }

        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private synchronized void handleDataFromHusdataInterface() {
        RegoConnection connection = this.connection;
        if (connection == null) {
            return;
        }

        while (!Thread.interrupted()) {
            try {
                if (!connection.isConnected()) {
                    bufferedReader = null;
                    connection.connect();

                    // Request real-time registers.
                    logger.debug("Requesting read and dump of real-time registers.");
                    final OutputStream outputStream = connection.outputStream();
                    outputStream.write(new String("XR\r\n").getBytes());
                    outputStream.flush();
                }

                if (bufferedReader == null) {
                    bufferedReader = new BufferedReader(new InputStreamReader(connection.inputStream()));
                }

                final String line = bufferedReader.readLine();
                if (line == null) {
                    throw new EOFException();
                }

                if (line.isEmpty()) {
                    continue;
                }

                logger.debug("Got '{}'", line);

                processReceivedData(line);
            } catch (SocketTimeoutException e) {
                // Do nothing. Just happen to allow the thread to check if it has to stop.
                break;
            } catch (IOException e) {
                logger.warn("Processing request failed", e);

                bufferedReader = null;

                if (!Thread.interrupted()) {
                    connection.close();
                    updateStatus(ThingStatus.OFFLINE);
                }

                break;
            } catch (Exception e) {
                logger.warn("Error occurred during message waiting", e);
                break;
            }
        }

        // If state here is still unknown, than something went wrong so set thing status to OFFLINE.
        if (thing.getStatus() == ThingStatus.UNKNOWN) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private void processReceivedData(final String line) {
        if (line.length() != 10) {
            logger.debug("Unexpected length for '{}'", line);
            return;
        }

        if (line.charAt(0) != 'X' || line.charAt(1) != 'R') {
            logger.debug("Expecting XRxxxxxxxx but got '{}'", line);
            return;
        }

        int dataType = Integer.parseInt(line.substring(2, 3), 16);
        int register = Integer.parseInt(line.substring(3, 6), 16);
        int value = (short) (Integer.parseInt(line.substring(6, 8), 16) * 256
                + Integer.parseInt(line.substring(8, 10), 16));

        logger.debug("dataType = {}, register = {}, value = {}", dataType, register, value);

        updateStatus(ThingStatus.ONLINE);

        int channel = ((dataType & 0x0f) << 12) | (register & 0x0fff);
        String channelID = MAPPINGS.get(channel);
        if (channelID == null) {
            logger.debug("Unsupported register {}.", register);
            return;
        }

        if (!isLinked(channelID)) {
            logger.debug("Ignoring channel {} since it is not linked.", channelID);
            return;
        }

        switch (dataType) {
            case 0x00: // Degrees
                updateState(channelID, new QuantityType<>(value / 10.0, Units.DEGREE_ANGLE));
                break;

            case 0x02: // Number
                updateState(channelID, new DecimalType(value / 10.0));
                break;

            case 0x03: // Percent
                updateState(channelID, new QuantityType<>(value / 10.0, Units.PERCENT));
                break;

            case 0x04: // Ampere
                updateState(channelID, new QuantityType<>(value / 10.0, Units.AMPERE));
                break;

            case 0x05: // kWh
                updateState(channelID, new QuantityType<>(value / 10.0, Units.KILOWATT_HOUR));
                break;

            case 0x06: // Hours
                updateState(channelID, new QuantityType<>(value, Units.HOUR));
                break;

            case 0x07: // Minutes
                updateState(channelID, new QuantityType<>(value, Units.MINUTE));
                break;

            case 0x09: // kw
                updateState(channelID, new QuantityType<>(value, MetricPrefix.KILO(Units.WATT)));
                break;

            case 0x01: // Switch
            case 0x08: // Degree minutes
            case 0x0A: // Pulses (For S0 El-meter pulse counter)
                updateState(channelID, new DecimalType(value));
                break;

            default:
                logger.debug("Ignoring {} due unsupported data type {}.", channelID, dataType);
                break;
        }
    }

    private static Map<Integer, String> mappings() {
        final Map<Integer, String> mappings = new HashMap<>();
        {
            // Sensor values
            mappings.put(0x0001, CHANNEL_GROUP_SENSOR_VALUES + "radiatorReturn");
            mappings.put(0x0002, CHANNEL_GROUP_SENSOR_VALUES + "radiatorForward");
            mappings.put(0x0003, CHANNEL_GROUP_SENSOR_VALUES + "heatFluidIn");
            mappings.put(0x0004, CHANNEL_GROUP_SENSOR_VALUES + "heatFluidOut");
            mappings.put(0x0005, CHANNEL_GROUP_SENSOR_VALUES + "coldFluidIn");
            mappings.put(0x0006, CHANNEL_GROUP_SENSOR_VALUES + "coldFluidOut");
            mappings.put(0x0007, CHANNEL_GROUP_SENSOR_VALUES + "outdoor");
            mappings.put(0x0008, CHANNEL_GROUP_SENSOR_VALUES + "indoor");
            mappings.put(0x0009, CHANNEL_GROUP_SENSOR_VALUES + "hotWater");
            mappings.put(0x000A, CHANNEL_GROUP_SENSOR_VALUES + "externalHotWater");
            mappings.put(0x000B, CHANNEL_GROUP_SENSOR_VALUES + "compressor");
            mappings.put(0x000E, CHANNEL_GROUP_SENSOR_VALUES + "airIntake");
            mappings.put(0x0011, CHANNEL_GROUP_SENSOR_VALUES + "pool");

            // Control data
            mappings.put(0x3104, CHANNEL_GROUP_CONTROL_DATA + "addHeatPowerPercent"); // %
            mappings.put(0x5104, CHANNEL_GROUP_CONTROL_DATA + "addHeatPowerEnergy"); // kWh
            mappings.put(0x0107, CHANNEL_GROUP_CONTROL_DATA + "radiatorReturnTarget");
            mappings.put(0x2108, CHANNEL_GROUP_CONTROL_DATA + "compressorSpeed");

            // Device values
            mappings.put(0x1A01, CHANNEL_GROUP_DEVICE_VALUES + "compressor");
            mappings.put(0x1A04, CHANNEL_GROUP_DEVICE_VALUES + "coldFluidPump");
            mappings.put(0x1A05, CHANNEL_GROUP_DEVICE_VALUES + "heatFluidPump");
            mappings.put(0x1A06, CHANNEL_GROUP_DEVICE_VALUES + "radiatorPump");
            mappings.put(0x1A07, CHANNEL_GROUP_DEVICE_VALUES + "switchValve");
            mappings.put(0x1A08, CHANNEL_GROUP_DEVICE_VALUES + "switchValve2");
            mappings.put(0x1A09, CHANNEL_GROUP_DEVICE_VALUES + "fan");
            mappings.put(0x1A0A, CHANNEL_GROUP_DEVICE_VALUES + "highPressostat");
            mappings.put(0x1A0B, CHANNEL_GROUP_DEVICE_VALUES + "lowPressostat");
            mappings.put(0x1A0C, CHANNEL_GROUP_DEVICE_VALUES + "heatingCable");
            mappings.put(0x1A0D, CHANNEL_GROUP_DEVICE_VALUES + "crankCaseHeater");
            mappings.put(0x1A20, CHANNEL_GROUP_DEVICE_VALUES + "alarm");
            mappings.put(0xAFF1, CHANNEL_GROUP_DEVICE_VALUES + "elMeter1");
            mappings.put(0xAFF2, CHANNEL_GROUP_DEVICE_VALUES + "elMeter2");

            // Settings
            mappings.put(0x0203, CHANNEL_GROUP_SETTINGS + "indoorTempSetting");
            mappings.put(0x2204, CHANNEL_GROUP_SETTINGS + "curveInflByInTemp");
            mappings.put(0x0205, CHANNEL_GROUP_SETTINGS + "heatCurve");
            mappings.put(0x0206, CHANNEL_GROUP_SETTINGS + "heatCurve2");
            mappings.put(0x0207, CHANNEL_GROUP_SETTINGS + "heatCurveFineAdj");
        }

        return Collections.unmodifiableMap(mappings);
    }
}
