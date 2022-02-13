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

import com.google.gson.annotations.SerializedName;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Audio output to a stereo pair of speakers, typically belonging to a room
 **/
@Schema(description = "Audio output to a stereo pair of speakers, typically belonging to a room ")
public class Zone {

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

    public Zone id(Integer id) {
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

    public Zone name(String name) {
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

    public Zone sourceId(Integer sourceId) {
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

    public Zone mute(Boolean mute) {
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

    public Zone vol(Integer vol) {
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

    public Zone disabled(Boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Zone {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    sourceId: ").append(toIndentedString(sourceId)).append("\n");
        sb.append("    mute: ").append(toIndentedString(mute)).append("\n");
        sb.append("    vol: ").append(toIndentedString(vol)).append("\n");
        sb.append("    disabled: ").append(toIndentedString(disabled)).append("\n");
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
