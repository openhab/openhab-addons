/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.eep.A5_12;

import static org.openhab.binding.enocean.EnOceanBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class A5_12_01 extends _4BSMessage {

    public A5_12_01(ERP1Message packet) {
        super(packet);
    }

    protected State getEnergyMeasurementData() {
        if (!getBit(getDB_0(), 2)) {
            float factor = 1;

            switch (getDB_0() & 3) {
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

            float energy = Long.parseLong(HexUtils.bytesToHex(new byte[] { getDB_3(), getDB_2(), getDB_1() }), 16)
                    * factor;
            return new QuantityType<>(energy, SmartHomeUnits.KILOWATT_HOUR);
        }

        return UnDefType.UNDEF;
    }

    protected State getPowerMeasurementData() {
        if (getBit(getDB_0(), 2)) {
            float factor = 1;

            switch (getDB_0() & 3) {
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

            float energy = Long.parseLong(HexUtils.bytesToHex(new byte[] { getDB_3(), getDB_2(), getDB_1() }), 16)
                    * factor;
            return new QuantityType<>(energy, SmartHomeUnits.WATT);
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected State convertToStateImpl(String channelId, State currentState, Configuration config) {

        switch (channelId) {
            case CHANNEL_INSTANTPOWER:
                return getPowerMeasurementData();
            case CHANNEL_TOTALUSAGE:
                return getEnergyMeasurementData();
        }

        return UnDefType.UNDEF;
    }
}
