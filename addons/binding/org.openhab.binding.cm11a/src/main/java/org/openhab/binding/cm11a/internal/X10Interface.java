/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cm11a.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openhab.binding.cm11a.handler.Cm11aAbstractHandler;
import org.openhab.binding.cm11a.handler.Cm11aBridgeHandler;
import org.openhab.binding.cm11a.handler.ReceivedDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * Driver for the CM11 X10 interface.
 *
 *
 * @author Anthony Green - Original code
 * @author Bob Raker - updates to setClock code, adapted code for use in openHAB2
 * @see <a href="http://www.heyu.org/docs/protocol.txt">CM11 Protocol specification</a>
 * @see <a href="http://www.rxtx.org">RXTX Serial API for Java</a>
 */
public class X10Interface extends Thread implements SerialPortEventListener {

    private Logger logger = LoggerFactory.getLogger(X10Interface.class);

    // X10 Function codes
    public static final int FUNC_ALL_UNITS_OFF = 0x0;
    public static final int FUNC_ALL_LIGHTS_ON = 0x1;
    public static final int FUNC_ON = 0x2;
    public static final int FUNC_OFF = 0x3;
    public static final int FUNC_DIM = 0x4;
    public static final int FUNC_BRIGHT = 0x5;
    public static final int FUNC_ALL_LIGHTS_OFF = 0x6;
    public static final int FUNC_EXTENDED = 0x7;
    public static final int FUNC_HAIL_REQ = 0x8;
    public static final int FUNC_HAIL_ACK = 0x9;
    public static final int FUNC_PRESET_DIM_1 = 0xA;
    public static final int FUNC_PRESET_DIM_2 = 0xB;
    public static final int FUNC_EXT_DATA_TRANSFER = 0xC;
    public static final int FUNC_STATUS_ON = 0xD;
    public static final int FUNC_STATUS_OFF = 0xE;
    public static final int FUNC_STATUS_REQ = 0xF;

    // Definitions for the header:code bits
    /**
     * Bit mask for the bit that is always set in a header:code
     */
    static final int HEAD = 0x04;
    /**
     * Bit mask for Function/Address bit of header:code.
     */
    static final int HEAD_FUNC = 0x02;
    /**
     * Bit mask for standard/extended transmission bit of header:code.
     */
    static final int HEAD_EXTENDED = 0x01;

    /**
     * Byte sent from PC to Interface to acknowledge the receipt of a correct checksum.
     * If the checksum was incorrect, the PC should retransmit.
     */
    static final int CHECKSUM_ACK = 0x00;
    /**
     * Byte send from Interface to PC to indicate it has sent the desired data over the
     * X10/power lines.
     */
    static final int IF_READY = 0x55;

    /**
     * Byte sent from Interface to PC to request that its clock is set.
     * Interface will send this to the PC repeatedly after a power-failure and will not respond to commands
     * until its clock has been set.
     */
    static final int CLOCK_SET_REQ = 0xA5;
    /**
     * Byte sent from PC to interface to start a transmission that sets the interface clock.
     */
    static final int CLOCK_SET_HEAD = 0x9B;

    /**
     * Byte sent from interface to PC to indicate that it has X10 data pending transmission to the PC.
     */
    static final int DATA_READY_REQ = 0x5a;
    static final int DATA_READY_HEAD = 0xc3;

    /**
     * This command is purely intended for the CP10.
     * The power-strip contains an input filter and electrical surge protection
     * that is monitored by the microcontroller. If this protection should
     * become compromised (i.e. resulting from a lightening strike) the
     * interface will attempt to wake the computer with a 'filter-fail poll'.
     */
    static final int INPUT_FILTER_FAIL_REQ = 0xf3;
    static final int INPUT_FILTER_FAIL_HEAD = 0xf3;

    /**
     * Byte sent from PC to interface to enable interface feature that brings serial port RI high when
     * data arrives to send to PC
     */
    static final int RI_ENABLE = 0xeb;
    static final int RI_DISABLE = 0x55;

