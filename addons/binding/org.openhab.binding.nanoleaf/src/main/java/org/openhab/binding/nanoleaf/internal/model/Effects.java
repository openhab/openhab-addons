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
package org.openhab.binding.nanoleaf.internal.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents effect commands for select and write
 *
 * @author Martin Raepple - Initial contribution
 */
public class Effects {

    @SerializedName("select")
    @Expose
    private String select;
    @SerializedName("effectsList")
    @Expose
    private List<String> effectsList = null;
    @SerializedName("write")
    @Expose
    private Write write;

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public List<String> getEffectsList() {
        return effectsList;
    }

    public void setEffectsList(List<String> effectsList) {
        this.effectsList = effectsList;
    }

    public Write getWrite() {
        return write;
    }

    public void setWrite(Write write) {
        this.write = write;
    }

}
