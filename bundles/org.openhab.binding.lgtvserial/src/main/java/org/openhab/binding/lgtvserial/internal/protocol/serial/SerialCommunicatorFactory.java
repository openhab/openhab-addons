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
package org.openhab.binding.lgtvserial.internal.protocol.serial;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to create and reuse singleton objects associated to a given COM port.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public class SerialCommunicatorFactory {

    private final Logger logger = LoggerFactory.getLogger(SerialCommunicatorFactory.class);

    private Map<SerialPortIdentifier, LGSerialCommunicator> instances = new HashMap<>();

    public synchronized LGSerialCommunicator getInstance(SerialPortIdentifier serialPortIdentifier)
            throws IOException, PortInUseException, UnsupportedCommOperationException {
        LGSerialCommunicator comm = instances.get(serialPortIdentifier);
        if (comm == null) {
            comm = createCommunicator(serialPortIdentifier);
            if (comm != null) {
                instances.put(serialPortIdentifier, comm);
            }
        }
        return comm;
    }

    private LGSerialCommunicator createCommunicator(final SerialPortIdentifier serialPortIdentifier)
            throws IOException, PortInUseException, UnsupportedCommOperationException {
        return new LGSerialCommunicator(serialPortIdentifier, new RegistrationCallback() {
            @Override
            public void onUnregister() {
                logger.debug("Unregistered last handler, closing");
                deleteInstance(serialPortIdentifier);
            }
        });
    }

    protected synchronized void deleteInstance(SerialPortIdentifier serialPortIdentifier) {
        instances.remove(serialPortIdentifier).close();
    }
}
