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
package org.openhab.binding.enocean.internal.eep.A5_11;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
@NonNullByDefault
public class A5_11_03 extends _4BSMessage {

    public A5_11_03(ERP1Message packet) {
        super(packet);
    }

    protected boolean isErrorState() {
        byte db1 = getDB1();

        int state = (db1 >> 4) & 0x03;

        // TODO: display error state on thing
        return state != 0;
    }

    protected State getPositionData() {
        byte db1 = getDB1();
        boolean pvf = getBit(db1, 7);

        if (pvf) {
            byte db0 = getDB0();

            boolean motp = getBit(db0, 6);
            int bsp = getDB3Value();

            if ((bsp >= 0) && (bsp <= 100)) {
                return new PercentType(motp ? 100 - bsp : bsp);
            }
        }

        return UnDefType.UNDEF;
    }

    protected State getAngleData() {
        byte db1 = getDB1();
        boolean avf = getBit(db1, 6);

        if (avf) {
            byte db2 = getDB2();

            boolean as = getBit(db2, 7);
            int an = (db2 & 0x7F) * 2;

            if ((an >= 0) && (an <= 180)) {
                return new QuantityType<>(as ? an * -1 : an, Units.DEGREE_ANGLE);
            }
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
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
