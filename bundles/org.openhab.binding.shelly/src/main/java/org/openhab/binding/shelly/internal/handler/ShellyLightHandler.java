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
package org.openhab.binding.shelly.internal.handler;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.handler.ShellyLightModel.RGBX.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLightChannel;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapServer;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyLightModel.Mode;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ShellyLightHandler} handles light (Bulb, Duo and RGBW2) specific commands and status.
 * All other commands will be routed to the ShellyBaseHandler.
 *
 * @author Markus Michels - Initial contribution
 * @author Andrew Fiddian-Green - Migrate to LightModel
 */
@NonNullByDefault
public class ShellyLightHandler extends ShellyBaseHandler implements ShellyLightModelHandler {
    private final Logger logger = LoggerFactory.getLogger(ShellyLightHandler.class);

    // map of ShellyLightModels keyed on their component index within the device
    protected final Map<Integer, ShellyLightModel> lightModels = new ConcurrentHashMap<>();

    /**
     * Enum to indicate what was updated by a channel command.
     */
    private enum WhatUpdated {
        NOTHING,
        LIGHT_MODEL,
        OTHER
    }

    public ShellyLightHandler(final Thing thing, final ShellyTranslationProvider translationProvider,
            final ShellyBindingRuntimeConfig bindingConfig, final ShellyThingTable thingTable,
            final Shelly1CoapServer coapServer, final HttpClient httpClient, WebSocketClient webSocketClient) {
        super(thing, translationProvider, bindingConfig, thingTable, coapServer, httpClient, webSocketClient);
    }

    @Override
    public void initialize() {
        logger.debug("Thing is using {}", this.getClass());
        super.initialize();
    }

    @Override
    public boolean handleDeviceCommand(ChannelUID channelUID, Command command) throws IllegalArgumentException {
        logger.trace("{}: handleDeviceCommand() channel {}, command {}", thingName, channelUID, command);
        String groupName = getString(channelUID.getGroupId());
        if (groupName.isEmpty()) {
            throw new IllegalArgumentException("Empty groupName");
        }
        try {
            acquireLock();
            try {
                int componentIndex = getLightIdFromGroup(groupName);
                ShellyLightModel model = lightModels.get(componentIndex);
                if (model == null) {
                    model = ShellyLightModel.create(this, componentIndex, profile, DIM_STEPSIZE);
                    model.acquire();
                    lightModels.put(componentIndex, model);
                }
                WhatUpdated whatUpdated = updateLightModelFromChannelCommand(model, channelUID, command);
                switch (whatUpdated) {
                    case LIGHT_MODEL:
                        updateRemoteDeviceFromLightModel(model);
                    case OTHER:
                        return true;
                    default:
                        return false;
                }
            } finally {
                releaseLock();
            }
        } catch (ShellyApiException e) {
            logger.debug("{}: Unable to handle command: {}", thingName, e.toString());
            return false;
        }
    }

    @Override
    public boolean updateDeviceStatus(ShellySettingsStatus genericStatus) throws ShellyApiException {
        if (!profile.isInitialized()) {
            logger.debug("{}: Device not yet initialized!", thingName);
            return false;
        }
        if (!profile.isLight) {
            logger.debug("{}: ERROR: Device is not a light but class ShellyHandlerLight is called!", thingName);
        }
        ShellyStatusLight status = api.getLightStatus();
        if (logger.isTraceEnabled()) {
            logger.trace("{}: updateDeviceStatus() called with {}", thingName, new Gson().toJson(status));
        }
        boolean updated = false;
        try {
            acquireLock();
            int componentIndex = 0;
            for (ShellyStatusLightChannel light : status.lights) {
                ShellyLightModel model = lightModels.get(componentIndex);
                if (model == null) {
                    model = ShellyLightModel.create(this, componentIndex, profile, DIM_STEPSIZE);
                    model.acquire();
                    lightModels.put(componentIndex, model);
                }
                updateLightModelFromStatus(model, light);
                updated |= updateChannelsFromLightStatusDTO(light, componentIndex);
                componentIndex++;
            }
        } finally {
            updated |= releaseLock();
        }
        return updated;
    }

    private void createLightChannels(ShellyStatusLightChannel status, int idx) {
        if (!areChannelsCreated()) {
            updateChannelDefinitions(ShellyChannelDefinitions.createLightChannels(getThing(), profile, status, idx));
        }
    }

    private static int setColor(Command command, Integer min, Integer max) {
        if (command instanceof PercentType pct) {
            return (int) Math.round(min + (pct.doubleValue() * (max - min) / 100));
        }
        if (command instanceof DecimalType dec) {
            return (int) Math.max(min, Math.min(max, dec.intValue()));
        }
        if (command instanceof OnOffType onOff) {
            return OnOffType.OFF == onOff ? min : max;
        }
        throw new IllegalArgumentException("Invalid command type: " + command.getClass().getName());
    }

