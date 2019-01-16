/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.eep.A5_04;

import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class A5_04_03 extends A5_04 {

    public A5_04_03(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected double getUnscaledTemperatureMax() {
        return 1023;
    }

    @Override
    protected double getScaledTemperatureMin() {
        return -20;
    }

    @Override
    protected double getScaledTemperatureMax() {
        return 60;
    }

    @Override
    protected int getUnscaledTemperatureValue() {
        return getDB_1Value() + ((getDB_2Value() & 0b11) << 8);
    }

    @Override
    protected double getUnscaledHumidityMax() {
        return 255;
    }

    @Override
    protected int getUnscaledHumidityValue() {
        return getDB_3Value();
    }
}
