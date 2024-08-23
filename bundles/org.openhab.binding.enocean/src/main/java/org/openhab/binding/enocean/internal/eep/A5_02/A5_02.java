/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.A5_02;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
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
        return getDB1Value();
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        double scaledTemp = getScaledMin()
                - (((getUnscaledMin() - getUnscaledTemperatureValue()) * (getScaledMin() - getScaledMax()))
                        / getUnscaledMin());
        return new QuantityType<>(scaledTemp, SIUnits.CELSIUS);
    }
}
