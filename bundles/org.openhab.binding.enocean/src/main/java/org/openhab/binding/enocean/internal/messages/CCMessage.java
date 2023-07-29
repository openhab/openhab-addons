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
package org.openhab.binding.enocean.internal.messages;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
class CCMessage extends BasePacket {

    public enum CCMessageType {
        CO_RD_VERSION((byte) 3, 1),
        CO_WR_IDBASE((byte) 7, 5),
        CO_RD_IDBASE((byte) 8, 1),
        CO_WR_REPEATER((byte) 9, 3),
        CO_RD_REPEATER((byte) 10, 1),
        CO_RD_SECUREDEVICE_PSK((byte) 0x22, 1),
        CO_RD_DUTYCYCLE_LIMIT((byte) 0x23, 1),
        CO_GET_FREQUENCY_INFO((byte) 0x25, 1);

        private byte value;
        private int dataLength;

        CCMessageType(byte value, int dataLength) {
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

    private CCMessageType type;

    public CCMessage(CCMessageType type) {
        this(type, new byte[] { type.getValue() });
    }

    public CCMessage(CCMessageType type, byte[] payload) {
        super(type.getDataLength(), 0, ESPPacketType.COMMON_COMMAND, payload);

        this.type = type;
    }

    public CCMessageType getCCMessageType() {
        return type;
    }
}
