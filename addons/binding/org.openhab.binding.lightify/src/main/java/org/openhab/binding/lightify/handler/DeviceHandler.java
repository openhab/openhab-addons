/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lightify.internal.link.Capability;
import org.openhab.binding.lightify.internal.link.LightifyLight;
import org.openhab.binding.lightify.internal.link.LightifyLink;
import org.openhab.binding.lightify.internal.link.LightifyLuminary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.lightify.internal.LightifyConstants.PROPERTY_ZONE_ID;
import static org.openhab.binding.lightify.internal.LightifyConstants.RGBW_CHANNEL_ID_COLOR;
import static org.openhab.binding.lightify.internal.LightifyConstants.RGBW_CHANNEL_ID_DIMMER;
import static org.openhab.binding.lightify.internal.LightifyConstants.RGBW_CHANNEL_ID_POWER;
import static org.openhab.binding.lightify.internal.LightifyConstants.RGBW_CHANNEL_ID_TEMPERATURE;
import static org.openhab.binding.lightify.internal.LightifyConstants.SB_CHANNEL_ID_DIMMER;
import static org.openhab.binding.lightify.internal.LightifyConstants.SB_CHANNEL_ID_POWER;
import static org.openhab.binding.lightify.internal.LightifyConstants.THING_TYPE_LIGHTIFY_BULB_RGBW;
import static org.openhab.binding.lightify.internal.LightifyConstants.THING_TYPE_LIGHTIFY_BULB_SB;
import static org.openhab.binding.lightify.internal.LightifyConstants.THING_TYPE_LIGHTIFY_BULB_TW;
import static org.openhab.binding.lightify.internal.LightifyConstants.THING_TYPE_LIGHTIFY_ZONE;
import static org.openhab.binding.lightify.internal.LightifyConstants.TW_CHANNEL_ID_DIMMER;
import static org.openhab.binding.lightify.internal.LightifyConstants.TW_CHANNEL_ID_POWER;
import static org.openhab.binding.lightify.internal.LightifyConstants.TW_CHANNEL_ID_TEMPERATURE;

