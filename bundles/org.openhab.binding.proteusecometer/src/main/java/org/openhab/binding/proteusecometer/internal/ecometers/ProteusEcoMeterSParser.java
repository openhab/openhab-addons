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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse the bytes from the device
 *
 * @author Matthias Herrmann - Initial contribution
 *
 */
@NonNullByDefault
class ProteusEcoMeterSParser {

    private final Logger logger = LoggerFactory.getLogger(ProteusEcoMeterSParser.class);

    /**
     * @param bytes Raw bytes send from the device
     * @return A structured version of the bytes, if possible
     */
    public Optional<ProteusEcoMeterSReply> parseFromBytes(final byte[] bytes) {
        return Optional.ofNullable(bytes).flatMap(b -> {
            final String hexString = HexUtils.bytesToHex(b);
            logger.trace("Received hex string: {}", hexString);

            if (hexString.length() < 4) {
                return Optional.empty();
            } else {
                final String marker = hexString.substring(0, 4);
                if (!"5349".equals(marker)) {
                    logger.trace("Marker is not {} but {}", "5349", marker);
                    return Optional.empty();
                } else if (hexString.length() < 40) {
                    logger.trace("hexString is of length {}, expected >= 40", hexString.length());
                    return Optional.empty();
                } else {
                    try {
                        return Optional
                                .of(new ProteusEcoMeterSReply(parseInt(hexString.substring(26, 28), "tempInFahrenheit"),
                                        parseInt(hexString.substring(28, 32), "sensorLevelInCm"),
                                        parseInt(hexString.substring(32, 36), "usableLevelInLiter"),
                                        parseInt(hexString.substring(36, 40), "totalCapacityInLiter")));
                    } catch (final NumberFormatException e) {
                        logger.debug("Error while parsing numbers", e);
                        return Optional.empty();
                    }
                }
            }
        });
    }

    private Integer parseInt(final String toParse, final String fieldName) throws NumberFormatException {
        try {
            return Integer.parseInt(toParse, 16);
        } catch (final NumberFormatException e) {
            logger.trace("Unable to parse field {}", fieldName, e);
            throw e;
        }
    }
}
