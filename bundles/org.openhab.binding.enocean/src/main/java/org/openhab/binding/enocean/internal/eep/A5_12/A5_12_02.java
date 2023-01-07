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
package org.openhab.binding.enocean.internal.eep.A5_12;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 *
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
@NonNullByDefault
public class A5_12_02 extends A5_12 {

    public A5_12_02(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State calcCumulativeValue(float value) {
        return new QuantityType<>(value, SIUnits.CUBIC_METRE);
    }

    @Override
    protected State calcCurrentValue(float value) {
        // value is given in litre/second, hence multiply by 60
        return new QuantityType<>(value * 60, Units.LITRE_PER_MINUTE);
    }
}
