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
package org.openhab.binding.enocean.internal.eep.A5_10;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 * From A5_10_15 up to A5_10_17 temperature is given as a 10Bit value (range: 1023..0).
 * Therefore higher values mean lower temperatures.
 * Temperature range -10..41.2.
 * 
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class A5_10_15 extends A5_10 {

    public A5_10_15(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected int getSetPointValue() {
        return getDB2Value() >>> 2;
    }

    @Override
    protected double getMinTemperatureValue() {
        return -10.0;
    }

    @Override
    protected double getMinUnscaledTemperatureValue() {
        return 1023.0;
    }

    @Override
    protected double getMaxTemperatureValue() {
        return 41.2;
    }

    @Override
    protected double getMaxUnscaledTemperatureValue() {
        return 0.0;
    }

    @Override
    protected double getTemperatureValue() {
        return ((getDB2Value() & 0b11) << 8) + getDB1Value();
    }
}
