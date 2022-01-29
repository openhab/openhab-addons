/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.D2_03;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.openhab.binding.enocean.internal.eep.Base._VLDMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class D2_03_0A extends _VLDMessage {

    protected final byte ShortPress = 0x01;
    protected final byte DoublePress = 0x02;
    protected final byte LongPress = 0x03;
    protected final byte LongRelease = 0x04;

    public D2_03_0A() {
        super();
    }

    public D2_03_0A(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected String convertToEventImpl(String channelId, String channelTypeId, String lastEvent,
            Configuration config) {
        switch (channelId) {
            case CHANNEL_PUSHBUTTON:
                return (bytes[1] == ShortPress) ? CommonTriggerEvents.PRESSED : null;
            case CHANNEL_DOUBLEPRESS:
                return (bytes[1] == DoublePress) ? CommonTriggerEvents.PRESSED : null;
            case CHANNEL_LONGPRESS:
                return (bytes[1] == LongPress) ? CommonTriggerEvents.PRESSED
                        : ((bytes[1] == LongRelease) ? CommonTriggerEvents.RELEASED : null);
            default:
                return null;
        }
    }

    @Override
    public State convertToStateImpl(String channelId, String channelTypeId, Function<String, State> getCurrentStateFunc,
            Configuration config) {
        if (CHANNEL_BATTERY_LEVEL.equals(channelId)) {
            return new QuantityType<>(bytes[0] & 0xFF, Units.PERCENT);
        }

        return UnDefType.UNDEF;
    }
}
