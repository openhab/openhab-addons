/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.handler;

import static org.openhab.binding.satel.internal.SatelBindingConstants.THING_TYPE_OUTPUT;

import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.command.ControlObjectCommand;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.types.OutputControl;
import org.openhab.binding.satel.internal.types.OutputState;
import org.openhab.binding.satel.internal.types.StateType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;

/**
 * The {@link SatelOutputHandler} is responsible for handling commands, which are
 * sent to one of the channels of an output device.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class SatelOutputHandler extends WirelessChannelsHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_OUTPUT);

    public SatelOutputHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected Optional<SatelCommand> convertCommand(ChannelUID channel, Command command) {
        if (command instanceof OnOffType && getStateType(channel.getId()) == OutputState.STATE) {
            final SatelBridgeHandler bridgeHandler = getBridgeHandler();
            boolean switchOn = (command == OnOffType.ON);
            int size = bridgeHandler.getIntegraType().hasExtPayload() ? 32 : 16;
            byte[] outputs = getObjectBitset(size, getThingConfig().getId());
            boolean newState = switchOn ^ getThingConfig().isStateInverted();
            return Optional.of(new ControlObjectCommand(newState ? OutputControl.ON : OutputControl.OFF, outputs,
                    bridgeHandler.getUserCode(), scheduler));
        }

        return Optional.empty();
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    protected StateType getStateType(String channelId) {
        StateType result = super.getStateType(channelId);
        if (result == StateType.NONE) {
            result = OutputState.valueOf(channelId.toUpperCase());
        }
        return result;
    }
}
