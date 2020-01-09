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
package org.openhab.binding.miio.internal.basic;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.openhab.binding.miio.internal.MiIoCommand;

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
    @SerializedName("propertyMethod")
    @Expose
    private String propertyMethod = MiIoCommand.GET_PROPERTY.getCommand();
    @SerializedName("maxProperties")
    @Expose
    private int maxProperties = 5;

    public List<String> getId() {
        return id;
    }

    public void setId(List<String> id) {
        this.id = id;
    }

    public String getPropertyMethod() {
        return propertyMethod;
    }

    public void setPropertyMethod(String propertyMethod) {
        this.propertyMethod = propertyMethod;
    }

    public int getMaxProperties() {
        return maxProperties;
    }

    public void setMaxProperties(int maxProperties) {
        this.maxProperties = maxProperties;
    }

    public List<MiIoBasicChannel> getChannels() {
        return miIoBasicChannels;
    }

    public void setChannels(List<MiIoBasicChannel> miIoBasicChannels) {
        this.miIoBasicChannels = miIoBasicChannels;
    }

}
