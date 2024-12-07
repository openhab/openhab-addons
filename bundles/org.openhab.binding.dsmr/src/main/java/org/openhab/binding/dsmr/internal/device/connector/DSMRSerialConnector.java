/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.device.connector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dsmr.internal.DSMRBindingConstants;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that implements the DSMR port functionality that comply to the Dutch Smart Meter Requirements.
 * <p>
 * This class will handle communication with the Serial Port itself and notify listeners about events and received P1
 * telegrams.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Split code and moved DSMR specific handling to separate class.
 */
@NonNullByDefault
public class DSMRSerialConnector extends DSMRBaseConnector implements SerialPortEventListener {
    /**
     * Serial port read time out in milliseconds.
     */
    private static final int SERIAL_PORT_READ_TIMEOUT_MILLISECONDS = (int) TimeUnit.SECONDS.toMillis(15);

    /**
     * The receive threshold/time out set on the serial port.
     */
    private static final int SERIAL_TIMEOUT_MILLISECONDS = 1000;

    private final Logger logger = LoggerFactory.getLogger(DSMRSerialConnector.class);

    /**
     * Manager to get new port from.
     */
    private final SerialPortManager portManager;

    /**
     * Serial port name.
     */
    private final String serialPortName;

    /**
     * Serial port instance.
     */
    private final AtomicReference<@Nullable SerialPort> serialPortReference = new AtomicReference<>();

    /**
     * DSMR Connector listener.
     */
    private final DSMRConnectorListener dsmrConnectorListener;

    /**
     * The portLock is used for the shared data used when opening and closing
     * the port. The following shared data must be guarded by the lock:
     * SerialPort, BufferedReader
     */
    private final Object portLock = new Object();

    /**
     * Creates a new DSMR serial connector. This is only a reference to a port. The port will
     * not be opened nor it is checked if the DSMR Port can successfully be opened.
     *
     * @param portManager Serial Port Manager
     * @param serialPortName Device identifier of the port (e.g. /dev/ttyUSB0)
     * @param dsmrConnectorListener The listener to send error or received data from the port
     */
    public DSMRSerialConnector(final SerialPortManager portManager, final String serialPortName,
            final DSMRConnectorListener dsmrConnectorListener) {
        super(dsmrConnectorListener);
        this.portManager = portManager;
        this.serialPortName = serialPortName;
        this.dsmrConnectorListener = dsmrConnectorListener;
    }

    public String getPortName() {
        return serialPortName;
    }

    /**
     * Opens the Operation System Serial Port.
     *
     * @param portSettings The serial port settings to open the port with
     */
    public void open(final DSMRSerialSettings portSettings) {
        DSMRErrorStatus errorStatus = null;

        synchronized (portLock) {
            final SerialPortIdentifier portIdentifier = portManager.getIdentifier(serialPortName);
            if (portIdentifier == null) {
                logger.debug("Port {} does not exists", serialPortName);

                errorStatus = DSMRErrorStatus.PORT_DONT_EXISTS;
            } else {
                errorStatus = open(portSettings, portIdentifier);
            }
            if (errorStatus != null) {
                // handle event within lock
                dsmrConnectorListener.handleError(errorStatus, "");
            }
        }
    }

    private @Nullable DSMRErrorStatus open(final DSMRSerialSettings portSettings,
            final SerialPortIdentifier portIdentifier) {
        DSMRErrorStatus errorStatus = null;

        try {
            logger.trace("Opening port {}", serialPortName);
            final SerialPort oldSerialPort = serialPortReference.get();

            // Opening Operating System Serial Port
            final SerialPort serialPort = portIdentifier.open(DSMRBindingConstants.DSMR_PORT_NAME,
                    SERIAL_PORT_READ_TIMEOUT_MILLISECONDS);

            // Configure Serial Port based on specified port speed
            logger.trace("Configure serial port parameters: {}", portSettings);
            serialPort.setSerialPortParams(portSettings.getBaudrate(), portSettings.getDataBits(),
                    portSettings.getStopbits(), portSettings.getParity());

            // SerialPort is ready, open the reader
            logger.trace("SerialPort opened successful on {}", serialPortName);
            open(serialPort.getInputStream());

            serialPort.addEventListener(this);

            // Start listening for events
            serialPort.notifyOnDataAvailable(true);
            serialPort.notifyOnBreakInterrupt(true);
            serialPort.notifyOnFramingError(true);
            serialPort.notifyOnOverrunError(true);
            serialPort.notifyOnParityError(true);

            try {
                serialPort.enableReceiveThreshold(SERIAL_TIMEOUT_MILLISECONDS);
            } catch (final UnsupportedCommOperationException e) {
                logger.debug("Enable receive threshold is unsupported");
            }
            try {
                serialPort.enableReceiveTimeout(SERIAL_TIMEOUT_MILLISECONDS);
            } catch (final UnsupportedCommOperationException e) {
                logger.debug("Enable receive timeout is unsupported");
            }
            // The binding is ready, let the meter know we want to receive values
            serialPort.setRTS(true);
            if (!serialPortReference.compareAndSet(oldSerialPort, serialPort)) {
                logger.warn("Possible bug because a new serial port value was set during opening new port.");
                errorStatus = DSMRErrorStatus.PORT_INTERNAL_ERROR;
            }
        } catch (final IllegalStateException ise) {
            logger.debug("Failed communicating, probably time out", ise);

            errorStatus = DSMRErrorStatus.PORT_PORT_TIMEOUT;
        } catch (final IOException ioe) {
            logger.debug("Failed to get inputstream for serialPort", ioe);

            errorStatus = DSMRErrorStatus.SERIAL_DATA_READ_ERROR;
        } catch (final TooManyListenersException tmle) {
            logger.warn("Possible bug because a listener was added while one already set.", tmle);

            errorStatus = DSMRErrorStatus.PORT_INTERNAL_ERROR;
        } catch (final PortInUseException piue) {
            logger.debug("Port already in use: {}", serialPortName, piue);

            errorStatus = DSMRErrorStatus.PORT_IN_USE;
        } catch (final UnsupportedCommOperationException ucoe) {
            logger.debug("Port does not support requested port settings (invalid dsmr:portsettings parameter?): {}",
                    serialPortName, ucoe);

            errorStatus = DSMRErrorStatus.PORT_NOT_COMPATIBLE;
        }
        return errorStatus;
    }

