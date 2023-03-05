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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.EXECUTE_ACTION;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

/**
 * The {@link SomfyTahomaActionGroupHandler} is responsible for handling commands,
 * which are sent to one of the channels of the action group thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaActionGroupHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaActionGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initializeThing(@Nullable ThingStatus bridgeStatus) {
        if (bridgeStatus != null) {
            url = getURL();
            if (bridgeStatus == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (EXECUTE_ACTION.equals(channelUID.getId()) && command instanceof OnOffType) {
            if (OnOffType.ON.equals(command)) {
                executeActionGroup();
            }
        }
    }
}
