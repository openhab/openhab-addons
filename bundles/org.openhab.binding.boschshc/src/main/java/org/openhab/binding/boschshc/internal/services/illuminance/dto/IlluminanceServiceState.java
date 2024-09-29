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
package org.openhab.binding.boschshc.internal.services.illuminance.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * Illuminance state of the motion detector sensor.
 * <p>
 * Example JSON:
 * 
 * <pre>
 * {
 *   "@type": "illuminanceLevelState",
 *   "illuminance": 32
 * }
 * </pre>
 * 
 * @author David Pace - Initial contribution
 *
 */
public class IlluminanceServiceState extends BoschSHCServiceState {

    public IlluminanceServiceState() {
        super("illuminanceLevelState");
    }

    public int illuminance;
}
