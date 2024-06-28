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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.handler.AccountHandlerMock;
import org.openhab.binding.mercedesme.internal.handler.ThingCallbackListener;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.BridgeImpl;

/**
 * {@link StatusTests} sequencess for testing ThingStatus
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class StatusTests {

    public static void tearDown(AccountHandlerMock ahm) {
        // ahm.setCallback(null);
        ahm.dispose();
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    void testInvalidConfig() {
        BridgeImpl bi = new BridgeImpl(new ThingTypeUID("test", "account"), "MB");
        Map<String, Object> config = new HashMap<>();
        config.put("callbackIP", "999.999.999.999");
        config.put("callbackPort", "99999");
        bi.setConfiguration(new Configuration(config));
        AccountHandlerMock ahm = new AccountHandlerMock(bi, null);
        ThingCallbackListener tcl = new ThingCallbackListener();
        ahm.setCallback(tcl);
        ahm.initialize();
        ThingStatusInfo tsi = tcl.getThingStatus();
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "EMail offline");
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, tsi.getStatusDetail(), "EMail config");
        assertEquals("@text/mercedesme.account.status.email-missing", tsi.getDescription(), "EMail text");
        tearDown(ahm);

        config.put("email", "a@b.c");
        bi.setConfiguration(new Configuration(config));
        tcl = new ThingCallbackListener();
        ahm.setCallback(tcl);
        ahm.initialize();
        tsi = tcl.getThingStatus();
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "Region offline");
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, tsi.getStatusDetail(), "Region config");
        assertEquals("@text/mercedesme.account.status.region-missing", tsi.getDescription(), "Region text");
        tearDown(ahm);

        config.put("region", "row");
        bi.setConfiguration(new Configuration(config));
        tcl = new ThingCallbackListener();
        ahm.setCallback(tcl);
        ahm.initialize();
        tsi = tcl.getThingStatus();
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "Auth offline");
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, tsi.getStatusDetail(), "Auth detail");
        tearDown(ahm);

        config.put("refreshInterval", 0);
        bi.setConfiguration(new Configuration(config));
        tcl = new ThingCallbackListener();
        ahm.setCallback(tcl);
        ahm.initialize();
        tsi = tcl.getThingStatus();
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "Refresh offline");
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, tsi.getStatusDetail(), "Refresh config");
        assertEquals("@text/mercedesme.account.status.refresh-invalid", tsi.getDescription(), "Refresh text");
        tearDown(ahm);
    }

    @Test
    void testNoTokenStored() {
        BridgeImpl bi = new BridgeImpl(new ThingTypeUID("test", "account"), "MB");
        Map<String, Object> config = new HashMap<>();
        config.put("refreshInterval", Integer.MAX_VALUE);
        config.put("region", "row");
        config.put("email", "a@b.c");
        config.put("callbackIP", "999.999.999.999");
        config.put("callbackPort", "99999");
        bi.setConfiguration(new Configuration(config));
        AccountHandlerMock ahm = new AccountHandlerMock(bi, null);
        ThingCallbackListener tcl = new ThingCallbackListener();
        ahm.setCallback(tcl);
        ahm.initialize();
        ThingStatusInfo tsi = tcl.getThingStatus();
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "Auth Offline");
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, tsi.getStatusDetail(), "Auth details");
        String statusDescription = tsi.getDescription();
        assertNotNull(statusDescription);
        assertTrue(statusDescription.contains("@text/mercedesme.account.status.authorization-needed"), "Auth text");
        tearDown(ahm);

        AccessTokenResponse token = new AccessTokenResponse();
        token.setExpiresIn(3000);
        token.setAccessToken(Constants.JUNIT_TOKEN);
        token.setRefreshToken(Constants.JUNIT_REFRESH_TOKEN);
        ahm.onAccessTokenResponse(token);
        ahm.connect();
        tsi = tcl.getThingStatus();
        assertEquals(ThingStatus.ONLINE, tsi.getStatus(), "Auth Online");
        tearDown(ahm);
    }

    @Test
    void testTokenStored() {
        BridgeImpl bi = new BridgeImpl(new ThingTypeUID("test", "account"), "MB");
        Map<String, Object> config = new HashMap<>();
        config.put("refreshInterval", Integer.MAX_VALUE);
        config.put("region", "row");
        config.put("email", "a@b.c");
        config.put("callbackIP", "999.999.999.999");
        config.put("callbackPort", "99999");
        bi.setConfiguration(new Configuration(config));
        AccessTokenResponse token = new AccessTokenResponse();
        token.setExpiresIn(3000);
        token.setAccessToken(Constants.JUNIT_TOKEN);
        token.setRefreshToken(Constants.JUNIT_REFRESH_TOKEN);
        token.setCreatedOn(Instant.now());
        token.setTokenType("Bearer");
        token.setScope(Constants.SCOPE);
        AccountHandlerMock ahm = new AccountHandlerMock(bi, Utils.toString(token));
        ThingCallbackListener tcl = new ThingCallbackListener();
        ahm.setCallback(tcl);
        ahm.initialize();
        ThingStatusInfo tsi = tcl.getThingStatus();
        assertEquals(ThingStatus.UNKNOWN, tsi.getStatus(),
                "Socket Unknown " + tsi.getStatusDetail() + " " + tsi.getDescription());
        ahm.connect();
        tsi = tcl.getThingStatus();
        assertEquals(ThingStatus.ONLINE, tsi.getStatus(), "Socket Online");
        tearDown(ahm);
    }
}
