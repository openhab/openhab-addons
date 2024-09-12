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
package org.openhab.binding.digitalstrom.internal.lib.structure;

import java.util.List;

import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device;

/**
 * The {@link Zone} represents a digitalSTROM-Zone.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - add java-doc
 * @author Matthias Siegele - add java-doc
 */
public interface Zone {

    /**
     * Returns the zone id of this {@link Zone}.
     *
     * @return zoneID
     */
    int getZoneId();

    /**
     * Sets the zone id of this {@link Zone}.
     *
     * @param id to set
     */
    void setZoneId(int id);

    /**
     * Returns the zone name of this {@link Zone}.
     *
     * @return zone name
     */
    String getName();

    /**
     * Sets the zone name of this {@link Zone}.
     *
     * @param name to set
     */
    void setName(String name);

    /**
     * Returns the {@link List} of all included groups as {@link DetailedGroupInfo}.
     *
     * @return list of all groups
     */
    List<DetailedGroupInfo> getGroups();

    /**
     * Adds a group as {@link DetailedGroupInfo}.
     *
     * @param group to add
     */
    void addGroup(DetailedGroupInfo group);

    /**
     * Returns a {@link List} of all included {@link Device}'s.
     *
     * @return device list
     */
    List<Device> getDevices();

    /**
     * Adds a {@link Device} to this {@link Zone}.
     *
     * @param device to add
     */
    void addDevice(Device device);
}
