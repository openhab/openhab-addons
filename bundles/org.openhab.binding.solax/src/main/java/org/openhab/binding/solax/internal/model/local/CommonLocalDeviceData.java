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
package org.openhab.binding.solax.internal.model.local;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.connectivity.rawdata.local.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.InverterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommonLocalDeviceData} is an abstract class that contains the common information, applicable for all
 * devices.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public abstract class CommonLocalDeviceData implements LocalData {

    private final Logger logger = LoggerFactory.getLogger(CommonLocalDeviceData.class);

    private LocalConnectRawDataBean data;

    public CommonLocalDeviceData(LocalConnectRawDataBean data) {
        this.data = data;
    }

    @Override
    public @Nullable String getRawData() {
        return data.getRawData();
    }

    @Override
    public @Nullable String getWifiSerial() {
        return data.getSn();
    }

    @Override
    public @Nullable String getWifiVersion() {
        return data.getVer();
    }

    @Override
    public InverterType getInverterType() {
        return InverterType.fromIndex(data.getType());
    }

    protected Short getFromRawData(int index) {
        try {
            short[] dataArray = data.getData();
            if (dataArray != null) {
                return dataArray[index];
            }
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Tried to get data out of bounds of the raw data array.", e);
        }
        return 0;
    }

    public long packU16(int indexMajor, int indexMinor) {
        short major = getFromRawData(indexMajor);
        short minor = getFromRawData(indexMinor);
        if (major == 0) {
            return minor;
        }

        return Integer.toUnsignedLong(major << 16 | minor & 0xFFFF);
    }

    @Override
    public LocalConnectRawDataBean getData() {
        return data;
    }
}
