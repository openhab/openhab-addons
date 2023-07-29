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
package org.openhab.binding.enocean.internal.eep.A5_07;

import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

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
        return new QuantityType<>((getDB_2Value() << 8) + ((getDB_1Value() & 0b11000000) >>> 6), Units.LUX);
    }
}
