/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.handler;

import com.noctarius.lightify.LightifyLink;
import com.noctarius.lightify.model.Capability;
import com.noctarius.lightify.model.ColorLight;
import com.noctarius.lightify.model.Device;
import com.noctarius.lightify.model.Luminary;
import com.noctarius.lightify.model.PowerSocket;
import com.noctarius.lightify.model.Switchable;
import com.noctarius.lightify.model.Zone;
import com.noctarius.lightify.protocol.Address;
import com.noctarius.lightify.protocol.LightifyUtils;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.noctarius.lightify.protocol.LightifyUtils.exceptional;
import static org.openhab.binding.lightify.LightifyConstants.RGBW_CHANNEL_ID_COLOR;
import static org.openhab.binding.lightify.LightifyConstants.RGBW_CHANNEL_ID_DIMMER;
import static org.openhab.binding.lightify.LightifyConstants.RGBW_CHANNEL_ID_POWER;
import static org.openhab.binding.lightify.LightifyConstants.RGBW_CHANNEL_ID_TEMPERATURE;
import static org.openhab.binding.lightify.LightifyConstants.SB_CHANNEL_ID_DIMMER;
import static org.openhab.binding.lightify.LightifyConstants.SB_CHANNEL_ID_POWER;
import static org.openhab.binding.lightify.LightifyConstants.SOCKET_CHANNEL_ID_POWER;
import static org.openhab.binding.lightify.LightifyConstants.THING_TYPE_LIGHTIFY_BULB_RGBW;
import static org.openhab.binding.lightify.LightifyConstants.THING_TYPE_LIGHTIFY_BULB_SB;
import static org.openhab.binding.lightify.LightifyConstants.THING_TYPE_LIGHTIFY_BULB_TW;
import static org.openhab.binding.lightify.LightifyConstants.THING_TYPE_LIGHTIFY_POWERSOCKET;
import static org.openhab.binding.lightify.LightifyConstants.THING_TYPE_LIGHTIFY_ZONE;
import static org.openhab.binding.lightify.LightifyConstants.TW_CHANNEL_ID_DIMMER;
import static org.openhab.binding.lightify.LightifyConstants.TW_CHANNEL_ID_POWER;
import static org.openhab.binding.lightify.LightifyConstants.TW_CHANNEL_ID_TEMPERATURE;
import static org.openhab.binding.lightify.internal.LightifyHandlerFactory.getThingTypeUID;

