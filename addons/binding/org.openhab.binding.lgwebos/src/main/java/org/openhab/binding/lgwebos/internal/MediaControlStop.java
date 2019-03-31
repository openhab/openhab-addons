/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.lgwebos.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.MediaControl;

/**
 * Handles Media Control Command Stop.
 *
 * @author Sebastian Prehn - initial contribution
 */
@NonNullByDefault
public class MediaControlStop extends BaseChannelHandler<Void, Object> {

    private MediaControl getControl(ConnectableDevice device) {
        return device.getCapability(MediaControl.class);
    }

    @Override
    public void onReceiveCommand(@Nullable ConnectableDevice device, String channelId, LGWebOSHandler handler,
            Command command) {
        if (device == null) {
            return;
        }
        if (hasCapability(device, MediaControl.Stop)) {
            getControl(device).stop(getDefaultResponseListener());
        }
    }
}
