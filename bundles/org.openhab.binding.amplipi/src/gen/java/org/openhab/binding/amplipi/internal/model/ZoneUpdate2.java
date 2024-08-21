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
package org.openhab.binding.amplipi.internal.model;

import com.google.gson.annotations.SerializedName;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Reconfiguration of a specific Zone
 **/
@Schema(description = "Reconfiguration of a specific Zone ")
public class ZoneUpdate2 {

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
    private Integer sourceId = 0;

    @Schema
    /**
     * Set to true if output is muted
     **/
    private Boolean mute = true;

    @Schema
    /**
     * Output volume in dB
     **/
    private Integer vol = -79;

    @Schema
    /**
     * Set to true if not connected to a speaker
     **/
    private Boolean disabled = false;

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

    public ZoneUpdate2 name(String name) {
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

    public ZoneUpdate2 sourceId(Integer sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    /**
     * Set to true if output is muted
     *
     * @return mute
     **/
    public Boolean getMute() {
        return mute;
    }

    public void setMute(Boolean mute) {
        this.mute = mute;
    }

    public ZoneUpdate2 mute(Boolean mute) {
        this.mute = mute;
        return this;
    }

    /**
     * Output volume in dB
     * minimum: -79
     * maximum: 0
     *
     * @return vol
     **/
    public Integer getVol() {
        return vol;
    }

    public void setVol(Integer vol) {
        this.vol = vol;
    }

    public ZoneUpdate2 vol(Integer vol) {
        this.vol = vol;
        return this;
    }

    /**
     * Set to true if not connected to a speaker
     *
     * @return disabled
     **/
    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public ZoneUpdate2 disabled(Boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    /**
     * Get id
     * minimum: 0
     * maximum: 35
     *
     * @return id
     **/
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ZoneUpdate2 id(Integer id) {
        this.id = id;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ZoneUpdate2 {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    sourceId: ").append(toIndentedString(sourceId)).append("\n");
        sb.append("    mute: ").append(toIndentedString(mute)).append("\n");
        sb.append("    vol: ").append(toIndentedString(vol)).append("\n");
        sb.append("    disabled: ").append(toIndentedString(disabled)).append("\n");
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
