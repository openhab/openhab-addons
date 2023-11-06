/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.intrusion.actions.arm.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * DTO for arming the intrusion detection system using a specified profile.
 * 
 * @author David Pace - Initial contribution
 *
 */
public class ArmActionRequest extends BoschSHCServiceState {

    public ArmActionRequest() {
        super("armRequest");
    }

    /**
     * The ID of the profile to be used for arming the system.
     */
    public String profileId;
}
