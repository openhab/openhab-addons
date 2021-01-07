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
package org.openhab.binding.cul.internal.serial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.TooManyListenersException;

import org.openhab.binding.cul.CULCommunicationException;
import org.openhab.binding.cul.internal.AbstractCULHandler;
import org.openhab.binding.cul.internal.CULConfig;
import org.openhab.binding.cul.internal.CULDeviceException;
import org.openhab.core.io.transport.serial.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation for culfw based devices which communicate via serial port
 * (cullite for example). This is based on rxtx and assumes constant parameters
 * for the serial port.
 *
 * @author Till Klocke - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.4.0
 */
public class CULSerialHandlerImpl extends AbstractCULHandler<CULSerialConfig> implements SerialPortEventListener {

    private final static Logger log = LoggerFactory.getLogger(CULSerialHandlerImpl.class);
    private final SerialPortManager serialPortManager;

    private SerialPort serialPort;
    private InputStream is;
    private OutputStream os;

    /**
     * Constructor including property map for specific configuration.
     *
     * @param config
     *            Configuration for this implementation.
     */
    public CULSerialHandlerImpl(CULConfig config, SerialPortManager serialPortManager) {
        super((CULSerialConfig) config);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                processNextLine();
            } catch (CULCommunicationException e) {
                log.error("Serial CUL connection read failed for {}", config.getDeviceAddress());
            }
        }
    }

    @Override
    protected void openHardware() throws CULDeviceException {
        String serialPortName = config.getDeviceAddress();
        log.debug("Opening serial CUL connection for {}", serialPortName);
        try {
            SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
            if (portIdentifier == null) {
                log.warn("Could not find serial port with name {}", serialPortName);
                throw new CULDeviceException("Could not find serial port with name " + serialPortName);
            }
            serialPort = portIdentifier.open(this.getClass().getName(), 2000);
            serialPort.setSerialPortParams(config.getBaudRate(), SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    config.getParityMode());
            is = serialPort.getInputStream();
            os = serialPort.getOutputStream();
            br = new BufferedReader(new InputStreamReader(is));
            bw = new BufferedWriter(new OutputStreamWriter(os));

            serialPort.notifyOnDataAvailable(true);
            log.debug("Adding serial port event listener");
            serialPort.addEventListener(this);
        } catch (PortInUseException e) {
            throw new CULDeviceException(e);
        } catch (UnsupportedCommOperationException e) {
            throw new CULDeviceException(e);
        } catch (IOException e) {
            throw new CULDeviceException(e);
        } catch (TooManyListenersException e) {
            throw new CULDeviceException(e);
        }
    }

    @Override
    protected void closeHardware() {
        log.debug("Closing serial device {}", config.getDeviceAddress());
        if (serialPort != null) {
            serialPort.removeEventListener();
        }
        try {
            if (br != null) {
                br.close();
            }
            if (bw != null) {
                bw.close();
            }
        } catch (IOException e) {
            log.error("Can't close the input and output streams propberly", e);
        } finally {
            if (serialPort != null) {
                serialPort.close();
            }
        }
    }
}
