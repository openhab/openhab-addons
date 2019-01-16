/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.eep.D5_00;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_CONTACT;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.eep.Base._1BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class D5_00_01 extends _1BSMessage {

    final byte OPEN = 0 | TeachInBit;
    final byte CLOSED = 1 | TeachInBit;

    public D5_00_01() {
        super();
    }

    public D5_00_01(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, State currentState, Configuration config) {
        if (!isValid()) {
            return UnDefType.UNDEF;
        }

        if (channelId == CHANNEL_CONTACT) {
            return bytes[0] == CLOSED ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        }

        return UnDefType.UNDEF;
    }
}
