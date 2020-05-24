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
package org.openhab.binding.tacmi.internal.message;

import org.openhab.binding.tacmi.internal.TACmiMeasureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles analog values as used in the analog message.
 *
 * @author Timo Wendt
 * @author Wolfgang Klimt
 * @author Christian Niessner (marvkis) - Ported to OpenHAB2
 */
public final class AnalogValue {
    public double value;
    public TACmiMeasureType measureType;

    private static Logger logger = LoggerFactory.getLogger(AnalogValue.class);

    /**
     * Create new AnalogValue with specified value and type
     */
    public AnalogValue(int rawValue, int type) {
        measureType = TACmiMeasureType.fromInt(type);
        value = ((double)rawValue) / measureType.getOffset();
        if (measureType.equals(TACmiMeasureType.UNSUPPORTED)) {
            logger.warn("Unsupported measure type {}, value is {}", type, value);
        } else {
            logger.debug("Got measure: type {}, raw value {}, converted: {}, {}", type, rawValue, measureType.name(),
                    value);
        }
    }
}
