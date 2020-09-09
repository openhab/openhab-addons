/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.reader.io.serialport.converter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teleinfo.internal.dto.common.Hhphc;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HhphcConverter} class defines a converter to translate a Teleinfo String value into
 * {@link Hhphc} object.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class HhphcConverter implements Converter {

    private final Logger logger = LoggerFactory.getLogger(HhphcConverter.class);

    @Override
    public @Nullable Object convert(String value) throws ConversionException {
        logger.debug("convert(String) [start]");
        if (logger.isTraceEnabled()) {
            logger.trace("value = {}", value);
        }

        Hhphc convertedValue = null;
        switch (value) {
            case "A":
                convertedValue = Hhphc.A;
                break;
            case "C":
                convertedValue = Hhphc.C;
                break;
            case "D":
                convertedValue = Hhphc.D;
                break;
            case "E":
                convertedValue = Hhphc.E;
                break;
            case "Y":
                convertedValue = Hhphc.Y;
                break;
            default:
                throw new ConversionException(value);
        }

        logger.debug("convert(String) [end]");
        return convertedValue;
    }
}
