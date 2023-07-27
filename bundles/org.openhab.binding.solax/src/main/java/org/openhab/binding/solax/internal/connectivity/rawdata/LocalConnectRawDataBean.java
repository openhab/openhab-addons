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
package org.openhab.binding.solax.internal.connectivity.rawdata;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.model.InverterData;
import org.openhab.binding.solax.internal.model.InverterType;
import org.openhab.binding.solax.internal.util.GsonSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link LocalConnectRawDataBean} collects the raw data and the specific implementation to return the parsed data.
 * If there are differences between the inverters probably would be wise to split the parsing in seprate class(es)
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class LocalConnectRawDataBean implements RawDataBean, InverterData {

    private final static Logger logger = LoggerFactory.getLogger(LocalConnectRawDataBean.class);

    private @Nullable String sn;
    private @Nullable String ver;
    private int type;
    private short @Nullable [] Data;
    private String @Nullable [] Information;
    private @Nullable String rawData;

    @Override
    public String toString() {
        return "LocalConnectRawDataBean [sn=" + sn + ", ver=" + ver + ", type=" + type + ", Information="
                + Arrays.toString(Information) + ", Data=" + Arrays.toString(Data) + "]";
    }

    public @Nullable String getSn() {
        return sn;
    }

    public void setSn(@Nullable String sn) {
        this.sn = sn;
    }

    public @Nullable String getVer() {
        return ver;
    }

    public void setVer(@Nullable String ver) {
        this.ver = ver;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public short @Nullable [] getData() {
        return Data;
    }

    public void setData(short @Nullable [] data) {
        Data = data;
    }

    public String @Nullable [] getInformation() {
        return Information;
    }

    public void setInformation(String @Nullable [] information) {
        Information = information;
    }

    @Override
    public @Nullable String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public static @Nullable LocalConnectRawDataBean fromJson(@Nullable String json) {
        if (json == null) {
            throw new IllegalArgumentException("Provided input JSON is null.");
        }

        try {
            Gson gson = GsonSupplier.getInstance();
            LocalConnectRawDataBean deserializedObject = gson.fromJson(json, LocalConnectRawDataBean.class);
            if (deserializedObject != null) {
                deserializedObject.setRawData(json);
                return deserializedObject;
            }
        } catch (JsonSyntaxException e) {
            logger.warn("Unable to deserialize from JSON.", e);
        }
        return null;
    }

    // Parsed inverter data interface implementation starts here

    @Override
    public @Nullable String getWifiSerial() {
        return getSn();
    }

    @Override
    public @Nullable String getWifiVersion() {
        return getVer();
    }

    @Override
    public InverterType getInverterType() {
        return InverterType.fromIndex(type);
    }

    @Override
    public short getInverterVoltage() {
        return (short) (getData(0) / 10);
    }

    @Override
    public short getInverterCurrent() {
        return (short) (getData(1) / 10);
    }

    @Override
    public short getInvertеrOutputPower() {
        return getData(2);
    }

    @Override
    public short getInverterFrequency() {
        return (short) (getData(3) / 100);
    }

    @Override
    public short getPV1Voltage() {
        return (short) (getData(4) / 10);
    }

    @Override
    public short getPV1Current() {
        return (short) (getData(6) / 10);
    }

    @Override
    public short getPV1Power() {
        return getData(8);
    }

    @Override
    public short getPV2Voltage() {
        return (short) (getData(5) / 10);
    }

    @Override
    public short getPV2Current() {
        return (short) (getData(7) / 10);
    }

    @Override
    public short getPV2Power() {
        return getData(9);
    }

    @Override
    public short getBatteryVoltage() {
        return (short) (getData(14) / 100);
    }

    @Override
    public short getBatteryCurrent() {
        return (short) (getData(15) / 100);
    }

    @Override
    public short getBatteryPower() {
        return getData(16);
    }

    @Override
    public short getBatteryTemperature() {
        return getData(17);
    }

    @Override
    public short getBatterySoC() {
        return getData(18);
    }

    @Override
    public long getOnGridTotalYield() {
        return packU16(11, 12) / 100;
    }

    @Override
    public short getOnGridDailyYield() {
        return (short) (getData(13) / 10);
    }

    @Override
    public short getFeedInPower() {
        return getData(32);
    }

    @Override
    public long getTotalFeedInEnergy() {
        return packU16(34, 35) / 100;
    }

    @Override
    public long getTotalConsumption() {
        return packU16(36, 37) / 100;
    }

    private short getData(int index) {
        try {
            short[] data = Data;
            if (data != null) {
                return data[index];
            }
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Tried to get data out of bounds of the raw data array.", e);
        }
        return 0;
    }

    private long packU16(int indexMajor, int indexMinor) {
        short major = getData(indexMajor);
        short minor = getData(indexMinor);
        if (major == 0) {
            return minor;
        }

        return ((major << 16) & 0xFFFF0000) | minor & 0xFFFF;
    }
}
