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
package org.openhab.binding.enocean.internal.eep.A5_38;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_DIMMER;
import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.ZERO;

import java.util.function.Function;

import org.openhab.binding.enocean.internal.config.EnOceanChannelDimmerConfig;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * This class tries to combine the classic EEP A5-38-08 CMD 0x02 dimming with the Eltako interpretation of this EEP.
 * It is doing it by channel config parameter "eltakoDimmer". The differences are:
 * <li>Dimming value 0-100%: standard 0-255, Eltako 0-100</li>
 * <li>Store value: standard DB0.1, Eltako DB0.2</li>
 *
 * @author Daniel Weber - Initial contribution
 */
public class A5_38_08_Dimming extends _4BSMessage {

    static final byte CommandId = 0x02;
    static final byte SwitchOff = 0x00;
    static final byte SwitchOn = 0x01;
    static final byte Switch100Percent = 0x64;

    public A5_38_08_Dimming() {
        super();
    }

    public A5_38_08_Dimming(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command outputCommand,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_DIMMER:
                byte dimmValue;

                if (outputCommand instanceof DecimalType) {
                    dimmValue = ((DecimalType) outputCommand).byteValue();
                } else if (outputCommand instanceof OnOffType) {
                    dimmValue = ((OnOffType) outputCommand == OnOffType.ON) ? Switch100Percent : ZERO;
                } else if (outputCommand instanceof IncreaseDecreaseType) {
                    dimmValue = ((IncreaseDecreaseType) outputCommand == IncreaseDecreaseType.INCREASE)
                            ? Switch100Percent
                            : ZERO;
                } else if (outputCommand instanceof UpDownType) {
                    dimmValue = ((UpDownType) outputCommand == UpDownType.UP) ? Switch100Percent : ZERO;
                } else {
                    throw new IllegalArgumentException(outputCommand.toFullString() + " is no valid dimming command.");
                }

                EnOceanChannelDimmerConfig c = config.as(EnOceanChannelDimmerConfig.class);

                byte storeByte = ZERO; // "Store final value" (standard) vs. "block value" (Eltako)

                if (!c.eltakoDimmer) {
                    dimmValue *= 2.55; // 0-100% = 0-255

                    if (c.storeValue) {
                        storeByte = 0x02; // set DB0.1
                    }
                } else {
                    if (c.storeValue) {
                        storeByte = 0x04; // set DB0.2
                    }
                }

                byte rampingTime = Integer.valueOf(c.rampingTime).byteValue();
                byte switchingCommand = (dimmValue == ZERO) ? SwitchOff : SwitchOn;

                setData(CommandId, dimmValue, rampingTime, (byte) (TeachInBit | storeByte | switchingCommand));

                break;
        }
    }

    @Override
    public State convertToStateImpl(String channelId, String channelTypeId, Function<String, State> getCurrentStateFunc,
            Configuration config) {
        switch (channelId) {
            case CHANNEL_DIMMER:
                if (!getBit(getDB_0(), 0)) {
                    // Switching Command is OFF (DB0.0==0), return 0%
                    return new PercentType(0);
                } else {
                    // DB2 contains the Dimming value (absolute[0...255] or relative/Eltako [0...100])
                    int dimmValue = getDB_2Value();

                    EnOceanChannelDimmerConfig c = config.as(EnOceanChannelDimmerConfig.class);

                    // if Standard dimmer and Dimming Range is absolute (DB0.2==0),
                    if (!c.eltakoDimmer && !getBit(getDB_0(), 2)) {
                        // map range [0...255] to [0%...100%]
                        dimmValue /= 2.55;
                    }

                    return new PercentType(dimmValue);
                }
        }

        return UnDefType.UNDEF;
    }
}