    /**
     * PHASE 1: Updates the light model from the incoming command (write before read)
     *
     * @param model the light model to update
     * @param channelUID the channel UID of the command
     * @param command the command to handle
     * @return the target of the update (LIGHT_MODEL, OTHER, or NONE)
     * @throws ShellyApiException
     */
    private WhatUpdated updateLightModelFromChannelCommand(ShellyLightModel model, ChannelUID channelUID,
            Command command) throws ShellyApiException {
        logger.trace("{}: updateLightModelFromChannelCommand() channel {}, command {})", thingName, channelUID,
                command);
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_PRIMARY_COLOR:
            case CHANNEL_PRIMARY_BRIGHTNESS:
                model.handleCommand(command);
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_PRIMARY_COLOR_TEMP:
            case CHANNEL_PRIMARY_COLOR_TEMP_ABS:
                model.handleColorTemperatureCommand(command);
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_LIGHT_POWER:
                model.handleCommand(command);
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_LIGHT_COLOR_MODE:
                model.setMode(OnOffType.ON == command ? Mode.COLOR : Mode.WHITE);
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_COLOR_PICKER:
                model.handleCommand(command);
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_COLOR_FULL:
                model.setRGBX(command);
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_COLOR_RED:
                model.setColor(R, setColor(command, SHELLY_MIN_COLOR, SHELLY_MAX_COLOR));
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_COLOR_GREEN:
                model.setColor(G, setColor(command, SHELLY_MIN_COLOR, SHELLY_MAX_COLOR));
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_COLOR_BLUE:
                model.setColor(B, setColor(command, SHELLY_MIN_COLOR, SHELLY_MAX_COLOR));
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_COLOR_WHITE:
                model.setColor(CW, setColor(command, SHELLY_MIN_COLOR, SHELLY_MAX_COLOR));
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_COLOR_GAIN:
                model.setGain(command);
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_BRIGHTNESS:
                model.setBrightness(command);
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_COLOR_TEMP:
                model.handleColorTemperatureCommand(command);
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_COLOR_EFFECT:
                model.setEffect(setColor(command, SHELLY_MIN_EFFECT, SHELLY_MAX_EFFECT));
                return WhatUpdated.LIGHT_MODEL;

            case CHANNEL_TIMER_AUTOON:
                api.setAutoTimer(model.getComponentIndex(), SHELLY_TIMER_AUTOON, getNumber(command).doubleValue());
                return WhatUpdated.OTHER;

            case CHANNEL_TIMER_AUTOOFF:
                api.setAutoTimer(model.getComponentIndex(), SHELLY_TIMER_AUTOOFF, getNumber(command).doubleValue());
                return WhatUpdated.OTHER;

            default: // non- light commands will be handled by the generic handler
                return WhatUpdated.NOTHING;
        }
    }

    /**
     * PHASE 2: Updates the device via the API from the final light model state (read after write)
     *
     * @param model the light model to update
     * @throws ShellyApiException if the API call fails
     */
    public void updateRemoteDeviceFromLightModel(ShellyLightModel model) throws ShellyApiException {
        logger.trace("{}: updateRemoteDeviceFromLightModel() with [{}]", thingName, model);
        boolean apiCommandSent = false;

        // ON-OFF (via own channel): send first as it may affect the processing of subsequent parameters
        if (model.supportsOnOffChannel() && model.isOnOffDirty()) { // config.getBrightnessAutoOn() not used
            api.setLightTurn(model.getComponentIndex(),
                    OnOffType.ON == model.getOnOff(true) ? SHELLY_API_ON : SHELLY_API_OFF);
            apiCommandSent = true;
        }

        // MODE: send this next as it also may affect the processing of subsequent parameters
        if (model.isModeDirty()) {
            api.setLightMode(Mode.COLOR == model.getMode() ? SHELLY_MODE_COLOR : SHELLY_MODE_WHITE);
            apiCommandSent = true;
        }

        // map of changed light parameters to send to the device
        Map<String, String> parms = new TreeMap<>();

        // ON-OFF (via white channel):
        if (model.supportsOnOffViaBrightnessChannel() && model.isOnOffDirty()) {
            parms.put(SHELLY_LIGHT_TURN, OnOffType.ON == model.getOnOff(true) ? SHELLY_API_ON : SHELLY_API_OFF);
        }

        // COLOR:
        if (model.supportsColorChannel() && model.isColorDirty()) {
            int[] rgbw = model.getRGBX();
            parms.put(SHELLY_COLOR_RED, String.valueOf(rgbw[0]));
            parms.put(SHELLY_COLOR_GREEN, String.valueOf(rgbw[1]));
            parms.put(SHELLY_COLOR_BLUE, String.valueOf(rgbw[2]));
            if (rgbw.length == 4) {
                parms.put(SHELLY_COLOR_WHITE, String.valueOf(rgbw[3]));
            }
        }

        // GAIN:
        if (model.supportsGainChannel() && model.isGainDirty() && model.getGainState() instanceof PercentType pct) {
            parms.put(SHELLY_COLOR_GAIN, String.valueOf(pct.intValue()));
        }

        // EFFECT:
        if (model.supportsEffectChannel() && model.isEffectDirty()
                && model.getEffectState() instanceof DecimalType dec) {
            parms.put(SHELLY_COLOR_EFFECT, String.valueOf(dec.intValue()));
        }

        // BRIGHTNESS:
        if (model.supportsBrightnessChannel() && model.isBrightnessDirty()
                && model.getBrightnessState() instanceof PercentType pct) {
            parms.put(SHELLY_COLOR_BRIGHTNESS, String.valueOf(pct.intValue()));
        }

        // COLOR TEMP:
        if (model.supportsColorTempChannel() && model.isColorTempDirty()
                && model.getColorTemperatureAbsoluteState() instanceof QuantityType<?> qty) {
            parms.put(SHELLY_COLOR_TEMP, String.valueOf(qty.intValue()));
        }

        if (!parms.isEmpty()) {
            logger.debug("{}: lightId {} set new light parameters {}", thingName, model.getComponentIndex(), parms);
            api.setLightParms(model.getComponentIndex(), parms);
            apiCommandSent = true;
        }

        /*
         * always request a status update after sending a command to ensure the model is in sync with the
         * device, and to update any related cross linked channels that may need to be changed
         */
        if (apiCommandSent) {
            requestUpdates(1, false);
        }
    }

    /**
     * PHASE 1: Updates the light model from the incoming status DTO (write before read)
     *
     * @param model the light model to update
     * @param light the incoming light status DTO
     */
    private void updateLightModelFromStatus(ShellyLightModel model, ShellyStatusLightChannel light) {
        if (logger.isTraceEnabled()) {
            logger.trace("{}: updateLightModelFromStatus() with {}", thingName, new Gson().toJson(light));
        }

        // COLOR: this may change model's mode
        if (light.red != null && light.green != null && light.blue != null) {
            if (light.white != null) {
                model.setRGBX(new int[] { light.red, light.green, light.blue, light.white });
            } else {
                model.setRGBX(new int[] { light.red, light.green, light.blue });
            }
        }

        // GAIN: this may change model's mode and on-off state
        if (light.gain != null) {
            model.setGain(getInteger(light.gain));
        }

        // EFFECT:
        if (light.effect != null) {
            model.setEffect(getInteger(light.effect));
        }

        // BRIGHTNESS: this may change model's mode and on-off state
        if (light.brightness != null) {
            model.setBrightness(getInteger(light.brightness));
        }

        // COLOR TEMP: this may change model's mode
        if (light.temp != null) {
            model.setColorTemp(getInteger(light.temp));
        }

        // MODE: setters may have updated the light model's mode
        // i.e. do nothing

        // ON-OFF: setters may have updated the light model's state so do this last
        if (light.ison != null) {
            model.setOnOff(light.ison);
        }
    }

    /**
     * PHASE 2: Updates the channels from the incoming light status DTO (write before read)
     *
     * @param light the incoming light status DTO
     * @param lightId the light Id
     * @return true if any channel was updated, false otherwise
     */
    private boolean updateChannelsFromLightStatusDTO(ShellyStatusLightChannel light, int lightId) {
        if (logger.isTraceEnabled()) {
            logger.trace("{}: updateChannelsFromLightStatusDTO() with {}", thingName, new Gson().toJson(light));
        }
        boolean updated = false;
        Integer channelId = lightId + 1;
        createLightChannels(light, lightId);

        // TIMERS:
        List<ShellySettingsRgbwLight> lights = profile.settings.lights;
        if (lights != null && lightId < lights.size() && lights.get(lightId) instanceof ShellySettingsRgbwLight ls) {
            String group = buildControlGroupName(profile, channelId);
            updated |= updateChannel(group, CHANNEL_TIMER_AUTOON, toQuantityType(getDouble(ls.autoOn), Units.SECOND));
            updated |= updateChannel(group, CHANNEL_TIMER_AUTOOFF, toQuantityType(getDouble(ls.autoOff), Units.SECOND));
            updated |= updateChannel(group, CHANNEL_TIMER_ACTIVE, getOnOff(light.hasTimer));
        }

        // OVERPOWER:
        if (getBool(light.overpower)) {
            postEvent(ALARM_TYPE_OVERPOWER, false);
        }

        return updated;
    }

    /**
     * PHASE 3: Updates the channels from the final light model state (read after write)
     *
     * @param model the light model to update
     *
     * @return true if any channel was updated, false otherwise
     */
    public boolean updateChannelsFromLightModel(ShellyLightModel model) {
        logger.trace("{}: updateDirtyChannelsForLightModel() with [{}]", thingName, model);
        boolean updated = false;
        int lightId = model.getComponentIndex(); // zero based
        int channelId = lightId + 1; // one based
        String group = null;

        // ON-OFF:
        // New supportsOnOffViaBrightnessChannel() devices don't have an own on-off channel to be updated, but
        // maybe legacy devices did, in which case this ensures non breaking of items linked to such channels
        if ((model.supportsOnOffChannel() || model.supportsOnOffViaBrightnessChannel()) && model.isOnOffDirty()) {
            group = buildControlGroupName(profile, channelId);
            updated |= updateChannel(group, CHANNEL_LIGHT_POWER, model.getOnOffState());
        }

        // MODE:
        if (model.isModeDirty()) {
            group = CHANNEL_GROUP_LIGHT_CONTROL;
            updated |= updateChannel(group, CHANNEL_LIGHT_COLOR_MODE, model.getModeState());
        }

        // COLOR:
        if (model.supportsColorChannel() && model.isColorDirty()) {
            group = CHANNEL_GROUP_COLOR_CONTROL;
            updated |= updateChannel(group, CHANNEL_COLOR_RED, model.getColorState(R));
            updated |= updateChannel(group, CHANNEL_COLOR_GREEN, model.getColorState(G));
            updated |= updateChannel(group, CHANNEL_COLOR_BLUE, model.getColorState(B));
            if (model.getRGBx().length == 4) {
                updated |= updateChannel(group, CHANNEL_COLOR_WHITE, model.getColorState(CW));
            }
            updated |= updateChannel(group, CHANNEL_COLOR_PICKER, model.getColorState());
            updated |= updateChannel(group, CHANNEL_COLOR_FULL, model.getFullColorState());

        }

        // GAIN:
        if (model.supportsGainChannel() && model.isGainDirty()) {
            group = CHANNEL_GROUP_COLOR_CONTROL;
            updated |= updateChannel(group, CHANNEL_COLOR_GAIN, model.getGainState());
        }

        // EFFECT:
        if (model.supportsEffectChannel() && model.isEffectDirty()) {
            group = CHANNEL_GROUP_COLOR_CONTROL;
            updated |= updateChannel(group, CHANNEL_COLOR_EFFECT, model.getEffectState());
        }

        // BRIGHTNESS:
        if (model.supportsBrightnessChannel() && model.isBrightnessDirty()) {
            group = buildWhiteGroupName(profile, channelId);
            updated |= updateChannel(group, CHANNEL_BRIGHTNESS, model.getBrightnessState());
        }

        // COLOR TEMP:
        if (model.supportsColorTempChannel() && model.isColorTempDirty()) {
            group = buildWhiteGroupName(profile, channelId);
            updated |= updateChannel(group, CHANNEL_COLOR_TEMP, model.getColorTemperaturePercentState());
        }

        // PRIMARY GROUP:
        if (model.isDirty()) {
            group = CHANNEL_GROUP_PRIMARY;
            if (model.configGetLightCapabilities().supportsColor()) {
                updated |= updateChannel(group, CHANNEL_PRIMARY_COLOR, model.getColorState());
            } else if (model.configGetLightCapabilities().supportsBrightness()) {
                updated |= updateChannel(group, CHANNEL_PRIMARY_BRIGHTNESS, model.getBrightnessState());
            }
            if (model.configGetLightCapabilities().supportsColorTemperature()) {
                updated |= updateChannel(group, CHANNEL_PRIMARY_COLOR_TEMP, model.getColorTemperaturePercentState());
                updated |= updateChannel(group, CHANNEL_PRIMARY_COLOR_TEMP_ABS,
                        model.getColorTemperatureAbsoluteState());
            }
        }

        return updated;
    }

    @Override
    public @Nullable ShellyLightModel getLightModel(int componentIndex) {
        ShellyLightModel model = lightModels.get(componentIndex);
        logger.debug("{}: getLightModel({}) returns {}", thingName, componentIndex, model);
        return model;
    }

    @Override
    public void acquireLock() {
        for (ShellyLightModel model : lightModels.values()) {
            model.acquire();
        }
        logger.debug("{}: all light models acquired", thingName);
    }

    @Override
    public boolean releaseLock() {
        boolean result = false;
        for (ShellyLightModel model : lightModels.values()) {
            result |= model.release();
        }
        logger.debug("{}: all light models released", thingName);
        return result;
    }
}
