/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
    EMPTYNODE(0, true),
    PROTOINFO(1, true),
    INIT_NEIGHBORS(2, true),
    FAILED_CHECK(3, true),
    WAIT(4, true),
    PING(5, true),
    DETAILS(6, true),
    MANUFACTURER(7, true),
    SECURITY_REPORT(8, true),
    APP_VERSION(9, true),
    DISCOVERY_COMPLETE(10, true),
    VERSION(11, true),
    ENDPOINTS(12, true),
    UPDATE_DATABASE(13, true),
    STATIC_VALUES(14, true),
    ASSOCIATIONS(15, false),
    SET_WAKEUP(16, false),
    SET_ASSOCIATION(17, false),
    STATIC_END(18, false),

    // States below are not restored from the configuration files
    SESSION_START(19, false),
    GET_CONFIGURATION(20, false),
    DYNAMIC_VALUES(21, false),
    DYNAMIC_END(22, false),

    // States below are performed during initialisation, but also during heal
    HEAL(23, false),
    DELETE_ROUTES(24, false),
    SUC_ROUTE(25, false),
    RETURN_ROUTES(26, false),
    NEIGHBORS(27, true),

    DONE(28, false);

    private int stage;
    private boolean mandatory;

    /**
     * A mapping between the integer code and its corresponding
     * Node Stage to facilitate lookup by code.
     */
    private static Map<Integer, ZWaveNodeInitStage> codeToNodeStageMapping;

    private ZWaveNodeInitStage(int s, boolean m) {
        stage = s;
        mandatory = m;
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
