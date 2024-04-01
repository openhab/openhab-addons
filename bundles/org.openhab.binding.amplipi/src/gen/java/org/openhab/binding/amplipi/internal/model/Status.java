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

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Full Controller Configuration and Status
 **/
@Schema(description = "Full Controller Configuration and Status ")
public class Status {

    @Schema
    private List<Source> sources = null;

    @Schema
    private List<Zone> zones = null;

    @Schema
    private List<Group> groups = null;

    @Schema
    private List<Stream> streams = null;

    @Schema
    private List<Preset> presets = null;

    @Schema
    private Info info;

    /**
     * Get sources
     *
     * @return sources
     **/
    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    public Status sources(List<Source> sources) {
        this.sources = sources;
        return this;
    }

    public Status addSourcesItem(Source sourcesItem) {
        this.sources.add(sourcesItem);
        return this;
    }

    /**
     * Get zones
     *
     * @return zones
     **/
    public List<Zone> getZones() {
        return zones;
    }

    public void setZones(List<Zone> zones) {
        this.zones = zones;
    }

    public Status zones(List<Zone> zones) {
        this.zones = zones;
        return this;
    }

    public Status addZonesItem(Zone zonesItem) {
        this.zones.add(zonesItem);
        return this;
    }

    /**
     * Get groups
     *
     * @return groups
     **/
    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public Status groups(List<Group> groups) {
        this.groups = groups;
        return this;
    }

    public Status addGroupsItem(Group groupsItem) {
        this.groups.add(groupsItem);
        return this;
    }

    /**
     * Get streams
     *
     * @return streams
     **/
    public List<Stream> getStreams() {
        return streams;
    }

    public void setStreams(List<Stream> streams) {
        this.streams = streams;
    }

    public Status streams(List<Stream> streams) {
        this.streams = streams;
        return this;
    }

    public Status addStreamsItem(Stream streamsItem) {
        this.streams.add(streamsItem);
        return this;
    }

    /**
     * Get presets
     *
     * @return presets
     **/
    public List<Preset> getPresets() {
        return presets;
    }

    public void setPresets(List<Preset> presets) {
        this.presets = presets;
    }

    public Status presets(List<Preset> presets) {
        this.presets = presets;
        return this;
    }

    public Status addPresetsItem(Preset presetsItem) {
        this.presets.add(presetsItem);
        return this;
    }

    /**
     * Get info
     *
     * @return info
     **/
    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Status info(Info info) {
        this.info = info;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Status {\n");

        sb.append("    sources: ").append(toIndentedString(sources)).append("\n");
        sb.append("    zones: ").append(toIndentedString(zones)).append("\n");
        sb.append("    groups: ").append(toIndentedString(groups)).append("\n");
        sb.append("    streams: ").append(toIndentedString(streams)).append("\n");
        sb.append("    presets: ").append(toIndentedString(presets)).append("\n");
        sb.append("    info: ").append(toIndentedString(info)).append("\n");
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
