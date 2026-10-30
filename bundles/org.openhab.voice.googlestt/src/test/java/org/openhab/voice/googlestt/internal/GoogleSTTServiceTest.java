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
package org.openhab.voice.googlestt.internal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.voice.googlestt.internal.GoogleSTTConstants.SERVICE_PID;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Tests OAuth token cleanup for the Google STT service.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class GoogleSTTServiceTest {
    @Test
    void clearsStoredTokenOnlyAfterCredentialsAreCleared() {
        OAuthFactory oAuthFactory = mock(OAuthFactory.class);
        when(oAuthFactory.createOAuthClientService(eq(SERVICE_PID), anyString(), anyString(), eq("id"), eq("secret"),
                anyString(), eq(false))).thenReturn(mock(OAuthClientService.class));
        GoogleSTTService service = new GoogleSTTService(oAuthFactory, mock(ConfigurationAdmin.class));

        service.activate(Map.of());
        verify(oAuthFactory, never()).deleteServiceAndAccessToken(SERVICE_PID);

        service.modified(Map.of("clientId", "id", "clientSecret", "secret"));
        service.modified(Map.of("clientId", "id"));
        verify(oAuthFactory, never()).deleteServiceAndAccessToken(SERVICE_PID);

        service.modified(Map.of());
        var order = inOrder(oAuthFactory);
        order.verify(oAuthFactory).ungetOAuthService(SERVICE_PID);
        order.verify(oAuthFactory).deleteServiceAndAccessToken(SERVICE_PID);
        service.dispose();
        verify(oAuthFactory).deleteServiceAndAccessToken(SERVICE_PID);
    }
}
