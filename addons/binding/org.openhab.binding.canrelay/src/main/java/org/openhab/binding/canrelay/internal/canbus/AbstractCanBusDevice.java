/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.canbus;

import static org.openhab.binding.canrelay.internal.CanRelayBindingConstants.CANRELAY_PORT_NAME;
import static org.openhab.binding.canrelay.internal.canbus.CanBusDeviceStatus.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract CanBusDevice implementation. Contains generic logic to connect to serial port and prepare inputs and
 * outputs. Descendants can inherit from this class and implement device specific logic to setup the device and
 * implement the logic to send traffic over
 *
 * @author Lubos Housa - Initial contribution
 */
public abstract class AbstractCanBusDevice implements CanBusDevice, SerialPortEventListener {
    private static final Logger logger = LoggerFactory.getLogger(AbstractCanBusDevice.class);

    /** port read timeout */
    private static final int PORT_READ_TIMEOUT = 15000;
    /** canBus device startup timeout */
    private static final int STARTUP_TIMEOUT_MS = 100;
    /** thread pool size for executor to check connection */
    private static final int EXECUTOR_POOL_COUNT = 2;

    private static final Map<Integer, String> serialPortErrorDescriptions = new HashMap<Integer, String>();

    private static final int CONNECTION_WATCHDOG_STARTUP_DELAY_MS = 5000;
    private static final int CONNECTION_WATCHDOG_DEFAULT_DELAY_MS = 30000;
    static {
        serialPortErrorDescriptions.put(SerialPortEvent.BI, "Break interrupt");
        serialPortErrorDescriptions.put(SerialPortEvent.FE, "Frame error");
        serialPortErrorDescriptions.put(SerialPortEvent.OE, "Overrun error");
        serialPortErrorDescriptions.put(SerialPortEvent.PE, "Parity error");
    }

    private SerialPortManager serialPortManager;

    /** runtime variables to keep between connect and disconnect */
    @NonNull
    private CanBusDeviceStatus status;

    private String device;
    private SerialPort serialPort;

    /** we would allow descendants to talk to the device via these APIs */
    protected Reader input;
    protected Writer output;

    /** suppliers of new instances of reader and writer when needed */
    private final Function<InputStream, Reader> readerProvider;
    private final Function<OutputStream, Writer> writerProvider;

    /** executor service for background watch dog checking the connection periodically */
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(EXECUTOR_POOL_COUNT);
    private Future<?> watchdog;

    /** take default values of delays, but allow these to be changed */
    private int watchDogStartupDelayMs = CONNECTION_WATCHDOG_STARTUP_DELAY_MS;
    private int watchDogDelayMs = CONNECTION_WATCHDOG_DEFAULT_DELAY_MS;

    /** registered listeners */
    private final List<CanBusDeviceListener> listeners = new LinkedList<>();

    /**
     * Creates new instance of this can bus device
     *
     * @param readerProvider provider of new instances of reader for a given input stream. Used by this device at
     *                           runtime as needed when opening the ports
     * @param writerProvider provider of new instances of writer for a given output stream. Used by this device at
     *                           runtime as needed when opening the ports
     */
    public AbstractCanBusDevice(Function<InputStream, Reader> readerProvider,
            Function<OutputStream, Writer> writerProvider) {
        this.readerProvider = readerProvider;
        this.writerProvider = writerProvider;
        this.status = CanBusDeviceStatus.defaultStatus();
    }

    public AbstractCanBusDevice() {
        // by default no point in buffered versions, we need immediate output/input from the streams)
        this(input -> new InputStreamReader(input), output -> new OutputStreamWriter(output));
    }

