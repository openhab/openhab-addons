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
package org.openhab.binding.boschshc.internal.services.intrusion;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.services.BoschSHCSystemService;
import org.openhab.binding.boschshc.internal.services.intrusion.dto.IntrusionDetectionSystemState;

/**
 * Allows to retrieve the system state of the intrusion detection system.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class IntrusionDetectionSystemStateService extends BoschSHCSystemService<IntrusionDetectionSystemState> {

    public IntrusionDetectionSystemStateService() {
        super(BoschSHCBindingConstants.SERVICE_INTRUSION_DETECTION, IntrusionDetectionSystemState.class,
                "intrusion/states/system");
    }
}
