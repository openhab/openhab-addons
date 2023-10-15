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
package org.openhab.binding.enocean.internal.eep.A5_38;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_DIMMER;
import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.ZERO;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * <ul>
 * <li>Dimming value 0-100%: standard 0-255, Eltako 0-100</li>
 * <li>Store value: standard DB0.1, Eltako DB0.2</li>
 * </ul>
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class A5_38_08_Dimming extends _4BSMessage {

    static final byte COMMAND_ID = 0x02;
    static final byte SWITCH_OFF = 0x00;
    static final byte SWITCH_ON = 0x01;
    static final byte SWITCH_100_PERCENT = 0x64;

    public A5_38_08_Dimming() {
        super();
    }

    public A5_38_08_Dimming(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command outputCommand,
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
        switch (channelId) {
            case CHANNEL_DIMMER:
                byte dimmValue;

                if (outputCommand instanceof DecimalType decimalCommand) {
                    dimmValue = decimalCommand.byteValue();
                } else if (outputCommand instanceof OnOffType onOffCommand) {
                    dimmValue = (onOffCommand == OnOffType.ON) ? SWITCH_100_PERCENT : ZERO;
                } else if (outputCommand instanceof IncreaseDecreaseType increaseDecreaseCommand) {
                    dimmValue = (increaseDecreaseCommand == IncreaseDecreaseType.INCREASE) ? SWITCH_100_PERCENT : ZERO;
                } else if (outputCommand instanceof UpDownType upDownCommand) {
                    dimmValue = (upDownCommand == UpDownType.UP) ? SWITCH_100_PERCENT : ZERO;
                } else {
                    throw new IllegalArgumentException(outputCommand.toFullString() + " is no valid dimming command.");
                }
                if (config != null) {
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
                    byte switchingCommand = (dimmValue == ZERO) ? SWITCH_OFF : SWITCH_ON;

                    setData(COMMAND_ID, dimmValue, rampingTime, (byte) (TEACHIN_BIT | storeByte | switchingCommand));
                } else {
                    logger.error("Cannot handle command {}, when configuration is null", outputCommand.toFullString());
                }

                break;
        }
    }

    @Override
    public State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_DIMMER:
                if (!getBit(getDB0(), 0)) {
                    // Switching Command is OFF (DB0.0==0), return 0%
                    return new PercentType(0);
                } else {
                    // DB2 contains the Dimming value (absolute[0...255] or relative/Eltako [0...100])
                    int dimmValue = getDB2Value();

                    EnOceanChannelDimmerConfig c = config.as(EnOceanChannelDimmerConfig.class);

                    // if Standard dimmer and Dimming Range is absolute (DB0.2==0),
                    if (!c.eltakoDimmer && !getBit(getDB0(), 2)) {
                        // map range [0...255] to [0%...100%]
                        dimmValue /= 2.55;
                    }

                    return new PercentType(dimmValue);
                }
        }

        return UnDefType.UNDEF;
    }
}
