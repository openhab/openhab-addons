/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.handler;

import static org.openhab.binding.deconz.internal.BindingConstants.*;
import static org.openhab.binding.deconz.internal.Util.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deconz.internal.DeconzDynamicCommandDescriptionProvider;
import org.openhab.binding.deconz.internal.DeconzDynamicStateDescriptionProvider;
import org.openhab.binding.deconz.internal.Util;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.dto.LightMessage;
import org.openhab.binding.deconz.internal.dto.LightState;
import org.openhab.binding.deconz.internal.types.ResourceType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This light thing doesn't establish any connections, that is done by the bridge Thing.
 *
 * It waits for the bridge to come online, grab the websocket connection and bridge configuration
 * and registers to the websocket connection as a listener.
 *
 * A REST API call is made to get the initial light/rollershutter state.
 *
 * Every light and rollershutter is supported by this Thing, because a unified state is kept
 * in {@link #lightStateCache}. Every field that got received by the REST API for this specific
 * sensor is published to the framework.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class LightThingHandler extends DeconzBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Set.of(THING_TYPE_COLOR_TEMPERATURE_LIGHT,
            THING_TYPE_DIMMABLE_LIGHT, THING_TYPE_COLOR_LIGHT, THING_TYPE_EXTENDED_COLOR_LIGHT, THING_TYPE_ONOFF_LIGHT,
            THING_TYPE_WINDOW_COVERING, THING_TYPE_WARNING_DEVICE, THING_TYPE_DOORLOCK);

    private static final long DEFAULT_COMMAND_EXPIRY_TIME = 250; // in ms
    private static final int BRIGHTNESS_DIM_STEP = 26; // ~ 10%

    private final Logger logger = LoggerFactory.getLogger(LightThingHandler.class);

    private final DeconzDynamicStateDescriptionProvider stateDescriptionProvider;
    private final DeconzDynamicCommandDescriptionProvider commandDescriptionProvider;

    private long lastCommandExpireTimestamp = 0;
    private boolean needsPropertyUpdate = false;

    /**
     * The light state. Contains all possible fields for all supported lights
     */
    private LightState lightStateCache = new LightState();
    private LightState lastCommand = new LightState();
    @Nullable
    private Integer onTime = null; // in 0.1s
    private String colorMode = "";

    // set defaults, we can override them later if we receive better values
    private int ctMax = ZCL_CT_MAX;
    private int ctMin = ZCL_CT_MIN;

    public LightThingHandler(Thing thing, Gson gson, DeconzDynamicStateDescriptionProvider stateDescriptionProvider,
            DeconzDynamicCommandDescriptionProvider commandDescriptionProvider) {
        super(thing, gson, ResourceType.LIGHTS);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.commandDescriptionProvider = commandDescriptionProvider;
    }

    @Override
    public void initialize() {
        if (thing.getThingTypeUID().equals(THING_TYPE_COLOR_TEMPERATURE_LIGHT)
                || thing.getThingTypeUID().equals(THING_TYPE_EXTENDED_COLOR_LIGHT)) {
            try {
                Map<String, String> properties = thing.getProperties();
                String ctMaxString = properties.get(PROPERTY_CT_MAX);
                ctMax = ctMaxString == null ? ZCL_CT_MAX : Integer.parseInt(ctMaxString);
                String ctMinString = properties.get(PROPERTY_CT_MIN);
                ctMin = ctMinString == null ? ZCL_CT_MIN : Integer.parseInt(ctMinString);

                // minimum and maximum are inverted due to mired/kelvin conversion!
                StateDescriptionFragment stateDescriptionFragment = StateDescriptionFragmentBuilder.create()
                        .withMinimum(new BigDecimal(miredToKelvin(ctMax)))
                        .withMaximum(new BigDecimal(miredToKelvin(ctMin))).build();
                stateDescriptionProvider.setDescriptionFragment(
                        new ChannelUID(thing.getUID(), CHANNEL_COLOR_TEMPERATURE), stateDescriptionFragment);
            } catch (NumberFormatException e) {
                needsPropertyUpdate = true;
            }
        }
        ThingConfig thingConfig = getConfigAs(ThingConfig.class);
        colorMode = thingConfig.colormode;

        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_ONTIME)) {
            if (command instanceof QuantityType<?>) {
                QuantityType<?> onTimeSeconds = ((QuantityType<?>) command).toUnit(Units.SECOND);
                if (onTimeSeconds != null) {
                    onTime = 10 * onTimeSeconds.intValue();
                } else {
                    logger.warn("Channel '{}' received command '{}', could not be converted to seconds.", channelUID,
                            command);
                }
            }
            return;
        }

        if (command instanceof RefreshType) {
            valueUpdated(channelUID.getId(), lightStateCache);
            return;
        }

        LightState newLightState = new LightState();
        Boolean currentOn = lightStateCache.on;
        Integer currentBri = lightStateCache.bri;

        switch (channelUID.getId()) {
            case CHANNEL_ALERT:
                if (command instanceof StringType) {
                    newLightState.alert = command.toString();
                } else {
                    return;
                }
                break;
            case CHANNEL_EFFECT:
                if (command instanceof StringType) {
                    // effect command only allowed for lights that are turned on
                    newLightState.on = true;
                    newLightState.effect = command.toString();
                } else {
                    return;
                }
                break;
            case CHANNEL_EFFECT_SPEED:
                if (command instanceof DecimalType) {
                    newLightState.on = true;
                    newLightState.effectSpeed = Util.constrainToRange(((DecimalType) command).intValue(), 0, 10);
                } else {
                    return;
                }
                break;
            case CHANNEL_SWITCH:
            case CHANNEL_LOCK:
                if (command instanceof OnOffType) {
                    newLightState.on = (command == OnOffType.ON);
                } else {
                    return;
                }
                break;
            case CHANNEL_BRIGHTNESS:
            case CHANNEL_COLOR:
                if (command instanceof OnOffType) {
                    newLightState.on = (command == OnOffType.ON);
                } else if (command instanceof IncreaseDecreaseType) {
                    // try to get best value for current brightness
                    int oldBri = currentBri != null ? currentBri
                            : (Boolean.TRUE.equals(currentOn) ? BRIGHTNESS_MAX : BRIGHTNESS_MIN);
                    if (command.equals(IncreaseDecreaseType.INCREASE)) {
                        newLightState.bri = Util.constrainToRange(oldBri + BRIGHTNESS_DIM_STEP, BRIGHTNESS_MIN,
                                BRIGHTNESS_MAX);
                    } else {
                        newLightState.bri = Util.constrainToRange(oldBri - BRIGHTNESS_DIM_STEP, BRIGHTNESS_MIN,
                                BRIGHTNESS_MAX);
                    }
                } else if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;
                    // XY color is the implicit default: Use XY color mode if i) no color mode is set or ii) if the bulb
                    // is in CT mode or iii) already in XY mode. Only if the bulb is in HS mode, use this one.
                    if ("hs".equals(colorMode)) {
                        newLightState.hue = (int) (hsbCommand.getHue().doubleValue() * HUE_FACTOR);
                        newLightState.sat = Util.fromPercentType(hsbCommand.getSaturation());
                    } else {
                        PercentType[] xy = hsbCommand.toXY();
                        if (xy.length < 2) {
                            logger.warn("Failed to convert {} to xy-values", command);
                        }
                        newLightState.xy = new double[] { xy[0].doubleValue() / 100.0, xy[1].doubleValue() / 100.0 };
                    }
                    newLightState.bri = Util.fromPercentType(hsbCommand.getBrightness());
                } else if (command instanceof PercentType) {
                    newLightState.bri = Util.fromPercentType((PercentType) command);
                } else if (command instanceof DecimalType) {
                    newLightState.bri = ((DecimalType) command).intValue();
                } else {
                    return;
                }

                // send on/off state together with brightness if not already set or unknown
                Integer newBri = newLightState.bri;
                if (newBri != null) {
                    newLightState.on = (newBri > 0);
                }

                // fix sending bri=0 when light is already off
                if (newBri != null && newBri == 0 && currentOn != null && !currentOn) {
                    return;
                }

                Double transitiontime = config.transitiontime;
                if (transitiontime != null) {
                    // value is in 1/10 seconds
                    newLightState.transitiontime = (int) Math.round(10 * transitiontime);
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                if (command instanceof DecimalType) {
                    int miredValue = kelvinToMired(((DecimalType) command).intValue());
                    newLightState.ct = constrainToRange(miredValue, ctMin, ctMax);
                    newLightState.on = true;
                }
                break;
            case CHANNEL_POSITION:
                if (command instanceof UpDownType) {
                    newLightState.on = (command == UpDownType.DOWN);
                } else if (command == StopMoveType.STOP) {
                    if (currentOn != null && currentOn && currentBri != null && currentBri <= BRIGHTNESS_MAX) {
                        // going down or currently stop (254 because of rounding error)
                        newLightState.on = true;
                    } else if (currentOn != null && !currentOn && currentBri != null && currentBri > BRIGHTNESS_MIN) {
                        // going up or currently stopped
                        newLightState.on = false;
                    }
                } else if (command instanceof PercentType) {
                    newLightState.bri = fromPercentType((PercentType) command);
                } else {
                    return;
                }
                break;
            default:
                // no supported command
                return;
        }

        Boolean newOn = newLightState.on;
        if (newOn != null && !newOn) {
            // if light shall be off, no other commands are allowed, so reset the new light state
            newLightState.clear();
            newLightState.on = false;
        } else if (newOn != null && newOn) {
            newLightState.ontime = onTime;
        }

        sendCommand(newLightState, command, channelUID, () -> {
            Integer transitionTime = newLightState.transitiontime;
            lastCommandExpireTimestamp = System.currentTimeMillis()
                    + (transitionTime != null ? transitionTime : DEFAULT_COMMAND_EXPIRY_TIME);
            lastCommand = newLightState;
        });
    }

    @Override
    protected void processStateResponse(DeconzBaseMessage stateResponse) {
        if (!(stateResponse instanceof LightMessage)) {
            return;
        }

        LightMessage lightMessage = (LightMessage) stateResponse;

        if (needsPropertyUpdate) {
            // if we did not receive a ctmin/ctmax, then we probably don't need it
            needsPropertyUpdate = false;

            Integer ctmax = lightMessage.ctmax;
            Integer ctmin = lightMessage.ctmin;
            if (ctmin != null && ctmax != null) {
                Map<String, String> properties = new HashMap<>(thing.getProperties());
                properties.put(PROPERTY_CT_MAX, Integer.toString(Util.constrainToRange(ctmax, ZCL_CT_MIN, ZCL_CT_MAX)));
                properties.put(PROPERTY_CT_MIN, Integer.toString(Util.constrainToRange(ctmin, ZCL_CT_MIN, ZCL_CT_MAX)));
                updateProperties(properties);
            }
        }

        LightState lightState = lightMessage.state;
        if (lightState != null && lightState.effect != null) {
            checkAndUpdateEffectChannels(lightMessage);
        }

        messageReceived(config.id, lightMessage);
    }

    private enum EffectLightModel {
        LIDL_MELINARA,
        TINT_MUELLER,
        UNKNOWN;
    }

    private void checkAndUpdateEffectChannels(LightMessage lightMessage) {
        EffectLightModel model = EffectLightModel.UNKNOWN;
        // try to determine which model we have
        if (lightMessage.manufacturername.equals("_TZE200_s8gkrkxk")) {
            // the LIDL Melinara string does not report a proper model name
            model = EffectLightModel.LIDL_MELINARA;
        } else if (lightMessage.manufacturername.equals("MLI")) {
            model = EffectLightModel.TINT_MUELLER;
        } else {
            logger.debug(
                    "Could not determine effect light type for thing {}, if you feel this is wrong request adding support on GitHub.",
                    thing.getUID());
        }

        ChannelUID effectChannelUID = new ChannelUID(thing.getUID(), CHANNEL_EFFECT);
        createChannel(CHANNEL_EFFECT, ChannelKind.STATE);

        switch (model) {
            case LIDL_MELINARA:
                // additional channels
                createChannel(CHANNEL_EFFECT_SPEED, ChannelKind.STATE);

                List<String> options = List.of("none", "steady", "snow", "rainbow", "snake", "tinkle", "fireworks",
                        "flag", "waves", "updown", "vintage", "fading", "collide", "strobe", "sparkles", "carnival",
                        "glow");
                commandDescriptionProvider.setCommandOptions(effectChannelUID, toCommandOptionList(options));
                break;
            case TINT_MUELLER:
                options = List.of("none", "colorloop", "sunset", "party", "worklight", "campfire", "romance",
                        "nightlight");
                commandDescriptionProvider.setCommandOptions(effectChannelUID, toCommandOptionList(options));
                break;
            default:
                options = List.of("none", "colorloop");
                commandDescriptionProvider.setCommandOptions(effectChannelUID, toCommandOptionList(options));
        }
    }

    private List<CommandOption> toCommandOptionList(List<String> options) {
        return options.stream().map(c -> new CommandOption(c, c)).collect(Collectors.toList());
    }

    private void valueUpdated(String channelId, LightState newState) {
        Integer bri = newState.bri;
        Integer hue = newState.hue;
        Integer sat = newState.sat;
        Boolean on = newState.on;

        switch (channelId) {
            case CHANNEL_ALERT:
                String alert = newState.alert;
                if (alert != null) {
                    updateState(channelId, new StringType(alert));
                }
                break;
            case CHANNEL_SWITCH:
            case CHANNEL_LOCK:
                if (on != null) {
                    updateState(channelId, OnOffType.from(on));
                }
                break;
            case CHANNEL_COLOR:
                if (on != null && on == false) {
                    updateState(channelId, OnOffType.OFF);
                } else if (bri != null && "xy".equals(newState.colormode)) {
                    final double @Nullable [] xy = newState.xy;
                    if (xy != null && xy.length == 2) {
                        HSBType color = HSBType.fromXY((float) xy[0], (float) xy[1]);
                        updateState(channelId, new HSBType(color.getHue(), color.getSaturation(), toPercentType(bri)));
                    }
                } else if (bri != null && hue != null && sat != null) {
                    updateState(channelId,
                            new HSBType(new DecimalType(hue / HUE_FACTOR), toPercentType(sat), toPercentType(bri)));
                }
                break;
            case CHANNEL_BRIGHTNESS:
                if (bri != null && on != null && on) {
                    updateState(channelId, toPercentType(bri));
                } else {
                    updateState(channelId, OnOffType.OFF);
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                Integer ct = newState.ct;
                if (ct != null && ct >= ctMin && ct <= ctMax) {
                    updateState(channelId, new DecimalType(miredToKelvin(ct)));
                }
                break;
            case CHANNEL_POSITION:
                if (bri != null) {
                    updateState(channelId, toPercentType(bri));
                }
                break;
            case CHANNEL_EFFECT:
                String effect = newState.effect;
                if (effect != null) {
                    updateState(channelId, new StringType(effect));
                }
                break;
            case CHANNEL_EFFECT_SPEED:
                Integer effectSpeed = newState.effectSpeed;
                if (effectSpeed != null) {
                    updateState(channelId, new DecimalType(effectSpeed));
                }
                break;
            default:
        }
    }

    @Override
    public void messageReceived(String sensorID, DeconzBaseMessage message) {
        if (message instanceof LightMessage) {
            LightMessage lightMessage = (LightMessage) message;
            logger.trace("{} received {}", thing.getUID(), lightMessage);
            LightState lightState = lightMessage.state;
            if (lightState != null) {
                if (lastCommandExpireTimestamp > System.currentTimeMillis()
                        && !lightState.equalsIgnoreNull(lastCommand)) {
                    // skip for SKIP_UPDATE_TIMESPAN after last command if lightState is different from command
                    logger.trace("Ignoring differing update after last command until {}", lastCommandExpireTimestamp);
                    return;
                }
                if (colorMode.isEmpty()) {
                    String cmode = lightState.colormode;
                    if (cmode != null && ("hs".equals(cmode) || "xy".equals(cmode))) {
                        // only set the color mode if it is hs or xy, not ct
                        colorMode = cmode;
                    }
                }
                lightStateCache = lightState;
                if (Boolean.TRUE.equals(lightState.reachable)) {
                    updateStatus(ThingStatus.ONLINE);
                    thing.getChannels().stream().map(c -> c.getUID().getId()).forEach(c -> valueUpdated(c, lightState));
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.light-not-reachable");
                    thing.getChannels().stream().map(c -> c.getUID()).forEach(c -> updateState(c, UnDefType.UNDEF));
                }
            }
        }
    }
}
