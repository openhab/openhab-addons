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
package org.openhab.binding.boschshc.internal.services.multilevelswitch.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.core.library.types.PercentType;

/**
 * State for devices with switches that can have multiple different levels.
 *
 * @author David Pace - Initial contribution
 *
 */
public class MultiLevelSwitchServiceState extends BoschSHCServiceState {

    public MultiLevelSwitchServiceState() {
        super("multiLevelSwitchState");
    }

    /**
     * Represents a percentage level between 0 and 100
     */
    public int level;

    public PercentType toPercentType() {
        return new PercentType(level);
    }
}
