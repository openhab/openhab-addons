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
package org.openhab.binding.enocean.internal.eep.A5_10;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class A5_10_15 extends A5_10 {

    private final double maxScaledTemp = 41.2;
    private final double minScaledTemp = -10.0;

    public A5_10_15(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected int getSetPointValue() {
        return getDB2Value() >>> 2;
    }

    @Override
    protected double getMaxUnscaledValue() {
        return 1023.0;
    }

    @Override
    protected State getTemperature() {
        int value = ((getDB2Value() & 0b11) << 8) + getDB1Value();
        double temp = maxScaledTemp - (value * (maxScaledTemp - minScaledTemp) / getMaxUnscaledValue());
        return new QuantityType<>(temp, SIUnits.CELSIUS);
    }
}