    /**
     * THe house code to be monitored. Not sure what this means, but it is part of the clock set instruction.
     * For the moment hardcoded here to be House 'E'.
     */
    static final int MONITORED_HOUSE_CODE = 0x10;

    static final Map<Character, Integer> HOUSE_CODES;
    static final Map<Integer, Integer> DEVICE_CODES;

    static {
        HashMap<Character, Integer> houseCodes = new HashMap<Character, Integer>(16);
        houseCodes.put('A', 0x60);
        houseCodes.put('B', 0xE0);
        houseCodes.put('C', 0x20);
        houseCodes.put('D', 0xA0);
        houseCodes.put('E', 0x10);
        houseCodes.put('F', 0x90);
        houseCodes.put('G', 0x50);
        houseCodes.put('H', 0xD0);
        houseCodes.put('I', 0x70);
        houseCodes.put('J', 0xF0);
        houseCodes.put('K', 0x30);
        houseCodes.put('L', 0xB0);
        houseCodes.put('M', 0x00);
        houseCodes.put('N', 0x80);
        houseCodes.put('O', 0x40);
        houseCodes.put('P', 0xC0);

        HOUSE_CODES = Collections.unmodifiableMap(houseCodes);

        HashMap<Integer, Integer> deviceCodes = new HashMap<Integer, Integer>(16);
        deviceCodes.put(1, 0x06);
        deviceCodes.put(2, 0x0E);
        deviceCodes.put(3, 0x02);
        deviceCodes.put(4, 0x0A);
        deviceCodes.put(5, 0x01);
        deviceCodes.put(6, 0x09);
        deviceCodes.put(7, 0x05);
        deviceCodes.put(8, 0x0D);
        deviceCodes.put(9, 0x07);
        deviceCodes.put(10, 0x0F);
        deviceCodes.put(11, 0x03);
        deviceCodes.put(12, 0x0B);
        deviceCodes.put(13, 0x00);
        deviceCodes.put(14, 0x08);
        deviceCodes.put(15, 0x04);
        deviceCodes.put(16, 0x0C);

        DEVICE_CODES = Collections.unmodifiableMap(deviceCodes);
    }

    // Constants that control the interaction with the hardware
    static final int IO_PORT_OPEN_TIMEOUT = 5000;

    /**
     * Number of CM11a dim increments for dimable devices
     */
    static final int CM11A_DIM_INCREMENTS = 22;

    /**
     * How long to wait between attempts to reconnect to the interface. (ms)
     */
    static final int IO_RECONNECT_INTERVAL = 5000;
    /**
     * Maximum number of times to retry sending a bit of data when checksum errors occur.
     */
    static final int IO_MAX_SEND_RETRY_COUNT = 5;

    static final int SERIAL_TIMEOUT_MSEC = 4000;

    // Hardware IO attributes
    protected CommPortIdentifier portId;
    protected SerialPort serialPort;
    protected boolean connected = false;
    protected DataOutputStream serialOutput;
    protected OutputStream serialOutputStr;
    protected DataInputStream serialInput;
    protected InputStream serialInputStr;

    // Listeners that are to be notified when new data is received from the cm11a
    private List<ReceivedDataListener> receiveListeners = new ArrayList<>();

    /**
     * Flag to indicate that background thread should be killed. Used to deactivate plugin.
     */
    protected volatile boolean killThread = false;

    // Scheduling attributes
    /**
     * Queue of as-yet un-actioned requests.
     */
    protected BlockingQueue<Cm11aAbstractHandler> deviceUpdateQueue = new ArrayBlockingQueue<Cm11aAbstractHandler>(256);

    // Need to keep last addresses found for data that comes in over the serial interface because if the incoming
    // command is a dim or bright the address isn't included. In addition some controllers will send the address
    // in one message and the function in a second one
    private List<String> lastAddresses;

    /**
     * Need to have access to BridgeHandler so it's status can be updated.
     */
    private Cm11aBridgeHandler bridgeHandler;

