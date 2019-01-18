/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.A5_04;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_BATTERY_VOLTAGE;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
public class A5_04_02_Eltako extends A5_04_02 {

    public A5_04_02_Eltako(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, State currentState,
            Configuration config) {
        if (channelId.equals(CHANNEL_BATTERY_VOLTAGE)) {
            double voltage = getDB_3Value() * 6.58 / 255.0; // not sure if this is right
            return new QuantityType<>(voltage, SmartHomeUnits.VOLT);
        }

        return super.convertToStateImpl(channelId, channelTypeId, currentState, config);
    }
}
