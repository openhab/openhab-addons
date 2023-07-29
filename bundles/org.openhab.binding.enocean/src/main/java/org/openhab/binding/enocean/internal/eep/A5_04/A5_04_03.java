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
