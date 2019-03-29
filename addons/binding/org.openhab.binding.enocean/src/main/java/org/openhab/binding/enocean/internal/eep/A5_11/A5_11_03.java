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
package org.openhab.binding.enocean.internal.eep.A5_11;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.PercentType;
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
public class A5_11_03 extends _4BSMessage {

    public A5_11_03(ERP1Message packet) {
        super(packet);
    }

    protected boolean isErrorState() {
        byte db1 = getDB_1();

        int state = (db1 >> 4) & 0x03;

        if (state != 0) {
            // TODO: display error state on thing
            return true;
        } else {
            return false;
        }
    }

    protected State getPositionData() {
        byte db1 = getDB_1();
        boolean pvf = getBit(db1, 7);

        if (pvf) {
            byte db0 = getDB_0();

            boolean motp = getBit(db0, 6);
            int bsp = getDB_3Value();

            if ((bsp >= 0) && (bsp <= 100)) {
                return new PercentType(motp ? 100 - bsp : bsp);
            }
        }

        return UnDefType.UNDEF;
    }

    protected State getAngleData() {
        byte db1 = getDB_1();
        boolean avf = getBit(db1, 6);

        if (avf) {
            byte db2 = getDB_2();

            boolean as = getBit(db2, 7);
            int an = (db2 & 0x7F) * 2;

            if ((an >= 0) && (an <= 180)) {
                return new QuantityType<>(as ? an * -1 : an, SmartHomeUnits.DEGREE_ANGLE);
            }
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, State currentState, Configuration config) {
        if (isErrorState()) {
            return UnDefType.UNDEF;
        }

        switch (channelId) {
            case CHANNEL_ROLLERSHUTTER:
                return getPositionData();
            case CHANNEL_ANGLE:
                return getAngleData();
        }

        return UnDefType.UNDEF;
    }
}
