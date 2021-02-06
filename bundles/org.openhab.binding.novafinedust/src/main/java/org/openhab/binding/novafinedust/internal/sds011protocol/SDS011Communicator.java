/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.novafinedust.internal.sds011protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.TooManyListenersException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.novafinedust.internal.SDS011Handler;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.CommandMessage;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.Constants;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.ModeReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.SensorFirmwareReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.SensorMeasuredDataReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.SensorReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.SleepReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.WorkingPeriodReply;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central instance to communicate with the device, i.e. receive data from it and send commands to it
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class SDS011Communicator {

    private static final int MAX_SENDOR_REPORTINGS_UNTIL_EXPECTED_REPLY = 20;

    private final Logger logger = LoggerFactory.getLogger(SDS011Communicator.class);

    private SerialPortIdentifier portId;
    private SDS011Handler thingHandler;
    private @Nullable SerialPort serialPort;

    private @Nullable OutputStream outputStream;
    private @Nullable InputStream inputStream;
    private @Nullable ScheduledExecutorService scheduler;

    public SDS011Communicator(SDS011Handler thingHandler, SerialPortIdentifier portId,
            ScheduledExecutorService scheduler) {
        this.thingHandler = thingHandler;
        this.portId = portId;
        this.scheduler = scheduler;
    }

    /**
     * Initialize the communication with the device, i.e. open the serial port etc.
     *
     * @param mode the {@link WorkMode} if we want to use polling or reporting
     * @param interval the time between polling or reportings
     * @return {@code true} if we can communicate with the device
     * @throws PortInUseException
     * @throws TooManyListenersException
     * @throws IOException
     * @throws UnsupportedCommOperationException
     */
    public boolean initialize(WorkMode mode, Duration interval)
            throws PortInUseException, TooManyListenersException, IOException, UnsupportedCommOperationException {
        boolean initSuccessful = true;

        logger.trace("Initializing with mode={}, interval={}", mode, interval);

        SerialPort localSerialPort = portId.open(thingHandler.getThing().getUID().toString(), 2000);
        logger.trace("Port opened, object is={}", localSerialPort);
        localSerialPort.setSerialPortParams(9600, 8, 1, 0);
        logger.trace("Serial parameters set on port");

        outputStream = localSerialPort.getOutputStream();
        inputStream = localSerialPort.getInputStream();

        if (inputStream == null || outputStream == null) {
            throw new IOException("Could not create input or outputstream for the port");
        }
        logger.trace("Input and Outputstream opened for the port");

        // wake up the device
        initSuccessful &= sendSleep(false);
        logger.trace("Wake up call done, initSuccessful={}", initSuccessful);
        initSuccessful &= getFirmware();
        logger.trace("Firmware requested, initSuccessful={}", initSuccessful);

        if (mode == WorkMode.POLLING) {
            initSuccessful &= setMode(WorkMode.POLLING);
            logger.trace("Polling mode set, initSuccessful={}", initSuccessful);
            initSuccessful &= setWorkingPeriod((byte) 0);
            logger.trace("Working period for polling set, initSuccessful={}", initSuccessful);
        } else {
            // reporting
            initSuccessful &= setWorkingPeriod((byte) interval.toMinutes());
            logger.trace("Working period for reporting set, initSuccessful={}", initSuccessful);
            initSuccessful &= setMode(WorkMode.REPORTING);
            logger.trace("Reporting mode set, initSuccessful={}", initSuccessful);
        }

        this.serialPort = localSerialPort;

        return initSuccessful;
    }

    private @Nullable SensorReply sendCommand(CommandMessage message) throws IOException {
        byte[] commandData = message.getBytes();
        if (logger.isDebugEnabled()) {
            logger.debug("Will send command: {} ({})", HexUtils.bytesToHex(commandData), Arrays.toString(commandData));
        }

        try {
            write(commandData);
        } catch (IOException ioex) {
            logger.debug("Got an exception while writing a command, will not try to fetch a reply for it.", ioex);
            throw ioex;
        }

        try {
            // Give the sensor some time to handle the command
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.warn("Problem while waiting for reading a reply to our command.");
            Thread.currentThread().interrupt();
        }
        SensorReply reply = readReply();
        // in case there is still another reporting active, we want to discard the sensor data and read the reply to our
        // command again, this might happen more often in case the sensor has buffered some data
        for (int i = 0; i < MAX_SENDOR_REPORTINGS_UNTIL_EXPECTED_REPLY; i++) {
            if (reply instanceof SensorMeasuredDataReply) {
                reply = readReply();
            } else {
                break;
            }
        }
        return reply;
    }

    private void write(byte[] commandData) throws IOException {
        OutputStream localOutputStream = outputStream;
        if (localOutputStream != null) {
            localOutputStream.write(commandData);
            localOutputStream.flush();
        }
    }

    private boolean setWorkingPeriod(byte period) throws IOException {
        CommandMessage m = new CommandMessage(Command.WORKING_PERIOD, new byte[] { Constants.SET_ACTION, period });
        logger.debug("Sending work period: {}", period);
        SensorReply reply = sendCommand(m);
        logger.debug("Got reply to setWorkingPeriod command: {}", reply);
        if (reply instanceof WorkingPeriodReply) {
            WorkingPeriodReply wpReply = (WorkingPeriodReply) reply;
            if (wpReply.getPeriod() == period && wpReply.getActionType() == Constants.SET_ACTION) {
                return true;
            }
        }
        return false;
    }

    private boolean setMode(WorkMode workMode) throws IOException {
        byte haveToRequestData = 0;
        if (workMode == WorkMode.POLLING) {
            haveToRequestData = 1;
        }

        CommandMessage m = new CommandMessage(Command.MODE, new byte[] { Constants.SET_ACTION, haveToRequestData });
        logger.debug("Sending mode: {}", workMode);
        SensorReply reply = sendCommand(m);
        logger.debug("Got reply to setMode command: {}", reply);
        if (reply instanceof ModeReply) {
            ModeReply mr = (ModeReply) reply;
            if (mr.getActionType() == Constants.SET_ACTION && mr.getMode() == workMode) {
                return true;
            }
        }
        return false;
    }

    private boolean sendSleep(boolean doSleep) throws IOException {
        byte payload = (byte) 1;
        if (doSleep) {
            payload = (byte) 0;
        }

        CommandMessage m = new CommandMessage(Command.SLEEP, new byte[] { Constants.SET_ACTION, payload });
        logger.debug("Sending doSleep: {}", doSleep);
        SensorReply reply = sendCommand(m);
        logger.debug("Got reply to sendSleep command: {}", reply);

        if (!doSleep) {
            // sometimes the sensor does not wakeup on the first attempt, thus we try again
            for (int i = 0; reply == null && i < 3; i++) {
                reply = sendCommand(m);
                logger.debug("Got reply to sendSleep command after retry#{}: {}", i + 1, reply);
            }
        }

        if (reply instanceof SleepReply) {
            SleepReply sr = (SleepReply) reply;
            if (sr.getActionType() == Constants.SET_ACTION && sr.getSleep() == payload) {
                return true;
            }
        }
        return false;
    }

    private boolean getFirmware() throws IOException {
        CommandMessage m = new CommandMessage(Command.FIRMWARE, new byte[] {});
        logger.debug("Sending get firmware request");
        SensorReply reply = sendCommand(m);
        logger.debug("Got reply to getFirmware command: {}", reply);

        if (reply instanceof SensorFirmwareReply) {
            SensorFirmwareReply fwReply = (SensorFirmwareReply) reply;
            thingHandler.setFirmware(fwReply.getFirmware());
            return true;
        }
        return false;
    }

    /**
     * Request data from the device
     *
     * @throws IOException
     */
    public void requestSensorData() throws IOException {
        CommandMessage m = new CommandMessage(Command.REQUEST_DATA, new byte[] {});
        byte[] data = m.getBytes();
        if (logger.isDebugEnabled()) {
            logger.debug("Requesting sensor data, will send: {}", HexUtils.bytesToHex(data));
        }
        write(data);
        try {
            Thread.sleep(200); // give the device some time to handle the command
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting before reading a reply to our rquest data command.");
            Thread.currentThread().interrupt();
        }
        readSensorData();
    }

    private @Nullable SensorReply readReply() throws IOException {
        byte[] readBuffer = new byte[Constants.REPLY_LENGTH];

        InputStream localInpuStream = inputStream;

        int b = -1;
        if (localInpuStream != null) {
            logger.trace("Reading for reply until first byte is found");
            while ((b = localInpuStream.read()) != Constants.MESSAGE_START_AS_INT) {
                logger.debug("Trying to find first reply byte now...");
            }
            readBuffer[0] = (byte) b;
            int remainingBytesRead = localInpuStream.read(readBuffer, 1, Constants.REPLY_LENGTH - 1);
            if (logger.isDebugEnabled()) {
                logger.debug("Read remaining bytes: {}, full reply={}", remainingBytesRead,
                        HexUtils.bytesToHex(readBuffer));
            }
            return ReplyFactory.create(readBuffer);
        }
        return null;
    }

    public void readSensorData() throws IOException {
        logger.trace("readSensorData() called");
        SensorReply reply = readReply();
        logger.trace("readSensorData(): Read reply={}", reply);
        if (reply instanceof SensorMeasuredDataReply) {
            SensorMeasuredDataReply sensorData = (SensorMeasuredDataReply) reply;
            logger.trace("We received sensor data");
            if (sensorData.isValidData()) {
                logger.trace("Sensor data is valid => updating channels");
                thingHandler.updateChannels(sensorData);
            }
        }
    }

    /**
     * Shutdown the communication, i.e. send the device to sleep and close the serial port
     */
    public void dispose(boolean sendtoSleep) {
        SerialPort localSerialPort = serialPort;
        if (localSerialPort != null) {
            if (sendtoSleep) {
                sendDeviceToSleepOnDispose();
            }

            logger.debug("Closing the port now");
            localSerialPort.close();

            serialPort = null;
        }
        this.scheduler = null;
    }

    private void sendDeviceToSleepOnDispose() {
        @Nullable
        ScheduledExecutorService localScheduler = scheduler;
        if (localScheduler != null) {
            Future<?> sleepJob = null;
            try {
                sleepJob = localScheduler.submit(() -> {
                    try {
                        sendSleep(true);
                    } catch (IOException e) {
                        logger.debug("Exception while sending sleep on disposing the communicator (will ignore it)", e);
                    }
                });
                sleepJob.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                logger.warn("Could not send device to sleep, because command takes longer than 5 seconds.");
                sleepJob.cancel(true);
            } catch (ExecutionException e) {
                logger.debug("Could not execute sleep command.", e);
            } catch (InterruptedException e) {
                logger.debug("Sending device to sleep was interrupted.");
                Thread.currentThread().interrupt();
            }
        } else {
            logger.debug("Scheduler was null, could not send device to sleep.");
        }
    }
}
