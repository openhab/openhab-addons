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
package org.openhab.binding.hue.internal.api.dto.clip1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Container for all data on a bridge.
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class FullConfig {
    private Map<String, FullLight> lights;
    private Map<String, FullGroup> groups;
    private Config config;

    /**
     * Returns detailed information about all lights known to the bridge.
     *
     * @return detailed lights list
     */
    public List<FullLight> getLights() {
        ArrayList<FullLight> lightsList = new ArrayList<>();

        for (Map.Entry<String, FullLight> entry : lights.entrySet()) {
            String id = entry.getKey();
            FullLight light = entry.getValue();
            light.setId(id);
            lightsList.add(light);
        }

        return lightsList;
    }

    /**
     * Returns detailed information about all groups on the bridge.
     *
     * @return detailed groups list
     */
    public List<FullGroup> getGroups() {
        ArrayList<FullGroup> groupsList = new ArrayList<>();

        for (Map.Entry<String, FullGroup> entry : groups.entrySet()) {
            String id = entry.getKey();
            FullGroup group = entry.getValue();
            group.setId(id);
            groupsList.add(group);
        }

        return groupsList;
    }

    /**
     * Returns bridge configuration.
     * Use HueBridge.getConfig() if you only need this.
     *
     * @return bridge configuration
     */
    public Config getConfig() {
        return config;
    }
}
