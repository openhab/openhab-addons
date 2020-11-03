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
