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
package org.openhab.binding.enocean.internal.eep.A5_06;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_ILLUMINATION;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
@NonNullByDefault
public class A5_06_01_ELTAKO extends _4BSMessage {

    public A5_06_01_ELTAKO(ERP1Message packet) {
        super(packet);
    }

    private State getIllumination() {
        int db2 = getDB2Value();

        if (db2 == 0) {
            int db3 = getDB3Value();
            return new QuantityType<>(db3 * 0.5, Units.LUX);
        } else {
            return new QuantityType<>(db2 * 116.48 + 300.0, Units.LUX);
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_ILLUMINATION:
                return getIllumination();
        }

        return UnDefType.UNDEF;
    }
}
