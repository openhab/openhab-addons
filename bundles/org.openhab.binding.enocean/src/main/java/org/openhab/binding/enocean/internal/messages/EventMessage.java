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

import java.util.stream.Stream;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class EventMessage extends BasePacket {

    public enum EventMessageType {
        UNKNOWN((byte) 0x00, 1),
        SA_RECLAIM_NOT_SUCCESSFUL((byte) 0x01, 1),
        SA_CONFIRM_LEARN((byte) 0x02, 17),
        SA_LEARN_ACK((byte) 0x03, 4),
        CO_READY((byte) 0x04, 2),
        CO_EVENT_SECUREDEVICES((byte) 0x05, 6),
        CO_DUTYCYCLE_LIMIT((byte) 0x06, 2),
        CO_TRANSMIT_FAILED((byte) 0x07, 2);

        private byte value;
        private int dataLength;

        EventMessageType(byte value, int dataLength) {
            this.value = value;
            this.dataLength = dataLength;
        }

        public byte getValue() {
            return this.value;
        }

        public int getDataLength() {
            return dataLength;
        }

        public static EventMessageType getEventMessageType(byte value) {
            return Stream.of(EventMessageType.values()).filter(t -> t.value == value).findFirst().orElse(UNKNOWN);
        }
    }

    private EventMessageType type;

    EventMessage(int dataLength, int optionalDataLength, byte[] payload) {
        super(dataLength, optionalDataLength, ESPPacketType.EVENT, payload);

        type = EventMessageType.getEventMessageType(payload[0]);
    }

    public EventMessageType getEventMessageType() {
        return type;
    }
}
