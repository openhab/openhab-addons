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

import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SmokeDetectorCheckState}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class SmokeDetectorCheckStateTest {

    @Test
    void testFrom() {
        assertSame(SmokeDetectorCheckState.SMOKE_TEST_OK, SmokeDetectorCheckState.from("SMOKE_TEST_OK"));
        assertSame(SmokeDetectorCheckState.SMOKE_TEST_REQUESTED, SmokeDetectorCheckState.from("SMOKE_TEST_REQUESTED"));
        assertSame(SmokeDetectorCheckState.SMOKE_TEST_FAILED, SmokeDetectorCheckState.from("SMOKE_TEST_FAILED"));
        assertSame(SmokeDetectorCheckState.NONE, SmokeDetectorCheckState.from("NONE"));
        assertSame(SmokeDetectorCheckState.NONE, SmokeDetectorCheckState.from("invalid string"));
    }
}
