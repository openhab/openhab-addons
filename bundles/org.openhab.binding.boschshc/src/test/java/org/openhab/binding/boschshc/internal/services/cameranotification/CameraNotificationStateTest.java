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
package org.openhab.binding.boschshc.internal.services.cameranotification;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.OnOffType;

/**
 * Unit tests for {@link CameraNotificationState}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class CameraNotificationStateTest {

    @Test
    void testFromOnOffType() {
        assertSame(CameraNotificationState.ENABLED, CameraNotificationState.from(OnOffType.ON));
        assertSame(CameraNotificationState.DISABLED, CameraNotificationState.from(OnOffType.OFF));
    }

    @Test
    void testToOnOffType() {
        assertSame(OnOffType.ON, CameraNotificationState.ENABLED.toOnOffType());
        assertSame(OnOffType.OFF, CameraNotificationState.DISABLED.toOnOffType());
    }
}
