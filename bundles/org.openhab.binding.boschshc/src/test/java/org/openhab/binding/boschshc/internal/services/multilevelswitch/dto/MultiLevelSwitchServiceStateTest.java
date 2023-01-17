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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.PercentType;

/**
 * Unit tests for {@link MultiLevelSwitchServiceState}.
 *
 * @author David Pace - Initial contribution
 *
 */
public class MultiLevelSwitchServiceStateTest {

    @Test
    void testToPercentType() {
        MultiLevelSwitchServiceState multiLevelSwitchState = new MultiLevelSwitchServiceState();
        multiLevelSwitchState.level = 42;
        assertEquals(new PercentType(42), multiLevelSwitchState.toPercentType());
    }
}
