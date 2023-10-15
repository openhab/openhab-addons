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

/**
 * From A5_10_10 up to A5_10_14 temperature is given as a 8Bit value (range: 0..250!).
 * Therefore higher values mean higher temperatures.
 * Temperature range 0..40.
 * 
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class A5_10_10 extends A5_10 {

    public A5_10_10(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected int getSetPointValue() {
        return getDB3Value();
    }

    @Override
    protected double getMinTemperatureValue() {
        return 0.0;
    }

    @Override
    protected double getMinUnscaledTemperatureValue() {
        return 0.0;
    }

    @Override
    protected double getMaxTemperatureValue() {
        return 40.0;
    }

    @Override
    protected double getMaxUnscaledTemperatureValue() {
        return 250.0;
    }
}
