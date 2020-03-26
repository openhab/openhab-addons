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
package org.openhab.binding.cbus.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.cbus.CBusBindingConstants;

/**
 * The {@link CBusTemperatureHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */
@NonNullByDefault
public class CBusTemperatureHandler extends CBusGroupHandler {

    public CBusTemperatureHandler(Thing thing) {
        super(thing, CBusBindingConstants.CBUS_APPLICATION_TEMPERATURE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Read only thing - no commands to handle
    }
}
