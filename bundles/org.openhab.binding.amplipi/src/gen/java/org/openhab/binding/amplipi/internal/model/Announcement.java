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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A PA-like Announcement IF no zones or groups are specified, all available zones are used
 **/
public class Announcement {

    /**
     * URL to media to play as the announcement
     **/
    private String media;

    /**
     * Output volume in dB
     **/
    private Integer vol = -40;

    /**
     * Source to announce with
     **/
    private Integer sourceId = 3;

    /**
     * Set of zone ids belonging to a group
     **/
    private List<Integer> zones = null;

    /**
     * List of group ids
     **/
    private List<Integer> groups = null;

    /**
     * URL to media to play as the announcement
     *
     * @return media
     **/
    @JsonProperty("media")
    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public Announcement media(String media) {
        this.media = media;
        return this;
    }

    /**
     * Output volume in dB
     * minimum: -79
     * maximum: 0
     *
     * @return vol
     **/
    @JsonProperty("vol")
    public Integer getVol() {
        return vol;
    }

    public void setVol(Integer vol) {
        this.vol = vol;
    }

    public Announcement vol(Integer vol) {
        this.vol = vol;
        return this;
    }

    /**
     * Source to announce with
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

    public Announcement sourceId(Integer sourceId) {
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

    public Announcement zones(List<Integer> zones) {
        this.zones = zones;
        return this;
    }

    public Announcement addZonesItem(Integer zonesItem) {
        this.zones.add(zonesItem);
        return this;
    }

    /**
     * List of group ids
     *
     * @return groups
     **/
    @JsonProperty("groups")
    public List<Integer> getGroups() {
        return groups;
    }

    public void setGroups(List<Integer> groups) {
        this.groups = groups;
    }

    public Announcement groups(List<Integer> groups) {
        this.groups = groups;
        return this;
    }

    public Announcement addGroupsItem(Integer groupsItem) {
        this.groups.add(groupsItem);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Announcement {\n");

        sb.append("    media: ").append(toIndentedString(media)).append("\n");
        sb.append("    vol: ").append(toIndentedString(vol)).append("\n");
        sb.append("    sourceId: ").append(toIndentedString(sourceId)).append("\n");
        sb.append("    zones: ").append(toIndentedString(zones)).append("\n");
        sb.append("    groups: ").append(toIndentedString(groups)).append("\n");
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
