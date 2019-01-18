/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.gardena.internal.config;

import com.google.gson.annotations.SerializedName;

/**
 * GardenaConfg wrapper for valid Gardena JSON serialization.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GardenaConfigWrapper {

    @SerializedName(value = "sessions")
    private GardenaConfig config;

    public GardenaConfigWrapper() {
    }

    public GardenaConfigWrapper(GardenaConfig config) {
        this.config = config;
    }

    /**
     * Returns the config.
     */
    public GardenaConfig getConfig() {
        return config;
    }

    /**
     * Sets the config.
     */
    public void setConfig(GardenaConfig config) {
        this.config = config;
    }

}
