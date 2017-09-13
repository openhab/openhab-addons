/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Top level data for all the Nest stuff, this is the format the Nest data comes back from Nest in.
 *
 * @author David Bennett - Initial Contribution
 */
public class TopLevelData {
    @SerializedName("devices")
    private NestDevices devices;
    @SerializedName("metadata")
    private NestMetadata metadata;
    @SerializedName("structures")
    private Map<String, Structure> structures;

    public NestDevices getDevices() {
        return devices;
    }

    public NestMetadata getMetadata() {
        return metadata;
    }

    public Map<String, Structure> getStructures() {
        return structures;
    }
}
