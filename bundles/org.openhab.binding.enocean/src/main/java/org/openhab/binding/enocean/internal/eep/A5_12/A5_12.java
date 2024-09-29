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
package org.openhab.binding.enocean.internal.eep.A5_12;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelTariffInfoConfig;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.eep.EEPHelper;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
@NonNullByDefault
public abstract class A5_12 extends _4BSMessage {
    public A5_12(ERP1Message packet) {
        super(packet);
    }

    protected State calcCumulativeValue(float value) {
        return new QuantityType<>(value, Units.ONE);
    }

    protected State calcCurrentValue(float value) {
        return new QuantityType<>(value, Units.ONE);
    }

    protected State getCumulativeValue() {
        byte db0 = getDB0();
        boolean dt = getBit(db0, 2);

        if (!dt) {
            byte div = (byte) (db0 & 0x03);

            float factor = 1;

            switch (div) {
                case 0:
                    factor = 1;
                    break;
                case 1:
                    factor /= 10;
                    break;
                case 2:
                    factor /= 100;
                    break;
                case 3:
                    factor /= 1000;
                    break;
                default:
                    return UnDefType.UNDEF;
            }

            float cumulativeValue = Long.parseLong(HexUtils.bytesToHex(new byte[] { getDB3(), getDB2(), getDB1() }), 16)
                    * factor;
            return calcCumulativeValue(cumulativeValue);
        }

        return UnDefType.UNDEF;
    }

    protected State getCurrentValue() {
        byte db0 = getDB0();
        boolean dt = getBit(db0, 2);

        if (dt) {
            byte div = (byte) (db0 & 0x03);

            float factor = 1;

            switch (div) {
                case 0:
                    factor = 1;
                    break;
                case 1:
                    factor /= 10;
                    break;
                case 2:
                    factor /= 100;
                    break;
                case 3:
                    factor /= 1000;
                    break;
                default:
                    return UnDefType.UNDEF;
            }

            float currentValue = Long.parseLong(HexUtils.bytesToHex(new byte[] { getDB3(), getDB2(), getDB1() }), 16)
                    * factor;

            return calcCurrentValue(currentValue);
        }

        return UnDefType.UNDEF;
    }

    protected int getTariffInfo() {
        return ((getDB0() >>> 4) & 0xff);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        EnOceanChannelTariffInfoConfig c = config.as(EnOceanChannelTariffInfoConfig.class);
        if (c.tariff != getTariffInfo()) {
            return UnDefType.UNDEF;
        }

        switch (channelTypeId) {
            case CHANNEL_INSTANTPOWER:
            case CHANNEL_CURRENTFLOW:
            case CHANNEL_CURRENTNUMBER:
                return getCurrentValue();
            case CHANNEL_TOTALUSAGE:
                State value = getCumulativeValue();
                State currentState = getCurrentStateFunc.apply(channelId);
                return EEPHelper.validateTotalUsage(value, currentState, config);
            case CHANNEL_CUMULATIVEVALUE:
            case CHANNEL_COUNTER:
                return getCumulativeValue();
        }

        return UnDefType.UNDEF;
    }
}
