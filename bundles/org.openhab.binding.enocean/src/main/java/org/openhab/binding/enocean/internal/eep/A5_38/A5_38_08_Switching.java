/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.ZERO;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class A5_38_08_Switching extends _4BSMessage {

    static final byte COMMAND_ID = 0x01;
    static final byte SWITCH_OFF = 0x00;
    static final byte SWITCH_ON = 0x01;

    public A5_38_08_Switching() {
    }

    public A5_38_08_Switching(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command outputCommand,
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
        if ((OnOffType) outputCommand == OnOffType.ON) {
            setData(COMMAND_ID, ZERO, ZERO, (byte) (TEACHIN_BIT | SWITCH_ON));
        } else {
            setData(COMMAND_ID, ZERO, ZERO, (byte) (TEACHIN_BIT | SWITCH_OFF));
        }
    }
}
