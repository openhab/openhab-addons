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
package org.openhab.binding.vesync.internal.handler.requests;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncAuthTokenRequest;

/**
 * The {@link VeSyncAuthTokenRequestTest} class implements unit test case for {@link VeSyncAuthTokenRequest}
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VeSyncAuthTokenRequestTest {

    @Test
    public void testConstructor() {
        // Test with valid parameters
        String email = "test@example.com";
        String password = "testpassword";
        String accountId = "123456789";
        
        VeSyncAuthTokenRequest request = new VeSyncAuthTokenRequest(email, password, accountId);
        
        // Verify that the constructor properly initializes fields
        assertEquals(email, request.email);
        assertEquals(password, request.password);
        assertEquals(accountId, request.accountId);
        assertEquals("authByPWDOrOTM", request.method);
        assertEquals("generic", request.authProtocolType);
        assertEquals("OpenHAB", request.clientInfo);
        assertEquals("vesyncApp", request.clientType);
        assertEquals("VeSync 5.6.60", request.clientVersion);
        assertFalse(request.debugMode);
        assertFalse(request.emailSubscriptions);
        assertEquals("Android", request.osInfo);
        assertEquals("287f129ed1ca25cc0888348c0104744b9", request.terminalId);
        assertEquals("America/New_York", request.timeZone);
        assertEquals("", request.token);
        assertEquals("US", request.userCountryCode);
        assertEquals("eldodkfj", request.appId);
        assertEquals("eldodkfj", request.sourceAppID);
        
        // Verify that traceId is set (it should be a timestamp)
        assertNotNull(request.traceId);
        assertTrue(request.traceId.matches("\\d+"));
    }
    
    @Test
    public void testInheritance() {
        // Verify that VeSyncAuthTokenRequest extends VeSyncBaseRequest
        VeSyncAuthTokenRequest request = new VeSyncAuthTokenRequest("test@example.com", "password", "123");
        
        // Check that it has inherited fields from VeSyncBaseRequest
        assertNotNull(request.traceId);
        assertNotNull(request.method);
        assertNotNull(request.acceptLanguage);
    }
    
    @Test
    public void testDefaultValues() {
        // Test that default values are set correctly
        VeSyncAuthTokenRequest request = new VeSyncAuthTokenRequest("test@example.com", "password", "123");
        
        // Verify default values for boolean fields
        assertFalse(request.debugMode);
        assertFalse(request.emailSubscriptions);
        
        // Verify default values for string fields
        assertEquals("generic", request.authProtocolType);
        assertEquals("OpenHAB", request.clientInfo);
        assertEquals("vesyncApp", request.clientType);
        assertEquals("VeSync 5.6.60", request.clientVersion);
        assertEquals("Android", request.osInfo);
        assertEquals("America/New_York", request.timeZone);
        assertEquals("", request.token);
        assertEquals("US", request.userCountryCode);
        assertEquals("eldodkfj", request.appId);
        assertEquals("eldodkfj", request.sourceAppID);
        assertEquals("287f129ed1ca25cc0888348c0104744b9", request.terminalId);
    }
}