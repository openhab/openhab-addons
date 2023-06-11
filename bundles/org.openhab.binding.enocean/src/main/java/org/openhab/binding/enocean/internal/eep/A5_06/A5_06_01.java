/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.A5_06;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
public class A5_06_01 extends _4BSMessage {

    public A5_06_01(ERP1Message packet) {
        super(packet);
    }

    private State getBatteryVoltage() {
        int db3 = getDB_3Value();

        double voltage = db3 / 50.0; // 0..255 = 0.0..5.1V

        return new QuantityType<>(voltage, Units.VOLT);
    }

    private State getIllumination() {
        boolean rs = getBit(getDB_0(), 0);

        double illumination = rs ? getDB_2Value() * 116.48 + 300.0 : getDB_1Value() * 232.94 + 600.0;

        return new QuantityType<>(illumination, Units.LUX);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_BATTERY_VOLTAGE:
                return getBatteryVoltage();
            case CHANNEL_ILLUMINATION:
                return getIllumination();
        }

        return UnDefType.UNDEF;
    }
}
