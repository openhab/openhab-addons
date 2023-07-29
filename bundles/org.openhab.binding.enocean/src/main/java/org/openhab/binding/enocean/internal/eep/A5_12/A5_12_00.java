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

import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 *
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
public class A5_12_00 extends A5_12 {

    public A5_12_00(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State calcCumulativeValue(float value) {
        return new QuantityType<>(value, Units.ONE);
    }

    @Override
    protected State calcCurrentValue(float value) {
        return new QuantityType<>(value, Units.ONE);
    }
}
