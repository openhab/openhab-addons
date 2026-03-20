/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.model;

import static org.openhab.binding.dirigera.internal.Constants.CHANNEL_ILLUMINANCE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ConversionModel} converts raw values from the DIRIGERA API to openHAB types
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConversionModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionModel.class);

    public static Number convert(String type, Number value) {
        return switch (type) {
            case CHANNEL_ILLUMINANCE -> {
                // see Page 4-5
                // https://zigbeealliance.org/wp-content/uploads/2019/12/07-5123-06-zigbee-cluster-library-specification.pdf
                double integerValue = value.intValue();
                if (integerValue == 0) {
                    yield 0.0;
                } else {
                    yield Math.pow(10, (integerValue - 1) / 10000.0);
                }
            }
            default -> {
                LOGGER.warn("No conversion found for type {}, returning raw value", type);
                yield 0.0;
            }
        };
    }
}