    /**
     *
     * @param serialPort serial port device. e.g. /dev/ttyS0
     * @throws NoSuchPortException
     *
     */
    public X10Interface(String serialPort, Cm11aBridgeHandler bridgeHandler) throws NoSuchPortException {
        super();
        logger.trace("**** Constructing X10Interface for serial port: {} *******", serialPort);
        portId = CommPortIdentifier.getPortIdentifier(serialPort);
        this.bridgeHandler = bridgeHandler;
    }

    /**
     * Establishes a serial connection to the hardware, if one is not already established.
     */
    protected boolean connect() {
        if (!connected) {
            if (serialPort != null) {
                logger.trace("Closing stale serialPort object before reconnecting");
                serialPort.close();
            }
            logger.debug("Connecting to X10 hardware on serial port: {}", portId.getName());
            try {
                serialPort = portId.open("Openhab CM11A Binding", IO_PORT_OPEN_TIMEOUT);
                serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                serialPort.disableReceiveTimeout();
                serialPort.enableReceiveThreshold(1);

                serialOutputStr = serialPort.getOutputStream();
                serialOutput = new DataOutputStream(serialOutputStr);
                serialInputStr = serialPort.getInputStream();
                serialInput = new DataInputStream(serialInputStr);

                serialPort.addEventListener(this);
                connected = true;

                serialPort.notifyOnDataAvailable(true);
                serialPort.notifyOnRingIndicator(true);

                // Bob Raker - add serial timeout to prevent possible hang conditions
                serialPort.enableReceiveTimeout(SERIAL_TIMEOUT_MSEC);
                if (!serialPort.isReceiveTimeoutEnabled()) {
                    logger.info("Serial receive timeout not supported by this driver.");
                }

                bridgeHandler.changeBridgeStatusToUp();
            } catch (PortInUseException e) {
                String message = String.format("Serial port %s is in use by another application (%s)", portId.getName(),
                        e.currentOwner);
                logger.warn("{}", message);
                bridgeHandler.changeBridgeStatusToDown(message);
            } catch (UnsupportedCommOperationException e) {
                logger.warn("Serial port {} doesn't support the required baud/parity/stopbits or Timeout",
                        portId.getName());
            } catch (IOException e) {
                logger.warn("IO Problem with serial port {} . {}", portId.getName(), e.getMessage());
            } catch (TooManyListenersException e) {
                logger.warn(
                        "TooManyListeners error when trying to connect to serial port.  Interface is unlikely to work, raise a bug report.",
                        e);
                bridgeHandler.changeBridgeStatusToDown("ToManyListenersException");
            }
        } else {
            logger.trace("Already connected to hardware, skipping reconnection.");
        }

        return connected;
    }

    /**
     * Transmits a standard (non-extended) X10 function.
     *
     * @param address
     * @param function
     * @param dims 0-22, number of dims/brights to send.
     * @return true if update was successful
     * @throws InvalidAddressException
     * @throws IOException
     */
    public boolean sendFunction(String address, int function, int dims) throws InvalidAddressException, IOException {
        boolean success = false;

        if (!validateAddress(address)) {
            throw new InvalidAddressException("Address " + address + " is not a valid X10 address");
        }

        int houseCode = HOUSE_CODES.get(address.charAt(0));
        int deviceCode = DEVICE_CODES.get(Integer.parseInt(address.substring(1)));

        int[] data = new int[2];

        if (connect()) {
            synchronized (serialPort) {
                logger.trace("Sending a standard X10 function to device: {}", address);
                // First send address
                data[0] = HEAD;
                data[1] = houseCode | deviceCode;
                sendData(data);

                // Now send function call
                data[0] = HEAD | HEAD_FUNC | (dims << 3);
                data[1] = houseCode | function;
                sendData(data);

                success = true;
            }
        }
        return success;
    }

    /**
     * Validates that the given string is a valid X10 address. Returns true if this is the case.
     *
     * @param address
     * @return
     */
    public static boolean validateAddress(String address) {
        return (!(address.length() < 2 || address.length() > 3
                || !HOUSE_CODES.containsKey(new Character(address.charAt(0)))
                || !DEVICE_CODES.containsKey(Integer.parseInt(address.substring(1)))));
    }

