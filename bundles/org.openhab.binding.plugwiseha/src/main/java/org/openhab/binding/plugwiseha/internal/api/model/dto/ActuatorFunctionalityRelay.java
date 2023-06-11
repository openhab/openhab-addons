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
 */
@XStreamAlias("relay_functionality")
public class ActuatorFunctionalityRelay extends ActuatorFunctionality {

    @SuppressWarnings("unused")
    private String state;
    @SuppressWarnings("unused")
    private Boolean lock;

    public ActuatorFunctionalityRelay(String state) {
        this.state = state;
    }

    public ActuatorFunctionalityRelay(String state, Boolean lock) {
        this.state = state;
        this.lock = lock;
    }
}
