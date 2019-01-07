/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.eep.A5_02;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public abstract class A5_02 extends _4BSMessage {

    public A5_02(ERP1Message packet) {
        super(packet);
    }

    protected double getUnscaledMin() {
        return 255;
    }

    protected double getUnscaledMax() {
        return 0;
    }

    protected abstract double getScaledMin();

    protected abstract double getScaledMax();

    protected int getUnscaledTemperatureValue() {
        return getDB_1Value();
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, State currentState, Configuration config) {
        if (!isValid()) {
            return UnDefType.UNDEF;
        }

        double scaledTemp = getScaledMin()
                - (((getUnscaledMin() - getUnscaledTemperatureValue()) * (getScaledMin() - getScaledMax()))
                        / getUnscaledMin());
        return new QuantityType<>(scaledTemp, SIUnits.CELSIUS);
    }
}
