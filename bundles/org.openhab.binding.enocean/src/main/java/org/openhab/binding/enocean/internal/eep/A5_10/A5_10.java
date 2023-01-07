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
package org.openhab.binding.enocean.internal.eep.A5_10;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public abstract class A5_10 extends _4BSMessage {

    public A5_10(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_FANSPEEDSTAGE:
                if (getDB3Value() > 209) {
                    return new StringType("-1");
                } else if (getDB3Value() > 189) {
                    return new StringType("0");
                } else if (getDB3Value() > 164) {
                    return new StringType("1");
                } else if (getDB3Value() > 144) {
                    return new StringType("2");
                } else {
                    return new StringType("3");
                }

            case CHANNEL_SETPOINT:
                return new DecimalType(getDB2Value());

            case CHANNEL_TEMPERATURE:
                double temp = (getDB1Value() - 255) / -6.375;
                return new QuantityType<>(temp, SIUnits.CELSIUS);

            case CHANNEL_OCCUPANCY:
                return getBit(getDB0(), 0) ? OnOffType.OFF : OnOffType.ON;
        }

        return UnDefType.UNDEF;
    }
}
