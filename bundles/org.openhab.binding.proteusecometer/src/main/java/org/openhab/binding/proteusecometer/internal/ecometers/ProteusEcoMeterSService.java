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
package org.openhab.binding.proteusecometer.internal.ecometers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.proteusecometer.internal.WrappedException;
import org.openhab.binding.proteusecometer.internal.serialport.SerialPortService;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read from Proteus EcoMeter S
 *
 * @author Matthias Herrmann - Initial contribution
 *
 */
@NonNullByDefault
public class ProteusEcoMeterSService {

    private final Logger logger = LoggerFactory.getLogger(ProteusEcoMeterSService.class);

    /**
     * Initialize the communication with the device, i.e. open the serial port etc.
     *
     * @return {@code true} if we can communicate with the device
     * @throws IOException
     */
    public Stream<ProteusEcoMeterSReply> read(final String portId, final SerialPortService serialPort)
            throws IOException {
        logger.trace("communicate");

        final InputStream inputStream = serialPort.getInputStream(portId, 115200, 8, 1, 0);
        final Supplier<Optional<ProteusEcoMeterSReply>> supplier = () -> {
            logger.trace("Input stream opened for the port");

            try {
                final byte[] deviceBytes = new byte[22];
                inputStream.read(deviceBytes, 0, 22);
                final String hexString = HexUtils.bytesToHex(deviceBytes);
                logger.trace("Received hex string: {}", hexString);
                final ProteusEcoMeterSParser parser = new ProteusEcoMeterSParser();
                final Optional<ProteusEcoMeterSReply> dataOpt = parser.parseFromBytes(deviceBytes);

                if (dataOpt.isEmpty()) {
                    logger.warn("Received bytes I don't understand: {}", hexString);
                }
                return dataOpt;
            } catch (final IOException e) {
                throw new WrappedException(e);
            } finally {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                }
            }
        };

        return Stream.generate(supplier).takeWhile(reply -> !Thread.interrupted()).filter(Optional::isPresent)
                .map(Optional::get);
    }
}
