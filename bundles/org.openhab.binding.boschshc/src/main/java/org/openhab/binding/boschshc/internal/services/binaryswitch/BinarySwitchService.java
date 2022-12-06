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
package org.openhab.binding.boschshc.internal.services.binaryswitch;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.binaryswitch.dto.BinarySwitchServiceState;

/**
 * Service for devices and services that can be turned on and off.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class BinarySwitchService extends BoschSHCService<BinarySwitchServiceState> {

    public BinarySwitchService() {
        super("BinarySwitch", BinarySwitchServiceState.class);
    }
}
