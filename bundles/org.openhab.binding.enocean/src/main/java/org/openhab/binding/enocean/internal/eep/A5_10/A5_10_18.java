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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 * From A5_10_18 up to A5_10_17 temperature is given as a 8Bit value (range: 250(!)..0).
 * Therefore higher values mean lower temperatures.
 * Temperature range 0..40.
 * 
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class A5_10_18 extends A5_10 {

    public A5_10_18(ERP1Message packet) {
        super(packet);
    }

    protected double getMinUnscaledTemperatureValue() {
        return 250.0;
    }

    @Override
    protected State getFanSpeedStage() {
        return new DecimalType((getDB0Value() >>> 4) - 1);
    }
}
