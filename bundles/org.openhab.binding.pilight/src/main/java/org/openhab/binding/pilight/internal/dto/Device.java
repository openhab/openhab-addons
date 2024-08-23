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
package org.openhab.binding.pilight.internal.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class describing a device in pilight
 *
 * @author Jeroen Idserda - Initial contribution
 * @author Stefan Röllin - Port to openHAB 2 pilight binding
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {

    private String uuid;

    private String origin;

    private String timestamp;

    private List<String> protocol;

    private String state;

    private Integer dimlevel = null;

    // @SerializedName("dimlevel-maximum")
    private Integer dimlevelMaximum = null;

    private Integer dimlevelMinimum = null;

    private List<Map<String, String>> id;

    private Map<String, String> properties = new HashMap<>();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getProtocol() {
        return protocol;
    }

    public void setProtocol(List<String> protocol) {
        this.protocol = protocol;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getDimlevel() {
        return dimlevel;
    }

    public void setDimlevel(Integer dimlevel) {
        this.dimlevel = dimlevel;
    }

    public Integer getDimlevelMaximum() {
        return dimlevelMaximum;
    }

    @JsonProperty("dimlevel-maximum")
    public void setDimlevelMaximum(Integer dimlevelMaximum) {
        this.dimlevelMaximum = dimlevelMaximum;
    }

    public Integer getDimlevelMinimum() {
        return dimlevelMinimum;
    }

    @JsonProperty("dimlevel-minimum")
    public void setDimlevelMinimum(Integer dimlevelMinimum) {
        this.dimlevelMinimum = dimlevelMinimum;
    }

    public List<Map<String, String>> getId() {
        return id;
    }

    public void setId(List<Map<String, String>> id) {
        this.id = id;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @JsonAnySetter
    public void set(String name, Object value) {
        properties.put(name, value.toString());
    }
}
