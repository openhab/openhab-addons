/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.device;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.openhab.binding.dsmr.device.DSMRDeviceConstants.DeviceStateDetail;
import org.openhab.binding.dsmr.device.p1telegram.P1TelegramParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * Class that implements the DSMR port meters that comply to the Dutch Smart Meter Requirements.
 * <p>
 * This class provides a simple public interface: read and close.
 * <p>
 * The read method will claim OS resources if necessary. If the read method
 * encounters problems it will automatically close itself
 * <p>
 * The close method can be called asynchronous and will release OS resources.
 * <p>
 * In this way the DSMR port can restore the connection automatically
 * <p>
 * <code>
 * An example DSMR telegram in accordance to IEC 62056-21 Mode D.<br>
 * /ISk5\2MT382-1000<br>
 * 0-0:96.1.1(4B384547303034303436333935353037)<br>
 * 1-0:1.8.1(12345.678*kWh)<br>
 * 1-0:1.8.2(12345.678*kWh)<br>
 * 1-0:2.8.1(12345.678*kWh)<br>
 * 1-0:2.8.2(12345.678*kWh)<br>
 * 0-0:96.14.0(0002)<br>
 * 1-0:1.7.0(001.19*kW)<br>
 * 1-0:2.7.0(000.00*kW)<br>
 * 0-0:17.0.0(016*A)<br>
 * 0-0:96.3.10(1)<br>
 * 0-0:96.13.1(303132333435363738)<br>
 * 0-0:96.13.0(303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F<br>
 * 303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F<br>
 * 303132333435363738393A3B3C3D3E3F)<br>
 * 0-1:96.1.0(3232323241424344313233343536373839)<br>
 * 0-1:24.1.0(03)<br>
 * 0-1:24.3.0(090212160000)(00)(60)(1)(0-1:24.2.1)(m3)<br>
 * (00000.000)<br>
 * 0-1:24.4.0(1)<br>
 * !<br>
 * </code>
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class DSMRPort {
    /* logger */
    private static final Logger logger = LoggerFactory.getLogger(DSMRPort.class);

    /* private object variables */
    private final String portName;
    private final int readTimeoutMSec;

    /* serial port resources */
    private SerialPort serialPort;
    private BufferedInputStream bis;
    private byte[] buffer = new byte[1024]; // 1K

    /* state variables */
    private DSMRPortSettings portSettings;
    private DSMRPortSettings fixedPortSettings; // Used if DSMR binding has a static port configuration
    private boolean isOpen = false;

    /* helpers */
    private P1TelegramParser p1Parser;

    /*
     * The portLock is used for the shared data used when opening and closing
     * the port. The following shared data must be guarded by the lock:
     * SerialPort, BufferedReader
     */
    private Object portLock = new Object();

    /**
     * Creates a new DSMRPort. This is only a reference to a port. The port will
     * not be opened nor it is checked if the DSMR Port can successfully be
     * opened.
     *
     * @param portName
     *            Device identifier of the post (e.g. /dev/ttyUSB0)
     * @param p1Parser
     *            {@link P1TelegramParser}
     * @param readTimeoutMSec
     *            communication timeout in milliseconds
     * @param fixedPortSettings
     *            {@link PortSettings} object containing fixed port settings. This parameter
     *            may be null. The binding will then use specification default settings
     *            HIGH_SPEED (i.e. 115200 8N1) and LOW_SPEED (9600 7E1) and auto detect which
     *            is applicable.
     *            If the parameter is set, the binding will ONLY use the specified settings
     *            auto detect functionality will only use the specified settings.
     */
    public DSMRPort(String portName, P1TelegramParser p1Parser, int readTimeoutMSec,
            DSMRPortSettings fixedPortSettings) {
        this.portName = portName;
        this.readTimeoutMSec = readTimeoutMSec;
        this.p1Parser = p1Parser;
        this.fixedPortSettings = fixedPortSettings;

        portSettings = DSMRPortSettings.HIGH_SPEED_SETTINGS;
    }

    /**
     * Closes the DSMRPort and release OS resources
     */
    public void close() {
        synchronized (portLock) {
            logger.info("Closing DSMR port");

            // Close resources
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    logger.debug("Failed to close reader", ioe);
                }
            }
            if (serialPort != null) {
                serialPort.close();
            }

            // Release resources
            bis = null;
            serialPort = null;

            isOpen = false;
        }
    }

    /**
     * Start reading from the DSMR port.
     *
     * @return {@link DeviceStateDetail} containing the details about the DeviceState
     */
    public DeviceStateDetail read() {
        // open port if it is not open
        if (!isOpen) {
            logger.warn("DSMRPort is not open, no values will be read");

            close();

            return DeviceStateDetail.PORT_NOT_OPEN;
        }

        try {
            // Read without block
            int bytesAvailable = bis.available();
            while (bytesAvailable > 0) {
                int bytesRead = bis.read(buffer, 0, Math.min(bytesAvailable, buffer.length));

                if (bytesRead > 0) {
                    p1Parser.parseData(buffer, 0, bytesRead);
                } else {
                    logger.debug("Expected bytes {} to read, but {} bytes were read", bytesAvailable, bytesRead);
                }
                bytesAvailable = bis.available();
            }
            return DeviceStateDetail.PORT_READ_OK;
        } catch (IOException ioe) {
            /*
             * Read is interrupted. This can be due to a broken connection or
             * closing the port
             */
            if (!isOpen) {
                // Closing on purpose
                logger.info("Read aborted: DSMRPort is closed");

                return DeviceStateDetail.PORT_NOT_OPEN;
            } else {
                // Closing due to broken connection

                logger.warn("DSMRPort is not available anymore, closing port");
                logger.debug("Caused by:", ioe);

                close();

                return DeviceStateDetail.PORT_READ_ERROR;
            }
        } catch (NullPointerException npe) {
            if (!isOpen) {
                // Port was closed
                logger.info("Read aborted: DSMRPort is closed");
            } else {
                logger.error("Unexpected problem occured", npe);

                close();
            }
            return DeviceStateDetail.PORT_NOT_OPEN;
        }
    }

    /**
     * Switch the Serial Port speed (LOW --> HIGH and vice versa).
     */
    void switchPortSpeed() {
        if (fixedPortSettings == null) {
            logger.debug("No fixed port setting (autodetect ENABLED), switch between specification standard settings");

            // Checking instance reference here since these are final
            if (portSettings == DSMRPortSettings.HIGH_SPEED_SETTINGS) {
                portSettings = DSMRPortSettings.LOW_SPEED_SETTINGS;
            } else {
                portSettings = DSMRPortSettings.HIGH_SPEED_SETTINGS;
            }
            logger.debug("Switched port settings to: {}", portSettings);
        } else {
            portSettings = fixedPortSettings;
            logger.info("Fixed port settings configured (autodetect DISABLED): {}", portSettings);
        }
    }

    /**
     * Checks if the given port name is autodetected by gnu.io or already listed
     * in the system property gnu.io.rxtx.SerialPorts
     *
     * @param portName String containing the port name to lookup
     * @return true if port exists, false otherwise
     */
    private boolean portExists(String portName) {
        @SuppressWarnings("unchecked")
        Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();

        boolean portExists = false;

        logger.debug("Searching autodetected ports for: {}", portName);
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                logger.debug("Found serial port: {}", portIdentifier.getName());
                if (portIdentifier.getName().equals(portName)) {
                    portExists = true;
                }
            }
        }
        if (!portExists) {
            Properties properties = System.getProperties();
            String currentPorts = properties.getProperty("gnu.io.rxtx.SerialPorts", "");
            if (currentPorts.indexOf(portName) >= 0) {
                logger.debug("{} is listed in system property gnu.io.rxtx.SerialPorts", portName);

                portExists = true;
            } else {
                logger.debug("{} is not listed in system property gnu.io.rxtx.SerialPorts", portName);
            }
        }
        return portExists;
    }

    /**
     * Opens the Operation System Serial Port
     * <p>
     * This method opens the port and set Serial Port parameters according to
     * the DSMR specification. Since the specification is clear about these
     * parameters there are not configurable.
     * <p>
     * If there are problem while opening the port, it is the responsibility of
     * the calling method to handle this situation (and for example close the
     * port again).
     * <p>
     * Opening an already open port is harmless. The method will return
     * immediately
     *
     * @return {@link DeviceStateDetail} containing the details about the DeviceState
     */
    public DeviceStateDetail open() {
        synchronized (portLock) {
            // Sanity check
            if (isOpen) {
                logger.debug("Serial Port is already open, keep current port instance");

                return DeviceStateDetail.PORT_OK;
            }

            try {
                // GNU.io autodetects standard serial port names
                // Add non standard port names if not exists (fixes part of #4175)
                if (!portExists(portName)) {
                    logger.warn("Port {} does not exists according to the system, we will still try to open it",
                            portName);
                }
                // Opening Operating System Serial Port
                logger.debug("Creating CommPortIdentifier");
                CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
                logger.debug("Opening CommPortIdentifier");
                CommPort commPort = portIdentifier.open("org.openhab.binding.dsmr", readTimeoutMSec);
                logger.debug("Configure serial port");
                serialPort = (SerialPort) commPort;
                serialPort.enableReceiveThreshold(1);
                serialPort.enableReceiveTimeout(readTimeoutMSec);

                // Configure Serial Port based on specified port speed
                logger.debug("Configure serial port parameters: {}", portSettings);

                if (portSettings != null) {
                    serialPort.setSerialPortParams(portSettings.getBaudrate(), portSettings.getDataBits(),
                            portSettings.getStopbits(), portSettings.getParity());

                    /* special settings for low speed port (checking reference here) */
                    if (portSettings == DSMRPortSettings.LOW_SPEED_SETTINGS) {
                        serialPort.setDTR(false);
                        serialPort.setRTS(true);
                    }
                } else {
                    logger.error("Invalid port parameters, closing port:{}", portSettings);

                    return DeviceStateDetail.PORT_CONFIGURATION_ERROR;
                }
            } catch (NoSuchPortException nspe) {
                logger.error("Port {} does not exists", portName, nspe);

                return DeviceStateDetail.PORT_CONFIGURATION_NO_PORT;
            } catch (PortInUseException piue) {
                logger.error("Port already in use: {}", portName, piue);

                return DeviceStateDetail.PORT_IN_USE;
            } catch (UnsupportedCommOperationException ucoe) {
                logger.error(
                        "Port does not support requested port settings " + "(invalid dsmr:portsettings parameter?): {}",
                        portName, ucoe);

                return DeviceStateDetail.PORT_NOT_COMPATIBLE;
            }

            // SerialPort is ready, open the reader
            logger.info("SerialPort opened successful");
            try {
                bis = new BufferedInputStream(serialPort.getInputStream());
            } catch (IOException ioe) {
                logger.error("Failed to get inputstream for serialPort. Closing port", ioe);

                return DeviceStateDetail.PORT_ERROR;
            }
            logger.info("DSMR Port opened successful");
            isOpen = true;

            return DeviceStateDetail.PORT_OK;
        }
    }
}
