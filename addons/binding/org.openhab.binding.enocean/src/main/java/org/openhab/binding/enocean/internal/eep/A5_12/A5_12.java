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
package org.openhab.binding.enocean.internal.eep.A5_12;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.enocean.internal.eep.EEPHelper;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
public abstract class A5_12 extends _4BSMessage {
    public A5_12(ERP1Message packet) {
        super(packet);
    }

    protected State calcCumulativeValue(float value) {
        return new QuantityType<>(value, SmartHomeUnits.ONE);
    }

    protected State calcCurrentValue(float value) {
        return new QuantityType<>(value, SmartHomeUnits.ONE);
    }

    protected State getCumulativeValue() {
        byte db0 = getDB_0();
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

            float cumulativeValue = Long.parseLong(HexUtils.bytesToHex(new byte[] { getDB_3(), getDB_2(), getDB_1() }),
                    16) * factor;
            return calcCumulativeValue(cumulativeValue);
        }

        return UnDefType.UNDEF;
    }

    protected State getCurrentValue() {
        byte db0 = getDB_0();
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

            float currentValue = Long.parseLong(HexUtils.bytesToHex(new byte[] { getDB_3(), getDB_2(), getDB_1() }), 16)
                    * factor;

            return calcCurrentValue(currentValue);
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, State currentState,
            Configuration config) {
        switch (channelId) {
            case CHANNEL_INSTANTPOWER:
            case CHANNEL_INSTANTLITRE:
            case CHANNEL_CURRENTNUMBER:
                return getCurrentValue();
            case CHANNEL_TOTALUSAGE:
                State value = getCumulativeValue();

                return EEPHelper.validateTotalUsage(value, currentState, config);
            case CHANNEL_TOTALCUBICMETRE:
            case CHANNEL_COUNTER:
                return getCumulativeValue();
        }

        return UnDefType.UNDEF;
    }
}