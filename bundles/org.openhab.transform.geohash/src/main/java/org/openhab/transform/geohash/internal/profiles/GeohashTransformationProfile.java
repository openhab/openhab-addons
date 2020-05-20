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
package org.openhab.transform.geohash.internal.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Profile to offer the MapTransformationservice on a ItemChannelLink
 *
 * @author Stefan Triller - initial contribution
 *
 */
@NonNullByDefault
public class GeohashTransformationProfile implements StateProfile {

    public static final ProfileTypeUID PROFILE_TYPE_UID = new ProfileTypeUID(
            TransformationService.TRANSFORM_PROFILE_SCOPE, "GEOHASH");

    private final Logger logger = LoggerFactory.getLogger(GeohashTransformationProfile.class);

    private final TransformationService service;
    private final ProfileCallback callback;

    private static final String PRECISION_PARAM = "precision";
    private static final String SOURCE_FORMAT_PARAM = "sourceFormat";

    private final @NonNullByDefault({}) String precision;
    private final @NonNullByDefault({}) String sourceFormat;

    public GeohashTransformationProfile(ProfileCallback callback, ProfileContext context,
            TransformationService service) {
        this.service = service;
        this.callback = callback;

        Object paramPrecision = context.getConfiguration().get(PRECISION_PARAM);
        Object paramSource = context.getConfiguration().get(SOURCE_FORMAT_PARAM);

        logger.debug("Profile configured with '{}'='{}', '{}'={}", PRECISION_PARAM, paramPrecision, SOURCE_FORMAT_PARAM,
                paramSource);
        // SOURCE_FORMAT_PARAM is an advanced parameter and we assume "%s" if it is not set
        if (paramSource == null) {
            paramSource = "%s";
        }
        // PRECISION_PARAM is an advanced parameter and we assume "6" if it is not set
        if (paramPrecision == null) {
            paramPrecision = "6";
        }
        if (paramPrecision instanceof String && paramSource instanceof String) {
            precision = (String) paramPrecision;
            sourceFormat = (String) paramSource;
        } else {
            logger.error("Parameter '{}' and '{}' have to be Strings. Profile will be inactive.", PRECISION_PARAM,
                    SOURCE_FORMAT_PARAM);
            precision = null;
            sourceFormat = null;
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return PROFILE_TYPE_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        callback.handleUpdate(state);
    }

    @Override
    public void onCommandFromItem(Command command) {
        callback.handleCommand(command);
    }

    @Override
    public void onCommandFromHandler(Command command) {
        if (precision == null || sourceFormat == null) {
            logger.warn(
                    "Please specify a precision and a source format for this Profile in the '{}', and '{}' parameters. Returning the original command now.",
                    PRECISION_PARAM, SOURCE_FORMAT_PARAM);
            callback.sendCommand(command);
            return;
        }
        callback.sendCommand((Command) transformState(command));
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        if (precision == null || sourceFormat == null) {
            logger.warn(
                    "Please specify a precision and a source format for this Profile in the '{}', and '{}' parameters. Returning the original command now.",
                    PRECISION_PARAM, SOURCE_FORMAT_PARAM);
            callback.sendUpdate(state);
            return;
        }
        callback.sendUpdate((State) transformState(state));
    }

    private Type transformState(Type state) {
        String result = state.toFullString();
        try {
            result = TransformationHelper.transform(service, precision, sourceFormat, state.toFullString());
        } catch (TransformationException e) {
            logger.warn("Could not geohash state '{}' with precision '{}' and format '{}'", state, precision,
                    sourceFormat);
        }
        StringType resultType = new StringType(result);
        logger.debug("Transformed '{}' into '{}'", state, resultType);
        return resultType;
    }
}
