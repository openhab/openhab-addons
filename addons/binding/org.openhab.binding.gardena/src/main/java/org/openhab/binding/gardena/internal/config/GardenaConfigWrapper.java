/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
