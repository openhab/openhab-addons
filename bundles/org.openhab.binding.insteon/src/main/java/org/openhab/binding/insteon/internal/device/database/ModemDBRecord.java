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
package org.openhab.binding.insteon.internal.device.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.Msg;

/**
 * The {@link ModemDBRecord} holds a link database record for a modem
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ModemDBRecord extends DatabaseRecord {

    public ModemDBRecord(RecordType type, int group, InsteonAddress address, byte[] data) {
        super(type, group, address, data);
    }

    public ModemDBRecord(DatabaseRecord record) {
        super(record);
    }

    public int getDeviceCategory() {
        return getData1();
    }

    public int getSubCategory() {
        return getData2();
    }

    public int getFirmwareVersion() {
        return getData3();
    }

    /**
     * Creates a copy of this record with new data
     *
     * @param data the new data to use
     * @return a new record instance with the new data
     */
    public ModemDBRecord withNewData(byte[] data) {
        return new ModemDBRecord(getType(), getGroup(), getAddress(), data);
    }

    /**
     * Factory method for creating a new ModemDBRecord from a set of parameters
     *
     * @param address the record address
     * @param group the record group
     * @param isController if is controller record
     * @param data the record data
     * @return the modem db record
     */
    public static ModemDBRecord create(InsteonAddress address, int group, boolean isController, byte[] data) {
        RecordFlags flags = isController ? RecordFlags.CONTROLLER : RecordFlags.RESPONDER;
        RecordType type = flags.getRecordType();

        return new ModemDBRecord(type, group, address, data);
    }

    /**
     * Factory method for creating a new ModemDBRecord from an Insteon record message
     *
     * @param msg the Insteon record message to parse
     * @return the modem db record
     * @throws FieldException
     */
    public static ModemDBRecord fromRecordMsg(Msg msg) throws FieldException {
        RecordType type = new RecordType(msg.getInt("RecordFlags"));
        int group = msg.getInt("ALLLinkGroup");
        InsteonAddress address = msg.getInsteonAddress("LinkAddr");
        byte[] data = new byte[] { msg.getByte("LinkData1"), msg.getByte("LinkData2"), msg.getByte("LinkData3") };

        return new ModemDBRecord(type, group, address, data);
    }

    /**
     * Factory method for creating a new ModemDBRecord from an Insteon linking completed message
     *
     * @param msg the Insteon linking completed message to parse
     * @return the modem db record
     * @throws FieldException
     */
    public static ModemDBRecord fromLinkingMsg(Msg msg) throws FieldException {
        LinkMode mode = LinkMode.valueOf(msg.getInt("LinkCode"));
        RecordType type = mode.getRecordType();
        int group = msg.getInt("ALLLinkGroup");
        InsteonAddress address = msg.getInsteonAddress("LinkAddr");
        byte[] data = new byte[3];

        if (mode == LinkMode.CONTROLLER) {
            data = new byte[] { msg.getByte("DeviceCategory"), msg.getByte("DeviceSubcategory"),
                    msg.getByte("FirmwareVersion") };
        }

        return new ModemDBRecord(type, group, address, data);
    }

    /**
     * Factory method for creating a list of ModemDBRecord from an Insteon record dump
     *
     * @param stream the Insteon record dump input stream to use
     * @return the list of modem db records
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public static List<ModemDBRecord> fromRecordDump(InputStream stream) throws IllegalArgumentException, IOException {
        List<ModemDBRecord> records = new ArrayList<>();

        if (stream.available() % ModemDBRecord.SIZE != 0) {
            throw new IllegalArgumentException("Invalid record dump length");
        }

        while (stream.available() > 0) {
            byte[] buf = stream.readNBytes(ModemDBRecord.SIZE);
            RecordType type = new RecordType(Byte.toUnsignedInt(buf[0]));
            int group = Byte.toUnsignedInt(buf[1]);
            InsteonAddress address = new InsteonAddress(buf[2], buf[3], buf[4]);
            byte[] data = new byte[] { buf[5], buf[6], buf[7] };

            records.add(new ModemDBRecord(type, group, address, data));
        }

        return records;
    }
}
