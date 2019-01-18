/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.dto;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Hue data store object. Contains all lights, configuration, user whitelist etc.
 * Is used as a data store but also as API DTO.
 *
 * @author Dan Cunningham - Initial contribution
 * @author David Graeff - Add groups,scenes,rules,sensors,resourcelinks and config entries
 *
 */
@NonNullByDefault
public class HueDataStore {
    public HueAuthorizedConfig config = new HueAuthorizedConfig();
    public TreeMap<Integer, HueDevice> lights = new TreeMap<>();
    public TreeMap<Integer, HueGroup> groups = new TreeMap<>();
    public Map<Integer, Dummy> scenes = new TreeMap<>();
    public Map<Integer, Dummy> rules = new TreeMap<>();
    public Map<Integer, Dummy> sensors = new TreeMap<>();
    public Map<Integer, Dummy> schedules = new TreeMap<>();
    public Map<Integer, Dummy> resourcelinks = Collections.emptyMap();

    public HueDataStore() {
        resetGroupsAndLights();
    }

    public void resetGroupsAndLights() {
        groups.clear();
        lights.clear();
        // There must be a group 0 all the time!
        groups.put(0, new HueGroup("All lights", null, Collections.emptyMap()));
    }

    public int generateNextLightHueID() {
        return lights.size() == 0 ? 1 : new Integer(lights.lastKey().intValue() + 1);
    }

    public int generateNextGroupHueID() {
        return groups.size() == 0 ? 1 : new Integer(groups.lastKey().intValue() + 1);
    }

    public static class Dummy {
    }
}
