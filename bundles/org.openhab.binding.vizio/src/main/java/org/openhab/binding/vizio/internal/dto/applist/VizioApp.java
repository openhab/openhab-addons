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
package org.openhab.binding.vizio.internal.dto.applist;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VizioApp} class contains the name and config data for an app that runs on a Vizio TV
 *
 * @author Michael Lobstein - Initial contribution
 */
public class VizioApp {
    @SerializedName("name")
    private String name = "";
    @SerializedName("config")
    private VizioAppConfig config = new VizioAppConfig();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VizioAppConfig getConfig() {
        return config;
    }

    public void setConfig(VizioAppConfig config) {
        this.config = config;
    }
}
