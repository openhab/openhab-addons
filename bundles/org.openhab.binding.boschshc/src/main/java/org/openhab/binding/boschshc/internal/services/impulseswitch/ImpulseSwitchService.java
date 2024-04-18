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
package org.openhab.binding.boschshc.internal.services.impulseswitch;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.impulseswitch.dto.ImpulseSwitchServiceState;

/**
 * Service to send impulses to relays.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class ImpulseSwitchService extends BoschSHCService<ImpulseSwitchServiceState> {

    public static final String IMPULSE_SWITCH_SERVICE_NAME = "ImpulseSwitch";

    public ImpulseSwitchService() {
        super(IMPULSE_SWITCH_SERVICE_NAME, ImpulseSwitchServiceState.class);
    }
}