/**
 * <p>The {@link org.eclipse.smarthome.core.thing.binding.ThingHandler} implementation to handle commands
 * for a OSRAM Lightify light bulb / stripe or zone (group of lights).</p>
 * <p>Commands are sent through the Lightify gateway the device is paired to. The connection link, managed
 * by the assigned {@link GatewayHandler} will be used to forward the command to.</p>
 *
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public class DeviceHandler
        extends BaseThingHandler {

    /**
     * Supported {@link ThingTypeUID}s for this handler
     */
    public static final Set<ThingTypeUID> SUPPORTED_TYPES = Collections.unmodifiableSet(new HashSet<ThingTypeUID>() {
        {
            add(THING_TYPE_LIGHTIFY_BULB_SB);
            add(THING_TYPE_LIGHTIFY_BULB_TW);
            add(THING_TYPE_LIGHTIFY_BULB_RGBW);
            add(THING_TYPE_LIGHTIFY_POWERSOCKET);
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
        LightifyUtils.Exceptional statusUpdater = () -> {
            updateStatus(ThingStatus.ONLINE);
            /*logger.info("Starting update task for: {}", getThing().getUID());
            GatewayHandler gatewayHandler = getGatewayHandler();
            LightifyLink lightifyLink = gatewayHandler.getLightifyLink();
            Device device = getDevice();
            if (!(device instanceof Zone)) {
                logger.info("Status update for {}", device);
                lightifyLink.performStatusUpdate(device, this::updateDeviceStatus);
            } else {
                // TODO zone update
                updateStatus(ThingStatus.ONLINE);
            }*/
        };

        Runnable runnable = () -> exceptional(statusUpdater, e -> logger.error("Update task failed for:" + getThing().getUID(), e));

        // Deactivated for now, seems to overload the poor, little gateway
        updateTaskRegistration = scheduler.scheduleWithFixedDelay(runnable, 1, 5, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (updateTaskRegistration != null) {
            updateTaskRegistration.cancel(true);
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("Command: {}, {}", command, channelUID);
        try {
            switch (channelUID.getId()) {
                case RGBW_CHANNEL_ID_POWER:
                case SB_CHANNEL_ID_POWER:
                case TW_CHANNEL_ID_POWER:
                case SOCKET_CHANNEL_ID_POWER:
                    if (command instanceof PercentType) {
                        handleDimmer(command);
                    } else {
                        handlePowerSwitch(command);
                    }
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
                    break;
                case "REFRESH":
                    handleRefresh();
                default:
                    logger.warn("Unhandled command '{}' on channel '{}'", command, channelUID);
            }
        } catch (Exception e) {
            logger.error("Error while handling command: " + command, e);
        }
    }

    private void handleRefresh() {
        logger.info("Handling refresh for: {}", getThing().getUID());
        Device device = getDevice();
        LightifyLink lightifyLink = getGatewayHandler().getLightifyLink();
        lightifyLink.performStatusUpdate(device, this::updateDeviceStatus);
    }

    private void handleColor(Command command) {
        LightifyLink lightifyLink = getGatewayHandler().getLightifyLink();
        Device device = getDevice();

        if (device instanceof Luminary) {
            Luminary luminary = (Luminary) device;
            if (luminary.hasCapability(Capability.RGB) && command instanceof HSBType) {
                HSBType hsb = (HSBType) command;

                byte red = (byte) hsb.getRed().intValue();
                byte green = (byte) hsb.getGreen().intValue();
                byte blue = (byte) hsb.getBlue().intValue();

                lightifyLink.performRGB(luminary, red, green, blue, 0, this::updateDeviceStatus);
            }
        } else if (device instanceof Zone && command instanceof HSBType) {
            Zone zone = (Zone) device;

            HSBType hsb = (HSBType) command;

            byte red = (byte) hsb.getRed().intValue();
            byte green = (byte) hsb.getGreen().intValue();
            byte blue = (byte) hsb.getBlue().intValue();

            for (Address address : zone.getAddresses()) {
                Device item = lightifyLink.findDevice(address.toAddressCode());
                if (item instanceof Luminary && ((Luminary) item).hasCapability(Capability.RGB)) {
                    lightifyLink.performRGB((Luminary) item, red, green, blue, 0, this::updateDeviceStatus);
                }
            }
        }
    }

    private void handleTemperature(Command command) {
        LightifyLink lightifyLink = getGatewayHandler().getLightifyLink();
        Device device = getDevice();

        if (device instanceof Luminary) {
            Luminary luminary = (Luminary) device;
            if (luminary.hasCapability(Capability.TunableWhite) && command instanceof DecimalType) {
                short value = (short) ((DecimalType) command).intValue();

                lightifyLink.performTemperature(luminary, value, 0, this::updateDeviceStatus);
            }
        } else if (device instanceof Zone && command instanceof DecimalType) {
            Zone zone = (Zone) device;
            short value = (short) ((DecimalType) command).intValue();

            for (Address address : zone.getAddresses()) {
                Device item = lightifyLink.findDevice(address.toAddressCode());
                if (item instanceof Luminary && ((Luminary) item).hasCapability(Capability.TunableWhite)) {
                    lightifyLink.performTemperature((Luminary) item, value, 0, this::updateDeviceStatus);
                }
            }
        }
    }

    private void handleDimmer(Command command) {
        LightifyLink lightifyLink = getGatewayHandler().getLightifyLink();
        Device device = getDevice();

        if (device instanceof Luminary) {
            Luminary luminary = (Luminary) device;
            if (luminary.hasCapability(Capability.Dimmable) && command instanceof PercentType) {
                int value = ((PercentType) command).intValue();
                executeDimming(value, device, lightifyLink);
            }
        } else if (device instanceof Zone && command instanceof PercentType) {
            Zone zone = (Zone) device;
            int value = ((PercentType) command).intValue();

            for (Address address : zone.getAddresses()) {
                Device item = lightifyLink.findDevice(address.toAddressCode());
                logger.info("Executing dimming on: {}", address.toString());
                executeDimming(value, item, lightifyLink);
            }
        }
    }

    private void executeDimming(int value, Device device, LightifyLink lightifyLink) {
        boolean powerOff = value == 0;
        if (powerOff || !(device instanceof Luminary) || !((Luminary) device).hasCapability(Capability.Dimmable)) {
            lightifyLink.performSwitch((Switchable) device, !powerOff, this::updateDeviceStatus);

        } else {
            Luminary luminary = (Luminary) device;
            if (!luminary.isOn()) {
                lightifyLink.performSwitch((Switchable) device, true, this::updateDeviceStatus);
            }
            lightifyLink.performLuminance(luminary, (byte) value, 0, this::updateDeviceStatus);
        }
    }

    private void handlePowerSwitch(Command command) {
        LightifyLink lightifyLink = getGatewayHandler().getLightifyLink();
        Device device = getDevice();
        logger.info("Switch state: {}", device);
        logger.info("Command-Type: {}", command.getClass());

        boolean activate = false;
        if (command instanceof OnOffType) {
            activate = command == OnOffType.ON;
        } else if (command instanceof DecimalType) {
            activate = ((DecimalType) command).intValue() > 0;
        }

        if (device != null) {
            if (device instanceof Switchable && command instanceof OnOffType) {
                lightifyLink.performSwitch((Switchable) device, activate, this::updateDeviceStatus);
            }
        } else if (device instanceof Zone) {
            Zone zone = (Zone) device;

            for (Address address : zone.getAddresses()) {
                Device item = lightifyLink.findDevice(address.toAddressCode());
                if (item instanceof Switchable) {
                    lightifyLink.performSwitch((Switchable) item, activate, this::updateDeviceStatus);
                }
            }
        }
    }

    private Device getDevice() {
        GatewayHandler gatewayHandler = getGatewayHandler();
        if (gatewayHandler == null) {
            logger.warn("GatewayHandler not found");
            return null;
        }

        LightifyLink lightifyLink = gatewayHandler.getLightifyLink();
        if (lightifyLink == null) {
            logger.warn("LightifyLink not found");
            return null;
        }

        String address = getThing().getUID().getId();
        ThingTypeUID thingTypeUID = getThing().getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_LIGHTIFY_BULB_RGBW) || thingTypeUID.equals(THING_TYPE_LIGHTIFY_BULB_TW) || thingTypeUID
                .equals(THING_TYPE_LIGHTIFY_BULB_SB)) {

            Device device = lightifyLink.findDevice(address);
            logger.debug("Retrieving device: {}={}", address, device);
            return device;
        }

        Zone zone = lightifyLink.findZone(address);
        logger.debug("Retrieving zone: {}={}", address, zone);
        return zone;
    }

    private GatewayHandler getGatewayHandler() {
        return (GatewayHandler) getBridge().getHandler();
    }

    private void updateDeviceStatus(Switchable switchable) {
        updateDeviceStatus((Device) switchable);
    }

    private void updateDeviceStatus(Luminary luminary) {
        updateDeviceStatus((Device) luminary);
    }

    private void updateDeviceStatus(Device device) {
        logger.info("updateDeviceStatus: {}", device);
        if (!(device instanceof Zone)) {
            updateStatus(device.isReachable() ? ThingStatus.ONLINE : ThingStatus.ONLINE);
        }

        if (device instanceof Luminary) {
            Luminary luminary = (Luminary) device;
            if (luminary.hasCapability(Capability.RGB)) {
                
                updateState(device, RGBW_CHANNEL_ID_POWER, luminary.asSwitchable().isOn() ? OnOffType.ON : OnOffType.OFF);
                updateState(device, RGBW_CHANNEL_ID_DIMMER, new PercentType(luminary.asDimmableLight().getLuminance()));
                updateState(device, RGBW_CHANNEL_ID_TEMPERATURE, new DecimalType(luminary.asTunableWhiteLight().getTemperature()));

                ColorLight colorLight = luminary.asColorLight();
                int r = colorLight.getRed();
                int g = colorLight.getGreen();
                int b = colorLight.getBlue();
                updateState(device, RGBW_CHANNEL_ID_COLOR, HSBType.fromRGB(r, g, b));

            } else if (luminary.hasCapability(Capability.TunableWhite)) {
                updateState(device, TW_CHANNEL_ID_POWER, luminary.asSwitchable().isOn() ? OnOffType.ON : OnOffType.OFF);
                updateState(device, TW_CHANNEL_ID_DIMMER, new PercentType(luminary.asDimmableLight().getLuminance()));
                updateState(device, TW_CHANNEL_ID_TEMPERATURE, new DecimalType(luminary.asTunableWhiteLight().getTemperature()));

            } else if (luminary.hasCapability(Capability.PureWhite)) {
                updateState(device, SB_CHANNEL_ID_POWER, luminary.asSwitchable().isOn() ? OnOffType.ON : OnOffType.OFF);
                updateState(device, SB_CHANNEL_ID_DIMMER, new PercentType(luminary.asDimmableLight().getLuminance()));
            }
        } else if (device instanceof PowerSocket) {
            updateState(device, SOCKET_CHANNEL_ID_POWER, ((PowerSocket) device).isOn() ? OnOffType.ON : OnOffType.OFF);
        }
    }

    private void updateState(Device device, String channelId, State state) {
        ChannelUID channelUID = findChannel(device, channelId);
        updateState(channelUID, state);
    }

    private ChannelUID findChannel(Device device, String channelId) {
        ThingTypeUID thingTypeUID = getThingTypeUID(device);
        ThingUID thingUID = new ThingUID(thingTypeUID, getBridge().getUID(), device.getAddress().toAddressCode());
        return new ChannelUID(thingUID, channelId);
    }
}
