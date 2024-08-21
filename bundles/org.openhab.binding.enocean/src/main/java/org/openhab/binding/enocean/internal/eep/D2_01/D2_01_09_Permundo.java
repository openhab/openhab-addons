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
package org.openhab.binding.enocean.internal.eep.D2_01;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.EnOceanBindingConstants;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class D2_01_09_Permundo extends D2_01 {

    public D2_01_09_Permundo() {
        super();
    }

    public D2_01_09_Permundo(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
        if (channelId.equals(CHANNEL_REPEATERMODE)) {
            setRepeaterMode(command);
        } else if (channelId.equals(CHANNEL_ECOMODE)) {
            setEcoMode(command);
        } else {
            super.convertFromCommandImpl(channelId, channelTypeId, command, getCurrentStateFunc, config);
        }
    }

    private void setRepeaterMode(Command command) {
        if (command == RefreshType.REFRESH) {
            senderId = new byte[0]; // make this message invalid as we do not support refresh of repeater status
        } else if (command instanceof StringType stringCommand) {
            switch (stringCommand.toString()) {
                case EnOceanBindingConstants.REPEATERMODE_LEVEL_1:
                    setRORG(RORG.MSC).setData((byte) 0x03, (byte) 0x35, (byte) 0x01);
                    break;
                case EnOceanBindingConstants.REPEATERMODE_LEVEL_2:
                    setRORG(RORG.MSC).setData((byte) 0x03, (byte) 0x35, (byte) 0x02);
                    break;
                default:
                    setRORG(RORG.MSC).setData((byte) 0x03, (byte) 0x35, (byte) 0x00);
            }
        }
    }

    private void setEcoMode(Command command) {
        if (command == RefreshType.REFRESH) {
            senderId = new byte[0]; // make this message invalid as we do not support refresh of ecomode status
        } else if (command instanceof OnOffType onOffCommand) {
            if (onOffCommand == OnOffType.ON) {
                setRORG(RORG.MSC).setData((byte) 0x03, (byte) 0x36, (byte) 0x01);
            } else {
                setRORG(RORG.MSC).setData((byte) 0x03, (byte) 0x36, (byte) 0x00);
            }
        }
    }
}
