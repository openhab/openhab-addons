/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.openhab.transform.vat.internal.config.VATConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Profile to offer the {@link VATTransformationService} on an ItemChannelLink.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class VATTransformationProfile implements StateProfile {

    private final Logger logger = LoggerFactory.getLogger(VATTransformationProfile.class);

    private final ProfileCallback callback;
    private final TransformationService service;
    private final LocaleProvider localeProvider;

    private VATConfig configuration;

    public VATTransformationProfile(final ProfileCallback callback, final TransformationService service,
            final ProfileContext context, LocaleProvider localeProvider) {
        this.callback = callback;
        this.service = service;
        this.localeProvider = localeProvider;
        this.configuration = context.getConfiguration().as(VATConfig.class);
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
        callback.sendCommand((Command) transformState(command));
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        callback.sendUpdate((State) transformState(state));
    }

    private Type transformState(Type state) {
        String result = state.toFullString();
        String percentage = getVATPercentage();
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
            logger.debug("Transformed '{}' into '{}'", state, resultType);
        }
        return resultType;
    }

    private String getVATPercentage() {
        if (!configuration.percentage.isBlank()) {
            return getOverriddenVAT();
        }

        String country = localeProvider.getLocale().getCountry();
        String rate = RATES.get(country);
        if (rate == null) {
            logger.warn("No VAT rate for country {}", country);
            return "0";
        }
        return rate;
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
