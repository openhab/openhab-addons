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
package org.openhab.binding.enocean.internal.eep.Base;

import static org.openhab.binding.enocean.internal.messages.ESP3Packet.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.eep.EEP;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public abstract class _SIGMessage extends EEP {

    public static final byte MID_ENERGY_STATUS = 0x06;

    public _SIGMessage() {
        super();
    }

    public _SIGMessage(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected int getDataLength() {
        ERP1Message localPacket = packet;
        if (localPacket != null) {
            return localPacket.getPayload().length - ESP3_SENDERID_LENGTH - ESP3_RORG_LENGTH - ESP3_STATUS_LENGTH;
        } else {
            return bytes.length;
        }
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        return true;
    }
}
