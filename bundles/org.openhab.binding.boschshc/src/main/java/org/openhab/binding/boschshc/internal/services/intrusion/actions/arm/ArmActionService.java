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
package org.openhab.binding.boschshc.internal.services.intrusion.actions.arm;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.services.AbstractStatelessBoschSHCServiceWithRequestBody;
import org.openhab.binding.boschshc.internal.services.intrusion.actions.arm.dto.ArmActionRequest;

/**
 * Service to arm the intrusion detection system using a specified profile.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class ArmActionService extends AbstractStatelessBoschSHCServiceWithRequestBody<ArmActionRequest> {

    public ArmActionService() {
        super(BoschSHCBindingConstants.SERVICE_INTRUSION_DETECTION, "intrusion/actions/arm");
    }
}
