/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.eep.A5_14;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_BATTERY_VOLTAGE;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
public abstract class A5_14 extends _4BSMessage {
    public A5_14(ERP1Message packet) {
        super(packet);
    }

    private State getBatteryVoltage() {
        int db3 = getDB_3Value();

        if (db3 > 250) {
            logger.warn("EEP A5-14 error code {}", db3);
            return UnDefType.UNDEF;
        }

        double voltage = db3 / 50.0; // 0..250 = 0.0..5.0V

        return new QuantityType<>(voltage, SmartHomeUnits.VOLT);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, State currentState,
            Configuration config) {
        switch (channelId) {
            case CHANNEL_BATTERY_VOLTAGE:
                return getBatteryVoltage();
        }

        return UnDefType.UNDEF;
    }
}