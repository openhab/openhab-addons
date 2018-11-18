/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.canbus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CanBusDevice implementation for USBTin. See https://www.fischl.de/usbtin/#commands
 *
 * @author Lubos Housa - Initial contribution
 */
@NonNullByDefault
public class USBTinDevice extends AbstractCanBusDevice {

    private static final Logger logger = LoggerFactory.getLogger(USBTinDevice.class);

    /** serial port specific attributes go here */
    private static final int SERIAL_PORT_BAUD_RATE = 115200;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private static final int PARITY = 0;

    private static final String NEWLINE = "\r";
    private static final String RESET_OVERFLOW_COMMAND = "W2D00";
    private static final String CLOSE_CAN_COMMAND = "C";
    private static final String OPEN_CAN_COMMAND = "O";
    private static final char READ_STATUS_COMMAND = 'F';
    private static final char CAN_TRANSMIT_COMMAND = 't';
    private static final char CAN_TRANSMIT_SUCCESS_FLAG = 'z';
    private static final char USBTIN_ERROR = 0x07;

    private static final int STARTUP_TIMEOUT_SHORT_MS = 100;
    private static final int STARTUP_TIMEOUT_LONG_MS = 1000;

    @NonNullByDefault({})
    private static final Map<Integer, String> canBusBaudRatesCommands = new HashMap<>();
    static {
        canBusBaudRatesCommands.put(10000, "S0");
        canBusBaudRatesCommands.put(20000, "S1");
        canBusBaudRatesCommands.put(50000, "S2");
        canBusBaudRatesCommands.put(100000, "S3");
        canBusBaudRatesCommands.put(125000, "S4");
        canBusBaudRatesCommands.put(250000, "S5");
        canBusBaudRatesCommands.put(500000, "S6");
        canBusBaudRatesCommands.put(800000, "S7");
        canBusBaudRatesCommands.put(1000000, "S8");
    }

    /**
     * Creates new instance of this can bus device with default reader and writer provider
     */
    public USBTinDevice() {
    }

    /**
     * Creates new instance of this can bus device
     *
     * @param readerProvider provider of new instances of reader for a given input stream. Used by this device at
     *                           runtime as needed when opening the ports
     * @param writerProvider provider of new instances of writer for a given output stream. Used by this device at
     *                           runtime as needed when opening the ports
     */
    public USBTinDevice(Function<InputStream, Reader> readerProvider, Function<OutputStream, Writer> writerProvider) {
        super(readerProvider, writerProvider);
    }

    /**
     * Writes command to output and reads all available input
     *
     * @param command command to run on USBTin
     * @throws IOException
     */
    private void command(String command) throws IOException {
        if (output == null) {
            logger.debug("output not set, unable to invoke command {}. Device most likely not connected.", command);
            return;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Running command {} against USBTin", command);
        }

        // output.newLine() would read system property for newline char, but we actually want hardcoded new line all the
        // time since that is what the device understands
        output.write(command + NEWLINE);
        output.flush();
        readAll();
    }

    private void closeCanCommand() throws IOException {
        command(CLOSE_CAN_COMMAND);
    }

    /**
     * Prepare device for the first use
     */
    private void prepareDevice() throws IOException {
        logger.debug("Preparing device. Closing any potential previously opened sessions with USBTin");
        command("");
        closeCanCommand();
        sleep(STARTUP_TIMEOUT_SHORT_MS);
        closeCanCommand();
        // this is to just let the device potentially recover from any traffic before, we would sink all data remaining
        readAll();
        sleep(STARTUP_TIMEOUT_LONG_MS);
        readAll();
        // reset overflow errors
        command(RESET_OVERFLOW_COMMAND);
        logger.debug("Device prepared.");
    }

    private boolean openCan(int canBusBaudRate) throws IOException {
        logger.debug("About to set CANBUS baudrate: {}", canBusBaudRate);
        String baudRateCommand = canBusBaudRatesCommands.get(canBusBaudRate);
        if (baudRateCommand == null) {
            logger.warn("Unsupported baudrate: {}. Unable to open the canbus using this device", canBusBaudRate);
            // the USBTIN page https://www.fischl.de/usbtin/#commands describes generic baudrate settings through
            // registers, not implementing it here though
            return false;
        }

        command(baudRateCommand);
        logger.debug("Baudrate set. Opening canbus in active mode.");
        command(OPEN_CAN_COMMAND);
        readAll(); // sink in any acks for the previous commands
        logger.debug("Connected to CANBUS.");

        return true;
    }

    @Override
    protected boolean connectToCanBus(int canBusBaudRate) {
        try {
            prepareDevice();
            return openCan(canBusBaudRate);
        } catch (IOException e) {
            logger.warn("Some error ocurred when connecting to the CANBUS.", e);
            return false;
        }
    }

    @Override
    protected void checkConnection() throws IOException {
        command(READ_STATUS_COMMAND + "");
    }

    private void logStatusDetail(String status) {
        try {
            int statInt = Integer.parseUnsignedInt(status, 16);
            if (((statInt >> 5) & 1) == 1) {
                logger.warn(
                        "USBTin reports Error warning (EWARN) bit set in MCP2515. CANBUS traffic may be impacted in USBTin. Is it connected on the CANBUS to other controllers?");
            }
            if (((statInt >> 4) & 1) == 1) {
                logger.warn(
                        "USBTin reports Data overrun (RX1OVR or RX0OVR) bit set in MCP2515. Very likely some of the previous traffic was not reflected properly in CANBUS from USBTin");
            }
            if (((statInt >> 2) & 1) == 1) {
                logger.warn(
                        "USBTin reports Error passive (TXEP or RXEP) bit set in MCP2515. CANBUS traffic very likely not working in USBTin. Is it connected on the CANBUS to other controllers?");
            }
            if ((statInt & 1) == 1) {
                logger.warn(
                        "USBTin reports Bus error (TXBO) bit set in MCP2515. CANBUS traffic very likely not working in USBTin. Is it connected on the CANBUS to other controllers?");
            }
        } catch (NumberFormatException e) {
            logger.warn("Unable to parse status {} from USBTin.", status);
            logger.warn("Detail: ", e);
        }
    }

