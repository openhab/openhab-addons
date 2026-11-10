/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tailwind.internal.dto;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TailwindCmdStatus} is used to define the JSON status response from
 * the TailWind controller API or UDP server.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
public class TailwindCmdStatus implements Serializable {

    @SerializedName("version")

    private String version;
    @SerializedName("data")

    private TailwindCmdStatusData data;
    private static final long serialVersionUID = 9156544887706283356L;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public TailwindCmdStatusData getData() {
        return data;
    }

    public void setData(TailwindCmdStatusData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TailwindCmdStatus.class.getName()).append('@')
                .append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("version");
        sb.append('=');
        sb.append(((this.version == null) ? "<null>" : this.version));
        sb.append(',');
        sb.append("data");
        sb.append('=');
        sb.append(((this.data == null) ? "<null>" : this.data));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }
}
