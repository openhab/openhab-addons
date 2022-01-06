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
package org.openhab.binding.wled.internal.api;

import static org.openhab.binding.wled.internal.WLedBindingConstants.*;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wled.internal.WLedHandler;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;

/**
 * The {@link WledApiV0130} is the json Api methods for firmware version 0.13.0 and newer
 * as newer firmwares come out with breaking changes, extend this class into a newer firmware version class.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class WledApiV0130 extends WledApiV0110 {

    public WledApiV0130(WLedHandler handler, HttpClient httpClient) {
        super(handler, httpClient);
    }

    @Override
    public void initialize() throws ApiException {
        super.initialize();
        ArrayList<Channel> removeChannels = new ArrayList<>();
        // This version of firmware removed these channels
        Channel channel = handler.getThing().getChannel(CHANNEL_PRESET_DURATION);
        if (channel != null) {
            removeChannels.add(channel);
        }
        channel = handler.getThing().getChannel(CHANNEL_PRESET_CYCLE);
        if (channel != null) {
            removeChannels.add(channel);
        }
        handler.removeChannels(removeChannels);
    }

    @Override
    protected void processState() throws ApiException {
        super.processState();
        handler.update(CHANNEL_PLAYLISTS, new StringType(Integer.toString(state.stateResponse.pl)));
    }
}
