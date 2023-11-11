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

import com.google.gson.annotations.SerializedName;

/**
 * @author Marco Descher - Initial contribution
 */
public class Datapoint {

    @SerializedName("Config")
    private Config config;

    @SerializedName("DatapointValue")
    private DatapointValue datapointValue;

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public DatapointValue getDatapointValue() {
        return datapointValue;
    }

    public void setDatapointValue(DatapointValue datapointValue) {
        this.datapointValue = datapointValue;
    }
}