    /**
     * Queues a standard (non-extended) X10 function for transmission.
     *
     * @param address
     * @param function
     * @return true for success
     * @throws InvalidAddressException
     * @throws IOException
     */
    public boolean sendFunction(String address, int function) throws InvalidAddressException, IOException {
        return sendFunction(address, function, 0);
    }

    /**
     * Add specified device into the queue for hardware updates.
     *
     * <p>
     * If device is already queued, it will be removed from queue and moved to the end.
     * </p>
     *
     * @param device
     */
    public void scheduleHWUpdate(Cm11aAbstractHandler device) {
        deviceUpdateQueue.remove(device);
        if (!deviceUpdateQueue.offer(device)) {
            logger.warn(
                    "X10 function call queue full.  Too many outstanding commands.  This command will be discarded");
        }
        logger.debug("Added item to cm11a queue for: {}", device.getThing().getLabel());
    }

    /**
     * Sends data to the hardware and handles the checksuming and retry process.
     *
     * <p>
     * When applicable, method blocks until the data has actually been sent over the powerlines using X10
     * </p>
     *
     * @param data Data to be sent.
     * @throws IOException
     */
    protected void sendData(int[] data) throws IOException {
        int calcChecksum = 0;
        int checksumResponse = -1;

        // Calculate expected checksum:
        for (int i = 0; i < data.length; i++) {
            // Note that ints are signed in Java, but the checksum uses unsigned ints.
            // Hence some jiggery pokery to get an unsigned int from the data int.
            calcChecksum = (calcChecksum + (0x000000FF & (data[i]))) & 0x000000FF;
            logger.trace("Checksum calc: int {} = {}", i, Integer.toHexString(data[i]));
        }

        if (connect()) {
            synchronized (serialPort) {
                long startTime = System.currentTimeMillis(); // do some timing analysis

                // Stop background data listener as we want to have a dialogue with the interface here.
                serialPort.notifyOnDataAvailable(false);

                // Need to catch possible EOF exception if there is a timeout
                try {
                    int retryCount = 0;
                    while (checksumResponse != calcChecksum) {
                        retryCount++;
                        for (int i = 0; i < data.length; i++) {
                            serialOutput.write(data[i]);
                            serialOutput.flush();
                        }
                        long sendTime = System.currentTimeMillis();
                        logger.trace("Sent the following data out the serial port in {} msec, {}",
                                (sendTime - startTime), Arrays.toString(data));
                        checksumResponse = serialInput.readUnsignedByte();
                        logger.trace("Attempted to send data, try number: {} Checksum expected: {} received: {}",
                                retryCount, Integer.toHexString(calcChecksum), Integer.toHexString(checksumResponse));
                        long ckSumTime = System.currentTimeMillis();
                        logger.trace("Received serial port check sum in {} msec", (ckSumTime - sendTime));

                        if (checksumResponse != calcChecksum) {
                            // On initial device power up, nothing works until we set the clock. Check to see if the
                            // unexpected data was actually a request from interface to PC.
                            processRequestFromIFace(checksumResponse);

                            if (retryCount > IO_MAX_SEND_RETRY_COUNT) {
                                logger.warn("Failed to send data to X10 hardware due to too many checksum failures");
                                serialPort.notifyOnDataAvailable(true);
                                throw new IOException("Max retries exceeded");
                            }
                        }
                    }

                    logger.trace(
                            "Data transmission to interface was successful, sending ACK.  X10 transmission over powerline will now commence.");
                    long ackTime = System.currentTimeMillis();
                    serialOutput.write(CHECKSUM_ACK);
                    serialOutput.flush();

                    int response = serialInput.readUnsignedByte();
                    if (response == IF_READY) {
                        long cmpltdTime = System.currentTimeMillis();
                        logger.trace("Serial port X10 ACK completed in {} msec, TOTAL X10 TRANSMISSION TIME in {} ms",
                                (cmpltdTime - ackTime), (cmpltdTime - startTime));
                    } else {
                        logger.warn("Expected IF_READY ({}) response from hardware but received: {} instead",
                                Integer.toHexString(IF_READY & 0x00000FF), Integer.toHexString(response & 0x00000FF));
                    }
                } catch (EOFException ex) {
                    logger.warn(
                            "Received EOF exception while sending X10 command after {} ms. Make sure the cm11a is connected to the serial port",
                            (System.currentTimeMillis() - startTime));
                }
                serialPort.notifyOnDataAvailable(true);
            }
        }
    }

