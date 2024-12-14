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
package org.openhab.binding.metofficedatahub.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The {@link SiteApiAuthenticationTest} class implements unit test case for {@link SiteApiAuthenticationTest}
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class SiteApiAuthenticationTest {

    private static Request dummyRequest = new HttpClient().newRequest("http://127.0.0.1:9999");;

    @Test
    public void testInitialNonAuthenticatedState() {
        SiteApiAuthentication saa = new SiteApiAuthentication();
        assertFalse(saa.getIsAuthenticated());
    }

    @Test
    public void testAuthenticatedProcessing() {
        SiteApiAuthentication saa = new SiteApiAuthentication();
        Result result = new Result(dummyRequest,getResultWithStatus(200));
        saa.processResult(result);
        assertTrue(saa.getIsAuthenticated());
    }

    @Test
    public void testUnauthenticatedProcessing() {
        SiteApiAuthentication saa = new SiteApiAuthentication();
        Result result = new Result(dummyRequest,getResultWithStatus(403));
        saa.processResult(result);
        assertFalse(saa.getIsAuthenticated());
    }

    @Test
    public void testIsAuthenticatedUpdates() {
        SiteApiAuthentication saa = new SiteApiAuthentication();
        Result goodResult = new Result(dummyRequest,getResultWithStatus(200));
        Result badresult = new Result(dummyRequest,getResultWithStatus(403));
        saa.processResult(goodResult);
        assertTrue(saa.getIsAuthenticated());
        saa.processResult(badresult);
        assertFalse(saa.getIsAuthenticated());
        saa.processResult(goodResult);
        assertTrue(saa.getIsAuthenticated());
    }

    @Test
    public void testBadJwtDetected() {
        SiteApiAuthentication saa = new SiteApiAuthentication();
        assertThrowsExactly(AuthTokenException.class, () -> { saa.setApiKey("");});
        assertThrowsExactly(AuthTokenException.class, () -> { saa.setApiKey("someInvalidToken.part");});
        assertThrowsExactly(AuthTokenException.class, () -> { saa.setApiKey("\"someInvalidToken.part.new");});
    }

    @Test
    public void testGoodJwtDetected() {
        SiteApiAuthentication saa = new SiteApiAuthentication();
        assertDoesNotThrow(() -> { saa.setApiKey("eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiQWRtaW4iLCJJc3N1ZXIiOiJJc3N1ZXIiLCJVc2VybmFtZSI6IkphdmFJblVzZSIsImV4cCI6MTczMDg1Mzg2NiwiaWF0IjoxNzMwODUzODY2fQ.Wmfe4npC037y0uoW4dnhizSXOPqFSn3OI3XbeklVQkA");});
    }

    private Response getResultWithStatus(final int status) {
        return new HttpResponse(dummyRequest, List.of()).status(status);
    }

}
