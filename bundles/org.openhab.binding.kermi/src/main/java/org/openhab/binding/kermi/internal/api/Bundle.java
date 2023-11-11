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
package org.openhab.binding.kermi.internal.api;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author Marco Descher - Initial contribution
 */
public class Bundle {

    @SerializedName("DatapointBundleId")
    private String datapointBundleId;

    @SerializedName("Datapoints")
    private List<Datapoint> datapoints;

    @SerializedName("DisplayName")
    private String displayName;

    public String getDatapointBundleId() {
        return datapointBundleId;
    }

    public void setDatapointBundleId(String datapointBundleId) {
        this.datapointBundleId = datapointBundleId;
    }

    public List<Datapoint> getDatapoints() {
        return datapoints;
    }

    public void setDatapoints(List<Datapoint> datapoints) {
        this.datapoints = datapoints;
    }
}
