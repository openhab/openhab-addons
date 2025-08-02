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
import org.eclipse.jdt.annotation.Nullable;
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

    private final int classId;
    private final ObisCode obisCode;
    private final int version;
    private final @Nullable String label;

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
        if (entries.size() < 3) {
            throw new IllegalArgumentException(
                    "Meter information must contain 3 or more elements, but got: " + entries.size());
        }
        classId = entries.get(0).getValue();
        byte[] obisBytes = entries.get(1).getValue();
        obisCode = new ObisCode(obisBytes);
        version = entries.get(2).getValue();
        label = entries.size() > 3 ? entries.get(3).getValue() : null;
    }

    public AttributeAddress getAttributeAddress() {
        return new AttributeAddress(classId, obisCode, DLMS_ATTRIBUTE_ID_VALUE);
    }

    public String getChannelId() {
        return SmartMeterBindingConstants.getObisChannelId(obisCode.toString());
    }

    public @Nullable String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "DlmsChannelInfo [classId=%d, channelId=%s, version=%d, label=%s]" //
                .formatted(classId, getChannelId(), version, label);
    }
}
