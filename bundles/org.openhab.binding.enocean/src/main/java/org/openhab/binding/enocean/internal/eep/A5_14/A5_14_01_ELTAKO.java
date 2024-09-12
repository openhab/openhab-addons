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
package org.openhab.binding.enocean.internal.eep.A5_14;

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
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
@NonNullByDefault
public class A5_14_01_ELTAKO extends _4BSMessage {

    public A5_14_01_ELTAKO(ERP1Message packet) {
        super(packet);
    }

    private State getEnergyStorage() {
        int db3 = getDB3Value();

        double voltage = db3 / 51.0; // 0..255 = 0.0..5.0V

        return new QuantityType<>(voltage, Units.VOLT);
    }

    private State getBatteryVoltage() {
        int db2 = getDB2Value();

        double voltage = db2 / 51.0; // 0..255 = 0.0..5.0V

        return new QuantityType<>(voltage, Units.VOLT);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_ENERGY_STORAGE:
                return getEnergyStorage();
            case CHANNEL_BATTERY_VOLTAGE:
                return getBatteryVoltage();
        }

        return UnDefType.UNDEF;
    }
}
