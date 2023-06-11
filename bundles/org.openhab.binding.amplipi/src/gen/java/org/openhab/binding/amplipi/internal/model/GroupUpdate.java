/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * Reconfiguration of a Group
 **/
@Schema(description = "Reconfiguration of a Group ")
public class GroupUpdate {

    @Schema
    /**
     * Friendly name
     **/
    private String name;

    @Schema
    @SerializedName("source_id")
    /**
     * id of the connected source
     **/
    private Integer sourceId;

    @Schema
    /**
     * Set of zones belonging to a group
     **/
    private List<Integer> zones;

    @Schema
    /**
     * Set to true if output is all zones muted
     **/
    private Boolean mute;

    @Schema
    @SerializedName("vol_delta")
    /**
     * Average utput volume in dB
     **/
    private Integer volDelta;

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

    public GroupUpdate name(String name) {
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

    public GroupUpdate sourceId(Integer sourceId) {
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

    public GroupUpdate zones(List<Integer> zones) {
        this.zones = zones;
        return this;
    }

    public GroupUpdate addZonesItem(Integer zonesItem) {
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

    public GroupUpdate mute(Boolean mute) {
        this.mute = mute;
        return this;
    }

    /**
     * Average utput volume in dB
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

    public GroupUpdate volDelta(Integer volDelta) {
        this.volDelta = volDelta;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GroupUpdate {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    sourceId: ").append(toIndentedString(sourceId)).append("\n");
        sb.append("    zones: ").append(toIndentedString(zones)).append("\n");
        sb.append("    mute: ").append(toIndentedString(mute)).append("\n");
        sb.append("    volDelta: ").append(toIndentedString(volDelta)).append("\n");
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
