/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.junit.Test;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConfiguration;
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
    public void testTokenDecoding() {
        String headerValue = "https://www.bmw-connecteddrive.com/app/static/external-dispatch.html#access_token=SfXKgkEXeeFJkVqdD4XMmfUU224MRuyh&token_type=Bearer&expires_in=7199";
        HttpClientFactory hcf = mock(HttpClientFactory.class);
        when(hcf.getCommonHttpClient()).thenReturn(mock(HttpClient.class));
        ConnectedDriveProxy dcp = new ConnectedDriveProxy(hcf, mock(ConnectedDriveConfiguration.class));
        dcp.tokenFromUrl(headerValue);
        Token t = dcp.getToken();
        assertEquals("Token", "Bearer SfXKgkEXeeFJkVqdD4XMmfUU224MRuyh", t.getBearerToken());
    }
}