    private void saveClose(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
                logger.warn("Some problem when closing reader/writer during disconnect... Closing the rest now.", e);
            }
        }
    }

    /**
     * Connect to the serial port and prepare streams to communicate to it
     *
     * @return true if all was OK, false otherwise
     */
    private boolean connectToPort(@NonNull SerialPortIdentifier port) {
        try {
            // ok, we have the port, so attempt to open to it now...
            logger.debug("Opening port {}", device);
            serialPort = port.open(CANRELAY_PORT_NAME, PORT_READ_TIMEOUT);

            // Configure Serial Port based on specified port speed
            logger.debug("Configuring serial port baudrate, data/stop bits and parity");
            CanBusDeviceConfiguration config = getCanBusDeviceConfiguration();
            serialPort.setSerialPortParams(config.getBaudRate(), config.getDataBits(), config.getStopBits(),
                    config.getParity());

            // SerialPort is ready, open the reader and writer for later use
            input = this.readerProvider.apply(serialPort.getInputStream());
            output = this.writerProvider.apply(serialPort.getOutputStream());
            logger.debug("SerialPort opened successfuly at {}", device);
            return true;
        } catch (PortInUseException e) {
            logger.warn("Unable to connect to port since it is already in use. ", e);
        } catch (UnsupportedCommOperationException e) {
            logger.warn("Unable to set baudrate/parity/stop and data bits on the device.", e);
        } catch (IOException e) {
            logger.warn("Unable to open input/output to the device", e);
        }
        return false;
    }

    private boolean registerListener() {
        try {
            serialPort.addEventListener(this);

            // Start listening for events
            serialPort.notifyOnDataAvailable(true);
            serialPort.notifyOnBreakInterrupt(true);
            serialPort.notifyOnFramingError(true);
            serialPort.notifyOnOverrunError(true);
            serialPort.notifyOnParityError(true);
            return true;
        } catch (IllegalArgumentException | TooManyListenersException e) {
            logger.warn("Problem registering listener and notifications on the serial port.", e);
            return false;
        }
    }

    private void unregisterSerialPortListener() {
        if (serialPort != null) {
            serialPort.removeEventListener();
        }
    }

    private void processSerialPortError(String typeName, SerialPortEvent portEvent) {
        if (portEvent.getNewValue()) {
            logger.warn("An error event detected on the CANBUS Device. Type of the error: {}", typeName);
            notifyListenersDeviceError("The CanBusDevice serial port error " + typeName + " detected.");
        }
    }

    /**
     * Get the configuration for this can bus device in order to setup the port properly. The attributes are to
     * configure the serial port connected to the machine
     *
     * @return specific settings of this very device
     */
    protected abstract CanBusDeviceConfiguration getCanBusDeviceConfiguration();

    /**
     * Prepare device for the first use after the port was prepared and connect it to the underlying CANBUS. The
     * descendant should do device specific logic (i.e. talk to the device using input and output reader and writer in
     * this class. The method would only be called when the port was opened just fine, but descendants should still
     * check it for null in cases when devices are being disconnected. This device should get connected to the CANBUS
     * network at the given baudrate by invoking this method.
     *
     * This method does not need to be synchronized, the parent logic assures only 1 thread can invoke this method.
     *
     * @param canBusBaudRate desired baudRate of the CANBUS to setup this device to use
     * @return true if all was OK, false otherwise
     */
    protected abstract boolean connectToCanBus(int canBusBaudRate);

    /**
     * Checks the connection to the serial port using input and/or output. This method would get periodically called in
     * order to check whether the connection is OK or not
     *
     * @throws IOException thrown to indicate some error happened and the connection should be considered not OK anymore
     */
    protected abstract void checkConnection() throws IOException;

    /**
     * Handle some can bus data available and read by this device. Descendants should read the data available in the
     * input reader and act accordingly. Default implementation does nothing
     */
    protected void processIncomingData() {
    }

    /**
     * Close the device during disconnect. This method would be called after any listeners are removed and before input
     * and output is disabled. Default implementation does nothing
     */
    protected void closeDevice() {
    }

    /**
     * Notify the registered listeners about a new CANMessage received
     *
     * @param canMessage received CANMesage to broadcast
     */
    protected void notifyListeners(CanMessage canMessage) {
        listeners.forEach((listener) -> listener.onMessage(canMessage));
    }

    /**
     * Notify the registered listeners that the device is ready now
     */
    protected void notifyListenersDeviceReady() {
        listeners.forEach((listener) -> listener.onDeviceReady());
    }

    /**
     * Notify the registered listeners that the device reported some error
     *
     * @param desc description of the error
     */
    protected void notifyListenersDeviceError(String desc) {
        listeners.forEach((listener) -> listener.onDeviceError(desc));
    }

    /**
     * Notify the registered listeners that the device reported fatal error (not recoverable)
     *
     * @param desc description of the error
     */
    protected void notifyListenersDeviceFatalError(String desc) {
        listeners.forEach((listener) -> listener.onDeviceFatalError(desc));
    }

    /**
     * Sink in all data currently available in the input
     *
     * @throws IOException
     */
    protected void readAll() throws IOException {
        if (input == null) {
            logger.debug("input not set, unable to read anything from the device...");
            return;
        }

        while (input.ready()) {
            input.read();
        }
    }

    /**
     * Sleep the current thread for a given ammount of ms sinking any interrupted exception
     *
     * @param ms how many ms to sleep
     */
    protected void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignore) {
        }
    }

    @Override
    public synchronized CanBusDeviceStatus connect(@NonNull String device, int canBusBaudrate) {
        if (this.device != null) {
            logger.warn(
                    "Attempting to connect to device {} while this CanBusDevice is already connected to {}. Call disconnect first!",
                    device, this.device);
            return CanBusDeviceStatus.defaultStatus();
        }
        logger.debug("Connecting to CanBusDevice {}", device);
        this.status = CONNECTING;
        this.device = device;

        if (serialPortManager == null) {
            logger.warn(
                    "serialPortManager not set! Unable to connect to any port as a result. Call setSerialPortManager first!",
                    device);
            status = MISSING_PORT_ERROR;
            return status;
        }

        SerialPortIdentifier port = serialPortManager.getIdentifier(device);
        if (port == null) {
            logger.warn("Port {} was not found. Make sure you configured openHAB properly to be able to access it. ",
                    device);
            status = MISSING_PORT_ERROR;
            return status;
        }
        if (!connectToPort(port)) {
            status = PORT_CONNECT_ERROR;
            return status;
        }
        if (!connectToCanBus(canBusBaudrate)) {
            status = CANBUS_CONNECT_ERROR;
            return status;
        }
        if (!registerListener()) {
            status = LISTENERS_ERROR;
            return status;
        }

        // in some cases the startup is too fast and some initial events back from the device lost, so let some time
        // for the serial port listener to be ready and then sink all available data from input
        sleep(STARTUP_TIMEOUT_MS);
        try {
            readAll();
        } catch (IOException e) {
            logger.warn(
                    "Some error occured on startup after initializing it fully. An attempt to read all available data from the input of the device failed. Setting the device to an error state",
                    e);
            status = COMM_ERROR;
            return status;
        }

        this.status = CONNECTED;
        notifyListenersDeviceReady();
        registerWatchDog();
        return status;
    }

    private void registerWatchDog() {
        logger.debug("Registering watchdog to periodically check the CANBusDevice connection every {} seconds",
                watchDogDelayMs);
        watchdog = executor.scheduleWithFixedDelay(() -> {
            try {
                logger.debug("Checking CanBusDevice connection...");
                synchronized (this) {
                    checkConnection();
                }
                logger.debug("CanBusDevice connection is OK.");
            } catch (IOException e) {
                logger.warn(
                        "An error occurred during check of CanBusDevice connection, most likely the device is not working properly anymore.",
                        e);
                notifyListenersDeviceFatalError(e.getMessage());
            }
        }, watchDogStartupDelayMs, watchDogDelayMs, TimeUnit.MILLISECONDS);
    }

    private void unregisterWatchDog() {
        logger.debug("Unregistering watchdog checking the connection since it is no longer needed...");
        if (watchdog != null) {
            watchdog.cancel(false);
        }
    }

    /**
     * Reset the watchdog delay from the default value to the in-passed value
     *
     * @param delayMs new delay of the watch dog
     */
    public void setWatchdogDelay(int delayMs) {
        this.watchDogDelayMs = delayMs;
    }

    /**
     * Reset the watchdog startup delay from the default value to the in-passed value (how long to wait for first
     * watchdog run)
     *
     * @param delayMs new delay of the watch dog
     */
    public void setWatchdogStartupDelay(int delayMs) {
        this.watchDogStartupDelayMs = delayMs;
    }

    @Override
    public final synchronized void disconnect() {
        if (this.device == null) {
            logger.warn("Attempt to disconnect the device, but no device was connected earlier. Ignoring.");
            return;
        }

        unregisterWatchDog();
        closeDevice();
        unregisterSerialPortListener();

        String device = this.device;
        logger.debug("Disconnecting CanBusDevice {}", device);
        saveClose(input);
        saveClose(output);
        saveClose(serialPort);

        // now erase all the variables
        this.status = defaultStatus();
        this.input = null;
        this.output = null;
        this.serialPort = null;
        this.device = null;
        logger.debug("CanBusDevice {} disconnected", device);
    }

    @Override
    public CanBusDeviceStatus getStatus() {
        return status;
    }

    @Override
    public synchronized void serialEvent(SerialPortEvent event) {
        if (SerialPortEvent.DATA_AVAILABLE == event.getEventType()) {
            logger.debug("New data available in the device, processing...");
            processIncomingData();
        } else {
            String errorEvent = serialPortErrorDescriptions.get(event.getEventType());
            if (errorEvent != null) { // only some error events are recognized
                processSerialPortError(errorEvent, event);
            }
        }
    }

    @Override
    public void registerCanBusDeviceListener(CanBusDeviceListener listener) {
        listeners.add(listener);
    }

    @Override
    public void setSerialPortManager(SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }
}
