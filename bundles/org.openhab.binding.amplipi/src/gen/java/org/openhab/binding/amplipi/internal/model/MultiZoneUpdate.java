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
 * Reconfiguration of multiple zones specified by zone_ids and group_ids
 **/
public class MultiZoneUpdate {

    /**
     * Set of zone ids belonging to a group
     **/
    private List<Integer> zones = null;

    /**
     * List of group ids
     **/
    private List<Integer> groups = null;

    private ZoneUpdate update;

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

    public MultiZoneUpdate zones(List<Integer> zones) {
        this.zones = zones;
        return this;
    }

    public MultiZoneUpdate addZonesItem(Integer zonesItem) {
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

    public MultiZoneUpdate groups(List<Integer> groups) {
        this.groups = groups;
        return this;
    }

    public MultiZoneUpdate addGroupsItem(Integer groupsItem) {
        this.groups.add(groupsItem);
        return this;
    }

    /**
     * Get update
     *
     * @return update
     **/
    @JsonProperty("update")
    public ZoneUpdate getUpdate() {
        return update;
    }

    public void setUpdate(ZoneUpdate update) {
        this.update = update;
    }

    public MultiZoneUpdate update(ZoneUpdate update) {
        this.update = update;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MultiZoneUpdate {\n");

        sb.append("    zones: ").append(toIndentedString(zones)).append("\n");
        sb.append("    groups: ").append(toIndentedString(groups)).append("\n");
        sb.append("    update: ").append(toIndentedString(update)).append("\n");
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
