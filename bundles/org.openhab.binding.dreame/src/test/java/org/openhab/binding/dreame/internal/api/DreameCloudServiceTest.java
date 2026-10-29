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
package org.openhab.binding.dreame.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests cloud-specific protocol settings.
 *
 * @author Ronny Grun - Initial contribution
 */
class DreameCloudServiceTest {
    @Test
    void resolvesConfiguredServicesCaseInsensitively() throws DreameCloudException {
        assertEquals(DreameCloudService.DREAMEHOME, DreameCloudService.fromConfiguration(" dreamehome "));
        assertEquals(DreameCloudService.MOVAHOME, DreameCloudService.fromConfiguration("MOVAhome"));
        assertThrows(DreameCloudException.class, () -> DreameCloudService.fromConfiguration("unknown"));
    }

    @Test
    void providesMovaCloudProtocolSettings() {
        DreameCloudService mova = DreameCloudService.MOVAHOME;

        assertEquals("https://eu.iot.mova-tech.com:13267", mova.apiUrl("eu"));
        assertEquals("000002", mova.tenantId());
        assertEquals("Mova_Smarthome/1.5.59 (iPhone; iOS 16.0; Scale/3.00)", mova.userAgent());
        assertEquals(DreameCloudService.DREAMEHOME.authorization(), mova.authorization());
    }
}
