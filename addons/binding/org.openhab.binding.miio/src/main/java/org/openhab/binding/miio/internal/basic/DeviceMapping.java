/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal.basic;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping devices from json
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class DeviceMapping {

    @SerializedName("id")
    @Expose
    private List<String> id = new ArrayList<String>();
    @SerializedName("channels")
    @Expose
    private List<MiIoBasicChannel> miIoBasicChannels = new ArrayList<MiIoBasicChannel>();

    public List<String> getId() {
        return id;
    }

    public void setId(List<String> id) {
        this.id = id;
    }

    public List<MiIoBasicChannel> getChannels() {
        return miIoBasicChannels;
    }

    public void setChannels(List<MiIoBasicChannel> miIoBasicChannels) {
        this.miIoBasicChannels = miIoBasicChannels;
    }

}
