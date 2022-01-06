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
package org.openhab.binding.amplipi.internal.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Reconfiguration of a specific Group
 **/
public class GroupUpdateWithId {

    /**
     * Friendly name
     **/
    private String name;

    /**
     * id of the connected source
     **/
    private Integer sourceId;

    /**
     * Set of zone ids belonging to a group
     **/
    private List<Integer> zones = null;

    /**
     * Set to true if output is all zones muted
     **/
    private Boolean mute;

    /**
     * Average input volume in dB
     **/
    private Integer volDelta;

    private Integer id;

    /**
     * Friendly name
     *
     * @return name
     **/
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GroupUpdateWithId name(String name) {
        this.name = name;
        return this;
    }

    /**
     * id of the connected source
     * minimum: 0
     * maximum: 3
     *
     * @return sourceId
     **/
    @JsonProperty("source_id")
    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public GroupUpdateWithId sourceId(Integer sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    /**
     * Set of zone ids belonging to a group
     *
     * @return zones
     **/
    @JsonProperty("zones")
    public List<Integer> getZones() {
        return zones;
    }

    public void setZones(List<Integer> zones) {
        this.zones = zones;
    }

    public GroupUpdateWithId zones(List<Integer> zones) {
        this.zones = zones;
        return this;
    }

    public GroupUpdateWithId addZonesItem(Integer zonesItem) {
        this.zones.add(zonesItem);
        return this;
    }

    /**
     * Set to true if output is all zones muted
     *
     * @return mute
     **/
    @JsonProperty("mute")
    public Boolean getMute() {
        return mute;
    }

    public void setMute(Boolean mute) {
        this.mute = mute;
    }

    public GroupUpdateWithId mute(Boolean mute) {
        this.mute = mute;
        return this;
    }

    /**
     * Average input volume in dB
     * minimum: -79
     * maximum: 0
     *
     * @return volDelta
     **/
    @JsonProperty("vol_delta")
    public Integer getVolDelta() {
        return volDelta;
    }

    public void setVolDelta(Integer volDelta) {
        this.volDelta = volDelta;
    }

    public GroupUpdateWithId volDelta(Integer volDelta) {
        this.volDelta = volDelta;
        return this;
    }

    /**
     * Get id
     *
     * @return id
     **/
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public GroupUpdateWithId id(Integer id) {
        this.id = id;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GroupUpdateWithId {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    sourceId: ").append(toIndentedString(sourceId)).append("\n");
        sb.append("    zones: ").append(toIndentedString(zones)).append("\n");
        sb.append("    mute: ").append(toIndentedString(mute)).append("\n");
        sb.append("    volDelta: ").append(toIndentedString(volDelta)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
