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
package org.openhab.binding.boschshc.internal.services.smokedetectorcheck;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Possible states for a smoke detector.
 *
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public enum SmokeDetectorCheckState {
    NONE,
    SMOKE_TEST_REQUESTED,
    SMOKE_TEST_OK,
    SMOKE_TEST_FAILED;

    public static SmokeDetectorCheckState from(String stateString) {
        try {
            return SmokeDetectorCheckState.valueOf(stateString);
        } catch (Exception a) {
            return NONE;
        }
    }
}
