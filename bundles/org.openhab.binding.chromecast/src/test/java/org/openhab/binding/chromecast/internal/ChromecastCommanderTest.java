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
package org.openhab.binding.chromecast.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.chromecast.internal.ChromecastBindingConstants.MEDIA_PLAYER;

import org.digitalmediaserver.cast.CastDevice;
import org.digitalmediaserver.cast.Session;
import org.digitalmediaserver.cast.message.entity.Application;
import org.digitalmediaserver.cast.message.entity.MediaStatus;
import org.digitalmediaserver.cast.message.entity.ReceiverStatus;
import org.digitalmediaserver.cast.message.enumeration.IdleReason;
import org.digitalmediaserver.cast.message.enumeration.PlayerState;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.OnOffType;

/**
 * Tests for {@link ChromecastCommander}.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class ChromecastCommanderTest {

    private final CastDevice chromeCast = mock(CastDevice.class);
    private final ChromecastScheduler scheduler = mock(ChromecastScheduler.class);
    private final ChromecastStatusUpdater statusUpdater = mock(ChromecastStatusUpdater.class);
    private final ChromecastCommander commander = new ChromecastCommander(chromeCast, scheduler, statusUpdater);

    @Test
    void stopCommandStopsAnyRunningApplication() throws Exception {
        Application application = mock(Application.class);
        when(application.getAppId()).thenReturn("APP_ID");
        when(chromeCast.getRunningApplication()).thenReturn(application);

        commander.handleCloseApp(OnOffType.ON);

        verify(chromeCast).stopApplication(application, false);
    }

    @Test
    void automaticCleanupDoesNotStopApplicationOwnedByAnotherSender() throws Exception {
        Application application = mock(Application.class);
        ReceiverStatus receiverStatus = mock(ReceiverStatus.class);
        Session session = mock(Session.class);
        MediaStatus mediaStatus = mock(MediaStatus.class);

        when(chromeCast.isConnected()).thenReturn(true);
        when(chromeCast.getReceiverStatus()).thenReturn(receiverStatus);
        when(receiverStatus.getRunningApplication()).thenReturn(application);
        when(application.getTransportId()).thenReturn("transport-id");
        when(application.getAppId()).thenReturn(MEDIA_PLAYER);
        when(application.getSessionId()).thenReturn("other-session");
        when(chromeCast.startSession("openHAB", application)).thenReturn(session);
        when(session.getMediaStatus()).thenReturn(mediaStatus);
        when(mediaStatus.getPlayerState()).thenReturn(PlayerState.IDLE);
        when(mediaStatus.getIdleReason()).thenReturn(IdleReason.FINISHED);
        when(statusUpdater.getAppSessionId()).thenReturn("openhab-session");

        commander.handleRefresh();

        verify(chromeCast, never()).stopApplication(application, false);
    }
}
