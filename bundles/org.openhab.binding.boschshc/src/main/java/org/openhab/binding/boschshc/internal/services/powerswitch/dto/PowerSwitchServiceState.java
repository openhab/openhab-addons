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
package org.openhab.binding.boschshc.internal.services.powerswitch.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchState;

/**
 * Represents the state of a power switch device as reported from the Smart Home Controller
 *
 * @author Stefan Kästle - Initial contribution
 * @author Christian Oeing - Adjustments to match general service state structure
 */
public class PowerSwitchServiceState extends BoschSHCServiceState {

    public PowerSwitchServiceState() {
        super("powerSwitchState");
    }

    /**
     * Current state of power switch.
     */
    public PowerSwitchState switchState;
}
