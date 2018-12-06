/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public Map<Integer, HueGroup> groups = new TreeMap<>();
    public Map<Integer, Dummy> scenes = new TreeMap<>();
    public Map<Integer, Dummy> rules = new TreeMap<>();
    public Map<Integer, Dummy> sensors = new TreeMap<>();
    public Map<Integer, Dummy> schedules = new TreeMap<>();
    public Map<Integer, Dummy> resourcelinks = Collections.emptyMap();

    public HueDataStore() {
        // There must be a group 0 all the time!
        groups.put(0, new HueGroup("All lights", null, Collections.emptyMap()));
    }

    public static class Dummy {
    }

    public static class UserAuth {
        public String name = "";
        public String createDate = "";
        public String lastUseDate = "";

        /**
         * For de-serialization.
         */
        public UserAuth() {
        }

        /**
         * Create a new user
         *
         * @param name Visible name
         */
        public UserAuth(String name) {
            this.name = name;
            this.createDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
