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
package org.openhab.transform.vat.internal.profile;

import static org.openhab.transform.vat.internal.VATTransformationConstants.*;

import java.math.BigDecimal;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.LocaleProvider;
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
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.openhab.transform.vat.internal.RateProvider;
import org.openhab.transform.vat.internal.config.VATConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Profile to offer the {@link VATTransformationProfile} on an ItemChannelLink.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class VATTransformationProfile implements TimeSeriesProfile {

    private final Logger logger = LoggerFactory.getLogger(VATTransformationProfile.class);

    private final ProfileCallback callback;
    private final TransformationService service;
    private final LocaleProvider localeProvider;
    private final RateProvider rateProvider;
    private final VATConfig configuration;

    public VATTransformationProfile(final ProfileCallback callback, final TransformationService service,
            final ProfileContext context, final LocaleProvider localeProvider, final RateProvider rateProvider) {
        this.callback = callback;
        this.service = service;
        this.localeProvider = localeProvider;
        this.rateProvider = rateProvider;

        configuration = context.getConfiguration().as(VATConfig.class);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return PROFILE_TYPE_UID;
    }

    @Override
    public void onCommandFromItem(Command command) {
        callback.handleCommand(command);
    }

    @Override
    public void onStateUpdateFromItem(State state) {
    }

    @Override
    public void onCommandFromHandler(Command command) {
        callback.sendCommand((Command) transformState(command, Instant.now()));
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        callback.sendUpdate((State) transformState(state, Instant.now()));
    }

    @Override
    public void onTimeSeriesFromHandler(TimeSeries timeSeries) {
        TimeSeries transformedTimeSeries = new TimeSeries(timeSeries.getPolicy());
        timeSeries.getStates().forEach(entry -> transformedTimeSeries.add(entry.timestamp(),
                (State) transformState(entry.state(), entry.timestamp())));
        callback.sendTimeSeries(transformedTimeSeries);
    }

    private Type transformState(Type state, Instant time) {
        String result = state.toFullString();
        String percentage = getVATPercentage(time);
        try {
            result = TransformationHelper.transform(service, percentage, "%s", result);
        } catch (TransformationException e) {
            logger.warn("Could not apply '{}' transformation on state '{}' with value '{}'.", PROFILE_TYPE_UID.getId(),
                    state, percentage);
        }
        Type resultType = state;
        if (result != null) {
            if (state instanceof DecimalType) {
                resultType = DecimalType.valueOf(result);
            } else if (state instanceof QuantityType) {
                resultType = QuantityType.valueOf(result);
            } else if (state instanceof UnDefType) {
                resultType = UnDefType.valueOf(result);
            }
            logger.debug("Transformed '{}' into '{}' at {}", state, resultType, time);
        }
        return resultType;
    }

    private String getVATPercentage(Instant time) {
        if (!configuration.percentage.isBlank()) {
            return getOverriddenVAT();
        }

        String country = localeProvider.getLocale().getCountry();
        BigDecimal rate = rateProvider.getPercentage(country, time);

        if (rate == null) {
            logger.warn("No VAT rate for country {} at {}", country, time);
            return "0";
        }
        return rate.toString();
    }

    private String getOverriddenVAT() {
        String percentage = configuration.percentage.trim();
        if (percentage.endsWith("%")) {
            percentage = percentage.substring(0, percentage.length() - 1).trim();
        }
        try {
            return new BigDecimal(percentage).toString();
        } catch (NumberFormatException e) {
            logger.warn("{} is not a valid percentage", percentage);
            return "0";
        }
    }
}
