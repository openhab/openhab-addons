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
package org.openhab.binding.homewizard.internal.devices.energy_socket;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.devices.HomeWizardDeviceHandler;
import org.openhab.binding.homewizard.internal.devices.HomeWizardEnergyMeterSubHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link HomeWizardEnergySocketHandler} implements functionality to handle a HomeWizard EnergySocket.
 *
 * @author Daniël van Os - Initial contribution
 * @author Gearrel Welvaart - Added extra channels and restructured a bit
 *
 */
@NonNullByDefault
public class HomeWizardEnergySocketHandler extends HomeWizardDeviceHandler {
    private final String STATE_URL = "state";

    /**
     * Constructor
     *
     * @param thing The thing to handle
     *
     */
    public HomeWizardEnergySocketHandler(Thing thing) {
        super(thing);
        supportedTypes.add(HomeWizardBindingConstants.HWE_SKT);
    }

    @Override
    protected void retrieveData() {
        super.retrieveData();

        try {
            handleStateData(getStateData());
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-device-offline");
            return;
        }
    }

    /**
     * Handle incoming commands.
     *
     * Power on/off, Power lock/unlock and Ring brightness are supported.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }

        HomeWizardDeviceStatePayload result = null;

        switch (channelUID.getIdWithoutGroup()) {
            case HomeWizardBindingConstants.CHANNEL_RING_BRIGHTNESS: {
                result = sendStateCommand(
                        String.format("{\"brightness\": %d}", percentageToBrightness(command.toFullString())));
                if (result != null) {
                    updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SKT_CONTROL,
                            HomeWizardBindingConstants.CHANNEL_RING_BRIGHTNESS,
                            new DecimalType(brightnessToPercentage(result.getBrightness())));
                }
                break;
            }
            case HomeWizardBindingConstants.CHANNEL_POWER_SWITCH: {
                boolean onOff = command.equals(OnOffType.ON);
                result = sendStateCommand(String.format("{\"power_on\": %b}", onOff));
                if (result != null) {
                    updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SKT_CONTROL,
                            HomeWizardBindingConstants.CHANNEL_POWER_SWITCH, OnOffType.from(result.getPowerOn()));
                }
                break;
            }
            case HomeWizardBindingConstants.CHANNEL_POWER_LOCK: {
                boolean onOff = command.equals(OnOffType.ON);
                result = sendStateCommand(String.format("{\"switch_lock\": %b}", onOff));
                if (result != null) {
                    updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SKT_CONTROL,
                            HomeWizardBindingConstants.CHANNEL_POWER_LOCK, OnOffType.from(result.getSwitchLock()));
                }
                break;
            }
            default:
                super.handleCommand(channelUID, command);
                break;
        }
    }

    /**
     * Device specific handling of the returned data.
     *
     * @param data The data obtained from the API call
     */
    @Override
    protected void handleMeasurementData(String data) {
        HomeWizardEnergyMeterSubHandler.handleMeasurementData(data, this);

        var payload = gson.fromJson(data, HomeWizardEnergySocketMeasurementPayload.class);
        if (payload != null) {
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                    HomeWizardBindingConstants.CHANNEL_REACTIVE_POWER,
                    new QuantityType<>(payload.getReactivePower(), Units.VAR));
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                    HomeWizardBindingConstants.CHANNEL_APPARENT_POWER,
                    new QuantityType<>(payload.getApparentPower(), Units.VOLT_AMPERE));
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                    HomeWizardBindingConstants.CHANNEL_POWER_FACTOR, new DecimalType(payload.getPowerFactor()));
        }
        super.handleMeasurementData(data);
    }

    /**
     * Device specific handling of the returned state data.
     *
     * @param data The data obtained from the API call
     */
    public void handleStateData(String data) {
        HomeWizardDeviceStatePayload payload = null;
        try {
            payload = gson.fromJson(data, HomeWizardDeviceStatePayload.class);
        } catch (JsonSyntaxException ex) {
            logger.warn("No State data available");
        }
        if (payload != null) {
            if (!thing.getThingTypeUID().equals(HomeWizardBindingConstants.THING_TYPE_ENERGY_SOCKET)) {
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SKT_CONTROL,
                        HomeWizardBindingConstants.CHANNEL_POWER_SWITCH, OnOffType.from(payload.getPowerOn()));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SKT_CONTROL,
                        HomeWizardBindingConstants.CHANNEL_POWER_LOCK, OnOffType.from(payload.getSwitchLock()));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SKT_CONTROL,
                        HomeWizardBindingConstants.CHANNEL_RING_BRIGHTNESS,
                        new DecimalType(brightnessToPercentage(payload.getBrightness())));
            } else {
                updateState("", HomeWizardBindingConstants.CHANNEL_POWER_SWITCH, OnOffType.from(payload.getPowerOn()));
                updateState("", HomeWizardBindingConstants.CHANNEL_POWER_LOCK, OnOffType.from(payload.getSwitchLock()));
                updateState("", HomeWizardBindingConstants.CHANNEL_RING_BRIGHTNESS,
                        new DecimalType(brightnessToPercentage(payload.getBrightness())));
            }
        }
    }

    /**
     * @return json response from the state api
     * @throws InterruptedException, TimeoutException, ExecutionException
     */
    public String getStateData() throws InterruptedException, TimeoutException, ExecutionException {
        var response = getResponseFrom(getApiUrl() + STATE_URL);
        if (response.getStatus() == HttpStatus.OK_200) {
            return response.getContentAsString();
        } else {
            logger.warn("No State data available");
            return "";
        }
    }

    /**
     * Sends a command to the state interface of the device.
     *
     * @param command The command to send.
     */
    private @Nullable HomeWizardDeviceStatePayload sendStateCommand(String command) {
        HomeWizardDeviceStatePayload updatedState = null;
        var url = getApiUrl() + STATE_URL;
        try {
            var response = putDataTo(url, command).getContentAsString();
            updatedState = gson.fromJson(response, HomeWizardDeviceStatePayload.class);
        } catch (Exception ex) {
            logger.warn("Failed to send command {} to {}", command, url);
        }
        return updatedState;
    }

    /**
     * Converts a brightness value (0..255) to a percentage.
     *
     * @param brightness The brightness to convert.
     * @return brightness percentage
     */
    private static double brightnessToPercentage(int brightness) {
        return (100.0 * (brightness / 255.0));
    }

    /**
     * Converts a percentage to a brightness value (0..255)
     *
     * @param percentage The percentage to convert.
     * @return brightness value
     */
    private int percentageToBrightness(String percentage) {
        try {
            return (int) (Double.valueOf(percentage.replaceAll("[^\\d.]", "")) * 255.0 / 100.0);
        } catch (NumberFormatException ex) {
            logger.warn("Received invalid brightness percentage from socket");
            return 0;
        }
    }
}
