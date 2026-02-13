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
import org.openhab.transform.math.internal.profiles.config.AddTransformationProfileConfiguration;

/**
 * Profile to offer the {@link org.openhab.transform.math.internal.AddTransformationService} on a ItemChannelLink.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class AddTransformationProfile extends AbstractArithmeticMathTransformationProfile {

    public static final ProfileTypeUID PROFILE_TYPE_UID = new ProfileTypeUID(
            TransformationService.TRANSFORM_PROFILE_SCOPE, "ADD");

    static final String ADDEND_PARAM = "addend";

    private final String addend;

    public AddTransformationProfile(ProfileCallback callback, ProfileContext context, TransformationService service,
            ItemRegistry itemRegistry) {
        super(callback, context, service, itemRegistry, PROFILE_TYPE_UID);

        AddTransformationProfileConfiguration config = getConfigAs(context,
                AddTransformationProfileConfiguration.class);
        addend = config.addend;
    }

    @Override
    public void onCommandFromHandler(Command command) {
        String localAddend = getItemStateOrElse(addend);
        if (localAddend == null) {
            logger.warn(
                    "Please specify an addend for this Profile in the '{}' parameter. Returning the original command now.",
                    ADDEND_PARAM);
            callback.sendCommand(command);
            return;
        }
        callback.sendCommand((Command) transformState(command, localAddend));
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        String localAddend = getItemStateOrElse(addend);
        if (localAddend == null) {
            logger.warn(
                    "Please specify an addend for this Profile in the '{}' parameter. Returning the original state now.",
                    ADDEND_PARAM);
            callback.sendUpdate(state);
            return;
        }
        callback.sendUpdate((State) transformState(state, localAddend));
    }

    @Override
    public void onTimeSeriesFromHandler(TimeSeries timeSeries) {
        String localAddend = getItemStateOrElse(addend);
        if (localAddend == null) {
            logger.warn(
                    "Please specify an addend for this Profile in the '{}' parameter. Returning the original state now.",
                    ADDEND_PARAM);
            callback.sendTimeSeries(timeSeries);
            return;
        }
        TimeSeries transformedTimeSeries = new TimeSeries(timeSeries.getPolicy());
        timeSeries.getStates().forEach(entry -> transformedTimeSeries.add(entry.timestamp(),
                (State) transformState(entry.state(), localAddend)));
        callback.sendTimeSeries(transformedTimeSeries);
    }
}
