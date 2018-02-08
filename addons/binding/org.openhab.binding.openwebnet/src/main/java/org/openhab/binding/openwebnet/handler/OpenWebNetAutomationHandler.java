/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.handler;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.internal.AutomationState;
import org.openhab.binding.openwebnet.internal.LightState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetAutomationHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public class OpenWebNetAutomationHandler extends OpenWebNetZigBeeThingHandler {

    @SuppressWarnings("null")
    private Logger logger = LoggerFactory.getLogger(OpenWebNetAutomationHandler.class);

    @SuppressWarnings("null")
    public static final Set<@NonNull ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(OpenWebNetBindingConstants.THING_TYPE_AUTOMATION);

    public OpenWebNetAutomationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        @Nullable
        Bridge bridge = getBridge();
        @Nullable
        OpenWebNetBridgeHandler bridgeHandler = (bridge == null) ? null : (OpenWebNetBridgeHandler) bridge.getHandler();

        AutomationState state = AutomationState.findByCommand(command);
        if (state != null) {
            logger.info("{} -> {} received for the channel {}", this, state.toString(), channelUID.getId());
            if (bridgeHandler != null) {
                bridgeHandler.setAutomation(getWhere(1), state.id, false);
            }
        } else if (command instanceof PercentType) {
            int percent = ((PercentType) command).intValue();
            logger.info("'{} -> Setting to level {}% received for the channel {}", this, percent, channelUID.getId());
            if (bridgeHandler != null) {
                bridgeHandler.setPositionAutomation(getWhere(1), percent, false);
            }
        } else if (command instanceof RefreshType) {
            logger.info("{} -> Refresh received for the channel {}", this, channelUID.getId());
            if (bridgeHandler != null) {
                bridgeHandler.getAutomation(getWhere(1), false);
            }
        } else {
            logger.warn("{} -> Command {} @ {} received for the channel {}", this, command, command.getClass(),
                    channelUID.getId());
        }
    }

    @Override
    public void onStatusChange(@NonNull LightState state) {
        logger.warn("{} -> Should never be called with an Lighting state ({}) on channel {}", this, state.state,
                state.channel);
    }

    @Override
    public void onStatusChange(@NonNull AutomationState state) {
        updateState(thing.getChannels().get(0).getUID(), state.state);
    }

    @Override
    public String toString() {
        return "Automation Handler for MAC=" + getMacAddress();
    }
}
