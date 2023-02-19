/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkFT12;
import tuwien.auto.calimero.link.medium.TPSettings;

/**
 * Serial specific {@link AbstractKNXClient} implementation.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class SerialClient extends AbstractKNXClient {

    private static final String CALIMERO_ERROR_CANNOT_OPEN_PORT = "failed to open serial port";

    private final Logger logger = LoggerFactory.getLogger(SerialClient.class);

    private final SerialPortManager serialPortManager;
    private final String serialPort;
    private final boolean useCemi;

    public SerialClient(int autoReconnectPeriod, ThingUID thingUID, int responseTimeout, int readingPause,
            int readRetriesLimit, ScheduledExecutorService knxScheduler, String serialPort, boolean useCemi,
            SerialPortManager serialPortManager, StatusUpdateCallback statusUpdateCallback) {
        super(autoReconnectPeriod, thingUID, responseTimeout, readingPause, readRetriesLimit, knxScheduler,
                statusUpdateCallback);
        this.serialPortManager = serialPortManager;
        this.serialPort = serialPort;
        this.useCemi = useCemi;
    }

    @Override
    protected KNXNetworkLink establishConnection() throws KNXException, InterruptedException {
        try {
            logger.debug("Establishing connection to KNX bus through FT1.2 on serial port {}{}.", serialPort,
                    (useCemi ? " using CEMI" : ""));
            // CEMI support by Calimero library, userful for newer serial devices like KNX RF sticks, kBerry,
            // etc.; default is still old EMI frame format
            if (useCemi) {
                return KNXNetworkLinkFT12.newCemiLink(serialPort, new TPSettings());
            }
            return new KNXNetworkLinkFT12(serialPort, new TPSettings());

        } catch (NoClassDefFoundError e) {
            throw new KNXException(
                    "The serial FT1.2 KNX connection requires the RXTX libraries to be available, but they could not be found!",
                    e);
        } catch (KNXException e) {
            final String msg = e.getMessage();
            // TODO add a test for this string match; error message might change in later version of Calimero library
            if ((msg != null) && (msg.startsWith(CALIMERO_ERROR_CANNOT_OPEN_PORT))) {
                String availablePorts = serialPortManager.getIdentifiers().map(SerialPortIdentifier::getName)
                        .collect(Collectors.joining("\n"));
                if (!availablePorts.isEmpty()) {
                    availablePorts = " Available ports are:\n" + availablePorts;
                }
                throw new KNXException("Serial port '" + serialPort + "' could not be opened." + availablePorts);
            } else {
                throw e;
            }
        }
    }
}
