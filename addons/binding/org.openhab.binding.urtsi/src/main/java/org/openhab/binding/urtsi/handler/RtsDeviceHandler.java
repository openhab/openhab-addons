/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.urtsi.handler;

import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.urtsi.UrtsiBindingConstants;
import org.openhab.binding.urtsi.internal.config.RtsDeviceConfig;
import org.openhab.binding.urtsi.internal.mapping.UrtsiChannelMapping;

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
