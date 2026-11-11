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
package org.openhab.binding.lgthinq.lgservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lgthinq.lgservices.api.RestResult;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;

/**
 * Tests ThinQ API V2 air conditioner control commands.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
class LGThinQACApiV2ClientServiceTest {

    @Test
    void sendsAutoDryAsSetCommand() throws LGThinqApiException {
        TestService service = new TestService();

        service.turnAutoDryMode("bridge", "device", "1");

        assertEquals("bridge", service.bridgeName);
        assertEquals("device", service.deviceId);
        assertEquals("Set", service.command);
        assertEquals("airState.miscFuncState.autoDry", service.keyName);
        assertEquals(1, service.value);
    }

    @Test
    void keepsOtherGenericModesAsOperationCommands() throws LGThinqApiException {
        TestService service = new TestService();

        service.turnCoolJetMode("bridge", "device", "0");

        assertEquals("Operation", service.command);
        assertEquals("airState.wMode.jet", service.keyName);
        assertEquals(0, service.value);
    }

    private static class TestService extends LGThinQACApiV2ClientServiceImpl {
        private String bridgeName = "";
        private String deviceId = "";
        private String command = "";
        private String keyName = "";
        private int value;

        TestService() {
            super(mock(HttpClient.class));
        }

        @Override
        protected RestResult sendBasicControlCommands(String bridgeName, String deviceId, String command,
                String keyName, int value) {
            this.bridgeName = bridgeName;
            this.deviceId = deviceId;
            this.command = command;
            this.keyName = keyName;
            this.value = value;
            return new RestResult("{\"resultCode\":\"0000\"}", 200);
        }
    }
}
