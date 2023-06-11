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
package org.openhab.binding.upb.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Model for the first two bytes of UPB messages.
 *
 * @author cvanorman - Initial contribution
 * @since 1.9.0
 */
@NonNullByDefault
public class ControlWord {

    private static final int TRANSMIT_COUNT_SHIFT = 2;
    private static final int TRANSMIT_COUNT_MASK = 0b00001100;
    private static final int TRANSMIT_SEQUENCE_MASK = 0b00000011;
    private static final int ACK_PULSE_MASK = 0b00010000;
    private static final int ID_PULSE_MASK = 0b00100000;
    private static final int ACK_MESSAGE_MASK = 0b01000000;
    private static final int REPEATER_COUNT_SHIFT = 5;
    private static final int REPEATER_COUNT_MASK = 0b01100000;
    private static final int PACKET_LENGTH_MASK = 0b00011111;
    private static final int LINK_MASK = 0b10000000;

    private byte hi = 0;
    private byte lo = 0;

    /**
     * Sets the two bytes of the control word.
     *
     * @param lo
     *            the low-order byte.
     * @param hi
     *            the high-order byte.
     */
    public void setBytes(final byte hi, final byte lo) {
        this.hi = hi;
        this.lo = lo;
    }

    /**
     * @return the high byte of the control word
     */
    public byte getHi() {
        return hi;
    }

    /**
     * @return the low byte of the control word
     */
    public byte getLo() {
        return lo;
    }

    /**
     * @return the LNK bit
     */
    public boolean isLink() {
        return (hi & LINK_MASK) > 0;
    }

    /**
     * @param link
     *            the link to set
     */
    public void setLink(boolean link) {
        hi = (byte) (link ? hi | LINK_MASK : hi & ~LINK_MASK);
    }

    /**
     * @return the repeaterCount
     */
    public int getRepeaterCount() {
        return (hi & REPEATER_COUNT_MASK) >> REPEATER_COUNT_SHIFT;
    }

    /**
     * @param repeaterCount
     *            the repeaterCount to set
     */
    public void setRepeaterCount(int repeaterCount) {
        hi = (byte) (hi | (repeaterCount << REPEATER_COUNT_SHIFT));
    }

    /**
     * @return the packetLength
     */
    public int getPacketLength() {
        return hi & PACKET_LENGTH_MASK;
    }

    /**
     * @param packetLength
     *            the packetLength to set
     */
    public void setPacketLength(int packetLength) {
        hi = (byte) (hi | packetLength);
    }

    /**
     * @return the transmitCount
     */
    public int getTransmitCount() {
        return (lo & TRANSMIT_COUNT_MASK) >> TRANSMIT_COUNT_SHIFT;
    }

    /**
     * @param transmitCount
     *            the transmitCount to set
     */
    public void setTransmitCount(int transmitCount) {
        lo = (byte) (lo | (transmitCount << TRANSMIT_COUNT_SHIFT));
    }

    /**
     * @return the transmitSequence
     */
    public int getTransmitSequence() {
        return lo & TRANSMIT_SEQUENCE_MASK;
    }

    /**
     * @param transmitSequence
     *            the transmitSequence to set
     */
    public void setTransmitSequence(int transmitSequence) {
        lo = (byte) (lo | transmitSequence);
    }

    /**
     * @return the ackPulse
     */
    public boolean isAckPulse() {
        return (lo & ACK_PULSE_MASK) > 0;
    }

    /**
     * @param ackPulse
     *            the ackPulse to set
     */
    public void setAckPulse(boolean ackPulse) {
        lo = (byte) (ackPulse ? lo | ACK_PULSE_MASK : lo & ~ACK_PULSE_MASK);
    }

    /**
     * @return the idPulse
     */
    public boolean isIdPulse() {
        return (lo & ID_PULSE_MASK) > 0;
    }

    /**
     * @param idPulse
     *            the idPulse to set
     */
    public void setIdPulse(boolean idPulse) {
        lo = (byte) (idPulse ? lo | ID_PULSE_MASK : lo & ~ID_PULSE_MASK);
    }

    /**
     * @return the ackMessage
     */
    public boolean isAckMessage() {
        return (lo & ACK_MESSAGE_MASK) > 0;
    }

    /**
     * @param ackMessage
     *            the ackMessage to set
     */
    public void setAckMessage(boolean ackMessage) {
        lo = (byte) (ackMessage ? lo | ACK_MESSAGE_MASK : lo & ~ACK_MESSAGE_MASK);
    }
}
