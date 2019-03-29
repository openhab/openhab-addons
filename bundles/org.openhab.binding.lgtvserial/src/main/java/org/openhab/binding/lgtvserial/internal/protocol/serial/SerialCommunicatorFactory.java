/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.HashMap;
import java.util.Map;

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

    private Map<String, LGSerialCommunicator> instances = new HashMap<>();

    public synchronized LGSerialCommunicator getInstance(String port) {
        LGSerialCommunicator comm = instances.get(port);
        if (comm == null) {
            comm = createCommunicator(port);
            if (comm != null) {
                instances.put(port, comm);
            }
        }
        return comm;
    }

    private LGSerialCommunicator createCommunicator(final String portName) {
        return new LGSerialCommunicator(portName, new RegistrationCallback() {
            @Override
            public void onUnregister() {
                logger.debug("Unregistered last handler, closing");
                deleteInstance(portName);
            }
        });
    }

    protected synchronized void deleteInstance(String port) {
        instances.remove(port).close();
    }

}
