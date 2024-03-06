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
package org.openhab.binding.boschshc.internal.services.privacymode;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.OnOffType;

/**
 * Unit tests for {@link PrivacyModeState}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class PrivacyModeStateTest {

    @Test
    void testFromOnOffType() {
        assertSame(PrivacyModeState.ENABLED, PrivacyModeState.from(OnOffType.ON));
        assertSame(PrivacyModeState.DISABLED, PrivacyModeState.from(OnOffType.OFF));
    }

    @Test
    void testToOnOffType() {
        assertSame(OnOffType.ON, PrivacyModeState.ENABLED.toOnOffType());
        assertSame(OnOffType.OFF, PrivacyModeState.DISABLED.toOnOffType());
    }
}
