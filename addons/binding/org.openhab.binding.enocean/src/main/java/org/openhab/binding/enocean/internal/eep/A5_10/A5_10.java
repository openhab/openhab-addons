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
package org.openhab.binding.enocean.internal.eep.A5_10;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public abstract class A5_10 extends _4BSMessage {

    public A5_10(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, State currentState, Configuration config) {
        if (!isValid()) {
            return UnDefType.UNDEF;
        }

        switch (channelId) {
            case CHANNEL_FANSPEEDSTAGE:
                if (getDB_3Value() > 209) {
                    return new StringType("-1");
                } else if (getDB_3Value() > 189) {
                    return new StringType("0");
                } else if (getDB_3Value() > 164) {
                    return new StringType("1");
                } else if (getDB_3Value() > 144) {
                    return new StringType("2");
                } else {
                    return new StringType("3");
                }

            case CHANNEL_SETPOINT:
                return new DecimalType(getDB_2Value());

            case CHANNEL_TEMPERATURE:
                double temp = (getDB_1Value() - 255) / -6.375;
                return new QuantityType<>(temp, SIUnits.CELSIUS);

            case CHANNEL_OCCUPANCY:
                return getBit(getDB_0(), 0) ? OnOffType.OFF : OnOffType.ON;
        }

        return UnDefType.UNDEF;
    }
}
