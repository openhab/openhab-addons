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
package org.openhab.binding.enocean.internal.eep.A5_14;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * Window/Door-Sensor with States Open/Closed/Tilt, Supply voltage monitor and
 * vibration alarm.
 *
 * @author Stefan Schimanski - Initial contribution
 */
public class A5_14_0A extends A5_14_09 {
    public A5_14_0A(ERP1Message packet) {
        super(packet);
    }

    private State getVibration() {
        boolean alarm = getBit(getDB_0(), 0);
        return alarm ? OnOffType.ON : OnOffType.OFF;
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        if (channelId.equals(CHANNEL_VIBRATION)) {
            return getVibration();
        }

        return super.convertToStateImpl(channelId, channelTypeId, getCurrentStateFunc, config);
    }
}
