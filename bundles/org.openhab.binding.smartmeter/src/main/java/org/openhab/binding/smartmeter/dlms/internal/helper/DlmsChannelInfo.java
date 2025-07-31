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
package org.openhab.binding.smartmeter.dlms.internal.helper;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartmeter.SmartMeterBindingConstants;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;

/**
 * A class to hold information about a DLMS/COSEM meter channel.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class DlmsChannelInfo {

    public final int classId;
    public final ObisCode obisCode;

    public DlmsChannelInfo(DataObject item) throws IllegalArgumentException, ClassCastException {
        if (item.getType() != Type.STRUCTURE) {
            throw new IllegalArgumentException("Invalid meter information: " + item);
        }
        List<DataObject> fields = item.getValue();
        classId = fields.get(0).getValue();
        byte[] obisBytes = fields.get(1).getValue();
        obisCode = new ObisCode(obisBytes);
    }

    public AttributeAddress getAttributeAddress() {
        return new AttributeAddress(classId, obisCode, 2);
    }

    public String getChannelId() {
        return SmartMeterBindingConstants.getObisChannelId(obisCode.toString());
    }

    @Override
    public String toString() {
        return "DlmsChannelInfo [channelId=" + getChannelId() + ", classId=" + classId + "]";
    }
}
