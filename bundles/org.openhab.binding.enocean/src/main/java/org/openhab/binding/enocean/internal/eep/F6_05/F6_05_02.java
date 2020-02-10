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
package org.openhab.binding.enocean.internal.eep.F6_05;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.eep.Base._RPSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class F6_05_02 extends _RPSMessage {

    protected static final byte ALARM_OFF = 0x00;
    protected static final byte ALARM_ON = 0x10;
    protected static final byte ENERGY_LOW = 0x30;

    public F6_05_02() {
        super();
    }

    public F6_05_02(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        if (!isValid()) {
            return UnDefType.UNDEF;
        }

        switch (channelId) {
            case CHANNEL_SMOKEDETECTION:
                return bytes[0] == ALARM_OFF ? OnOffType.OFF : (bytes[0] == ALARM_ON ? OnOffType.ON : UnDefType.UNDEF);
            case CHANNEL_BATTERYLOW:
                return bytes[0] == ENERGY_LOW ? OnOffType.ON : UnDefType.UNDEF;
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        return super.validateData(bytes) && (bytes[0] == ALARM_OFF || bytes[0] == ALARM_ON || bytes[0] == ENERGY_LOW);
    }
}
