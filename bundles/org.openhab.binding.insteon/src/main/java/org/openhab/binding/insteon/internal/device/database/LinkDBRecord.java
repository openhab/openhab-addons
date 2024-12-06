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
package org.openhab.binding.insteon.internal.device.database;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.RampRate;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.Msg;

/**
 * The {@link LinkDBRecord} holds a link database record for a device
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class LinkDBRecord extends DatabaseRecord {

    public LinkDBRecord(int location, RecordType type, int group, InsteonAddress address, byte[] data) {
        super(location, type, group, address, data);
    }

    public LinkDBRecord(DatabaseRecord record) {
        super(record);
    }

    public int getOnLevel() {
        return getData1();
    }

    public RampRate getRampRate() {
        return RampRate.valueOf(getData2());
    }

    public int getComponentId() {
        return getData3();
    }

    /**
     * Factory method for creating a new LinkDBRecord from a set of parameters
     *
     * @param location the record location to use
     * @param address the record address to use
     * @param group the record group to use
     * @param isController if is controller record
     * @param data the record data to use
     * @return the link db record
     */
    public static LinkDBRecord create(int location, InsteonAddress address, int group, boolean isController,
            byte[] data) {
        RecordFlags flags = isController ? RecordFlags.CONTROLLER : RecordFlags.RESPONDER;
        RecordType type = flags.getRecordType();

        return new LinkDBRecord(location, type, group, address, data);
    }

    /**
     * Factory method for creating a new LinkDBRecord from an Insteon record data buffer
     *
     * @param buf the record data buffer to parse (backwards)
     * @param location the record location to use
     * @return the link db record
     */
    public static LinkDBRecord fromRecordData(byte[] buf, int location) {
        RecordType type = new RecordType(Byte.toUnsignedInt(buf[7]));
        int group = Byte.toUnsignedInt(buf[6]);
        InsteonAddress address = new InsteonAddress(buf[5], buf[4], buf[3]);
        byte[] data = { buf[2], buf[1], buf[0] };

        return new LinkDBRecord(location, type, group, address, data);
    }

    /**
     * Factory method for creating a new LinkDBRecord from an Insteon record message
     *
     * @param msg the record message to parse
     * @return the link db record
     * @throws FieldException
     */
    public static LinkDBRecord fromRecordMsg(Msg msg) throws FieldException {
        int location = msg.getInt16("userData3");
        RecordType type = new RecordType(msg.getInt("userData6"));
        int group = msg.getInt("userData7");
        InsteonAddress address = new InsteonAddress(msg.getBytes("userData8", 3));
        byte[] data = msg.getBytes("userData11", 3);

        return new LinkDBRecord(location, type, group, address, data);
    }

    /**
     * Factory method for creating a new LinkDBRecord from another instance as inactive
     *
     * @param record the link db record to use
     * @return the inactive link db record
     */
    public static LinkDBRecord asInactive(LinkDBRecord record) {
        RecordType type = RecordType.asInactive(record.getFlags());

        return new LinkDBRecord(record.getLocation(), type, record.getGroup(), record.getAddress(), record.getData());
    }

    /**
     * Factory method for creating a new LinkDBRecord from another instance with new data
     *
     * @param data the new data to use
     * @param record the link db record to use
     * @return the link db record with new data
     */
    public static LinkDBRecord withNewData(byte[] data, LinkDBRecord record) {
        return new LinkDBRecord(record.getLocation(), record.getType(), record.getGroup(), record.getAddress(), data);
    }

    /**
     * Factory method for creating a new LinkDBRecord from another instance with new location
     *
     * @param location the new location to use
     * @param record the link db record to use
     * @return the link db record with new location
     */
    public static LinkDBRecord withNewLocation(int location, LinkDBRecord record) {
        return new LinkDBRecord(location, record.getType(), record.getGroup(), record.getAddress(), record.getData());
    }
}
