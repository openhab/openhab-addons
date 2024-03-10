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
package org.openhab.binding.solax.internal.connectivity.rawdata.local;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.connectivity.rawdata.RawDataBean;
import org.openhab.binding.solax.internal.util.GsonSupplier;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link LocalConnectRawDataBean} collects the raw data and the specific implementation to return the parsed data.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class LocalConnectRawDataBean implements RawDataBean {

    private @Nullable String sn;
    private @Nullable String ver;
    private int type;
    @SerializedName("Data")
    private short @Nullable [] data;
    @SerializedName("Information")
    private String @Nullable [] information;
    private @Nullable String rawData;

    @Override
    public String toString() {
        return "LocalConnectRawDataBean [sn=" + sn + ", ver=" + ver + ", type=" + type + ", Information="
                + Arrays.toString(information) + ", Data=" + Arrays.toString(data) + "]";
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
        return data;
    }

    public void setData(short @Nullable [] data) {
        this.data = data;
    }

    public String @Nullable [] getInformation() {
        return information;
    }

    public void setInformation(String @Nullable [] information) {
        this.information = information;
    }

    @Override
    public @Nullable String getRawData() {
        return rawData;
    }

    @Override
    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public static LocalConnectRawDataBean fromJson(String json) {
        if (json.isEmpty()) {
            throw new IllegalArgumentException("JSON payload should not be empty");
        }

        Gson gson = GsonSupplier.getInstance();
        LocalConnectRawDataBean deserializedObject = gson.fromJson(json, LocalConnectRawDataBean.class);
        if (deserializedObject == null) {
            throw new IllegalStateException("Unexpected null result when deserializing JSON");
        }
        deserializedObject.setRawData(json);
        return deserializedObject;
    }
}
