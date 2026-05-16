/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.roborock.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.binding.roborock.internal.RoborockBindingConstants.COMMAND_APP_START;
import static org.openhab.binding.roborock.internal.RoborockBindingConstants.COMMAND_GET_MAP;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

@NonNullByDefault({})
class RoborockTransportRoutingTest {

    @Test
    void cloudOnlyMethodIsRecognized() {
        assertTrue(RoborockTransportRouting.isCloudOnlyMethod(COMMAND_GET_MAP));
    }

    @Test
    void directModeRoutesMapToCloud() {
        assertEquals(RoborockCommunicationMode.CLOUD,
                RoborockTransportRouting.selectTransportMode(RoborockCommunicationMode.DIRECT, COMMAND_GET_MAP));
    }

    @Test
    void directModeKeepsDirectForRegularCommands() {
        assertEquals(RoborockCommunicationMode.DIRECT,
                RoborockTransportRouting.selectTransportMode(RoborockCommunicationMode.DIRECT, COMMAND_APP_START));
    }
}
