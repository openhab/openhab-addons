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

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A group of zones that can share the same audio input and be controlled as a group ie. Updstairs. Volume, mute, and
 * source_id fields are aggregates of the member zones.
 **/
@Schema(description = "A group of zones that can share the same audio input and be controlled as a group ie. Updstairs.  Volume, mute, and source_id fields are aggregates of the member zones.")
public class Group {

    @Schema
    /**
     * Unique identifier
     **/
    private Integer id;

    @Schema(required = true)
    /**
     * Friendly name
     **/
    private String name;

    @Schema
    @SerializedName("source_id")
    /**
     * id of the connected source
     **/
    private Integer sourceId = 0;

    @Schema(required = true)
    /**
     * Set of zones belonging to a group
     **/
    private Set<Integer> zones = new LinkedHashSet<Integer>();

    @Schema
    /**
     * Set to true if output is all zones muted
     **/
    private Boolean mute = true;

    @Schema
    @SerializedName("vol_delta")
    /**
     * Average utput volume in dB
     **/
    private Integer volDelta = -79;

    /**
     * Unique identifier
     *
     * @return id
     **/
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Group id(Integer id) {
        this.id = id;
        return this;
    }

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

    public Group name(String name) {
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

    public Group sourceId(Integer sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    /**
     * Set of zones belonging to a group
     *
     * @return zones
     **/
    public Set<Integer> getZones() {
        return zones;
    }

    public void setZones(Set<Integer> zones) {
        this.zones = zones;
    }

    public Group zones(Set<Integer> zones) {
        this.zones = zones;
        return this;
    }

    public Group addZonesItem(Integer zonesItem) {
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

    public Group mute(Boolean mute) {
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

    public Group volDelta(Integer volDelta) {
        this.volDelta = volDelta;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Group {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
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
