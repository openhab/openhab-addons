/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal.audiosink;

import static org.openhab.binding.upnpcontrol.internal.UpnpControlBindingConstants.NOTIFICATION_AUDIOSINK_EXTENSION;

import java.io.IOException;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.upnpcontrol.internal.handler.UpnpRendererHandler;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.library.types.PercentType;

/**
 *
 * This class works as a standard audio sink for openHAB, but with specific behavior for the audio players. It is only
 * meant to be used for playing notifications. When sending audio through this sink, the previously playing media will
 * be interrupted and will automatically resume after playing the notification. If no volume is specified, the
 * notification volume will be controlled by the media player notification volume configuration.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class UpnpNotificationAudioSink extends UpnpAudioSink {

    public UpnpNotificationAudioSink(UpnpRendererHandler handler, AudioHTTPServer audioHTTPServer, String callbackUrl) {
        super(handler, audioHTTPServer, callbackUrl);
    }

    @Override
    public String getId() {
        return handler.getThing().getUID().toString() + NOTIFICATION_AUDIOSINK_EXTENSION;
    }

    @Override
    public @Nullable String getLabel(@Nullable Locale locale) {
        return handler.getThing().getLabel() + NOTIFICATION_AUDIOSINK_EXTENSION;
    }

    @Override
    public void setVolume(@Nullable PercentType volume) throws IOException {
        if (volume != null) {
            handler.setNotificationVolume(volume);
        }
    }

    @Override
    protected void playMedia(String url) {
        String newUrl = url;
        if (!url.startsWith("x-") && !url.startsWith("http")) {
            newUrl = "x-file-cifs:" + url;
        }
        handler.playNotification(newUrl);
    }
}
