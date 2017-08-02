/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.CHANNEL_COLOR;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.CHANNEL_DIMMER;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.CHANNEL_SWITCH;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.CHANNEL_ABS_TEMPERATURE;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.CHANNEL_TEMPERATURE;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.PROPERTY_MAXIMUM_WHITE_TEMPERATURE;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.PROPERTY_MINIMUM_WHITE_TEMPERATURE;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.PROPERTY_IEEE_ADDRESS;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.THING_TYPE_LIGHTIFY_GROUP;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceConfiguration;

import org.openhab.binding.osramlightify.internal.LightifyDeviceState;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;

import org.openhab.binding.osramlightify.internal.messages.LightifyMessage;
import org.openhab.binding.osramlightify.internal.messages.LightifyGetProbedTemperatureMessage;
import org.openhab.binding.osramlightify.internal.messages.LightifySetColorMessage;
import org.openhab.binding.osramlightify.internal.messages.LightifySetLuminanceMessage;
import org.openhab.binding.osramlightify.internal.messages.LightifySetSwitchMessage;
import org.openhab.binding.osramlightify.internal.messages.LightifySetTemperatureMessage;

/**
 * The {@link org.eclipse.smarthome.core.thing.binding.ThingHandler} implementation
 * for devices paired with an OSRAM/Sylvania Lightify gateway.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LightifyDeviceHandler.class);

    private LightifyDeviceState lightifyDeviceState = new LightifyDeviceState();
    private LightifyDeviceConfiguration configuration = null;

    private double whiteTemperatureFactor;

    public LightifyDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        thingUpdated(getThing());

        // N.B. We do not go online or offline here. We do that when we are seen in a
        // list paired/group response for a bridge.
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void thingUpdated(Thing thing) {
        configuration = getConfigAs(LightifyDeviceConfiguration.class);

        LightifyBridgeHandler bridgeHandler = (LightifyBridgeHandler) getBridge().getHandler();
        LightifyBridgeConfiguration bridgeConfig = bridgeHandler.getConfiguration();

        if (configuration.transitionTime == null) {
            configuration.transitionTime = bridgeConfig.transitionTime;
        }

        if (configuration.transitionToOffTime == null) {
            configuration.transitionToOffTime = bridgeConfig.transitionToOffTime;
        }

        if (configuration.whiteTemperatureMin == null) {
            configuration.whiteTemperatureMin = bridgeConfig.whiteTemperatureMin;
            if (configuration.whiteTemperatureMin == null) {
                String valStr = getThing().getProperties().get(PROPERTY_MINIMUM_WHITE_TEMPERATURE);
                if (valStr != null) {
                    configuration.whiteTemperatureMin = Integer.parseInt(valStr);
                }
            }
        }

        if (configuration.whiteTemperatureMax == null) {
            configuration.whiteTemperatureMax = bridgeConfig.whiteTemperatureMax;
            if (configuration.whiteTemperatureMax == null) {
                String valStr = getThing().getProperties().get(PROPERTY_MAXIMUM_WHITE_TEMPERATURE);
                if (valStr != null) {
                    configuration.whiteTemperatureMax = Integer.parseInt(valStr);
                }
            }
        }

        if (configuration.whiteTemperatureMin != null && configuration.whiteTemperatureMax != null) {
            whiteTemperatureFactor = 100.0 / (configuration.whiteTemperatureMax - configuration.whiteTemperatureMin);
        } else {
            whiteTemperatureFactor = 100.0 / (6500.0 - 1800.0);
        }
    }

    public void setOnline() {
        Thing thing = getThing();

        // If we need to probe we'll do that now and leave the device offline. The next
        // time we see status for it we will have the probed data and can set it online.
        // Note that this is called by LightifyDeviceState which is called by the receive
        // handler in LightifyListPairedDevices which is called from the connector thread
        // so all the probe message we queue here form an atomic block with respect to
        // the state polling.
        if (!thing.getThingTypeUID().equals(THING_TYPE_LIGHTIFY_GROUP)
        && thing.getProperties().get(PROPERTY_MINIMUM_WHITE_TEMPERATURE) == null) {
            LightifyBridgeHandler bridgeHandler = (LightifyBridgeHandler) getBridge().getHandler();
            String deviceAddress = getThing().getProperties().get(PROPERTY_IEEE_ADDRESS);

            bridgeHandler.sendMessage(new LightifySetLuminanceMessage(deviceAddress, new PercentType(1)));

            bridgeHandler.sendMessage(new LightifySetTemperatureMessage(deviceAddress, new DecimalType(0)));
            bridgeHandler.sendMessage(new LightifyGetProbedTemperatureMessage(thing, deviceAddress, PROPERTY_MINIMUM_WHITE_TEMPERATURE));

            bridgeHandler.sendMessage(new LightifySetTemperatureMessage(deviceAddress, new DecimalType(65535)));
            bridgeHandler.sendMessage(new LightifyGetProbedTemperatureMessage(thing, deviceAddress, PROPERTY_MAXIMUM_WHITE_TEMPERATURE));

            bridgeHandler.sendMessage(new LightifySetTemperatureMessage(deviceAddress, lightifyDeviceState.getTemperature()));
            bridgeHandler.sendMessage(new LightifySetColorMessage(deviceAddress, lightifyDeviceState.getColor()));
            bridgeHandler.sendMessage(new LightifySetLuminanceMessage(deviceAddress, lightifyDeviceState.getLuminance()));
            // We have no way of knowing if the light was initially in colour mode or white
            // so we have no way to restore it. Therefore a probed light will be left off.
            bridgeHandler.sendMessage(new LightifySetSwitchMessage(deviceAddress, OnOffType.OFF));

            // Then rescan. Really this is just a repoll - we do not need discovery, we just
            // want to try and bring the device online again A.S.A.P. In fact, what we are
            // really saying is, bring the device online once all the above completes.
            bridgeHandler.getDiscoveryService().startScan(null);

        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public void setStatus(ThingStatus status) {
        updateStatus(status, ThingStatusDetail.NONE, null);
    }

    public void setStatus(ThingStatus status, ThingStatusDetail detail) {
        updateStatus(status, detail, null);
    }

    public void setStatus(ThingStatus status, ThingStatusDetail detail, String info) {
        updateStatus(status, detail, info);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // If the thing is not online then there is no point passing the command on
        // to the gateway. At best the gateway will just discard it, at worst the
        // gateway's send queue will be clogged until the command times out (that
        // could be as much as 7.680s on the ZigBee side as per the ZLL spec).
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        String deviceAddress = getThing().getProperties().get(PROPERTY_IEEE_ADDRESS);

        if (command instanceof RefreshType) {
            // Ignored.

        } else {
            logger.debug("{}, Command: {} {}", channelUID, command.getClass().getSimpleName(), command);

            LightifyBridgeHandler bridgeHandler = (LightifyBridgeHandler) getBridge().getHandler();

            if (command instanceof HSBType) {
                HSBType hsb = (HSBType) command;

                PercentType luminance = hsb.getBrightness();

                if (lightifyDeviceState.getLuminance().intValue() != luminance.intValue()) {
                    logger.debug("{}: set luminance: {}", channelUID, luminance);

                    bridgeHandler.sendMessage(
                        new LightifySetLuminanceMessage(deviceAddress, luminance)
                            .setTransitionTime((luminance.intValue() != 0 ? configuration.transitionTime : configuration.transitionToOffTime))
                    );
                }

                hsb = new HSBType(hsb.getHue(), hsb.getSaturation(), new PercentType(100));

                logger.debug("{}: set HSB: {}", channelUID, hsb);

                bridgeHandler.sendMessage(
                    new LightifySetColorMessage(deviceAddress, hsb)
                        .setTransitionTime((hsb.getBrightness().intValue() != 0 ? configuration.transitionTime : configuration.transitionToOffTime))
                );

            } else if (command instanceof PercentType) {
                if (channelUID.getId().equals(CHANNEL_TEMPERATURE)) {
                    // Everything else uses dimmers for white temperature so we have to too :-(
                    DecimalType temperature = new DecimalType(
                        configuration.whiteTemperatureMin
                        + (
                            ((PercentType) command).doubleValue()
                            * (configuration.whiteTemperatureMax - configuration.whiteTemperatureMin)
                            + 0.5
                          ) / 100.0
                    );

                    logger.debug("{}: set temperature: {}", channelUID, temperature);

                    bridgeHandler.sendMessage(
                        new LightifySetTemperatureMessage(deviceAddress, temperature)
                            .setTransitionTime(configuration.transitionTime)
                    );

                    // A command on the percent temperature channel generates a matching command
                    // on the absolute temperature channel.
                    postCommand(CHANNEL_ABS_TEMPERATURE, temperature);

                } else {
                    // It can only be luminance. It doesn't matter whether it is on the color
                    // or luminance channel. It's ALWAYS luminance.
                    PercentType luminance = (PercentType) command;

                    logger.debug("{}: set luminance: {}", channelUID, luminance);

                    bridgeHandler.sendMessage(
                        new LightifySetLuminanceMessage(deviceAddress, luminance)
                            .setTransitionTime((luminance.intValue() != 0 ? configuration.transitionTime : configuration.transitionToOffTime))
                    );
                }

            } else if (command instanceof DecimalType) {
                DecimalType temperature = (DecimalType) command;

                logger.debug("{}: set temperature: {}", channelUID, temperature);

                bridgeHandler.sendMessage(
                    new LightifySetTemperatureMessage(deviceAddress, temperature)
                        .setTransitionTime(configuration.transitionTime)
                );

                // A command on the absolute temperature channel generates a matching command
                // on the percent temperature channel.
                postCommand(CHANNEL_TEMPERATURE, temperatureToPercent(bridgeHandler, temperature));

            } else if (command instanceof OnOffType) {
                OnOffType onoff = (OnOffType) command;

                logger.debug("{}: set power: {}", channelUID, onoff);

                bridgeHandler.sendMessage(new LightifySetSwitchMessage(deviceAddress, onoff));

            } else {
                logger.error("Handling not implemented for: {}", command);
            }
        }
    }

    public void changedPower(OnOffType onOff) {
        logger.debug("{}: update: power {}", getThing().getUID(), onOff);

        updateState(CHANNEL_SWITCH, onOff);
        updateState(CHANNEL_DIMMER, onOff);
        updateState(CHANNEL_COLOR, onOff);
    }

    public void changedLuminance(PercentType luminance) {
        logger.debug("{}: update: luminance {}", getThing().getUID(), luminance);

        updateState(CHANNEL_DIMMER, luminance);
    }

    public void changedTemperature(LightifyBridgeHandler bridgeHandler, DecimalType temperature) {
        logger.debug("{}: update: temperature {}", getThing().getUID(), temperature);

        updateState(CHANNEL_ABS_TEMPERATURE, temperature);
        updateState(CHANNEL_TEMPERATURE, temperatureToPercent(bridgeHandler, temperature));
    }

    public void changedColor(HSBType color) {
        logger.debug("{}: update: colour {}", getThing().getUID(), color);

        updateState(CHANNEL_COLOR, color);
    }

    private PercentType temperatureToPercent(LightifyBridgeHandler bridgeHandler, DecimalType temperature) {
        if (temperature.doubleValue() <= configuration.whiteTemperatureMin) {
            return new PercentType(0);
        } else if (temperature.doubleValue() >= configuration.whiteTemperatureMax) {
            return new PercentType(100);
        } else {
            double percent = whiteTemperatureFactor * (temperature.doubleValue() - configuration.whiteTemperatureMin);

            if (percent < 0) {
                percent = 0;
            } else if (percent > 100) {
                percent = 100;
            }

            return new PercentType((int) percent);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        // If the bridge goes offline we go offline too. We only go online when the
        // (or a) bridge comes online AND sees us in a list paired response.
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    public LightifyDeviceState getLightifyDeviceState() {
        return lightifyDeviceState;
    }

    public LightifyDeviceConfiguration getConfiguration() {
        return configuration;
    }

    public void setMinimumWhiteTemperature(int temperature) {
        thing.setProperty(PROPERTY_MINIMUM_WHITE_TEMPERATURE, Integer.toString(temperature));

        if (configuration.whiteTemperatureMin == null) {
            configuration.whiteTemperatureMin = temperature;
        }
    }

    public void setMaximumWhiteTemperature(int temperature) {
        thing.setProperty(PROPERTY_MAXIMUM_WHITE_TEMPERATURE, Integer.toString(temperature));

        if (configuration.whiteTemperatureMax == null) {
            configuration.whiteTemperatureMax = temperature;
        }
    }
}
