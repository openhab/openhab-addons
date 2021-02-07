/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.solarwatt.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.channel.SolarwattChannelTypeProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The concrete device handlers process the device specific commands.
 *
 * This device handler is still pretty useless.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class DeviceHandler extends AbstractDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

    public DeviceHandler(Thing thing, SolarwattChannelTypeProvider channelTypeProvider) {
        super(thing, channelTypeProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        this.logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);
        super.handleCommand(channelUID, command);
    }
}
