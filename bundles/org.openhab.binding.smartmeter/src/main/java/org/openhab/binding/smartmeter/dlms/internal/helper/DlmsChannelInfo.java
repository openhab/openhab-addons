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

import static org.openhab.binding.smartmeter.SmartMeterBindingConstants.DLMS_ATTRIBUTE_ID_VALUE;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartmeter.SmartMeterBindingConstants;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.datatypes.DataObject;

/**
 * A class to parse and hold information about a DLMS/COSEM meter channel.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class DlmsChannelInfo {

    private static final int MINIMUM_METER_INFO_ENTRIES = 3;

    private final int classId;
    private final ObisCode obisCode;
    private final int version;

    @SuppressWarnings("unchecked")
    public DlmsChannelInfo(DataObject dataObject) throws IllegalArgumentException {
        List<DataObject> entries;
        if (dataObject.getValue() instanceof List<?> dataList) {
            entries = (List<DataObject>) dataList;
        } else if (dataObject.getValue() instanceof DataObject[] dataArray) {
            entries = Arrays.asList(dataArray);
        } else {
            throw new IllegalArgumentException("Invalid meter information: " + dataObject);
        }
        if (entries.size() < MINIMUM_METER_INFO_ENTRIES) {
            throw new IllegalArgumentException("Meter information must contain %d or more elements, but got %d"
                    .formatted(MINIMUM_METER_INFO_ENTRIES, entries.size()));
        }
        classId = entries.get(0).getValue();
        byte[] obisBytes = entries.get(1).getValue();
        obisCode = new ObisCode(obisBytes);
        version = entries.get(2).getValue();
    }

    public AttributeAddress getAttributeAddress() {
        return new AttributeAddress(classId, obisCode, DLMS_ATTRIBUTE_ID_VALUE);
    }

    public String getChannelId() {
        return SmartMeterBindingConstants.getObisChannelId(obisCode.toString());
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    @Override
    public String toString() {
        return "DlmsChannelInfo [classId=%d, channelId=%s, version=%d]" //
                .formatted(classId, getChannelId(), version);
    }
}
