/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal.handler;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.handler.utils.ExtendedBaseThingHandler;
import org.openhab.binding.velux.internal.handler.utils.Thing2VeluxActuator;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.utils.Localization;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * The{@link VeluxHandler} is responsible for handling commands, which are
 * sent via {@link VeluxBridgeHandler} to one of the channels.
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxHandler extends ExtendedBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(VeluxHandler.class);

    public VeluxHandler(Thing thing, Localization localization) {
        super(thing);
        logger.trace("VeluxHandler(thing={},localization={}) constructor called.", thing, localization);
    }

    @Override
    public void initialize() {
        logger.trace("initialize() called.");
        Bridge thisBridge = getBridge();
        logger.debug("initialize(): Initializing thing {} in combination with bridge {}.", getThing().getUID(),
                thisBridge);
        if (thisBridge == null) {
            logger.trace("initialize() updating ThingStatus to OFFLINE/CONFIGURATION_PENDING.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);

        } else if (thisBridge.getStatus() == ThingStatus.ONLINE) {
            logger.trace("initialize() updating ThingStatus to ONLINE.");
            updateStatus(ThingStatus.ONLINE);
            initializeProperties();
        } else {
            logger.trace("initialize() updating ThingStatus to OFFLINE/BRIDGE_OFFLINE.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
        logger.trace("initialize() done.");
    }

    private synchronized void initializeProperties() {
        logger.trace("initializeProperties() done.");
    }

    @Override
    public void dispose() {
        logger.trace("dispose() called.");
        super.dispose();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.trace("channelLinked({}) called.", channelUID.getAsString());

        if (thing.getStatus() == ThingStatus.ONLINE) {
            handleCommand(channelUID, RefreshType.REFRESH);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand({},{}) initiated by {}.", channelUID.getAsString(), command,
                Thread.currentThread());
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.trace("handleCommand() nothing yet to do as there is no bridge available.");
        } else {
            BridgeHandler handler = bridge.getHandler();
            if (handler == null) {
                logger.trace("handleCommand() nothing yet to do as thing is not initialized.");
            } else {
                handler.handleCommand(channelUID, command);
            }
        }
        logger.trace("handleCommand() done.");
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (isInitialized()) { // prevents change of address
            validateConfigurationParameters(configurationParameters);
            Configuration configuration = editConfiguration();
            for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
                logger.trace("handleConfigurationUpdate(): found modified config entry {}.",
                        configurationParameter.getKey());
                configuration.put(configurationParameter.getKey(), configurationParameter.getValue());
            }
            // persist new configuration and reinitialize handler
            dispose();
            updateConfiguration(configuration);
            initialize();
        } else {
            super.handleConfigurationUpdate(configurationParameters);
        }
    }

    /**
     * Remove previously statically created vane channel if the device does not support it. Or log a warning if it does
     * support a vane and the respective channel is missing.
     *
     * @param bridgeHandler the calling bridge handler.
     * @throws IllegalStateException if something went wrong.
     */
    public void updateDynamicChannels(VeluxBridgeHandler bridgeHandler) throws IllegalStateException {
        // roller shutters are the only things with a previously statically created vane channel
        if (!VeluxBindingConstants.THING_TYPE_VELUX_ROLLERSHUTTER.equals(thing.getThingTypeUID())) {
            return;
        }

        String id = VeluxBindingConstants.CHANNEL_VANE_POSITION;
        ChannelUID uid = new ChannelUID(thing.getUID(), id);
        Thing2VeluxActuator actuator = new Thing2VeluxActuator(bridgeHandler, uid);
        VeluxProduct product = bridgeHandler.existingProducts().get(actuator.getProductBridgeIndex());

        if (product.equals(VeluxProduct.UNKNOWN)) {
            throw new IllegalStateException("updateDynamicChannels(): Product unknown in the bridge");
        }

        Channel channel = thing.getChannel(id);
        boolean required = product.supportsVanePosition();

        if (!required && channel != null) {
            logger.debug("Removing unsupported channel for {}: {}", thing.getUID(), id);
            updateThing(editThing().withoutChannels(channel).build());
        } else if (required && channel == null) {
            logger.warn("Thing {} does not have a '{}' channel => please re-create it", thing.getUID(), id);
        }
    }
}
