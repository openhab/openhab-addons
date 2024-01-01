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
package org.openhab.binding.boschshc.internal.services.multilevelswitch;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.multilevelswitch.dto.MultiLevelSwitchServiceState;

/**
 * Service for devices with switches that can have multiple different levels.
 * <p>
 * Example: light bulbs with controllable brightness levels from 0 to 100%.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class MultiLevelSwitchService extends BoschSHCService<MultiLevelSwitchServiceState> {

    public MultiLevelSwitchService() {
        super("MultiLevelSwitch", MultiLevelSwitchServiceState.class);
    }
}
