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
package org.openhab.binding.boschshc.internal.services.bypass.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.UnDefType;

/**
 * Unit tests for {@link BypassState}.
 * 
 * @author David Pace - Initial contribution
 *
 */
class BypassStateTest {

    @Test
    void testToOnOffTypeOrUndef() {
        assertEquals(OnOffType.ON, BypassState.BYPASS_ACTIVE.toOnOffTypeOrUndef());
        assertEquals(OnOffType.OFF, BypassState.BYPASS_INACTIVE.toOnOffTypeOrUndef());
        assertEquals(UnDefType.UNDEF, BypassState.UNKNOWN.toOnOffTypeOrUndef());
    }
}
