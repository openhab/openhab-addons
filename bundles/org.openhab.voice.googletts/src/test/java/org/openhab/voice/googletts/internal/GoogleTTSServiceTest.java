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
package org.openhab.voice.googletts.internal;

import static org.mockito.Mockito.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.voice.TTSCache;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Tests OAuth token cleanup for the Google TTS service.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class GoogleTTSServiceTest {
    @Test
    void clearsStoredTokenOnlyAfterCredentialsAreCleared() {
        OAuthFactory oAuthFactory = mock(OAuthFactory.class);
        GoogleTTSService service = new GoogleTTSService(mock(ConfigurationAdmin.class), oAuthFactory,
                mock(TTSCache.class), Map.of());

        service.activate(Map.of());
        verify(oAuthFactory, never()).deleteServiceAndAccessToken(GoogleTTSService.SERVICE_PID);

        service.updateConfig(Map.of("clientId", "id"));
        verify(oAuthFactory, never()).deleteServiceAndAccessToken(GoogleTTSService.SERVICE_PID);

        service.updateConfig(Map.of());
        verify(oAuthFactory).deleteServiceAndAccessToken(GoogleTTSService.SERVICE_PID);
        service.dispose();
        verify(oAuthFactory, times(1)).deleteServiceAndAccessToken(GoogleTTSService.SERVICE_PID);
    }
}
