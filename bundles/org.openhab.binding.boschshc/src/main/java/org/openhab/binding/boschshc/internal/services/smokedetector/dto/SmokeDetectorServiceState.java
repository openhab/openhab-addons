/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.smokedetector.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.binding.boschshc.internal.services.smokedetector.SmokeDetectorCheckState;

/**
 * State for {@link SmokeDetectorService} to get the current smoke test state and request a new smoke test.
 *
 * @author Christian Oeing - Initial contribution
 */
public class SmokeDetectorServiceState extends BoschSHCServiceState {

    public SmokeDetectorServiceState() {
        super("smokeDetectorCheckState");
    }

    /**
     * Current state.
     */
    public SmokeDetectorCheckState value;
}
