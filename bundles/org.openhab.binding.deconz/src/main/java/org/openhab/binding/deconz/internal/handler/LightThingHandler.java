/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil;
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
    private @Nullable Integer onTime = null; // in 0.1s
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
                        .withStep(BigDecimal.valueOf(100)).withPattern("%.0f K")
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
            if (command instanceof QuantityType<?> quantity) {
                QuantityType<?> onTimeSeconds = quantity.toUnit(Units.SECOND);
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
            valueUpdated(channelUID, lightStateCache);
            return;
        }

        LightState newLightState = new LightState();
        Boolean currentOn = lightStateCache.on;
        Integer currentBri = lightStateCache.bri;

        switch (channelUID.getId()) {
            case CHANNEL_ALERT -> {
                if (command instanceof StringType) {
                    newLightState.alert = command.toString();
                } else {
                    return;
                }
            }
            case CHANNEL_EFFECT -> {
                if (command instanceof StringType) {
                    // effect command only allowed for lights that are turned on
                    newLightState.on = true;
                    newLightState.effect = command.toString();
                } else {
                    return;
                }
            }
            case CHANNEL_EFFECT_SPEED -> {
                if (command instanceof DecimalType) {
                    newLightState.on = true;
                    newLightState.effectSpeed = Util.constrainToRange(((DecimalType) command).intValue(), 0, 10);
                } else {
                    return;
                }
            }
            case CHANNEL_SWITCH, CHANNEL_LOCK -> {
                if (command instanceof OnOffType) {
                    newLightState.on = (command == OnOffType.ON);
                } else {
                    return;
                }
            }
            case CHANNEL_BRIGHTNESS, CHANNEL_COLOR -> {
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
                } else if (command instanceof HSBType hsbCommand) {
                    // XY color is the implicit default: Use XY color mode if i) no color mode is set or ii) if the bulb
                    // is in CT mode or iii) already in XY mode. Only if the bulb is in HS mode, use this one.
                    if ("hs".equals(colorMode)) {
                        newLightState.hue = (int) (hsbCommand.getHue().doubleValue() * HUE_FACTOR);
                        newLightState.sat = Util.fromPercentType(hsbCommand.getSaturation());
                        newLightState.bri = Util.fromPercentType(hsbCommand.getBrightness());
                    } else {
                        double[] xy = ColorUtil.hsbToXY(hsbCommand);
                        newLightState.xy = new double[] { xy[0], xy[1] };
                        newLightState.bri = Util.fromPercentType(hsbCommand.getBrightness());
                    }
                } else if (command instanceof PercentType percentCommand) {
                    newLightState.bri = Util.fromPercentType(percentCommand);
                } else if (command instanceof DecimalType decimalCommand) {
                    newLightState.bri = decimalCommand.intValue();
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
            }
            case CHANNEL_COLOR_TEMPERATURE -> {
                QuantityType<?> miredQuantity = null;
                if (command instanceof QuantityType<?> genericQuantity) {
                    miredQuantity = genericQuantity.toInvertibleUnit(Units.MIRED);
                } else if (command instanceof DecimalType decimal) {
                    miredQuantity = QuantityType.valueOf(decimal.intValue(), Units.KELVIN)
                            .toInvertibleUnit(Units.MIRED);
                }
                if (miredQuantity != null) {
                    newLightState.ct = constrainToRange(miredQuantity.intValue(), ctMin, ctMax);
                    newLightState.on = true;
                    Double transitiontime = config.transitiontime;
                    if (transitiontime != null) {
                        // value is in 1/10 seconds
                        newLightState.transitiontime = (int) Math.round(10 * transitiontime);
                    }
                }
            }
            case CHANNEL_POSITION -> {
                if (command instanceof UpDownType) {
                    newLightState.open = (command == UpDownType.UP);
                } else if (command == StopMoveType.STOP) {
                    newLightState.stop = true;
                } else if (command instanceof PercentType) {
                    newLightState.lift = ((PercentType) command).intValue();
                } else {
                    return;
                }
            }
            default -> {
                // no supported command
                return;
            }
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
        if (!(stateResponse instanceof LightMessage lightMessage)) {
            return;
        }

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

        ThingBuilder thingBuilder = editThing();
        boolean thingEdited = false;

        LightState lightState = lightMessage.state;
        if (lightState != null && lightState.effect != null
                && checkAndUpdateEffectChannels(thingBuilder, lightMessage)) {
            thingEdited = true;
        }

        if (checkLastSeen(thingBuilder, stateResponse.lastseen)) {
            thingEdited = true;
        }
        if (thingEdited) {
            updateThing(thingBuilder.build());
        }

        messageReceived(lightMessage);
    }

    private enum EffectLightModel {
        LIDL_MELINARA,
        TINT_MUELLER,
        UNKNOWN
    }

    private boolean checkAndUpdateEffectChannels(ThingBuilder thingBuilder, LightMessage lightMessage) {
        // try to determine which model we have
        EffectLightModel model = switch (lightMessage.manufacturername) {
            case "_TZE200_s8gkrkxk" -> EffectLightModel.LIDL_MELINARA;
            case "MLI" -> EffectLightModel.TINT_MUELLER;
            default -> EffectLightModel.UNKNOWN;
        };
        if (model == EffectLightModel.UNKNOWN) {
            logger.debug(
                    "Could not determine effect light type for thing {}, if you feel this is wrong request adding support on GitHub.",
                    thing.getUID());
        }

        ChannelUID effectChannelUID = new ChannelUID(thing.getUID(), CHANNEL_EFFECT);

        boolean thingEdited = false;

        if (thing.getChannel(CHANNEL_EFFECT) == null) {
            createChannel(thingBuilder, CHANNEL_EFFECT, ChannelKind.STATE);
            thingEdited = true;
        }

        switch (model) {
            case LIDL_MELINARA:
                if (thing.getChannel(CHANNEL_EFFECT_SPEED) == null) {
                    // additional channels
                    createChannel(thingBuilder, CHANNEL_EFFECT_SPEED, ChannelKind.STATE);
                    thingEdited = true;
                }

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

        return thingEdited;
    }

    private List<CommandOption> toCommandOptionList(List<String> options) {
        return options.stream().map(c -> new CommandOption(c, c)).collect(Collectors.toList());
    }

    private void valueUpdated(ChannelUID channelUID, LightState newState) {
        Boolean on = newState.on;

        switch (channelUID.getId()) {
            case CHANNEL_ALERT -> updateStringChannel(channelUID, newState.alert);
            case CHANNEL_SWITCH, CHANNEL_LOCK -> updateSwitchChannel(channelUID, on);
            case CHANNEL_COLOR -> updateColorChannel(channelUID, newState);
            case CHANNEL_BRIGHTNESS -> updatePercentTypeChannel(channelUID, newState.bri, newState.on);
            case CHANNEL_COLOR_TEMPERATURE -> updateColorTemperatureChannel(channelUID, newState);
            case CHANNEL_POSITION -> updatePosition(channelUID, newState);
            case CHANNEL_EFFECT -> updateStringChannel(channelUID, newState.effect);
            case CHANNEL_EFFECT_SPEED -> updateDecimalTypeChannel(channelUID, newState.effectSpeed);
        }
    }

    @Override
    public void messageReceived(DeconzBaseMessage message) {
        logger.trace("{} received {}", thing.getUID(), message);
        if (message instanceof LightMessage lightMessage) {
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
                    thing.getChannels().stream().map(Channel::getUID).forEach(c -> valueUpdated(c, lightState));
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.sensor-not-reachable");
                }
            }
        }
    }

    /**
     * Update the given {@link ChannelUID} depending on the given {@link LightState}. If the 'colorMode' is "xy"
     * then update the channel with an {@link HSBType} built from the given CIE XY co-ordinates and brightness,
     * otherwise if the 'colorMode' is not "ct" then update the channel with an {@link HSBType} from the given hue,
     * saturation and brightness. In either case if the 'on' field is false then the brightness is set to zero.
     * Furthermore if the color channel has been updated then cross-update the color temperature channel (if any)
     * to {@link UnDefType.UNDEF} as well.
     *
     * @param channelUID the UID of the channel being updated.
     * @param newState the new {@link LightState}
     */
    private void updateColorChannel(ChannelUID channelUID, LightState newState) {
        Boolean on = newState.on;
        Integer bri = newState.bri;
        Integer hue = newState.hue;
        Integer sat = newState.sat;

        boolean ctChannelUpdate = false;
        if (on != null && !on) {
            bri = 0;
        }

        if (bri != null && "xy".equals(newState.colormode)) {
            final double @Nullable [] xy = newState.xy;
            if (xy != null && xy.length == 2) {
                HSBType hsX = ColorUtil.xyToHsb(xy);
                HSBType hsb = new HSBType(hsX.getHue(), hsX.getSaturation(), toPercentType(bri));
                logger.trace("updateColorChannel(xy) channelUID:{}, hsb:{}", channelUID, hsb);
                updateState(channelUID, hsb);
                ctChannelUpdate = true;
            }
        } else if (bri != null && !"ct".equals(newState.colormode) && hue != null && sat != null) {
            HSBType hsb = new HSBType(new DecimalType(hue / HUE_FACTOR), toPercentType(sat), toPercentType(bri));
            logger.trace("updateColorChannel(hsb) channelUID:{}, hsb:{}", channelUID, hsb);
            updateState(channelUID, hsb);
            ctChannelUpdate = true;
        }

        // cross-update the color temperature channel (if any)
        if (ctChannelUpdate && thing.getChannel(CHANNEL_COLOR_TEMPERATURE) instanceof Channel ctChannel) {
            logger.trace("updateColorTemperatureChannel() channelUID:{}, ct:UNDEF", ctChannel.getUID());
            updateState(ctChannel.getUID(), UnDefType.UNDEF);
        }
    }

    /**
     * Update the given {@link ChannelUID} depending on the given {@link LightState}. If the 'colorMode' is "ct" and
     * there is a 'ct' value (in mired) then convert it to Kelvin and update the channel. If the color temperature
     * channel state has been updated then cross-update the color channel (if any) state to an {@link HSBType} that
     * matches the given Kelvin value on the "Planckian Locus" on the CIE color chart as well.
     *
     * @param channelUID the UID of the channel being updated.
     * @param newState the new {@link LightState}
     */
    private void updateColorTemperatureChannel(ChannelUID channelUID, LightState newState) {
        Integer ct = newState.ct;
        String colorMode = newState.colormode;
        if ((colorMode == null || "ct".equals(colorMode)) && ct != null && ct >= ctMin && ct <= ctMax) {
            int kelvin = miredToKelvin(ct);
            logger.trace("updateColorTemperatureChannel() channelUID:{}, kelvin:{}", channelUID, kelvin);
            updateState(channelUID, QuantityType.valueOf(kelvin, Units.KELVIN));

            // cross-update the color channel (if any)
            if (thing.getChannel(CHANNEL_COLOR) instanceof Channel colChannel) {
                int brightness = !Boolean.TRUE.equals(newState.on) ? 0 : newState.bri instanceof Integer bri ? bri : -1;
                if (brightness >= 0) {
                    HSBType hsX = ColorUtil.xyToHsb(ColorUtil.kelvinToXY(kelvin));
                    HSBType hsb = new HSBType(hsX.getHue(), hsX.getSaturation(), toPercentType(brightness));
                    logger.trace("updateColorChannel() channelUID:{}, hsb:{}", colChannel.getUID(), hsb);
                    updateState(colChannel.getUID(), hsb);
                }
            }
        }
    }

    private void updatePosition(ChannelUID channelUID, LightState newState) {
        Integer lift = newState.lift;
        if (lift != null) {
            updateState(channelUID, new PercentType(lift));
        }
    }
}
