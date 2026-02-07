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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.TimeSeriesProfile;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for {@link TimeSeriesProfile}s which applies simple math on the item state.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractMathTransformationProfile implements TimeSeriesProfile {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final TransformationService service;
    protected final ProfileCallback callback;

    private final ProfileTypeUID profileTypeUID;

    public AbstractMathTransformationProfile(ProfileCallback callback, TransformationService service,
            ProfileTypeUID profileTypeUID) {
        this.service = service;
        this.callback = callback;
        this.profileTypeUID = profileTypeUID;
    }

    protected @Nullable String getParam(ProfileContext context, String param) {
        return getParam(context, param, true);
    }

    protected @Nullable String getParam(ProfileContext context, String param, boolean logError) {
        Object paramValue = context.getConfiguration().get(param);
        logger.debug("Profile configured with '{}'='{}'", param, paramValue);
        if (paramValue instanceof String) {
            return (String) paramValue;
        } else if (paramValue instanceof BigDecimal) {
            final BigDecimal value = (BigDecimal) paramValue;
            return value.toPlainString();
        } else {
            if (logError) {
                logger.error("Parameter '{}' has to be a BigDecimal or a String. Profile will be inactive.", param);
            }
            return null;
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return profileTypeUID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
    }

    @Override
    public void onCommandFromItem(Command command) {
        callback.handleCommand(command);
    }

    protected Type transformState(Type source, String value) {
        String result = source.toFullString();
        try {
            result = TransformationHelper.transform(service, value, "%s", result);
        } catch (TransformationException e) {
            logger.warn("Could not apply '{}' transformation on state '{}' with value '{}': {}", profileTypeUID.getId(),
                    source, value, e.getMessage());
        }
        Type resultType = source;
        if (result != null) {
            if (source instanceof DecimalType) {
                resultType = DecimalType.valueOf(result);
            } else if (source instanceof QuantityType) {
                resultType = QuantityType.valueOf(result);
            }
            logger.debug("Transformed '{}' into '{}'", source, resultType);
        }
        return resultType;
    }
}
