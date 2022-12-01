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
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.Msg;

/**
 * The ModemDBRecord class holds a modem link database record
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ModemDBRecord extends BaseRecord {

    public ModemDBRecord(RecordType type, byte group, InsteonAddress address, byte[] data) {
        super(type, group, address, data);
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
     * Factory method for creating a ModemDBRecord from an Insteon record message
     *
     * @param msg the Insteon record message to parse
     * @return the modem db record
     * @throws FieldException
     */
    public static ModemDBRecord fromRecordMsg(Msg msg) throws FieldException {
        RecordType type = RecordType.fromRecordMsg(msg);
        byte group = msg.getByte("ALLLinkGroup");
        InsteonAddress address = msg.getAddress("LinkAddr");
        byte[] data = new byte[3];

        if (msg.getByte("Cmd") == 0x53 && type == RecordType.CONTROLLER) {
            data = new byte[] { msg.getByte("DeviceCategory"), msg.getByte("DeviceSubcategory"),
                    msg.getByte("FirmwareVersion") };
        } else if (msg.getByte("Cmd") == 0x57 || msg.getByte("Cmd") == 0x6F) {
            data = new byte[] { msg.getByte("LinkData1"), msg.getByte("LinkData2"), msg.getByte("LinkData3") };
        }

        return new ModemDBRecord(type, group, address, data);
    }
}
