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
package org.openhab.binding.tacmi.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tacmi.internal.TACmiMeasureType;

/**
 * This class handles analog values as used in the analog message.
 *
 * @author Timo Wendt - Initial contribution
 * @author Wolfgang Klimt
 * @author Christian Niessner - Ported to OpenHAB2
 */
@NonNullByDefault
public final class AnalogValue {
    public double value;
    public TACmiMeasureType measureType;

    /**
     * Create new AnalogValue with specified value and type
     */
    public AnalogValue(int rawValue, int type) {
        measureType = TACmiMeasureType.fromInt(type);
        value = ((double) rawValue) / measureType.getOffset();
    }
}
