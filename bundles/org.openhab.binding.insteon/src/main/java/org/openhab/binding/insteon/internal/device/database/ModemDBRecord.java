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
        super(LOCATION_ZERO, type, group, address, data);
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
     * Factory method for creating a new ModemDBRecord from another instance with new data
     *
     * @param data the new record data to use
     * @param record the modem db record to use
     * @return the modem db record with new type
     */
    public static ModemDBRecord withNewData(byte[] data, ModemDBRecord record) {
        return new ModemDBRecord(record.getType(), record.getGroup(), record.getAddress(), data);
    }
}
