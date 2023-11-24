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
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.BridgeImpl;

/**
 * {@link StatusTests} sequencess for testing ThingStatus
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class StatusTests {

    @Test
    void testInvalidConfig() {
        BridgeImpl bi = new BridgeImpl(new ThingTypeUID("test", "account"), "MB");
        Map<String, Object> config = new HashMap<String, Object>();
        bi.setConfiguration(new Configuration(config));
        AccountHandlerMock ahm = new AccountHandlerMock(bi, null);
        ThingCallbackListener tcl = new ThingCallbackListener();
        ahm.setCallback(tcl);
        ahm.initialize();
        assertEquals(ThingStatus.OFFLINE, tcl.status.getStatus(), "EMail offline");
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, tcl.status.getStatusDetail(), "EMail config");
        assertEquals("@text/mercedesme.account.status.email-missing", tcl.status.getDescription(), "EMail text");
        config.put("email", "a@b.c");
        bi.setConfiguration(new Configuration(config));
        ahm.initialize();
        assertEquals(ThingStatus.OFFLINE, tcl.status.getStatus(), "Region offline");
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, tcl.status.getStatusDetail(), "Region config");
        assertEquals("@text/mercedesme.account.status.region-missing", tcl.status.getDescription(), "Region text");
        config.put("region", "row");
        bi.setConfiguration(new Configuration(config));
        ahm.initialize();
        assertEquals(ThingStatus.OFFLINE, tcl.status.getStatus(), "Auth offline");
        assertEquals(ThingStatusDetail.NONE, tcl.status.getStatusDetail(), "Auth detail");
        config.put("refreshInterval", 0);
        bi.setConfiguration(new Configuration(config));
        ahm.initialize();
        assertEquals(ThingStatus.OFFLINE, tcl.status.getStatus(), "Refresh offline");
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, tcl.status.getStatusDetail(), "Refresh config");
        assertEquals("@text/mercedesme.account.status.refresh-invalid", tcl.status.getDescription(), "Refresh text");
    }

    @Test
    void testNoTokenStored() {
        BridgeImpl bi = new BridgeImpl(new ThingTypeUID("test", "account"), "MB");
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("refreshInterval", Integer.MAX_VALUE);
        config.put("region", "row");
        config.put("email", "a@b.c");
        bi.setConfiguration(new Configuration(config));
        AccountHandlerMock ahm = new AccountHandlerMock(bi, null);
        ThingCallbackListener tcl = new ThingCallbackListener();
        ahm.setCallback(tcl);
        ahm.initialize();
        assertEquals(ThingStatus.OFFLINE, tcl.status.getStatus(), "Auth Offline");
        assertEquals(ThingStatusDetail.NONE, tcl.status.getStatusDetail(), "Auth details");
        assertTrue(tcl.status.getDescription().contains("@text/mercedesme.account.status.authorization-needed"),
                "Auth text");
        AccessTokenResponse token = new AccessTokenResponse();
        token.setExpiresIn(3000);
        token.setAccessToken("testToken");
        token.setRefreshToken("testRefreshToken");
        ahm.onAccessTokenResponse(token);
        ahm.connect();
        assertEquals(ThingStatus.ONLINE, tcl.status.getStatus(), "Auth Online");
    }

    @Test
    void testTokenStored() {
        BridgeImpl bi = new BridgeImpl(new ThingTypeUID("test", "account"), "MB");
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("refreshInterval", Integer.MAX_VALUE);
        config.put("region", "row");
        config.put("email", "a@b.c");
        bi.setConfiguration(new Configuration(config));
        AccessTokenResponse token = new AccessTokenResponse();
        token.setExpiresIn(3000);
        token.setAccessToken("testToken");
        token.setRefreshToken("testRefreshToken");
        token.setCreatedOn(Instant.now());
        token.setTokenType("Bearer");
        token.setScope(Constants.SCOPE);
        AccountHandlerMock ahm = new AccountHandlerMock(bi, Utils.toString(token));
        ThingCallbackListener tcl = new ThingCallbackListener();
        ahm.setCallback(tcl);
        ahm.initialize();
        assertEquals(ThingStatus.UNKNOWN, tcl.status.getStatus(), "Auth Offline");
        ahm.onAccessTokenResponse(token);
        ahm.connect();
        assertEquals(ThingStatus.ONLINE, tcl.status.getStatus(), "Auth Online");
    }
}
