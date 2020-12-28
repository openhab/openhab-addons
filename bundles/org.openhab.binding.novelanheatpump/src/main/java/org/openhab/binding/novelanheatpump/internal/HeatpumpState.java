/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.novelanheatpump.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum HeatpumpState {
    STATE_ERROR("ERROR"),
    STATE_HEATING("HEATING"),
    STATE_STANDBY("STANDBY"),
    STATE_SWITCH_ON_DELAY("SWITCH_ON_DELAY"),
    STATE_SWITCHING_CYCLE_BLOCKING("SWITCHING_CYCLE_BLOCKING"),
    STATE_PROVIDER_LOCK_TIME("PROVIDER_LOCK_TIME"),
    STATE_SERVICE_WATER("SERVICE_WATER"),
    STATE_SCREED_HEAT_UP("SCREED_HEAT_UP"),
    STATE_DEFROSTING("DEFROSTING"),
    STATE_PUMP_FLOW("PUMP_FLOW"),
    STATE_DISINFECTION("DISINFECTION"),
    STATE_COOLING("COOLING"),
    STATE_POOL_WATER("POOL_WATER"),
    STATE_HEATING_EXT("HEATING_EXT"),
    STATE_SERVICE_WATER_EXT("SERVICE_WATER_EXT"),
    STATE_FLOW_MONITORING("FLOW_MONITORING"),
    STATE_ZWE_OPERATION("ZWE_OPERATION"),
    STATE_COMPRESSOR_HEATING("COMPRESSOR_HEATING"),
    STATE_SERVICE_WATER_ADDITIONAL_HEATING("SERVICE_WATER_ADDITIONAL_HEATING"),
    STATE_RUNNING("RUNNING"),
    STATE_STOPPED("STOPPED"),
    STATE_APPEAR("APPEAR"),
    STATE_UNKNOWN("UNKNOWN");

    private final String name;
    private static final Logger logger = LoggerFactory.getLogger(HeatpumpState.class);

    private HeatpumpState(String name) {
        this.name = name;
    }

    public static final HeatpumpState getStateByNumber(Integer number) {
        switch (number) {
            case -1:
                return STATE_ERROR;
            case 0:
                return STATE_RUNNING;
            case 1:
                return STATE_STOPPED;
            case 2:
                return STATE_APPEAR;
            case 4:
                return STATE_ERROR;
            case 5:
                return STATE_DEFROSTING;
            case 7:
                return STATE_COMPRESSOR_HEATING;
            case 8:
                return STATE_PUMP_FLOW;
            default:
                logger.info(
                        "found new value for reverse engineering !!!! No idea what the heatpump will do in state {}.", //$NON-NLS-1$
                        number);
                return STATE_UNKNOWN;
        }
    }

    public static final HeatpumpState getExtendedStateByNumber(Integer number) {
        switch (number) {
            case -1:
                return STATE_ERROR;
            case 0:
                return STATE_HEATING;
            case 1:
                return STATE_STANDBY;
            case 2:
                return STATE_SWITCH_ON_DELAY;
            case 3:
                return STATE_SWITCHING_CYCLE_BLOCKING;
            case 4:
                return STATE_PROVIDER_LOCK_TIME;
            case 5:
                return STATE_SERVICE_WATER;
            case 6:
                return STATE_SCREED_HEAT_UP;
            case 7:
                return STATE_DEFROSTING;
            case 8:
                return STATE_PUMP_FLOW;
            case 9:
                return STATE_DISINFECTION;
            case 10:
                return STATE_COOLING;
            case 12:
                return STATE_POOL_WATER;
            case 13:
                return STATE_HEATING_EXT;
            case 14:
                return STATE_SERVICE_WATER_EXT;
            case 16:
                return STATE_FLOW_MONITORING;
            case 17:
                return STATE_ZWE_OPERATION;
            case 18:
                return STATE_COMPRESSOR_HEATING;
            case 19:
                return STATE_SERVICE_WATER_ADDITIONAL_HEATING;
            default:
                logger.info(
                        "found new value for reverse engineering !!!! No idea what the heatpump will do in state {}.", //$NON-NLS-1$
                        number);
                return STATE_UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
