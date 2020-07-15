/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.D0;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.eep.Base._SIGMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class D0_06 extends _SIGMessage {

    public D0_06() {
        super();
    }

    public D0_06(ERP1Message packet) {
        super(packet);
    }

    @Override
    public State convertToStateImpl(String channelId, String channelTypeId, Function<String, State> getCurrentStateFunc,
            Configuration config) {
        if (CHANNEL_BATTERY_LEVEL.equals(channelId)) {
            return new QuantityType<>(bytes[1] & 0xFF, SmartHomeUnits.PERCENT);
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        return bytes[0] == MID_ENERGY_STATUS;
    }
}
