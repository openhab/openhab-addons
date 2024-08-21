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
package org.openhab.binding.boschshc.internal.services.powerswitch;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.powerswitch.dto.PowerSwitchServiceState;

/**
 * Service to get and set the state of a power switch.
 * 
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class PowerSwitchService extends BoschSHCService<PowerSwitchServiceState> {

    public static final String POWER_SWITCH_SERVICE_NAME = "PowerSwitch";

    public PowerSwitchService() {
        super(POWER_SWITCH_SERVICE_NAME, PowerSwitchServiceState.class);
    }
}
