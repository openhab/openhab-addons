/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.internal;

import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgwebos.handler.LGWebOSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaControl.PlayStateListener;
import com.connectsdk.service.capability.PlaylistControl;

/**
 * Handles commands of a Player Item.
 *
 *
 * @author Sebastian Prehn - initial contribution
 */
public class MediaControlPlayer extends BaseChannelHandler<PlayStateListener> {
    private final Logger logger = LoggerFactory.getLogger(MediaControlPlayer.class);

    private MediaControl getMediaControl(ConnectableDevice device) {
        return device.getCapability(MediaControl.class);
    }

    private PlaylistControl getPlayListControl(ConnectableDevice device) {
        return device.getCapability(PlaylistControl.class);
    }

    @Override
    public void onReceiveCommand(ConnectableDevice device, String channelId, LGWebOSHandler handler, Command command) {
        if (device == null) {
            return;
        }
        if (NextPreviousType.NEXT == command) {
            if (device.hasCapabilities(PlaylistControl.Next)) {
                getPlayListControl(device).next(createDefaultResponseListener());
            }
        } else if (NextPreviousType.PREVIOUS == command) {
            if (device.hasCapabilities(PlaylistControl.Previous)) {
                getPlayListControl(device).previous(createDefaultResponseListener());
            }
        } else if (PlayPauseType.PLAY == command) {
            if (device.hasCapabilities(MediaControl.Play)) {
                getMediaControl(device).play(createDefaultResponseListener());
            }
        } else if (PlayPauseType.PAUSE == command) {
            if (device.hasCapabilities(MediaControl.Pause)) {
                getMediaControl(device).pause(createDefaultResponseListener());
            }
        } else if (RewindFastforwardType.FASTFORWARD == command) {
            if (device.hasCapabilities(MediaControl.FastForward)) {
                getMediaControl(device).fastForward(createDefaultResponseListener());
            }
        } else if (RewindFastforwardType.REWIND == command) {
            if (device.hasCapabilities(MediaControl.Rewind)) {
                getMediaControl(device).rewind(createDefaultResponseListener());
            }
        } else {
            logger.warn("Only accept NextPreviousType, PlayPauseType, RewindFastforwardType. Type was {}.",
                    command.getClass());
        }
    }
}
