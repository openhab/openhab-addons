/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.handler.component;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_UI_SCREEN_SET;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcRequest;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiRpc;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyComponentDisplay} implements the screen control of the Shelly Wall Display.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyComponentDisplay {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellyComponentDisplay.class);

    /**
     * The device doesn't report the screen state, so the channel keeps the last commanded value.
     */
    public static void handleCommand(ShellyThingInterface thingHandler, Command command) throws ShellyApiException {
        if (!(command instanceof OnOffType onOff)) {
            return;
        }
        if (!(thingHandler.getApi() instanceof Shelly2ApiRpc api)) {
            throw new ShellyApiException("Screen control requires a Gen2+ device");
        }
        LOGGER.debug("{}: Turn screen {}", thingHandler.getThingName(), onOff);
        api.apiRequest(
                new Shelly2RpcRequest().withMethod(SHELLYRPC_METHOD_UI_SCREEN_SET).withOn(onOff == OnOffType.ON));
        thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_SCREEN, onOff);
    }
}
