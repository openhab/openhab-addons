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
package org.openhab.binding.cbus.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.binding.cbus.internal.cgate.Application;
import org.openhab.binding.cbus.internal.cgate.CGateException;
import org.openhab.binding.cbus.internal.cgate.Group;

/**
 * The {@link CBusTemperatureHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */
public class CBusTemperatureHandler extends CBusGroupHandler {

    public CBusTemperatureHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Read only thing - no commands to handle
    }

    @Override
    protected Group getGroup(int groupID) throws CGateException {
        Application lighting = cBusNetworkHandler.getNetwork()
                .getApplication(Integer.parseInt(CBusBindingConstants.CBUS_APPLICATION_TEMPERATURE));
        return lighting.getGroup(groupID);
    }
}
