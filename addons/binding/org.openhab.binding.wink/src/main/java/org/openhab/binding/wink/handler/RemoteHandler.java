/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.wink.client.IWinkDevice;
import org.openhab.binding.wink.client.WinkSupportedDevice;

/**
 * Its a remote
 *
 * @author Sebastian Marchand
 *
 */
public class RemoteHandler extends WinkBaseThingHandler {
    public RemoteHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleWinkCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    protected WinkSupportedDevice getDeviceType() {
        return WinkSupportedDevice.REMOTE;
    }

    @Override
    protected void updateDeviceState(IWinkDevice device) {
        // noop
    }
}
