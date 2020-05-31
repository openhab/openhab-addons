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
package org.openhab.binding.novafinedust.internal.sds011protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.TooManyListenersException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.util.HexUtils;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.novafinedust.internal.SDS011Handler;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.CommandMessage;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.Constants;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.ModeReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.SensorFirmwareReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.SensorMeasuredDataReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.SensorReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.SleepReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.WorkingPeriodReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central instance to communicate with the device, i.e. receive data from it and send commands to it
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class SDS011Communicator implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(SDS011Communicator.class);

    private SerialPortIdentifier portId;
    private SDS011Handler thingHandler;
    private @Nullable SerialPort serialPort;

    private @Nullable OutputStream outputStream;
    private @Nullable InputStream inputStream;

    public SDS011Communicator(SDS011Handler thingHandler, SerialPortIdentifier portId) {
        this.thingHandler = thingHandler;
        this.portId = portId;
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

        SerialPort localSerialPort = portId.open(thingHandler.getThing().getUID().toString(), 2000);
        localSerialPort.setSerialPortParams(9600, 8, 1, 0);

        outputStream = localSerialPort.getOutputStream();
        inputStream = localSerialPort.getInputStream();

        if (inputStream == null || outputStream == null) {
            throw new IOException("Could not create input or outputstream for the port");
        }

        // wake up the device
        initSuccessful &= sendSleep(false);
        initSuccessful &= getFirmware();

        if (mode == WorkMode.POLLING) {
            initSuccessful &= setMode(WorkMode.POLLING);
            initSuccessful &= setWorkingPeriod((byte) 0);
        } else {
            // reporting
            initSuccessful &= setWorkingPeriod((byte) interval.toMinutes());
            initSuccessful &= setMode(WorkMode.REPORTING);
        }

        // enable listeners only after we have configured the sensor above because for configuring we send and read data
        // sequentially
        localSerialPort.notifyOnDataAvailable(true);
        localSerialPort.addEventListener(this);
        this.serialPort = localSerialPort;

        return initSuccessful;
    }

    private @Nullable SensorReply sendCommand(CommandMessage message) throws IOException {
        byte[] commandData = message.getBytes();
        if (logger.isDebugEnabled()) {
            logger.debug("Will send command: {} ({})", HexUtils.bytesToHex(commandData), Arrays.toString(commandData));
        }

        write(commandData);

        try {
            // Give the sensor some time to handle the command
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.warn("Problem while waiting for reading a reply to our command.");
            Thread.currentThread().interrupt();
        }
        SensorReply reply = readReply();
        // in case there is still another reporting active, we want to discard the sensor data and read the reply to our
        // command again
        if (reply instanceof SensorMeasuredDataReply) {
            reply = readReply();
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
     * Request data from the device, they will be returned via the serialEvent callback
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
    }

    private @Nullable SensorReply readReply() throws IOException {
        byte[] readBuffer = new byte[Constants.REPLY_LENGTH];

        InputStream localInpuStream = inputStream;

        int b = -1;
        if (localInpuStream != null && localInpuStream.available() > 0) {
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

    /**
     * Data from the device is arriving and will be parsed accordingly
     */
    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            // we get here if data has been received
            SensorReply reply = null;
            try {
                reply = readReply();
                logger.debug("Got data from sensor: {}", reply);
            } catch (IOException e) {
                logger.warn("Could not read available data from the serial port: {}", e.getMessage());
            }
            if (reply instanceof SensorMeasuredDataReply) {
                SensorMeasuredDataReply sensorData = (SensorMeasuredDataReply) reply;
                if (sensorData.isValidData()) {
                    thingHandler.updateChannels(sensorData);
                }
            }
        }
    }

    /**
     * Shutdown the communication, i.e. send the device to sleep and close the serial port
     */
    public void dispose() {
        SerialPort localSerialPort = serialPort;
        if (localSerialPort != null) {
            try {
                // send the device to sleep to preserve power and extend the lifetime of the sensor
                sendSleep(true);
            } catch (IOException e) {
                // ignore because we are shutting down anyway
                logger.debug("Exception while disposing communicator (will ignore it)", e);
            } finally {
                localSerialPort.removeEventListener();
                localSerialPort.close();
                serialPort = null;
            }
        }

        try {
            InputStream localInputStream = inputStream;
            if (localInputStream != null) {
                localInputStream.close();
            }
        } catch (IOException e) {
            logger.debug("Error while closing the input stream: {}", e.getMessage());
        }

        try {
            OutputStream localOutputStream = outputStream;
            if (localOutputStream != null) {
                localOutputStream.close();
            }
        } catch (IOException e) {
            logger.debug("Error while closing the output stream: {}", e.getMessage());
        }
    }
}
