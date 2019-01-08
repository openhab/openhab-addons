/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.eep.A5_07;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class A5_07_03 extends A5_07_02 {

    public A5_07_03(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State getIllumination() {
        return new QuantityType<>((getDB_2Value() << 8) + ((getDB_1Value() & 0b11000000) >>> 6), SmartHomeUnits.LUX);
    }
}
