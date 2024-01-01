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
package org.openhab.binding.enocean.internal.eep.A5_02;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class A5_02_20 extends A5_02 {

    public A5_02_20(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected double getScaledMin() {
        return -10;
    }

    @Override
    protected double getScaledMax() {
        return 41.2;
    }

    @Override
    protected double getUnscaledMin() {
        return 1023;
    }

    @Override
    protected double getUnscaledMax() {
        return 0;
    }

    @Override
    protected int getUnscaledTemperatureValue() {
        return getDB1Value() + ((getDB2Value() & 0b11) << 8);
    }
}
