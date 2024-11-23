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
 * A set of partial configuration changes to make to sources, zones, and groups
 **/
@Schema(description = "A set of partial configuration changes to make to sources, zones, and groups ")
public class PresetState {

    @Schema
    private List<SourceUpdate2> sources = null;

    @Schema
    private List<ZoneUpdate2> zones = null;

    @Schema
    private List<GroupUpdate2> groups = null;

    /**
     * Get sources
     *
     * @return sources
     **/
    public List<SourceUpdate2> getSources() {
        return sources;
    }

    public void setSources(List<SourceUpdate2> sources) {
        this.sources = sources;
    }

    public PresetState sources(List<SourceUpdate2> sources) {
        this.sources = sources;
        return this;
    }

    public PresetState addSourcesItem(SourceUpdate2 sourcesItem) {
        this.sources.add(sourcesItem);
        return this;
    }

    /**
     * Get zones
     *
     * @return zones
     **/
    public List<ZoneUpdate2> getZones() {
        return zones;
    }

    public void setZones(List<ZoneUpdate2> zones) {
        this.zones = zones;
    }

    public PresetState zones(List<ZoneUpdate2> zones) {
        this.zones = zones;
        return this;
    }

    public PresetState addZonesItem(ZoneUpdate2 zonesItem) {
        this.zones.add(zonesItem);
        return this;
    }

    /**
     * Get groups
     *
     * @return groups
     **/
    public List<GroupUpdate2> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupUpdate2> groups) {
        this.groups = groups;
    }

    public PresetState groups(List<GroupUpdate2> groups) {
        this.groups = groups;
        return this;
    }

    public PresetState addGroupsItem(GroupUpdate2 groupsItem) {
        this.groups.add(groupsItem);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PresetState {\n");

        sb.append("    sources: ").append(toIndentedString(sources)).append("\n");
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
