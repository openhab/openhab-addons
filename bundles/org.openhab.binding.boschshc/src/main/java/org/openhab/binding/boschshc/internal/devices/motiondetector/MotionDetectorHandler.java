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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

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
    public void processUpdate(String id, @NonNull JsonElement state) {
        logger.debug("Motion detector: received update: {} {}", id, state);

        try {
            updateLatestMotionState(gson.fromJson(state, LatestMotionState.class));
        } catch (JsonSyntaxException e) {
            logger.warn("Received unknown update in in-wall switch: {}", state);
        }
    }
}
