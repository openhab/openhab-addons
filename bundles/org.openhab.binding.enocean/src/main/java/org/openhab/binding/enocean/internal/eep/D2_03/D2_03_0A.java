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
package org.openhab.binding.enocean.internal.eep.D2_03;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
@NonNullByDefault
public class D2_03_0A extends _VLDMessage {

    protected static final byte SHORT_PRESS = 0x01;
    protected static final byte DOUBLE_PRESS = 0x02;
    protected static final byte LONG_PRESS = 0x03;
    protected static final byte LONG_RELEASE = 0x04;

    public D2_03_0A() {
        super();
    }

    public D2_03_0A(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected @Nullable String convertToEventImpl(String channelId, String channelTypeId, @Nullable String lastEvent,
            Configuration config) {
        switch (channelId) {
            case CHANNEL_PUSHBUTTON:
                return (bytes[1] == SHORT_PRESS) ? CommonTriggerEvents.PRESSED : null;
            case CHANNEL_DOUBLEPRESS:
                return (bytes[1] == DOUBLE_PRESS) ? CommonTriggerEvents.PRESSED : null;
            case CHANNEL_LONGPRESS:
                return (bytes[1] == LONG_PRESS) ? CommonTriggerEvents.PRESSED
                        : ((bytes[1] == LONG_RELEASE) ? CommonTriggerEvents.RELEASED : null);
            default:
                return null;
        }
    }

    @Override
    public State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        if (CHANNEL_BATTERY_LEVEL.equals(channelId)) {
            return new QuantityType<>(bytes[0] & 0xFF, Units.PERCENT);
        }

        return UnDefType.UNDEF;
    }
}
