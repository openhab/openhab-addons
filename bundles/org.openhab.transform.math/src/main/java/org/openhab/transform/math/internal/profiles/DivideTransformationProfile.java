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

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.transform.math.internal.DivideTransformationService;

/**
 * Profile to offer the {@link DivideTransformationService} on a ItemChannelLink.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class DivideTransformationProfile extends AbstractArithmeticMathTransformationProfile {

    public static final ProfileTypeUID PROFILE_TYPE_UID = new ProfileTypeUID(
            TransformationService.TRANSFORM_PROFILE_SCOPE, "DIVIDE");

    static final String DIVISOR_PARAM = "divisor";

    private final @Nullable String divisor;

    public DivideTransformationProfile(ProfileCallback callback, ProfileContext context, TransformationService service,
            ItemRegistry itemRegistry) {
        super(callback, context, service, itemRegistry, PROFILE_TYPE_UID);

        divisor = getParam(context, DIVISOR_PARAM);

        if (BigDecimal.ZERO.compareTo(new BigDecimal(divisor)) == 0) {
            throw new IllegalArgumentException(String.format("The divisor must be non-zero but was '%s'.", divisor));
        }
    }

    @Override
    public void onCommandFromHandler(Command command) {
        String localDivisor = getItemStateOrElse(divisor);
        if (localDivisor == null) {
            logger.warn(
                    "Please specify a divisor for this Profile in the '{}' parameter. Returning the original command now.",
                    DIVISOR_PARAM);
            callback.sendCommand(command);
            return;
        }
        callback.sendCommand((Command) transformState(command, localDivisor));
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        String localDivisor = getItemStateOrElse(divisor);
        if (localDivisor == null) {
            logger.warn(
                    "Please specify a divisor for this Profile in the '{}' parameter. Returning the original state now.",
                    DIVISOR_PARAM);
            callback.sendUpdate(state);
            return;
        }
        callback.sendUpdate((State) transformState(state, localDivisor));
    }

    @Override
    public void onTimeSeriesFromHandler(TimeSeries timeSeries) {
        String localDivisor = getItemStateOrElse(divisor);
        if (localDivisor == null) {
            logger.warn(
                    "Please specify a divisor for this Profile in the '{}' parameter. Returning the original state now.",
                    DIVISOR_PARAM);
            callback.sendTimeSeries(timeSeries);
            return;
        }
        TimeSeries transformedTimeSeries = new TimeSeries(timeSeries.getPolicy());
        timeSeries.getStates().forEach(entry -> transformedTimeSeries.add(entry.timestamp(),
                (State) transformState(entry.state(), localDivisor)));
        callback.sendTimeSeries(transformedTimeSeries);
    }
}
