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
package org.openhab.binding.withings.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.withings.internal.WithingsBindingConstants;
import org.openhab.binding.withings.internal.api.device.DevicesResponseDTO;
import org.openhab.binding.withings.internal.exception.UnknownChannelException;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.*;
import org.openhab.core.types.State;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class ScaleThingHandler extends AbstractWithingsDeviceThingHandler {

    public ScaleThingHandler(Thing thing) {
        super(thing);
    }

    protected void updateChannels(DevicesResponseDTO.Device device) {
        for (Channel channel : getThing().getChannels()) {

            ChannelUID channelUID = channel.getUID();
            if (isLinked(channelUID)) {

                State state;
                switch (channelUID.getId()) {
                    case WithingsBindingConstants.CHANNEL_SCALE_BATTERY_LEVEL:
                        state = new StringType(device.getBattery());
                        break;
                    case WithingsBindingConstants.CHANNEL_SCALE_LAST_CONNECTION:
                        state = createDateTimeType(device.getLastSessionDate());
                        break;
                    default:
                        throw new UnknownChannelException(channelUID);
                }
                updateState(channelUID, state);
            }
        }
    }
}
