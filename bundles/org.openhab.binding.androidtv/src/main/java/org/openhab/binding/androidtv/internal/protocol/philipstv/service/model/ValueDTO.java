/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link TvSettingsUpdateDTO}
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
public class ValueDTO {

    @JsonProperty("Controllable")
    private String controllable = "";

    @JsonProperty
    private DataDTO data;

    @JsonProperty("Nodeid")
    private int nodeid;

    @JsonProperty("Available")
    private String available = "";

    public ValueDTO() {
    }

    public ValueDTO(DataDTO data) {
        this.data = data;
    }

    public void setControllable(String controllable) {
        this.controllable = controllable;
    }

    public String getControllable() {
        return controllable;
    }

    public void setData(DataDTO data) {
        this.data = data;
    }

    public DataDTO getData() {
        return data;
    }

    public void setNodeid(int nodeid) {
        this.nodeid = nodeid;
    }

    public int getNodeid() {
        return nodeid;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getAvailable() {
        return available;
    }

    @Override
    public String toString() {
        return "Value{" + "controllable = '" + controllable + '\'' + ",data = '" + data + '\'' + ",nodeid = '" + nodeid
                + '\'' + ",available = '" + available + '\'' + "}";
    }
}
