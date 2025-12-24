/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.client;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.spi.ServiceProvider;
import io.calimero.KNXException;
import io.calimero.serial.spi.SerialCom;
import io.calimero.serial.spi.SerialConnectionProvider;

/**
 * The {@link SerialTransportAdapter} provides org.openhab.core.io.transport.serial
 * services to the Calimero library.
 * 
 * {@literal @}ServiceProvider annotation (biz.aQute.bnd.annotation) automatically creates the file
 * /META-INF/services/io.calimero.serial.spi.SerialConnectionProvider
 * to register SerialTransportAdapter to the service loader.
 * Additional attributes for SerialTransportAdapter can be specified as well, e.g.
 * attribute = { "position=1" }
 * and will be part of MANIFEST.MF
 * 
 * @author Holger Friedrich - Initial contribution
 */
@ServiceProvider(value = SerialConnectionProvider.class)
@NonNullByDefault
public class SerialTransportAdapter implements SerialConnectionProvider {
    private static final int OPEN_TIMEOUT_MS = 200;
    private static final int RECEIVE_TIMEOUT_MS = 5;
    private static final int RECEIVE_THRESHOLD = 1024;
    private static final int BAUDRATE = 19200;

    private Logger logger = LoggerFactory.getLogger(SerialTransportAdapter.class);
    @Nullable
    private static SerialPortManager serialPortManager = null;

    public static void setSerialPortManager(SerialPortManager serialPortManager) {
        SerialTransportAdapter.serialPortManager = serialPortManager;
    }

    public SerialTransportAdapter() {
    }

    public SerialCom open(@Nullable Settings settings) throws IOException, KNXException {
        if (settings == null) {
            throw new IOException("Settings not available");
        }
        if (settings.portId() == null) {
            throw new IOException("Port not available");
        }
        logger = LoggerFactory.getLogger(SerialTransportAdapter.class.getName() + ":" + settings.portId());

        final @Nullable SerialPortManager tmpSerialPortManager = serialPortManager;
        if (tmpSerialPortManager == null) {
            throw new IOException("PortManager not available");
        }
        try {
            SerialPortIdentifier portIdentifier = tmpSerialPortManager.getIdentifier(settings.portId());
            if (portIdentifier != null) {
                if (portIdentifier.isCurrentlyOwned()) {
                    logger.warn("Configured port {} is currently in use by another application: {}", settings.portId(),
                            portIdentifier.getCurrentOwner());
                }
                logger.trace("Trying to open port {}", settings.portId());
                SerialPort serialPort = portIdentifier.open(this.getClass().getName(), OPEN_TIMEOUT_MS);
                // apply default settings for com port, may be overwritten by caller
                serialPort.setSerialPortParams(BAUDRATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_EVEN);
                serialPort.enableReceiveThreshold(RECEIVE_THRESHOLD);
                serialPort.enableReceiveTimeout(RECEIVE_TIMEOUT_MS);

                // Notification / event listeners are available and may be used to log/trace com failures
                // serialPort.notifyOnDataAvailable(true);
                logger.trace("Port opened successfully");

                return new SerialComAdapter(serialPort);
            } else {
                logger.warn("Port {} not available", settings.portId());
                throw new IOException("Port " + settings.portId() + " not available");
            }
        } catch (PortInUseException e) {
            logger.warn("Port {} already in use", settings.portId());
            throw new IOException("Port " + settings.portId() + " already in use", e);
        } catch (UnsupportedCommOperationException e) {
            logger.warn("Port {} unsupported com operation", settings.portId());
            throw new IOException("Port " + settings.portId() + " unsupported com operation", e);
        }
    }

    // disable NonNullByDefault for this function, legacy interface List<String>
    @NonNullByDefault({})
    public Set<String> portIdentifiers() {
        final @Nullable SerialPortManager tmpSerialPortManager = serialPortManager;
        if (tmpSerialPortManager == null) {
            return Collections.emptySet();
        }
        // typecast only required to avoid warning about less-annotated type
        return tmpSerialPortManager.getIdentifiers().map(SerialPortIdentifier::getName).collect(Collectors.toSet());
    }
}
