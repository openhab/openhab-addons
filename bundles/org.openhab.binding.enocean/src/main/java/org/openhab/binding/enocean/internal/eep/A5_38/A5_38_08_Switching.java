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

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.ZERO;

import java.util.function.Function;

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
public class A5_38_08_Switching extends _4BSMessage {

    static final byte CommandId = 0x01;
    static final byte SwitchOff = 0x00;
    static final byte SwitchOn = 0x01;

    public A5_38_08_Switching() {
        super();
    }

    public A5_38_08_Switching(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command outputCommand,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        if ((OnOffType) outputCommand == OnOffType.ON) {
            setData(CommandId, ZERO, ZERO, (byte) (TeachInBit | SwitchOn));
        } else {
            setData(CommandId, ZERO, ZERO, (byte) (TeachInBit | SwitchOff));
        }
    }
}
