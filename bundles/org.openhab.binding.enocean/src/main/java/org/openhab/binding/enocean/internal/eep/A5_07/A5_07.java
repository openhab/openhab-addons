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

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public abstract class A5_07 extends _4BSMessage {

    public A5_07(ERP1Message packet) {
        super(packet);
    }

    protected abstract State getIllumination();

    protected abstract State getMotion();

    protected abstract State getSupplyVoltage();

    protected State getSupplyVoltage(int value) {
        if (value > 250) {
            logger.warn("EEP A5-07 error code {}", value);
            return UnDefType.UNDEF;
        }

        double voltage = value / 50.0; // 0..250 = 0.0..5.0V
        return new QuantityType<>(voltage, Units.VOLT);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        if (channelId.equals(CHANNEL_ILLUMINATION)) {
            return getIllumination();
        } else if (channelId.equals(CHANNEL_MOTIONDETECTION)) {
            return getMotion();
        } else if (channelId.equals(CHANNEL_BATTERY_VOLTAGE)) {
            return getSupplyVoltage();
        }

        return UnDefType.UNDEF;
    }
}
