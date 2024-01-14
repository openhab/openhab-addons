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
package org.openhab.binding.knx.internal.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler.CommandExtensionData;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.Connection.BlockingMode;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkFT12;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.serial.FT12Connection;

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
            SerialPortManager serialPortManager, CommandExtensionData commandExtensionData,
            StatusUpdateCallback statusUpdateCallback) {
        super(autoReconnectPeriod, thingUID, responseTimeout, readingPause, readRetriesLimit, knxScheduler,
                commandExtensionData, statusUpdateCallback);
        this.serialPortManager = serialPortManager;
        this.serialPort = serialPort;
        this.useCemi = useCemi;
    }

    /**
     * try automatic detection of cEMI devices via the PEI identification frame
     *
     * @implNote This is based on an vendor specific extension and may not work for other devices.
     */
    protected boolean detectCemi() throws InterruptedException {
        final byte[] peiIdentifyReqFrame = { (byte) 0xa7 };
        final byte peiIdentifyCon = (byte) 0xa8;
        final byte peiWzIdentFrameLength = 11;

        logger.trace("Checking for cEMI support");

        try (FT12Connection serialConnection = new FT12Connection(serialPort)) {
            final CompletableFuture<byte[]> frameListener = new CompletableFuture<>();
            serialConnection.addConnectionListener(frameReceived -> {
                final byte[] content = frameReceived.getFrameBytes();
                if ((content.length > 0) && (content[0] == peiIdentifyCon)) {
                    logger.trace("Received PEI confirmation of {} bytes", content.length);
                    frameListener.complete(content);
                }
            });

            serialConnection.send(peiIdentifyReqFrame, BlockingMode.NonBlocking);
            byte[] content = frameListener.get(1, TimeUnit.SECONDS);

            if (peiWzIdentFrameLength == content.length) {
                // standard emi2 frame contain 9 bytes,
                // content[1..2] physical address
                // content[3..8] serial no
                //
                // Weinzierl adds 2 extra bytes, 0x0004 for capability cEMI,
                // see "Weinzierl KNX BAOS Starter Kit, User Guide"
                if (0 == content[9] && 4 == content[10]) {
                    logger.debug("Detected device with cEMI support");
                    return true;
                }
            }
        } catch (final ExecutionException | TimeoutException | KNXException na) {
            if (logger.isTraceEnabled()) {
                logger.trace("Exception detecting cEMI: ", na);
            }
        }

        logger.trace("Did not detect device with cEMI support");
        return false;
    }

    @Override
    protected KNXNetworkLink establishConnection() throws KNXException, InterruptedException {
        try {
            boolean useCemiL = useCemi;
            if (!useCemiL) {
                useCemiL = detectCemi();
            }
            logger.debug("Establishing connection to KNX bus through FT1.2 on serial port {}{}{}", serialPort,
                    (useCemiL ? " using cEMI" : ""), ((useCemiL != useCemi) ? " (autodetected)" : ""));
            // CEMI support by Calimero library, useful for newer serial devices like KNX RF sticks, kBerry,
            // etc.; default is still old EMI frame format
            if (useCemiL) {
                return KNXNetworkLinkFT12.newCemiLink(serialPort, new TPSettings());
            }

            return new KNXNetworkLinkFT12(serialPort, new TPSettings());

        } catch (NoClassDefFoundError e) {
            throw new KNXException(
                    "The serial FT1.2 KNX connection requires the serial libraries to be available, but they could not be found!",
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
