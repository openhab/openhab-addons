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
package org.openhab.binding.urtsi.internal.handler;

import org.openhab.binding.urtsi.internal.UrtsiBindingConstants;
import org.openhab.binding.urtsi.internal.config.RtsDeviceConfig;
import org.openhab.binding.urtsi.internal.mapping.UrtsiChannelMapping;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link RtsDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Oliver Libutzki - Initial contribution
 */
public class RtsDeviceHandler extends BaseThingHandler {

    public RtsDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(UrtsiBindingConstants.POSITION)) {
            RtsCommand rtsCommand = null;
            if (command instanceof UpDownType) {
                switch ((UpDownType) command) {
                    case UP:
                        rtsCommand = RtsCommand.UP;
                        break;
                    case DOWN:
                        rtsCommand = RtsCommand.DOWN;
                        break;
                    default:
                        break;
                }
            } else if (command instanceof StopMoveType) {
                switch ((StopMoveType) command) {
                    case STOP:
                        rtsCommand = RtsCommand.STOP;
                        break;
                    default:
                        break;
                }
            }
            if (rtsCommand != null) {
                // We delegate the execution to the bridge handler
                ThingHandler bridgeHandler = getBridge().getHandler();
                if (bridgeHandler instanceof UrtsiDeviceHandler) {
                    boolean executedSuccessfully = ((UrtsiDeviceHandler) bridgeHandler).executeRtsCommand(getThing(),
                            rtsCommand);
                    if (executedSuccessfully && command instanceof State) {
                        updateState(channelUID, (State) command);
                    }
                }
            }
        }
    }

    @Override
    public void initialize() {
        RtsDeviceConfig rtsDeviceConfig = getConfigAs(RtsDeviceConfig.class);
        String mappedChannel = UrtsiChannelMapping.getMappedChannel(rtsDeviceConfig.channel);
        if (mappedChannel == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The channel '" + rtsDeviceConfig.channel + "' is invalid.");
        } else {
            // Just use the status of the bridge as we do not have any information if there a RTS device listening at
            // the configured channel
            ThingStatus bridgeStatus = getBridge().getStatus();
            updateStatus(bridgeStatus);

        }
    }
}
