/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wled.internal.WledState.SegmentState;
import org.openhab.binding.wled.internal.handlers.WLedBridgeHandler;
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

    public WledApiV0130(WLedBridgeHandler handler, HttpClient httpClient) {
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
        if (!removeChannels.isEmpty()) {
            handler.removeBridgeChannels(removeChannels);
        }
    }

    @Override
    protected void processState(int segmentIndex) throws ApiException {
        super.processState(segmentIndex);
        handler.update(CHANNEL_PLAYLISTS, new StringType(Integer.toString(state.stateResponse.pl)));
    }

    @Override
    public List<String> getSegmentNames() {
        // segment names was only first added in 0.13.0 firmware
        List<String> segmentNames = new ArrayList<>(state.stateResponse.seg.length);
        for (SegmentState state : state.stateResponse.seg) {
            segmentNames.add(state.n);
        }
        return segmentNames;
    }
}
