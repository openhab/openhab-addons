/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.internal;

import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgwebos.handler.LGWebOSHandler;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.TVControl;

/**
 * Handles TV Control Channel Down Command.
 *
 * @author Sebastian Prehn - initial contribution
 */
public class TVControlDown extends BaseChannelHandler<Void> {

    private TVControl getControl(ConnectableDevice device) {
        return device.getCapability(TVControl.class);
    }

    @Override
    public void onReceiveCommand(ConnectableDevice device, String channelId, LGWebOSHandler handler, Command command) {
        if (device == null) {
            return;
        }
        if (device.hasCapabilities(TVControl.Channel_Down)) {
            getControl(device).channelDown(createDefaultResponseListener());
        }
    }
}
