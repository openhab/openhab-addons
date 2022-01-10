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
    public TreeMap<String, HueLightEntry> lights = new TreeMap<>();
    public TreeMap<String, HueGroupEntry> groups = new TreeMap<>();
    public Map<String, HueSceneEntry> scenes = new TreeMap<>();
    public Map<String, HueRuleEntry> rules = new TreeMap<>();
    public Map<String, HueSensorEntry> sensors = new TreeMap<>();
    public Map<String, HueScheduleEntry> schedules = new TreeMap<>();
    public Map<Integer, Dummy> resourcelinks = Collections.emptyMap();
    public Map<String, HueCapability> capabilities = new TreeMap<>();

    public HueDataStore() {
        resetGroupsAndLights();
        capabilities.put("lights", new HueCapability());
        capabilities.put("groups", new HueCapability());
        capabilities.put("scenes", new HueCapability());
        capabilities.put("rules", new HueCapability());
        capabilities.put("sensors", new HueCapability());
        capabilities.put("schedules", new HueCapability());
        capabilities.put("resourcelinks", new HueCapability());
    }

    public void resetGroupsAndLights() {
        groups.clear();
        lights.clear();
        // There must be a group 0 all the time!
        groups.put("0", new HueGroupEntry("All lights", null, null));
    }

    public void resetSensors() {
        sensors.clear();
    }

    public static class Dummy {
    }

    /**
     * Return a unique group id.
     */
    public String nextGroupID() {
        int nextId = groups.size();
        while (true) {
            String id = "hueemulation" + String.valueOf(nextId);
            if (!groups.containsKey(id)) {
                return id;
            }
            ++nextId;
        }
    }
}
