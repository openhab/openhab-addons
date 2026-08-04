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

        try {
            model.lock(channelUID, command);
            if (updateLightModelFromChannelCommand(model, channelUID, command)) {
                updateRemoteDeviceFromLightModel(model, lightId);
                return true;
            }
            return false;
        } catch (ShellyApiException e) {
            logger.debug("{}: Unable to handle command: {}", thingName, e.toString());
            return false;
        } catch (IllegalArgumentException e) {
            logger.debug("{}: Unable to handle command", thingName, e);
            return false;
        } finally {
            model.unlock();
        }
    }

    private ShellyLightModel getOrCreateLightModel(int lightId) {
        ShellyLightModel model = lightModels.get(lightId);
        if (model == null) {
            // create a new entry
            model = ShellyLightModel.create(thingName, lightId, thing.getThingTypeUID(), profile, DIM_STEPSIZE);
            lightModels.put(lightId, model);
        }
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

        boolean updated = false;

        int lightId = 0;
        for (ShellyStatusLightChannel light : status.lights) {
            ShellyLightModel model = getOrCreateLightModel(lightId);
            try {
                model.lock("updateDeviceStatus", genericStatus.json);
                updateLightModelFromLightStatus(model, light);
                updated |= updateChannelsFromLightModel(model, light, lightId);
            } finally {
                model.unlock();
            }
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

    private boolean setFullColor(String colorGroup, ShellyLightModel model) {
        String color = null;
        int[] rgbw = model.getRGBX();
        if (rgbw[0] == SHELLY_MAX_COLOR && rgbw[1] == SHELLY_MAX_COLOR && rgbw[2] == 0) {
            color = SHELLY_COLOR_YELLOW;
        } else if (rgbw[0] == SHELLY_MAX_COLOR && rgbw[1] == 0 && rgbw[2] == 0) {
            color = SHELLY_COLOR_RED;
        } else if (rgbw[0] == 0 && rgbw[1] == SHELLY_MAX_COLOR && rgbw[2] == 0) {
            color = SHELLY_COLOR_GREEN;
        } else if (rgbw[0] == 0 && rgbw[1] == 0 && rgbw[2] == SHELLY_MAX_COLOR) {
            color = SHELLY_COLOR_BLUE;
        } else if ((rgbw.length == 4 && rgbw[0] == 0 && rgbw[1] == 0 && rgbw[2] == 0 && rgbw[3] == SHELLY_MAX_COLOR)
                || (rgbw.length == 3 && rgbw[0] == SHELLY_MAX_COLOR && rgbw[1] == SHELLY_MAX_COLOR
                        && rgbw[2] == SHELLY_MAX_COLOR)) {
            color = SHELLY_COLOR_WHITE;
        }
        updateChannel(colorGroup, CHANNEL_COLOR_FULL, color != null ? new StringType(color) : UnDefType.UNDEF);
        return true;
    }

    @Override
    public ShellyLightModel getLightModel(int lightId) {
        if (lightModels.get(lightId) instanceof ShellyLightModel model) {
            return model;
        }
        throw new IllegalArgumentException("LightModel for lightId %d does not exist".formatted(lightId));
    }

    /**
     * PHASE 1: Updates the light model from the incoming command (write before read)
     * 
     * @param model the light model to update
     * @param channelUID the channel UID of the command
     * @param command the command to handle
     * @return true if the command was processed, false otherwise
     */
    private boolean updateLightModelFromChannelCommand(ShellyLightModel model, ChannelUID channelUID, Command command) {
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
                model.handleCommand(command);
                break;

            case CHANNEL_LIGHT_COLOR_MODE:
                model.setMode(OnOffType.ON == command ? Mode.COLOR : Mode.COLOR_TEMP);
                break;

            case CHANNEL_COLOR_PICKER:
                model.handleCommand(command);
                break;

            case CHANNEL_COLOR_FULL:
                model.setRGBX(command);
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
                model.setColor(CW, setColor(command, SHELLY_MIN_COLOR, SHELLY_MAX_COLOR));
                break;

            case CHANNEL_COLOR_GAIN:
                model.setGain(command);
                break;

            case CHANNEL_BRIGHTNESS:
                model.setBrightness(command);
                break;

            case CHANNEL_COLOR_TEMP:
                model.handleColorTemperatureCommand(command);
                break;

            case CHANNEL_COLOR_EFFECT:
                model.setEffect(setColor(command, SHELLY_MIN_EFFECT, SHELLY_MAX_EFFECT));
                break;
        }
        return true;
    }

    /**
     * PHASE 2: Updates the device via the API from the final light model state (read after write)
     * 
     * @param model the light model to update
     * @param lightId the light ID
     * @throws ShellyApiException if the API call fails
     */
    private void updateRemoteDeviceFromLightModel(ShellyLightModel model, int lightId) throws ShellyApiException {
        // POWER:
        /**
         * 
         * TODO: INFORMATION FOR CODE REVIEWER RELATING TO THE PRIOR IMPLEMENTATION OF POWER ON/OFF COMMANDS
         * 
         * The prior code used three different API power on/off commands in response to different channel commands
         * as follows:
         * 
         * - CHANNEL_LIGHT_POWER sends api.setLightTurn(lightId, ..)
         * - CHANNEL_BRIGHTNESS sends api.setLightParm(lightId, SHELLY_LIGHT_TURN, ..)
         * - CHANNEL_COLOR_PICKER sends parms.put(SHELLY_LIGHT_TURN, ..)
         * - and .. probably CHANNEL_COLOR_GAIN _should_ send api.setLightParm(lightId, SHELLY_LIGHT_TURN, ..)
         * 
         * By contrast the new LightModel allows any channel input (primary, power, brightness, gain, color picker,
         * etc.) to change the model's power on/off state in a consistent manner regardless of which channel was used.
         * So therefore we use a single API call to set the power state based on the model's final state.
         */
        boolean apiCommandSent = false;

        // MODE:
        if (profile.isBulb && model.isModeDirty()) {
            api.setLightMode(Mode.COLOR == model.getMode() ? SHELLY_MODE_COLOR : SHELLY_MODE_WHITE);
            apiCommandSent = true;
        }

        // map of changed light parameters to send to the device
        Map<String, String> parms = new TreeMap<>();

        // POWER:
        if (model.isOnOffDirty()) { // note: config.getBrightnessAutoOn() is no longer needed
            // common code to set the power state regardless of which channel caused it to change
            String onOffString = OnOffType.ON == model.getOnOff(true) ? SHELLY_API_ON : SHELLY_API_OFF;
            parms.put(SHELLY_LIGHT_TURN, onOffString);
        }

        // COLOR:
        if (profile.inColor) {
            if (model.isColorDirty()) {
                int[] rgbw = model.getRGBX();
                parms.put(SHELLY_COLOR_RED, String.valueOf(rgbw[0]));
                parms.put(SHELLY_COLOR_GREEN, String.valueOf(rgbw[1]));
                parms.put(SHELLY_COLOR_BLUE, String.valueOf(rgbw[2]));
                if (rgbw.length == 4) {
                    parms.put(SHELLY_COLOR_WHITE, String.valueOf(rgbw[3]));
                }
            }
            if (model.isGainDirty() && model.getGainState() instanceof PercentType pct) {
                parms.put(SHELLY_COLOR_GAIN, String.valueOf(pct.intValue()));
            }
            if (model.isEffectDirty() && model.getEffectState() instanceof DecimalType dec) {
                parms.put(SHELLY_COLOR_EFFECT, String.valueOf(dec.intValue()));
            }
        }

        // WHITE:
        if ((!profile.inColor && (!profile.isGen2 || profile.isRGBW2)) || profile.isBulb) {
            // TODO maybe just if (!profile.inColor) { ???
            if (model.isBrightnessDirty() && model.getBrightnessState() instanceof PercentType pct) {
                parms.put(SHELLY_COLOR_BRIGHTNESS, String.valueOf(pct.intValue()));
            }
        }

        // COLOR TEMP:
        if ((profile.isBulb || profile.isDuo)) {
            if (model.isColorTempDirty() && model.getColorTemperatureAbsoluteState() instanceof QuantityType<?> qty) {
                parms.put(SHELLY_COLOR_TEMP, String.valueOf(qty.intValue()));
            }
        }

        if (!parms.isEmpty()) {
            logger.debug("{}: lightId {} set new light parameters {}", thingName, lightId, parms);
            api.setLightParms(lightId, parms);
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
    private void updateLightModelFromLightStatus(ShellyLightModel model, ShellyStatusLightChannel light) {
        // ON/OFF:
        model.setOnOff(light.ison);

        // TIMERS & POWER:
        // this will be handled in PHASE 2 updateChannelsFromModel()

        // OVERPOWER:
        // this will be handled in PHASE 2 updateChannelsFromModel()

        // COLOR:
        if (profile.inColor) {
            // note: setters change the model's mode
            if (light.red != null && light.green != null && light.blue != null) {
                if (light.white != null) {
                    model.setRGBX(new int[] { light.red, light.green, light.blue, light.white });
                } else {
                    model.setRGBX(new int[] { light.red, light.green, light.blue });
                }
            }
            model.setGain(getInteger(light.gain));
            model.setEffect(getInteger(light.effect));
        }

        // WHITE:
        if ((!profile.inColor && (!profile.isGen2 || profile.isRGBW2)) || profile.isBulb) {
            // note: setters change the model's mode
            model.setBrightness(getInteger(light.brightness));
        }

        // COLOR TEMP:
        if ((profile.isBulb || profile.isDuo) && (light.temp != null)) {
            // note: setters change the model's mode
            model.setColorTemp(getInteger(light.temp));
        }

        // MODE:
        // note: setters change the model's mode, so do this last to ensure the mode is finally correct
        if (profile.isBulb) {
            Mode mode = SHELLY_MODE_COLOR.equals(profile.device.mode) ? Mode.COLOR : Mode.COLOR_TEMP;
            model.setMode(mode);
        }
    }

    /**
     * PHASE 2: Updates the channels from the final light model state (read after write)
     * 
     * @param model the light model to update
     * @param light the incoming light status DTO
     * @param lightId the light ID
     *
     * @return true if any channel was updated, false otherwise
     */
    private boolean updateChannelsFromLightModel(ShellyLightModel model, ShellyStatusLightChannel light, int lightId) {
        boolean updated = false;
        Integer channelId = lightId + 1;
        createLightChannels(light, lightId);
        String group;

        // MODE:
        if (profile.isBulb) {
            group = CHANNEL_GROUP_LIGHT_CONTROL;
            updated |= updateChannel(group, CHANNEL_LIGHT_COLOR_MODE, model.getModeState());
        }

        // ON/OFF:
        // TIMERS & POWER:
        List<ShellySettingsRgbwLight> lights = profile.settings.lights;
        if (lights != null && lights.get(lightId) instanceof ShellySettingsRgbwLight ls) {
            group = buildControlGroupName(profile, channelId);
            updated |= updateChannel(group, CHANNEL_TIMER_AUTOON, toQuantityType(getDouble(ls.autoOn), Units.SECOND));
            updated |= updateChannel(group, CHANNEL_TIMER_AUTOOFF, toQuantityType(getDouble(ls.autoOff), Units.SECOND));
            updated |= updateChannel(group, CHANNEL_LIGHT_POWER, model.getOnOffState());
            updated |= updateChannel(group, CHANNEL_TIMER_ACTIVE, getOnOff(light.hasTimer));
        }

        // OVERPOWER:
        if (getBool(light.overpower)) {
            postEvent(ALARM_TYPE_OVERPOWER, false);
        }

        // COLOR:
        if (profile.inColor) {
            group = CHANNEL_GROUP_COLOR_CONTROL;
            updated |= updateChannel(group, CHANNEL_COLOR_RED, model.getColorState(R));
            updated |= updateChannel(group, CHANNEL_COLOR_GREEN, model.getColorState(G));
            updated |= updateChannel(group, CHANNEL_COLOR_BLUE, model.getColorState(B));
            updated |= updateChannel(group, CHANNEL_COLOR_WHITE, model.getColorState(CW));
            updated |= updateChannel(group, CHANNEL_COLOR_GAIN, model.getGainState());
            updated |= updateChannel(group, CHANNEL_COLOR_EFFECT, model.getEffectState());
            updated |= updateChannel(group, CHANNEL_COLOR_PICKER, model.getColorState());
            updated |= setFullColor(group, model);
        }

        // WHITE:
        if ((!profile.inColor && (!profile.isGen2 || profile.isRGBW2)) || profile.isBulb) {
            group = buildWhiteGroupName(profile, channelId);
            updated |= updateChannel(group, CHANNEL_BRIGHTNESS + "$Switch", model.getOnOffState());
            updated |= updateChannel(group, CHANNEL_BRIGHTNESS + "$Value", model.getBrightnessState());
        }

        // COLOR TEMP:
        if ((profile.isBulb || profile.isDuo) && (light.temp != null)) {
            group = buildWhiteGroupName(profile, channelId);
            updated |= updateChannel(group, CHANNEL_COLOR_TEMP, model.getColorTemperaturePercentState());
            updated |= updateChannel(group, CHANNEL_COLOR_PICKER, model.getColorState());
        }

        // PRIMARY GROUP:
        group = CHANNEL_GROUP_PRIMARY;
        updated |= updateChannel(group, CHANNEL_PRIMARY_COLOR, model.getColorState());
        updated |= updateChannel(group, CHANNEL_PRIMARY_BRIGHTNESS, model.getBrightnessState());
        updated |= updateChannel(group, CHANNEL_PRIMARY_COLOR_TEMP, model.getColorTemperaturePercentState());
        updated |= updateChannel(group, CHANNEL_PRIMARY_COLOR_TEMP_ABS, model.getColorTemperatureAbsoluteState());

        return updated;
    }
}
