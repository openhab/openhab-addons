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
package org.openhab.binding.boschshc.internal.services.intrusion.actions.disarm;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.services.AbstractStatelessBoschSHCService;

/**
 * Service for disarming the intrusion detection system.
 * <p>
 * This service does not require a DTO because it uses a simple HTTP POST request without a request body.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class DisarmActionService extends AbstractStatelessBoschSHCService {

    public DisarmActionService() {
        super(BoschSHCBindingConstants.SERVICE_INTRUSION_DETECTION, "intrusion/actions/disarm");
    }
}
