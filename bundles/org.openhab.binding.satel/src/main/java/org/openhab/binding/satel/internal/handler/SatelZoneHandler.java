/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.satel.internal.SatelBindingConstants.THING_TYPE_ZONE;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.command.ControlObjectCommand;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.types.StateType;
import org.openhab.binding.satel.internal.types.ZoneControl;
import org.openhab.binding.satel.internal.types.ZoneState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;

/**
 * The {@link SatelZoneHandler} is responsible for handling commands, which are
 * sent to one of the channels of a zone.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class SatelZoneHandler extends WirelessChannelsHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_ZONE);

    public SatelZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected Optional<SatelCommand> convertCommand(ChannelUID channel, Command command) {
        if (command instanceof OnOffType) {
            final SatelBridgeHandler bridgeHandler = getBridgeHandler();
            boolean switchOn = (command == OnOffType.ON);
            StateType stateType = getStateType(channel.getId());
            int size = bridgeHandler.getIntegraType().hasExtPayload() ? 32 : 16;
            byte[] zones = getObjectBitset(size, getThingConfig().getId());
            ZoneControl action = null;
            switch ((ZoneState) stateType) {
                case BYPASS:
                    action = switchOn ? ZoneControl.BYPASS : ZoneControl.UNBYPASS;
                    break;
                case ISOLATE:
                    action = switchOn ? ZoneControl.ISOLATE : null;
                    break;
                default:
                    // do nothing for other types of state
                    break;
            }

            if (action != null) {
                return Optional.of(new ControlObjectCommand(action, zones, bridgeHandler.getUserCode(), scheduler));
            }
        }

        return Optional.empty();
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    protected StateType getStateType(String channelId) {
        StateType result = super.getStateType(channelId);
        if (result == StateType.NONE) {
            result = ZoneState.valueOf(channelId.toUpperCase());
        }
        return result;
    }
}
