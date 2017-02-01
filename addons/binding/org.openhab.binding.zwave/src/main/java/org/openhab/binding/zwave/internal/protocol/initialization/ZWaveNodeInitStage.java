/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.initialization;

/**
 * Node Stage Enumeration for node initialisation sequence.
 * The initialisation will be performed in the order of the enum definitions.
 *
 * @author Chris Jackson
 */
public enum ZWaveNodeInitStage {
    EMPTYNODE(true),
    PROTOINFO(true),
    INIT_NEIGHBORS(true),
    FAILED_CHECK(true),
    WAIT(true),
    PING(true),
    DETAILS(true),

    // States below form the main part of the initialisation
    // For newly included devices, we start here
    INCLUSION_START(true),
    IDENTIFY_NODE(true),
    MANUFACTURER(true),
    SECURITY_REPORT(true),
    APP_VERSION(true),
    DISCOVERY_COMPLETE(true),
    VERSION(true),
    ENDPOINTS(true),
    UPDATE_DATABASE(true),
    STATIC_VALUES(true),
    ASSOCIATIONS(false),
    SET_WAKEUP(false),
    SET_ASSOCIATION(false),
    DELETE_SUC_ROUTES(false),
    SUC_ROUTE(false),
    STATIC_END(false),

    // States below are not restored from the configuration files
    SESSION_START(false),
    GET_CONFIGURATION(false),
    DYNAMIC_VALUES(false),
    DYNAMIC_END(false),

    // States below are performed during initialisation, but also during heal
    HEAL_START(false),
    DELETE_ROUTES(false),
    RETURN_ROUTES(false),
    NEIGHBORS(false),

    DONE(false);

    private boolean mandatory;

    private ZWaveNodeInitStage(boolean manditory) {
        this.mandatory = manditory;
    }

    /**
     * Return the next stage after the current stage
     *
     * @return the next stage
     */
    public ZWaveNodeInitStage getNextStage() {
        for (ZWaveNodeInitStage stage : values()) {
            if (stage.ordinal() == this.ordinal() + 1) {
                return stage;
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
        if (ordinal() > SESSION_START.ordinal()) {
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
