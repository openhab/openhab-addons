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
package org.openhab.binding.boschshc.internal.services.smokedetectorcheck.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.binding.boschshc.internal.services.smokedetectorcheck.SmokeDetectorCheckState;

/**
 * State for {@link org.openhab.binding.boschshc.internal.services.smokedetectorcheck.SmokeDetectorCheckService}
 * to get the current smoke test state and request a new smoke test.
 *
 * @author Christian Oeing - Initial contribution
 */
public class SmokeDetectorCheckServiceState extends BoschSHCServiceState {

    public SmokeDetectorCheckServiceState() {
        super("smokeDetectorCheckState");
    }

    /**
     * Current state.
     */
    public SmokeDetectorCheckState value;
}
