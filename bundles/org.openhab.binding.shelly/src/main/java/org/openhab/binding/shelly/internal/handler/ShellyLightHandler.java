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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortLightStatus;
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
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyLightHandler} handles light (Bulb, Duo and RGBW2) specific commands and status. All other commands
 * will be routed of the ShellyBaseHandler.
 *
 * @author Markus Michels - Initial contribution
 * @author Andrew Fiddian-Green - Migrate to LightModel
 */
@NonNullByDefault
public class ShellyLightHandler extends ShellyBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(ShellyLightHandler.class);
    private final Map<Integer, ShellyLightModel> lightModels; // TODO do we need multiple LightModel instances?

    public ShellyLightHandler(final Thing thing, final ShellyTranslationProvider translationProvider,
            final ShellyBindingRuntimeConfig bindingConfig, final ShellyThingTable thingTable,
            final Shelly1CoapServer coapServer, final HttpClient httpClient, WebSocketClient webSocketClient) {
        super(thing, translationProvider, bindingConfig, thingTable, coapServer, httpClient, webSocketClient);
        lightModels = new TreeMap<>();
    }

    @Override
    public void initialize() {
        logger.debug("Thing is using  {}", this.getClass());
        super.initialize();
    }

    @Override
    public boolean handleDeviceCommand(ChannelUID channelUID, Command command) throws IllegalArgumentException {
        String groupName = getString(channelUID.getGroupId());
        if (groupName.isEmpty()) {
            throw new IllegalArgumentException("Empty groupName");
        }

        int lightId = getLightIdFromGroup(groupName);
        ShellyLightModel model = getOrCreateLightModel(lightId);
        String oldModelString = logger.isTraceEnabled() ? model.toString() : null;
        logger.debug("{}: Checking lightId {} channel {} command {}", thingName, lightId, channelUID, command);
        try {
            switch (channelUID.getIdWithoutGroup()) {
                default: // non-bulb commands will be handled by the generic handler
                    return false;

                case CHANNEL_PRIMARY_COLOR:
                case CHANNEL_PRIMARY_BRIGHTNESS:
                    model.handleCommand(command);
                    break;

                case CHANNEL_PRIMARY_COLOR_TEMP:
                case CHANNEL_PRIMARY_COLOR_TEMP_ABS:
                    model.handleColorTemperatureCommand(command);
                    break;

                case CHANNEL_LIGHT_POWER:
                    api.setLightParm(lightId, SHELLY_LIGHT_TURN,
                            OnOffType.ON == command ? SHELLY_API_ON : SHELLY_API_OFF);
                    model.handleCommand(command);
                    requestUpdates(1, false);
                    return true;

                case CHANNEL_LIGHT_COLOR_MODE:
                    model.setMode(OnOffType.ON == command ? Mode.COLOR : Mode.COLOR_TEMP);
                    break;

                case CHANNEL_COLOR_PICKER:
                    model.handleCommand(command);
                    break;

                case CHANNEL_COLOR_FULL:
                    model.setFullColorCommand(command);
                    break;

                case CHANNEL_COLOR_RED:
                    model.setColor(R, setColor(command, SHELLY_MIN_COLOR, SHELLY_MAX_COLOR));
                    break;

                case CHANNEL_COLOR_GREEN:
                    model.setColor(G, setColor(command, SHELLY_MIN_COLOR, SHELLY_MAX_COLOR));
                    break;

                case CHANNEL_COLOR_BLUE:
                    model.setColor(B, setColor(command, SHELLY_MIN_COLOR, SHELLY_MAX_COLOR));
                    break;

                case CHANNEL_COLOR_WHITE:
                    model.setColor(WC, setColor(command, SHELLY_MIN_COLOR, SHELLY_MAX_COLOR));
                    break;

                case CHANNEL_COLOR_GAIN:
                    model.setGain(setColor(command, SHELLY_MIN_GAIN, SHELLY_MAX_GAIN));
                    break;

                case CHANNEL_BRIGHTNESS:
                    model.setBrightness(setColor(command, SHELLY_MIN_BRIGHTNESS, SHELLY_MAX_BRIGHTNESS));
                    break;

                case CHANNEL_COLOR_TEMP:
                    model.handleColorTemperatureCommand(command);
                    break;

                case CHANNEL_COLOR_EFFECT:
                    model.setEffect(setColor(command, SHELLY_MIN_EFFECT, SHELLY_MAX_EFFECT));
                    break;
            }

            if (model.isModeDirty()) {
                api.setLightMode(Mode.COLOR == model.getMode() ? SHELLY_MODE_COLOR : SHELLY_MODE_WHITE);
            }

            if (model.isOnOffDirty()) {
                // TODO what is the difference between api.setLightTurn and api.setLightParm ?
                ShellyShortLightStatus light = //
                        api.setLightTurn(lightId, OnOffType.ON == model.getOnOff() ? SHELLY_API_ON : SHELLY_API_OFF);
                model.setOnOff(light.ison);
                model.setBrightness(light.brightness.intValue());
            }

            // send changed light parameters (if any) to the device
            sendParameters(profile, lightId, model, config.getBrightnessAutoOn());

            // TODO do we need to update cross dependent channels (or other channels)?
            // TODO do we need to set auto-update mode on channels?

            return true;
        } catch (ShellyApiException e) {
            logger.debug("{}: Unable to handle command: {}", thingName, e.toString());
            return false;
        } catch (IllegalArgumentException e) {
            logger.debug("{}: Unable to handle command", thingName, e);
            return false;
        } finally {
            if (model.isDirty()) {
                logger.debug("{}: Finished lightId {} channel {} command {}", thingName, lightId, channelUID, command);
                logger.trace("/nOld: [{}]/nNew: [{}]", oldModelString, model);
            }
            model.clearDirtyFlags();
        }
    }

    private ShellyLightModel getOrCreateLightModel(int lightId) {
        ShellyLightModel model = lightModels.get(lightId);
        boolean isNew = false;
        if (model == null) {
            model = ShellyLightModel.create(thing.getThingTypeUID(), profile, DIM_STEPSIZE); // create a new entry
            lightModels.put(lightId, model);
            isNew = true;
        }
        logger.trace("{}: {} lightId {} light model [{}]", thingName, isNew ? "Created" : "Loaded", lightId, model);
        return model;
    }

    @Override
    public boolean updateDeviceStatus(ShellySettingsStatus genericStatus) throws ShellyApiException {
        if (!profile.isInitialized()) {
            logger.debug("{}: Device not yet initialized!", thingName);
            return false;
        }
        if (!profile.isLight) {
            logger.debug("{}: ERROR: Device is not a light. but class ShellyHandlerLight is called!", thingName);
        }

        ShellyStatusLight status = api.getLightStatus();
        logger.trace("{}: Updating light status in {} mode, {} channel(s)", thingName, profile.device.mode,
                status.lights.size());

        // In white mode we have multiple channels
        int lightId = 0;
        boolean updated = false;
        for (ShellyStatusLightChannel light : status.lights) {
            Integer channelId = lightId + 1;
            String controlGroup = buildControlGroupName(profile, channelId);
            createLightChannels(light, lightId);

            ShellyLightModel model = getOrCreateLightModel(lightId);

            // The bulb has a combined channel set for color or white mode
            // The RGBW2 uses 2 different thing types: color=1 channel, white=4 channel
            if (profile.isBulb) {
                Mode mode = SHELLY_MODE_COLOR.equals(profile.device.mode) ? Mode.COLOR : Mode.COLOR_TEMP;
                model.setMode(mode);
                updateChannel(CHANNEL_GROUP_LIGHT_CONTROL, CHANNEL_LIGHT_COLOR_MODE,
                        OnOffType.from(Mode.COLOR == mode));
            }

            model.setOnOff(light.ison);

            List<ShellySettingsRgbwLight> lights = profile.settings.lights;
            if (lights != null) {
                // Channel control/timer
                ShellySettingsRgbwLight ls = lights.get(lightId);
                updated |= updateChannel(controlGroup, CHANNEL_TIMER_AUTOON,
                        toQuantityType(getDouble(ls.autoOn), Units.SECOND));
                updated |= updateChannel(controlGroup, CHANNEL_TIMER_AUTOOFF,
                        toQuantityType(getDouble(ls.autoOff), Units.SECOND));
                updated |= updateChannel(controlGroup, CHANNEL_LIGHT_POWER, model.getOnOffState());
                updated |= updateChannel(controlGroup, CHANNEL_TIMER_ACTIVE, getOnOff(light.hasTimer));
            }

            if (getBool(light.overpower)) {
                postEvent(ALARM_TYPE_OVERPOWER, false);
            }

            if (profile.inColor) {
                // TODO check logic for all light types
                logger.trace("{}: update color settings", thingName);
                model.setRGBX(getInteger(light.red), getInteger(light.green), getInteger(light.blue),
                        getInteger(light.white));
                model.setGain(getInteger(light.gain));
                model.setEffect(getInteger(light.effect));

                String colorGroup = CHANNEL_GROUP_COLOR_CONTROL;
                logger.trace("{}: Update channels for group {} => {}", thingName, colorGroup, model);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_RED, model.getColorState(R));
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_GREEN, model.getColorState(G));
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_BLUE, model.getColorState(B));
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_WHITE, model.getColorState(WC));
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_GAIN, model.getGainState());
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_EFFECT, model.getEffectState());
                setFullColor(colorGroup, model);

                logger.trace("{}: update {}.color picker", thingName, colorGroup);
                updated |= updateChannel(colorGroup, CHANNEL_COLOR_PICKER, model.getColorState());
            }

            if ((!profile.inColor && (!profile.isGen2 || profile.isRGBW2)) || profile.isBulb) {
                // TODO check logic for all light types
                String whiteGroup = buildWhiteGroupName(profile, channelId);
                model.setBrightness(getInteger(light.brightness));
                updated |= updateChannel(whiteGroup, CHANNEL_BRIGHTNESS + "$Switch", model.getOnOffState());
                updated |= updateChannel(whiteGroup, CHANNEL_BRIGHTNESS + "$Value", model.getBrightnessState());

                if ((profile.isBulb || profile.isDuo) && (light.temp != null)) {
                    // TODO check logic for all light types
                    model.setColorTemp(getInteger(light.temp));
                    updated |= updateChannel(whiteGroup, CHANNEL_COLOR_TEMP, model.getColorTemperaturePercentState());
                    logger.trace("{}: update {}.color picker", thingName, whiteGroup);
                    updated |= updateChannel(whiteGroup, CHANNEL_COLOR_PICKER, model.getColorState());
                }
            }

            updated |= updateChannel(CHANNEL_GROUP_PRIMARY, CHANNEL_PRIMARY_COLOR, model.getColorState());
            updated |= updateChannel(CHANNEL_GROUP_PRIMARY, CHANNEL_PRIMARY_BRIGHTNESS, model.getBrightnessState());
            updated |= updateChannel(CHANNEL_GROUP_PRIMARY, CHANNEL_PRIMARY_COLOR_TEMP,
                    model.getColorTemperaturePercentState());
            updated |= updateChannel(CHANNEL_GROUP_PRIMARY, CHANNEL_PRIMARY_COLOR_TEMP_ABS,
                    model.getColorTemperatureAbsoluteState());

            // continue with next light
            lightId++;
        }
        return updated;
    }

    private void createLightChannels(ShellyStatusLightChannel status, int idx) {
        if (!areChannelsCreated()) {
            updateChannelDefinitions(ShellyChannelDefinitions.createLightChannels(getThing(), profile, status, idx));
        }
    }

    public static int setColor(Command command, Integer min, Integer max) {
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

    private void setFullColor(String colorGroup, ShellyLightModel model) {
        double[] rgbw = model.getRGBx();
        String color = null;
        if (rgbw[0] == SHELLY_MAX_COLOR && rgbw[1] == SHELLY_MAX_COLOR && rgbw[2] == 0) {
            color = SHELLY_COLOR_YELLOW;
        } else if (rgbw[0] == SHELLY_MAX_COLOR && rgbw[1] == 0 && rgbw[2] == 0) {
            color = SHELLY_COLOR_RED;
        } else if (rgbw[0] == 0 && rgbw[1] == SHELLY_MAX_COLOR && rgbw[2] == 0) {
            color = SHELLY_COLOR_GREEN;
        } else if (rgbw[0] == 0 && rgbw[1] == 0 && rgbw[2] == SHELLY_MAX_COLOR) {
            color = SHELLY_COLOR_BLUE;
        } else if (rgbw[0] == 0 && rgbw[1] == 0 && rgbw[2] == 0 && rgbw[2] == SHELLY_MAX_COLOR) {
            color = SHELLY_COLOR_WHITE;
        }
        updateChannel(colorGroup, CHANNEL_COLOR_FULL, color != null ? new StringType(color) : UnDefType.UNDEF);
    }

    private void sendParameters(ShellyDeviceProfile profile, Integer lightId, ShellyLightModel model, boolean autoOn)
            throws ShellyApiException {
        Map<String, String> parms = new TreeMap<>();

        // TODO check autoOn logic
        if (model.isOnOffDirty() && model.getOnOffState() instanceof OnOffType onOff && autoOn) {
            // TODO what is the difference between api.setLightTurn and api.setLightParm and this call ?
            parms.put(SHELLY_LIGHT_TURN, OnOffType.ON == onOff ? SHELLY_API_ON : SHELLY_API_OFF);
        }
        if (model.isColorDirty() && profile.inColor) { // TODO check logic for all light types
            int[] rgbw = model.getRGBX();
            parms.put(SHELLY_COLOR_RED, String.valueOf(rgbw[0]));
            parms.put(SHELLY_COLOR_GREEN, String.valueOf(rgbw[1]));
            parms.put(SHELLY_COLOR_BLUE, String.valueOf(rgbw[2]));
            parms.put(SHELLY_COLOR_WHITE, String.valueOf(rgbw[3]));
        }
        if (model.isColorTempDirty() && model.getColorTemperatureAbsoluteState() instanceof QuantityType<?> qty) {
            parms.put(SHELLY_COLOR_TEMP, String.valueOf(qty.intValue()));
        }
        if (model.isGainDirty() && model.getGainState() instanceof PercentType pct) {
            parms.put(SHELLY_COLOR_GAIN, String.valueOf(pct.intValue()));
        }
        if (model.isBrightnessDirty() && model.getBrightnessState() instanceof PercentType pct) {
            parms.put(SHELLY_COLOR_BRIGHTNESS, String.valueOf(pct.intValue()));
        }
        if (model.isEffectDirty() && model.getEffectState() instanceof DecimalType dec) {
            parms.put(SHELLY_COLOR_EFFECT, String.valueOf(dec.intValue()));
        }
        if (!parms.isEmpty()) {
            logger.debug("{}: lightId {} set new light parameters {}", thingName, lightId, parms);
            api.setLightParms(lightId, parms);
        }
    }

    @Override
    public @Nullable ShellyLightModel getLightModel(int lightId) {
        return lightModels.get(lightId); // TODO do we need multiple LightModel instances?
    }
}
