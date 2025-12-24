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
package org.openhab.binding.enocean.internal.eep.D2_01;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_REPEATERMODE;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.EnOceanBindingConstants;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class D2_01_0F_NodON extends D2_01 {

    public D2_01_0F_NodON() {
    }

    public D2_01_0F_NodON(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
        if (channelId.equalsIgnoreCase(CHANNEL_REPEATERMODE)) {
            if (command instanceof RefreshType) {
                senderId = new byte[0]; // make this message invalid as we do not support refresh of repeter status
            } else if (command instanceof StringType stringCommand) {
                switch (stringCommand.toString()) {
                    case EnOceanBindingConstants.REPEATERMODE_LEVEL_1:
                        setRORG(RORG.MSC).setData((byte) 0x00, (byte) 0x46, (byte) 0x08, (byte) 0x01, (byte) 0x01);
                        break;
                    case EnOceanBindingConstants.REPEATERMODE_LEVEL_2:
                        setRORG(RORG.MSC).setData((byte) 0x00, (byte) 0x46, (byte) 0x08, (byte) 0x01, (byte) 0x02);
                        break;
                    default:
                        setRORG(RORG.MSC).setData((byte) 0x00, (byte) 0x46, (byte) 0x08, (byte) 0x00, (byte) 0x00);
                }
            }
        } else {
            super.convertFromCommandImpl(channelId, channelTypeId, command, getCurrentStateFunc, config);
        }
    }
}
