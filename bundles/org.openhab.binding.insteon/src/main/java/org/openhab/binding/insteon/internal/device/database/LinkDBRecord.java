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
package org.openhab.binding.insteon.internal.device.database;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.RampRate;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.utils.ByteUtils;

/**
 * The LinkDBRecord class holds a device link database record
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class LinkDBRecord extends BaseRecord {

    private int offset;

    public LinkDBRecord(int offset, RecordType type, byte group, InsteonAddress address, byte[] data) {
        super(type, group, address, data);
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public int getOnLevel() {
        return (int) Math.round(getData1() * 100 / 255.0);
    }

    public RampRate getRampRate() {
        return RampRate.valueOf(getData2());
    }

    public int getComponentId() {
        return getData3();
    }

    @Override
    public String toString() {
        return ByteUtils.getHexString(offset, 4) + " " + super.toString();
    }

    /**
     * Factory method for creating a LinkDBRecord from an Insteon record data buffer
     *
     * @param buf the record data buffer to parse (backwards)
     * @param offset the record offset to use
     * @return the link db record
     */
    public static LinkDBRecord fromRecordData(byte[] buf, int offset) {
        RecordType type = RecordType.fromRecordFlags(buf[7]);
        byte group = buf[6];
        InsteonAddress address = new InsteonAddress(buf[5], buf[4], buf[3]);
        byte[] data = { buf[2], buf[1], buf[0] };

        return new LinkDBRecord(offset, type, group, address, data);
    }

    /**
     * Factory method for creating a LinkDBRecord from an Insteon record message
     *
     * @param msg the record message to parse
     * @return the link db record
     * @throws FieldException
     */
    public static LinkDBRecord fromRecordMsg(Msg msg) throws FieldException {
        int offset = msg.getInt16("userData3");
        RecordType type = RecordType.fromRecordFlags(msg.getByte("userData6"));
        byte group = msg.getByte("userData7");
        InsteonAddress address = new InsteonAddress(msg.getBytes("userData8", 3));
        byte[] data = msg.getBytes("userData11", 3);

        return new LinkDBRecord(offset, type, group, address, data);
    }
}
