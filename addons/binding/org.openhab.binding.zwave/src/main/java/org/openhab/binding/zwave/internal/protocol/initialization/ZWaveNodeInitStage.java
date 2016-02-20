/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.initialization;

import java.util.HashMap;
import java.util.Map;

/**
 * Node Stage Enumeration for node initialisation.
 *
 * @author Chris Jackson
 * @author Brian Crosby
 */
public enum ZWaveNodeInitStage {
    EMPTYNODE(0, true, "Empty New Node"),
    PROTOINFO(1, true, "Protocol Information"),
    NEIGHBORS(2, true, "Node Neighbor Information"),
    FAILED_CHECK(3, true, "Checking if node is failed"),
    WAIT(4, true, "Waiting"),
    PING(5, true, "Ping Node"),
    DETAILS(6, true, "Node Information"),
    MANUFACTURER(7, true, "Manufacture Name and Product Identification"),
    APP_VERSION(8, true, "Application Version"),
    DISCOVERY_COMPLETE(9, true, "Discovery Complete"),
    DISCOVERY_WAIT(10, true, "Discovery Wait"),
    VERSION(11, true, "Command Class Versions"),
    ENDPOINTS(12, true, "Command Class Endpoints"),
    UPDATE_DATABASE(13, true, "Updating database"),
    STATIC_VALUES(14, true, "Static Information"),
    ASSOCIATIONS(15, false, "Associations"),
    SET_WAKEUP(16, false, "Wakeup Target"),
    SET_ASSOCIATION(17, false, "Wakeup Target"),
    GET_CONFIGURATION(18, false, "Getting configuration"),
    STATIC_END(19, false, "Static Initialisation Finished"),

    // States below are not restored from the configuration files
    SESSION_START(20, false, "Restore Marker"),
    DYNAMIC_VALUES(21, false, "Frequently Changed Information"),

    DONE(22, false, "Node Complete");

    private int stage;
    private boolean mandatory;
    private String label;

    /**
     * A mapping between the integer code and its corresponding
     * Node Stage to facilitate lookup by code.
     */
    private static Map<Integer, ZWaveNodeInitStage> codeToNodeStageMapping;

    private ZWaveNodeInitStage(int s, boolean m, String l) {
        stage = s;
        mandatory = m;
        label = l;
    }

    private static void initMapping() {
        codeToNodeStageMapping = new HashMap<Integer, ZWaveNodeInitStage>();
        for (ZWaveNodeInitStage s : values()) {
            codeToNodeStageMapping.put(s.stage, s);
        }
    }

    /**
     * Get the stage protocol number.
     *
     * @return number
     */
    public int getStage() {
        return this.stage;
    }

    /**
     * Get the stage label
     *
     * @return label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Lookup function based on the command class code.
     * Returns null if there is no command class with code i
     *
     * @param i the code to lookup
     * @return enumeration value of the command class.
     */
    public static ZWaveNodeInitStage getNodeStage(int i) {
        if (codeToNodeStageMapping == null) {
            initMapping();
        }

        return codeToNodeStageMapping.get(i);
    }

    /**
     * Return the next stage after the current stage
     *
     * @return the next stage
     */
    public ZWaveNodeInitStage getNextStage() {
        for (ZWaveNodeInitStage s : values()) {
            if (s.stage == this.stage + 1) {
                return s;
            }
        }

        return null;
    }

    /**
     * Check if the current stage has completed the static stages.
     *
     * @return true if static stages complete
     */
    public boolean isStaticComplete() {
        if (stage > SESSION_START.stage) {
            return true;
        }
        return false;
    }

    /**
     * Check if the current stage has completed the static stages.
     *
     * @return true if static stages complete
     */
    public boolean isStageMandatory() {
        return mandatory;
    }
}
