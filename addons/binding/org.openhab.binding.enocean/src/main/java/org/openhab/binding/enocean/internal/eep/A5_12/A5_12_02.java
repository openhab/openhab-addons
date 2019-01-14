/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.eep.A5_12;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
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
        return new QuantityType<>(value, SmartHomeUnits.LITRE);
    }
}
