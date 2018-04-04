/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.internal;

import java.util.Optional;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgwebos.handler.LGWebOSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.VolumeControl;
import com.connectsdk.service.capability.VolumeControl.MuteListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;

/**
 * Handles TV Control Mute Command.
 *
 * @author Sebastian Prehn - initial contribution
 */
public class VolumeControlMute extends BaseChannelHandler<MuteListener> {
    private final Logger logger = LoggerFactory.getLogger(VolumeControlMute.class);

    private VolumeControl getControl(ConnectableDevice device) {
        return device.getCapability(VolumeControl.class);
    }

    @Override
    public void onReceiveCommand(ConnectableDevice device, String channelId, LGWebOSHandler handler, Command command) {
        if (device == null) {
            return;
        }
        if (OnOffType.ON == command || OnOffType.OFF == command) {
            if (device.hasCapabilities(VolumeControl.Mute_Set)) {
                getControl(device).setMute(OnOffType.ON == command, createDefaultResponseListener());
            }
        } else {
            logger.warn("only accept OnOffType");
        }
    }

    @Override
    protected Optional<ServiceSubscription<MuteListener>> getSubscription(ConnectableDevice device, String channelId,
            LGWebOSHandler handler) {
        if (device.hasCapability(VolumeControl.Mute_Subscribe)) {
            return Optional.of(getControl(device).subscribeMute(new MuteListener() {

                @Override
                public void onError(ServiceCommandError error) {
                    logger.debug("{} {} {}", error.getCode(), error.getPayload(), error.getMessage());
                }

                @Override
                public void onSuccess(Boolean value) {
                    handler.postUpdate(channelId, value ? OnOffType.ON : OnOffType.OFF);
                }
            }));
        } else {
            return null;
        }
    }
}
