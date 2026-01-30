/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.transform.math.internal.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

/**
 * Profile to offer the BitwiseTransformationServicss on a ItemChannelLink.
 *
 * @author Christoph Weitkamp - Initial contribution
 * @author Jan N. Klug - Adapted To BoitwiseTransformations
 */
@NonNullByDefault
public abstract class BitwiseTransformationProfile extends AbstractMathTransformationProfile {

    static final String MASK_PARAM = "mask";

    private final @Nullable String mask;

    public BitwiseTransformationProfile(ProfileCallback callback, ProfileContext context, TransformationService service,
            ProfileTypeUID profileTypeUID) {
        super(callback, service, profileTypeUID);

        mask = getParam(context, MASK_PARAM);
    }

    @Override
    public void onCommandFromHandler(Command command) {
        String localMask = mask;
        if (localMask == null) {
            logger.warn(
                    "Please specify a mask for this Profile in the '{}' parameter. Returning the original command now.",
                    MASK_PARAM);
            callback.sendCommand(command);
            return;
        }
        callback.sendCommand((Command) transformState(command, localMask));
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        String localMask = mask;
        if (localMask == null) {
            logger.warn(
                    "Please specify a mask for this Profile in the '{}' parameter. Returning the original state now.",
                    MASK_PARAM);
            callback.sendUpdate(state);
            return;
        }
        callback.sendUpdate((State) transformState(state, localMask));
    }

    @Override
    public void onTimeSeriesFromHandler(TimeSeries timeSeries) {
        String localMask = mask;
        if (localMask == null) {
            logger.warn(
                    "Please specify a mask for this Profile in the '{}' parameter. Returning the original state now.",
                    MASK_PARAM);
            callback.sendTimeSeries(timeSeries);
            return;
        }
        TimeSeries transformedTimeSeries = new TimeSeries(timeSeries.getPolicy());
        timeSeries.getStates().forEach(entry -> transformedTimeSeries.add(entry.timestamp(),
                (State) transformState(entry.state(), localMask)));
        callback.sendTimeSeries(transformedTimeSeries);
    }
}
