/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class Reception {

    @SerializedName("Rssi")
    @Expose
    private Integer rssi;
    @SerializedName("Lqi")
    @Expose
    private Integer lqi;

    public Integer getRSSI() {
        return rssi;
    }

    public Integer getLQI() {
        return lqi;
    }

}