    /**
     * Closes the serial port and release OS resources.
     */
    @Override
    public void close() {
        synchronized (portLock) {
            logger.debug("Closing DSMR serial port");

            // Stop listening for serial port events
            SerialPort serialPort = serialPortReference.get();

            if (serialPort != null) {
                // Let meter stop sending values
                serialPort.setRTS(false);
                serialPort.removeEventListener();
                try {
                    final InputStream inputStream = serialPort.getInputStream();

                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (final IOException ioe) {
                    logger.debug("Failed to close serial port inputstream", ioe);
                }
                serialPort.close();
            }
            super.close();
            serialPort = null;
        }
    }

    /**
     * Set serial port settings or restarts the connection with the port settings if it's not open.
     *
     * @param portSettings the port settings to set on the serial port
     */
    public void setSerialPortParams(final DSMRSerialSettings portSettings) {
        synchronized (portLock) {
            if (isOpen()) {
                logger.debug("Update port {} with settings: {}", this.serialPortName, portSettings);
                try {
                    final SerialPort serialPort = serialPortReference.get();

                    if (serialPort != null) {
                        serialPort.setSerialPortParams(portSettings.getBaudrate(), portSettings.getDataBits(),
                                portSettings.getStopbits(), portSettings.getParity());
                    }
                } catch (final UnsupportedCommOperationException e) {
                    logger.debug(
                            "Port does {} not support requested port settings (invalid dsmr:portsettings parameter?): {}",
                            serialPortName, portSettings);
                    dsmrConnectorListener.handleError(DSMRErrorStatus.PORT_NOT_COMPATIBLE,
                            Objects.requireNonNullElse(e.getMessage(), ""));
                }
            } else {
                restart(portSettings);
            }
        }
    }

    /**
     * Switch the Serial Port speed (LOW --> HIGH and vice versa).
     */
    public void restart(final DSMRSerialSettings portSettings) {
        synchronized (portLock) {
            logger.trace("Restart port {} with settings: {}", this.serialPortName, portSettings);
            close();
            open(portSettings);
        }
    }

    @Override
    public void serialEvent(@Nullable final SerialPortEvent seEvent) {
        if (seEvent == null) {
            return;
        }
        if (logger.isTraceEnabled() && SerialPortEvent.DATA_AVAILABLE != seEvent.getEventType()) {
            logger.trace("Serial event: {}, new value:{}", seEvent.getEventType(), seEvent.getNewValue());
        }
        try {
            switch (seEvent.getEventType()) {
                case SerialPortEvent.DATA_AVAILABLE:
                    handleDataAvailable();
                    break;
                case SerialPortEvent.BI:
                    handleErrorEvent("Break interrupt", seEvent);
                    break;
                case SerialPortEvent.FE:
                    handleErrorEvent("Frame error", seEvent);
                    break;
                case SerialPortEvent.OE:
                    handleErrorEvent("Overrun error", seEvent);
                    break;
                case SerialPortEvent.PE:
                    handleErrorEvent("Parity error", seEvent);
                    break;
                default: // do nothing
            }
        } catch (final RuntimeException e) {
            logger.warn("RuntimeException during handling serial event: {}", seEvent.getEventType(), e);
        }
    }

    /**
     * Handles an error event. If open and it's a new value it should be handled by the listener.
     *
     * @param typeName type of the event, used in logging only
     * @param portEvent Serial port event that triggered the error.
     */
    private void handleErrorEvent(final String typeName, final SerialPortEvent portEvent) {
        if (isOpen() && portEvent.getNewValue()) {
            logger.trace("New DSMR port {} event", typeName);
            dsmrConnectorListener.handleError(DSMRErrorStatus.SERIAL_DATA_READ_ERROR, "");
        }
    }

    @Override
    protected void handleDataAvailable() {
        // open port if it is not open
        if (serialPortReference.get() == null) {
            logger.debug("Serial port is not open, no values will be read");
            return;
        }
        super.handleDataAvailable();
    }
}
