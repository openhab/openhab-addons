/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.utils.BimmerConstants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.HTTPConstants;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AuthTest} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AuthTest {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    @Test
    public void testAuthServerMap() {
        Map<String, String> authServers = BimmerConstants.AUTH_SERVER_MAP;
        assertEquals(3, authServers.size(), "Number of Servers");
        Map<String, String> api = BimmerConstants.SERVER_MAP;
        assertEquals(3, api.size(), "Number of Servers");
    }

    @Test
    public void testTokenDecoding() {
        String headerValue = "https://www.bmw-connecteddrive.com/app/static/external-dispatch.html#access_token=SfXKgkEXeeFJkVqdD4XMmfUU224MRuyh&token_type=Bearer&expires_in=7199";
        HttpClientFactory hcf = mock(HttpClientFactory.class);
        when(hcf.getCommonHttpClient()).thenReturn(mock(HttpClient.class));
        when(hcf.createHttpClient(HTTPConstants.AUTH_HTTP_CLIENT_NAME)).thenReturn(mock(HttpClient.class));
        ConnectedDriveConfiguration config = new ConnectedDriveConfiguration();
        config.region = BimmerConstants.REGION_ROW;
        ConnectedDriveProxy dcp = new ConnectedDriveProxy(hcf, config);
        dcp.tokenFromUrl(headerValue);
        Token t = dcp.getToken();
        assertEquals("Bearer SfXKgkEXeeFJkVqdD4XMmfUU224MRuyh", t.getBearerToken(), "Token");
    }

    public void testRealTokenUpdate() {
        ConnectedDriveConfiguration config = new ConnectedDriveConfiguration();
        config.region = BimmerConstants.REGION_ROW;
        config.userName = "bla";
        config.password = "blub";
        HttpClientFactory hcf = mock(HttpClientFactory.class);
        when(hcf.getCommonHttpClient()).thenReturn(mock(HttpClient.class));
        when(hcf.createHttpClient(HTTPConstants.AUTH_HTTP_CLIENT_NAME)).thenReturn(mock(HttpClient.class));
        ConnectedDriveProxy dcp = new ConnectedDriveProxy(hcf, config);
        Token t = dcp.getToken();
        logger.info("Token {}", t.getBearerToken());
        logger.info("Expires {}", t.isExpired());
    }
}
