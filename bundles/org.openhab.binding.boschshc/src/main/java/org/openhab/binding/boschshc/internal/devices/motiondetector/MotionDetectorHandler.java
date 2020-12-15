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
package org.openhab.binding.boschshc.internal.devices.motiondetector;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_LATEST_MOTION;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.devices.motiondetector.dto.LatestMotionState;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import com.google.gson.JsonElement;

/**
 * MotionDetectorHandler
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
public class MotionDetectorHandler extends BoschSHCHandler {

    public MotionDetectorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command for: {} - {}", channelUID.getThingUID(), command);

        if (CHANNEL_LATEST_MOTION.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                LatestMotionState state = this.getState("LatestMotion", LatestMotionState.class);
                if (state != null) {
                    updateLatestMotionState(state);
                }
            }
        }
    }

    void updateLatestMotionState(LatestMotionState state) {
        DateTimeType date = new DateTimeType(state.latestMotionDetected);
        updateState(CHANNEL_LATEST_MOTION, date);
    }

    @Override
    public void processUpdate(String id, JsonElement state) {
        logger.debug("Motion detector: received update: {} {}", id, state);

        @Nullable
        LatestMotionState latestMotionState = GSON.fromJson(state, LatestMotionState.class);
        if (latestMotionState == null) {
            logger.warn("Received unknown update in in-wall switch: {}", state);
            return;
        }
        updateLatestMotionState(latestMotionState);
    }
}
