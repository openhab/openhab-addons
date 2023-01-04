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
package org.openhab.binding.plugwiseha.internal.api.model.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author B. van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 */
@XStreamAlias("thermostat_functionality")
public class ActuatorFunctionalityThermostat extends ActuatorFunctionality {

    @SuppressWarnings("unused")
    private Double setpoint;

    @SuppressWarnings("unused")
    @XStreamAlias("preheating_allowed")
    private Boolean preheatingAllowed;

    @SuppressWarnings("unused")
    @XStreamAlias("cooling_allowed")
    private Boolean coolingAllowed;

    @SuppressWarnings("unused")
    @XStreamAlias("regulation_control")
    private String regulationControl;

    public ActuatorFunctionalityThermostat(Double temperature) {
        this.setpoint = temperature;
    }

    public ActuatorFunctionalityThermostat(Boolean preheatingAllowed, Boolean coolingAllowed,
            String regulationControl) {
        this.preheatingAllowed = preheatingAllowed;
        this.coolingAllowed = coolingAllowed;
        this.regulationControl = regulationControl;
    }
}
