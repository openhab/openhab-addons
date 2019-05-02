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
package org.openhab.binding.enocean.internal.eep.A5_06;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_ILLUMINATION;

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
public class A5_06_01_ELTAKO extends _4BSMessage {

    public A5_06_01_ELTAKO(ERP1Message packet) {
        super(packet);
    }

    private State getIllumination() {
        int db2 = getDB_2Value();

        if (db2 == 0) {
            int db3 = getDB_3Value();
            return new QuantityType<>(db3 * 0.5, SmartHomeUnits.LUX);
        } else {
            return new QuantityType<>(db2 * 116.48 + 300.0, SmartHomeUnits.LUX);
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, State currentState,
            Configuration config) {

        switch (channelId) {
            case CHANNEL_ILLUMINATION:
                return getIllumination();
        }

        return UnDefType.UNDEF;
    }
}
