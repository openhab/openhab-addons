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
package org.openhab.binding.enocean.internal.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class SAMessage extends BasePacket {

    public enum SAMessageType {
        SA_WR_LEARNMODE((byte) 0x01, 7),
        SA_RD_LEARNMODE((byte) 0x02, 1),
        SA_WR_LEARNCONFIRM((byte) 0x03, 1),
        SA_WR_CLIENTLEARNRQ((byte) 0x04, 6),
        SA_WR_RESET((byte) 0x05, 1),
        SA_RD_LEARNEDCLIENTS((byte) 0x06, 1),
        SA_WR_RECLAIMS((byte) 0x07, 1),
        SA_WR_POSTMASTER((byte) 0x08, 2),
        SA_RD_MAILBOX_STATUS((byte) 0x09, 9);

        private byte value;
        private int dataLength;

        SAMessageType(byte value, int dataLength) {
            this.value = value;
            this.dataLength = dataLength;
        }

        public byte getValue() {
            return this.value;
        }

        public int getDataLength() {
            return dataLength;
        }
    }

    private SAMessageType type;

    public SAMessage(SAMessageType type) {
        this(type, new byte[] { type.getValue() });
    }

    public SAMessage(SAMessageType type, byte[] payload) {
        super(type.getDataLength(), 0, ESPPacketType.SMART_ACK_COMMAND, payload);

        this.type = type;
    }

    public SAMessageType getSAMessageType() {
        return type;
    }
}
