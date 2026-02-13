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
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.transform.math.internal.profiles.config.MultiplyTransformationProfileConfiguration;

/**
 * Profile to offer the {@link org.openhab.transform.math.internal.MultiplyTransformationService} on a ItemChannelLink.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class MultiplyTransformationProfile extends AbstractArithmeticMathTransformationProfile {

    public static final ProfileTypeUID PROFILE_TYPE_UID = new ProfileTypeUID(
            TransformationService.TRANSFORM_PROFILE_SCOPE, "MULTIPLY");

    static final String MULTIPLICAND_PARAM = "multiplicand";

    private final String multiplicand;

    public MultiplyTransformationProfile(ProfileCallback callback, ProfileContext context,
            TransformationService service, ItemRegistry itemRegistry) {
        super(callback, context, service, itemRegistry, PROFILE_TYPE_UID);

        MultiplyTransformationProfileConfiguration config = getConfigAs(context,
                MultiplyTransformationProfileConfiguration.class);
        multiplicand = config.multiplicand;
    }

    @Override
    public void onCommandFromHandler(Command command) {
        String localMultiplicand = getItemStateOrElse(multiplicand);
        if (localMultiplicand == null) {
            logger.warn(
                    "Please specify a multiplicand for this Profile in the '{}' parameter. Returning the original command now.",
                    MULTIPLICAND_PARAM);
            callback.sendCommand(command);
            return;
        }
        callback.sendCommand((Command) transformState(command, localMultiplicand));
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        String localMultiplicand = getItemStateOrElse(multiplicand);
        if (localMultiplicand == null) {
            logger.warn(
                    "Please specify a multiplicand for this Profile in the '{}' parameter. Returning the original state now.",
                    MULTIPLICAND_PARAM);
            callback.sendUpdate(state);
            return;
        }
        callback.sendUpdate((State) transformState(state, localMultiplicand));
    }

    @Override
    public void onTimeSeriesFromHandler(TimeSeries timeSeries) {
        String localMultiplicand = getItemStateOrElse(multiplicand);
        if (localMultiplicand == null) {
            logger.warn(
                    "Please specify a multiplicand for this Profile in the '{}' parameter. Returning the original state now.",
                    MULTIPLICAND_PARAM);
            callback.sendTimeSeries(timeSeries);
            return;
        }
        TimeSeries transformedTimeSeries = new TimeSeries(timeSeries.getPolicy());
        timeSeries.getStates().forEach(entry -> transformedTimeSeries.add(entry.timestamp(),
                (State) transformState(entry.state(), localMultiplicand)));
        callback.sendTimeSeries(transformedTimeSeries);
    }
}