/**
 * <p>The {@link org.eclipse.smarthome.core.thing.binding.ThingHandler} implementation to handle commands
 * for a OSRAM Lightify light bulb / stripe or zone (group of lights).</p>
 * <p>Commands are sent through the Lightify gateway the device is paired to. The connection link, managed
 * by the assigned {@link GatewayHandler} will be used to forward the command to.</p>
 *
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public class DeviceHandler extends BaseThingHandler {

    /**
     * Supported {@link ThingTypeUID}s for this handler
     */
    public static final Set<ThingTypeUID> SUPPORTED_TYPES = Collections.unmodifiableSet(new HashSet<ThingTypeUID>() {
        {
            add(THING_TYPE_LIGHTIFY_BULB_SB);
            add(THING_TYPE_LIGHTIFY_BULB_TW);
            add(THING_TYPE_LIGHTIFY_BULB_RGBW);
            add(THING_TYPE_LIGHTIFY_ZONE);
        }
    });

    private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

    private volatile ScheduledFuture<?> updateTaskRegistration;

    public DeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        Runnable statusUpdater = () -> {
            GatewayHandler gatewayHandler = getGatewayHandler();
            LightifyLink lightifyLink = gatewayHandler.getLightifyLink();
            LightifyLuminary luminary = getLuminary();
            if (luminary instanceof LightifyLight) {
                lightifyLink.performStatusUpdate(luminary, this::updateState);
            }
        };

        updateTaskRegistration = scheduler.scheduleWithFixedDelay(statusUpdater, 1, 10, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        updateTaskRegistration.cancel(true);
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command: {}", command);
        switch (channelUID.getId()) {
            case RGBW_CHANNEL_ID_POWER:
            case SB_CHANNEL_ID_POWER:
            case TW_CHANNEL_ID_POWER:
                handlePowerSwitch(command);
                break;
            case RGBW_CHANNEL_ID_DIMMER:
            case SB_CHANNEL_ID_DIMMER:
            case TW_CHANNEL_ID_DIMMER:
                handleDimmer(command);
                break;
            case RGBW_CHANNEL_ID_TEMPERATURE:
            case TW_CHANNEL_ID_TEMPERATURE:
                handleTemperature(command);
                break;
            case RGBW_CHANNEL_ID_COLOR:
                handleColor(command);
        }
    }

    private void handleColor(Command command) {
        LightifyLuminary luminary = getLuminary();
        if (luminary != null) {
            if (command instanceof HSBType) {
                HSBType hsb = (HSBType) command;

                byte red = (byte) hsb.getRed().intValue();
                byte green = (byte) hsb.getGreen().intValue();
                byte blue = (byte) hsb.getBlue().intValue();

                luminary.setRGB(red, green, blue, (short) 0, this::updateState);
            }
        }
    }

    private void handleTemperature(Command command) {
        LightifyLuminary luminary = getLuminary();
        if (luminary != null) {
            if (command instanceof DecimalType) {
                short value = (short) ((DecimalType) command).intValue();
                luminary.setTemperature(value, (short) 0, this::updateState);
            }
        }
    }

    private void handleDimmer(Command command) {
        LightifyLuminary luminary = getLuminary();
        if (luminary != null) {
            if (command instanceof PercentType) {
                byte value = (byte) ((PercentType) command).intValue();
                luminary.setLuminance(value, (short) 0, this::updateState);
            }
        }
    }

    private void handlePowerSwitch(Command command) {
        LightifyLuminary luminary = getLuminary();
        if (luminary != null) {
            if (command instanceof OnOffType) {
                luminary.setSwitch(command == OnOffType.ON, this::updateState);
            }
        }
    }

    private LightifyLuminary getLuminary() {
        GatewayHandler gatewayHandler = getGatewayHandler();
        LightifyLink lightifyLink = gatewayHandler.getLightifyLink();

        ThingTypeUID thingTypeUID = getThing().getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_LIGHTIFY_BULB_RGBW)
                || thingTypeUID.equals(THING_TYPE_LIGHTIFY_BULB_TW)
                || thingTypeUID.equals(THING_TYPE_LIGHTIFY_BULB_SB)) {
            return lightifyLink.findDevice(getThing().getUID().getId());
        }

        String zoneId = getThing().getProperties().get(PROPERTY_ZONE_ID);
        return lightifyLink.findZone("zone::" + zoneId);
    }

    private GatewayHandler getGatewayHandler() {
        return (GatewayHandler) getBridge().getHandler();
    }

    private void updateState(LightifyLuminary luminary) {
        if (luminary.supports(Capability.RGB)) {
            updateState(RGBW_CHANNEL_ID_POWER, luminary.isPowered() ? OnOffType.ON : OnOffType.OFF);
            updateState(RGBW_CHANNEL_ID_DIMMER, new PercentType(luminary.getLuminance()));
            updateState(RGBW_CHANNEL_ID_TEMPERATURE, new DecimalType(luminary.getTemperature()));

            byte[] rgb = luminary.getRGB();
            int r = Byte.toUnsignedInt(rgb[0]);
            int g = Byte.toUnsignedInt(rgb[1]);
            int b = Byte.toUnsignedInt(rgb[2]);
            updateState(RGBW_CHANNEL_ID_COLOR, HSBType.fromRGB(r, g, b));

        } else if (luminary.supports(Capability.TunableWhite)) {
            updateState(TW_CHANNEL_ID_POWER, luminary.isPowered() ? OnOffType.ON : OnOffType.OFF);
            updateState(TW_CHANNEL_ID_DIMMER, new PercentType(luminary.getLuminance()));
            updateState(TW_CHANNEL_ID_TEMPERATURE, new DecimalType(luminary.getTemperature()));

        } else if (luminary.supports(Capability.PureWhite)) {
            updateState(SB_CHANNEL_ID_POWER, luminary.isPowered() ? OnOffType.ON : OnOffType.OFF);
            updateState(SB_CHANNEL_ID_DIMMER, new PercentType(luminary.getLuminance()));
        }
    }
}
