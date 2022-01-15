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

import com.google.gson.annotations.SerializedName;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Reconfiguration of a specific Group
 **/
@Schema(description = "Reconfiguration of a specific Group ")
public class GroupUpdate2 {

    @Schema
    /**
     * Friendly name
     **/
    private String name;

    @Schema
    /**
     * id of the connected source
     **/
    @SerializedName("source_id")
    private Integer sourceId = 0;

    @Schema
    /**
     * Set of zones belonging to a group
     **/
    private List<Integer> zones = null;

    @Schema
    /**
     * Set to true if output is all zones muted
     **/
    private Boolean mute = true;

    @Schema
    @SerializedName("vol_delta")
    /**
     * Average output volume in dB
     **/
    private Integer volDelta = -79;

    @Schema(required = true)
    private Integer id;

    /**
     * Friendly name
     *
     * @return name
     **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GroupUpdate2 name(String name) {
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
    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public GroupUpdate2 sourceId(Integer sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    /**
     * Set of zones belonging to a group
     *
     * @return zones
     **/
    public List<Integer> getZones() {
        return zones;
    }

    public void setZones(List<Integer> zones) {
        this.zones = zones;
    }

    public GroupUpdate2 zones(List<Integer> zones) {
        this.zones = zones;
        return this;
    }

    public GroupUpdate2 addZonesItem(Integer zonesItem) {
        this.zones.add(zonesItem);
        return this;
    }

    /**
     * Set to true if output is all zones muted
     *
     * @return mute
     **/
    public Boolean getMute() {
        return mute;
    }

    public void setMute(Boolean mute) {
        this.mute = mute;
    }

    public GroupUpdate2 mute(Boolean mute) {
        this.mute = mute;
        return this;
    }

    /**
     * Average output volume in dB
     * minimum: -79
     * maximum: 0
     *
     * @return volDelta
     **/
    public Integer getVolDelta() {
        return volDelta;
    }

    public void setVolDelta(Integer volDelta) {
        this.volDelta = volDelta;
    }

    public GroupUpdate2 volDelta(Integer volDelta) {
        this.volDelta = volDelta;
        return this;
    }

    /**
     * Get id
     *
     * @return id
     **/
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public GroupUpdate2 id(Integer id) {
        this.id = id;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GroupUpdate2 {\n");

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