    private Collection<CanMessage> parse(String stringMessage) {
        // we can get multiple messages separated by NEWLINE
        List<CanMessage> result = new LinkedList<>();
        for (String line : stringMessage.split(NEWLINE)) {
            if (line.isEmpty()) {
                continue;
            }
            char firstChar = line.charAt(0);
            switch (firstChar) {
                case USBTIN_ERROR:
                    logger.warn(
                            "USBTin sent an error flag, last command was not probably processed properly. Full message received: {}",
                            line);
                    notifyListenersDeviceError("USBTin sent an error flag " + USBTIN_ERROR);
                    break;
                case READ_STATUS_COMMAND:
                    if (line.length() >= 3) {
                        String status = line.substring(1, 3);
                        logger.debug("USBTin reported status {}", status);
                        logStatusDetail(status);
                    }
                    break;
                case CAN_TRANSMIT_COMMAND:
                    if (line.length() < 7) {
                        logger.warn(
                                "Invalid CanMessage received, ignoring. Expected to receive at least 7 characters to build a valid message, but the message {} has only {} characters",
                                line, line.length());
                        continue;
                    }
                    logger.debug("Valid CanMessage received, building it now");
                    try {
                        CanMessage.Builder message = CanMessage.newBuilder();
                        // characters 1,2,3 compose the canID in hex
                        message.id(Integer.parseUnsignedInt(line.substring(1, 4), 16));
                        // character 4 is the actual data byte size (in hex too, but can be 8 max anyway)
                        int dataSize = Integer.parseUnsignedInt(line.substring(4, 5));
                        // characters 5,6 and 7,8 and all following pairs up to 19,20 (8 pairs top) represent the
                        // databyte
                        for (int i = 0; i < dataSize; i++) {
                            int stringIndex = 5 + (2 * i);
                            message.withDataByte(
                                    Integer.parseUnsignedInt(line.substring(stringIndex, stringIndex + 2), 16));
                        }
                        result.add(message.build());
                    } catch (IndexOutOfBoundsException | IllegalArgumentException | IllegalStateException e) {
                        logger.warn("Unable to parse incoming canMessage. Ignoring this message {}", line);
                        logger.warn("", e);
                    }
                    break;
                case CAN_TRANSMIT_SUCCESS_FLAG:
                    logger.debug("Previous CanMessage was successfully sent as reported by USBTin.");
                    break;
                default:
                    logger.debug("Unknown message received from USBTin, ignoring: ", line);
            }
        }
        return result;
    }

    @Override
    protected void processIncomingData() {
        if (input == null) {
            // this can happen at times after unregistering all, so rather moving to debug only
            logger.debug(
                    "Input not set, unable to read anything from the device even though received notification about incoming traffic from the device");
            return;
        }

        try {
            StringBuilder inputMessage = new StringBuilder();
            while (input.ready()) {
                // ASCII chars sent across
                inputMessage.append((char) input.read());
            }
            String stringMessage = inputMessage.toString();
            logger.trace("Received data from the device: {}", stringMessage);

            // try to parse it (could be multiline messages) and if any are valid, broadcast it to listeners (we may at
            // some points during or after init or sending other commands also get notified about some activity from the
            // device, then would typically just get CRs here and the parse would ignore these (but may log some of
            // them)
            for (CanMessage message : parse(stringMessage)) {
                logger.debug("Recognized a valid CanMessage: {}. Notifying listeners.", message);
                notifyListeners(message);
            }
        } catch (IOException e) {
            logger.warn(
                    "Problem processing incoming data from the device. Very likely not all data was read (and possibly CANBUS traffic got lost).",
                    e);
        }

    }

    @Override
    protected CanBusDeviceConfiguration getCanBusDeviceConfiguration() {
        return CanBusDeviceConfiguration.newBuilder().baudRate(SERIAL_PORT_BAUD_RATE).dataBits(DATA_BITS)
                .stopBits(STOP_BITS).parity(PARITY).build();
    }

    @Override
    protected void closeDevice() {
        // now close the CAN channel
        logger.debug("Disconnecting from the CANBUS...");
        try {
            closeCanCommand();
            logger.debug("Disconnected.");
        } catch (IOException e) {
            logger.warn("Some error ocurred when closing the CANBUS.", e);
        }
    }

    @Override
    public synchronized void send(CanMessage message) throws CanBusCommunicationException {
        logger.debug("About to send a message {} over CANBUS...", message);
        // synchronize the method since the device is not thread safe and in case we get multiple requests at the same
        // time, they would be sent out one by one from here, so no need to introduce any queuing
        StringBuilder result = new StringBuilder();

        // append can transmit command, then CAN ID as 3 digit hex, put data byte size as the last digit
        result.append(CAN_TRANSMIT_COMMAND).append(String.format("%03x%x", message.getId(), message.getData().size()));
        // now loop through all data bytes and simply append as hex too
        for (Short dataByte : message.getData()) {
            result.append(String.format("%02x", dataByte));
        }
        String stringMessage = result.toString();
        try {
            logger.debug("The actual command being ran against the device: {}", stringMessage);
            command(stringMessage);
            logger.debug("Send succeeded.");
        } catch (IOException e) {
            throw new CanBusCommunicationException(
                    "Problem sending CanMessage " + message + ". String command " + stringMessage, e);
        }
    }
}