    @Override
    public void run() {
        logger.trace("Starting X10Interface background thread...");

        // call connect so any X10 events on the powerline will be picked up by the serialEvent listener
        connect();

        while (!killThread) {
            try {
                Cm11aAbstractHandler nextModule;
                logger.trace("Getting next module to be updated");
                nextModule = deviceUpdateQueue.take();
                logger.trace("Got a device.  Going to run it.");

                // Keep retrying to update this device until it is successful.
                updateHardware(nextModule);
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt(); // See https://www.ibm.com/developerworks/library/j-jtp05236/ for
                                                    // discussion of resetting interrupt flag
                logger.warn("Unexpected interrupt on X10 scheduling thread.  Ignoring and continuing anyway...");
            }
        }
        logger.trace("Stopping background thread...");
        this.notifyAll();
    }

    /**
     * Perform Hardware update. Keep trying until it is successful
     *
     * @param nextModule - Next module that needs to be updated
     * @throws InterruptedException
     */
    private void updateHardware(Cm11aAbstractHandler nextModule) throws InterruptedException {
        boolean success = false;
        while (!success) {
            try {
                if (connect()) {
                    nextModule.updateHardware(this);
                    success = true;
                } else {
                    Thread.sleep(IO_RECONNECT_INTERVAL);
                }
            } catch (IOException e) {
                connected = false;
                String message = "IO Exception when updating module hardware.  Will retry shortly";
                logger.warn(message, e);
                bridgeHandler.changeBridgeStatusToDown(message);
                Thread.sleep(IO_RECONNECT_INTERVAL);
            } catch (InvalidAddressException e) {
                logger.warn("Attempted to send an X10 Function call with invalid address.  Ignoring this.");
                success = true; // Pretend this was successful as retrying will be pointless.
            }
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        try {
            if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE || event.getEventType() == SerialPortEvent.RI) {
                synchronized (serialPort) {
                    logger.trace("Serial port data available or RI indicator event received");
                    while (serialPort.isRI() && serialInput.available() <= 0) {
                        logger.trace("Ring indicator is High but there is no data.  Waiting for data...");
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            logger.debug("Interrupted while sleeping", e);
                        }
                    }

                    while (serialInput.available() > 0) {
                        logger.trace("{} bytes of data available to be read", serialInput.available());
                        int readint = serialInput.read();

                        processRequestFromIFace(readint);

                        // Wait a while before rechecking to give interface time to switch off Ring Indicator.
                        while (serialPort.isRI() && serialInput.available() <= 0) {
                            logger.trace("Ring indicator is High but there is no data.  Waiting for data...");
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                // Reset the flag and continue
                            }
                        }
                    }
                    logger.trace(
                            "Reading data from interface complete.  Ring Indicator has cleared and no data is left to read.");
                }
            }
        } catch (IOException e) {
            logger.warn("IO Exception in serial port handler callback: {}", e.getMessage());
        }
    }

    /**
     * Processes a request made from the interface to the PC. Only handles requests initiated by the interface,
     * not those that form part of a conversation triggered by the PC.
     *
     * @param readint
     * @throws IOException
     */
    protected void processRequestFromIFace(int readint) throws IOException {
        switch (readint) {
            case CLOCK_SET_REQ:
                setClock();
                break;
            case DATA_READY_REQ:
                receiveCommandData();
                break;
            case INPUT_FILTER_FAIL_REQ:
                serialOutput.write(DATA_READY_HEAD);
                logger.warn(
                        "X10 Interface has indicated that the filter and/or surge protection in the device has failed.");
                break;
            default:
                logger.warn("Unexpected data received from X10 interface: {}", Integer.toHexString(readint));
        }
    }

    /**
     * Sets the internal clock on the X10 interface
     *
     * @throws IOException
     */
    private void setClock() throws IOException {
        logger.debug("Setting clock in X10 interface");
        Calendar cal = Calendar.getInstance();

        int[] clockData = new int[7];
        clockData[0] = CLOCK_SET_HEAD;
        clockData[1] = cal.get(Calendar.SECOND);
        clockData[2] = cal.get(Calendar.MINUTE) + (cal.get(Calendar.HOUR) % 2) * 60;
        clockData[3] = cal.get(Calendar.HOUR_OF_DAY) / 2;
        // Note: Calendar.DAY_OF_YEAR starts at 1 but the C language tm_yday starts at 0. Need 0 based DAY_OF_YEAR for
        // cm11a
        clockData[4] = (cal.get(Calendar.DAY_OF_YEAR) - 1) % 256;
        // Note: Calendar.DAY_OF_WEEK is 1 based and need 0 based (i.e. Sunday = 1
        clockData[5] = (((cal.get(Calendar.DAY_OF_YEAR - 1)) / 256) << 7)
                | (0x01 << (cal.get(Calendar.DAY_OF_WEEK) - 1));

        // There are other flags in this final byte to do with clearing timer, battery timer and monitored status.
        // I've no idea what they are, so have left them unset.
        clockData[6] = MONITORED_HOUSE_CODE;

        sendData(clockData);
    }

    /**
     * Process data that the X10 interface is waiting to send to the PC
     *
     * @throws IOException
     */
    private void receiveCommandData() throws IOException {
        logger.debug("Receiving X10 data from interface");

        // Send acknowledgement to interface
        serialOutput.write(DATA_READY_HEAD);

        // Read the buffer size
        // There might be several DATA_READY_REQ bytes which need to be ignored
        int length = 0;
        do {
            length = serialInput.read();
        } while (length == DATA_READY_REQ || length > 8); // if the length is >8 this isn't a valid length so ignore

        // Next read the Function / Address Mask byte
        int mask = serialInput.read();
        length--; // This read counts as part of the length
        logger.debug("Receiving [{}] bytes,  Addr/Func mask: {}", length, Integer.toBinaryString(mask));

        // It is possible for the cm11a buffer to be empty in which case no processing can take place
        if (length > 0) {
            int[] data = new int[length];
            for (int i = 0; i < length; i++) {
                int recvByte = serialInputStr.read();
                data[i] = recvByte;
                logger.debug("          Received X10 data [{}]: {}", i, Integer.toHexString(recvByte));
            }

            processCommandData(mask, data);
        } else {
            logger.warn("cm11a buffer was overrun. Any pending commands will be ignorred until the buffer clears.");
        }
    }

    /**
     * Process raw data received from the interface and convert into human readable data
     *
     * @param mask Function / Address Mask. Each bit corresponds to the contents of a data. A 0 bit
     *            corresponds to an address. A 1 bit corresponds to a function.
     * @param data
     */
    private void processCommandData(int mask, int[] data) {
        X10ReceivedData.X10COMMAND command = X10ReceivedData.X10COMMAND.UNDEF; // Just set it to something
        List<String> addresses = new ArrayList<>();
        int dims = 0;
        List<X10ReceivedData> rcvData = new ArrayList<>();

        for (int i = 0; i < data.length; i++) {
            int d = data[i];
            int dataType = mask & 0x01;
            if (dataType == 0) {
                // The data byte is an address
                int houseIndex = (d >> 4) & 0x0f;
                int unitIndex = d & 0x0f;
                addresses.add(Character.toString(X10ReceivedData.HOUSE_CODE[houseIndex])
                        + Integer.toString(X10ReceivedData.UNIT_CODE[unitIndex]));
            } else {
                // The data byte is a function
                command = X10ReceivedData.COMMAND_MAP.get(d & 0x0f);
                if (command == null) {
                    command = X10ReceivedData.X10COMMAND.UNDEF;
                } else if (command == X10ReceivedData.X10COMMAND.BRIGHT || command == X10ReceivedData.X10COMMAND.DIM) {
                    // Have to read one more byte which is the dim level
                    if (i < data.length) {
                        dims = data[++i];
                        // This dims is a number between 0 and 210 and is the CHANGE in brightness level
                        // The interface transmits 1 to 22 dims to go from full bright to full dim
                        // therefore this need to be converted to a number between 1 and 22. Always want to dim or
                        // brighten one increment. The conversion is therefore:
                        dims = (dims * CM11A_DIM_INCREMENTS) / 210;
                        dims = dims > 0 ? dims : 1;
                    }
                    // Also in this case no addresses would have been sent so use the saved addresses
                    // If there were no previous commands that specified an address then lastAddress will be null. In
                    // this case we can't do anything with this dim request.
                    if (lastAddresses == null) {
                        logger.info(
                                "cm11a received a dim command but there is no prior commands that included an address.");
                        continue;
                    }
                    addresses = lastAddresses;
                } else if (command == X10ReceivedData.X10COMMAND.ALL_LIGHTS_OFF
                        || command == X10ReceivedData.X10COMMAND.ALL_LIGHTS_ON
                        || command == X10ReceivedData.X10COMMAND.ALL_UNITS_OFF) {
                    logger.warn("cm11a received the command: {}. This command is ignored by this binding.", command);
                    continue;
                } else {
                    // A valid command must have been received.
                    // As indicated above, some controllers send the address in one transmission and the function in a
                    // second transaction.
                    // Check if we have gotten an address in the transmission and if not use lastAddresses if they
                    // are not null
                    if (addresses.isEmpty()) {
                        // No addresses were sent in this transmission, see if we can use lastAddresses
                        if (lastAddresses == null) {
                            logger.info("cm11a received a command but the transmission didn't include an address.");
                            continue;
                        } else {
                            addresses = lastAddresses;
                            logger.info(
                                    "cm11a received a command without any addresses. Addresses from a prior reception are available and will be used.");
                        }
                    }
                }

                // Every time we get a function it is the end of the transmission and we can bundle the data into an
                // X10ReceivedData object.
                X10ReceivedData rd = new X10ReceivedData(addresses.toArray(new String[0]), command, dims);
                rcvData.add(rd);
                logger.debug("cm11a: Added received data to queue: {}", rd.toString());
                // reset the data objects for the next thing in the buffer, if any
                command = X10ReceivedData.X10COMMAND.UNDEF;
                // Collections.copy(lastAddresses, addresses);
                lastAddresses = addresses;
                addresses = new ArrayList<>();
                dims = 0;
            }

            mask = mask >> 1;
        }

        // Done processing buffer from cm11a. Notify interested parties about the data
        for (X10ReceivedData rd : rcvData) {
            logger.debug("cm11a: Converted received data to human form: {}", rd.toString());
            notifyReceiveListeners(rd);
        }
    }

    /**
     * Called by classes that want to be notified when data has been received from the cm11a
     *
     * @param listener The class to be called
     */
    public void addReceivedDataListener(ReceivedDataListener listener) {
        receiveListeners.add(listener);
    }

    /**
     * Called to notify classes that data has been received
     *
     * @param rcv
     */
    private void notifyReceiveListeners(X10ReceivedData rcv) {
        for (ReceivedDataListener rl : receiveListeners) {
            rl.receivedX10Data(rcv);
        }
    }

    /**
     * Disconnect from hardware
     */
    public void disconnect() {
        // Kill the thread that performs the hardware updates
        killThread = true;

        if (serialInput != null) {
            try {
                serialInput.close();
            } catch (IOException e) {
                // nothing to do if there is an issue closing the stream.
            }
        }
        if (serialOutput != null) {
            try {
                serialOutput.close();
            } catch (IOException e) {
                // nothing to do if there is an issue closing the stream.
            }
        }

        if (serialPort != null) {
            serialPort.close();
        }
    }

}
