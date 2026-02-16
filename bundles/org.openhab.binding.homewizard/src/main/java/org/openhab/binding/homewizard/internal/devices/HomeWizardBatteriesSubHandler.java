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
package org.openhab.binding.homewizard.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link HomeWizardBatteriesSubHandler} implements functionality to retrieve batteries data.
 *
 * @author Gearrel Welvaart - Initial contribution
 */
@NonNullByDefault
public class HomeWizardBatteriesSubHandler {

    protected final static Logger logger = LoggerFactory.getLogger(HomeWizardBatteriesSubHandler.class);
    private final static Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    public static void handleCommand(Command command, HomeWizardDeviceHandler handler) {
        var mode = "";
        var permissions = "";

        switch (command.toFullString()) {
            case HomeWizardBindingConstants.BATTERIES_MODE_STANDBY: {
                mode = HomeWizardBindingConstants.BATTERIES_MODE_STANDBY;
                permissions = "\"permissions\": [],";
                break;
            }
            case HomeWizardBindingConstants.BATTERIES_MODE_TO_FULL: {
                mode = HomeWizardBindingConstants.BATTERIES_MODE_TO_FULL;
                break;
            }
            case HomeWizardBindingConstants.BATTERIES_MODE_ZERO: {
                mode = HomeWizardBindingConstants.BATTERIES_MODE_ZERO;
                permissions = String.format("\"permissions\": [\"%s\", \"%s\"],",
                        HomeWizardBindingConstants.BATTERIES_PERMISSION_CHARGE_ALLOWED,
                        HomeWizardBindingConstants.BATTERIES_PERMISSION_DISCHARGE_ALLOWED);
                break;
            }
            case HomeWizardBindingConstants.BATTERIES_MODE_ZERO_CHARGE_ONLY: {
                mode = HomeWizardBindingConstants.BATTERIES_MODE_ZERO;
                permissions = String.format("\"permissions\": [\"%s\"],",
                        HomeWizardBindingConstants.BATTERIES_PERMISSION_CHARGE_ALLOWED);
                break;
            }
            case HomeWizardBindingConstants.BATTERIES_MODE_ZERO_DISCHARGE_ONLY: {
                mode = HomeWizardBindingConstants.BATTERIES_MODE_ZERO;
                permissions = String.format("\"permissions\": [\"%s\"],",
                        HomeWizardBindingConstants.BATTERIES_PERMISSION_DISCHARGE_ALLOWED);
                break;
            }
            default: {
                logger.warn("Unsupported command {}.", command.toFullString());
                return;
            }
        }
        var cmd = String.format("{%s\"mode\": \"%s\"}", permissions, mode);

        handler.sendBatteriesCommand(cmd);
    }

    /**
     * Device specific handling of the returned batteries data.
     *
     * @param data The data obtained from the API call
     */
    public static void handleBatteriesData(String data, HomeWizardDeviceHandler handler) {
        HomeWizardBatteriesPayload payload = null;
        try {
            payload = gson.fromJson(data, HomeWizardBatteriesPayload.class);
        } catch (JsonSyntaxException ex) {
            logger.warn("No Batteries data available");
        }
        if (payload != null) {
            var mode = payload.getMode();
            if (mode.equals(HomeWizardBindingConstants.BATTERIES_MODE_ZERO)) {
                if (payload.isChargingAllowed() && !payload.isDischargingAllowed()) {
                    mode = HomeWizardBindingConstants.BATTERIES_MODE_ZERO_CHARGE_ONLY;
                }
                if (!payload.isChargingAllowed() && payload.isDischargingAllowed()) {
                    mode = HomeWizardBindingConstants.BATTERIES_MODE_ZERO_DISCHARGE_ONLY;
                }
                if (!payload.isChargingAllowed() && !payload.isDischargingAllowed()) {
                    mode = HomeWizardBindingConstants.BATTERIES_MODE_STANDBY;
                }
            }
            handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_BATTERIES,
                    HomeWizardBindingConstants.CHANNEL_BATTERIES_MODE, new StringType(mode));
            handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_BATTERIES,
                    HomeWizardBindingConstants.CHANNEL_BATTERIES_COUNT, new DecimalType(payload.getBatteryCount()));
            handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_BATTERIES,
                    HomeWizardBindingConstants.CHANNEL_BATTERIES_POWER,
                    new QuantityType<>(payload.getPower(), Units.WATT));
            handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_BATTERIES,
                    HomeWizardBindingConstants.CHANNEL_BATTERIES_TARGET_POWER,
                    new QuantityType<>(payload.getTargetPower(), Units.WATT));
            handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_BATTERIES,
                    HomeWizardBindingConstants.CHANNEL_BATTERIES_MAX_CONSUMPTION,
                    new QuantityType<>(payload.getMaxConsumption(), Units.WATT));
            handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_BATTERIES,
                    HomeWizardBindingConstants.CHANNEL_BATTERIES_MAX_PRODUCTION,
                    new QuantityType<>(payload.getMaxProduction(), Units.WATT));
        }
    }
}
