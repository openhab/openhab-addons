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
package org.openhab.binding.velux.internal.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.config.VeluxThingConfiguration;
import org.openhab.binding.velux.internal.handler.utils.ExtendedBaseThingHandler;
import org.openhab.binding.velux.internal.handler.utils.Thing2VeluxActuator;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.utils.Localization;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
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

    private VeluxThingConfiguration configuration = new VeluxThingConfiguration();
    private final Localization localization;

    public VeluxHandler(Thing thing, Localization localization) {
        super(thing);
        this.localization = localization;
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
        configuration = getConfigAs(VeluxThingConfiguration.class);
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
     * Initialise the dynamic vane position channel if the respective device supports it.
     *
     * @param bridgeHandler the calling bridge handler.
     * @throws IllegalStateException if something went wrong.
     */
    public void updateDynamicChannels(VeluxBridgeHandler bridgeHandler) throws IllegalStateException {
        // roller shutters are the only things allowed to have vane support
        if (!VeluxBindingConstants.THING_TYPE_VELUX_ROLLERSHUTTER.equals(thing.getThingTypeUID())) {
            return;
        }

        ChannelUID vaneChannelUID = new ChannelUID(thing.getUID(), VeluxBindingConstants.CHANNEL_VANE_POSITION);

        VeluxProduct product = bridgeHandler.existingProducts()
                .get((new Thing2VeluxActuator(bridgeHandler, vaneChannelUID)).getProductBridgeIndex());
        if (product.equals(VeluxProduct.UNKNOWN)) {
            throw new IllegalStateException("initializeVanePositionChannel(): Product unknown in the bridge");
        }

        // predicate to filter the vane position channel
        Predicate<Channel> vaneChannelPredicate = c -> VeluxBindingConstants.CHANNEL_TYPE_VANE
                .equals(c.getChannelTypeUID());

        // note: this is initially an immutable list
        List<Channel> channels = thing.getChannels();

        // current and required state of the vane channel
        boolean vaneChannelExisting = channels.stream().anyMatch(vaneChannelPredicate);
        boolean vaneChannelRequired = product.supportsVanePosition();

        if (vaneChannelExisting == vaneChannelRequired) {
            // no change is needed
            return;
        }

        // make a mutable copy of the original immutable channel list
        channels = new ArrayList<>(channels);

        if (vaneChannelRequired) {
            // build and add the vane channel
            // @formatter:off
            channels.add(ChannelBuilder.create(vaneChannelUID)
                    .withType(VeluxBindingConstants.CHANNEL_TYPE_VANE)
                    .withKind(ChannelKind.STATE)
                    .withAcceptedItemType(CoreItemFactory.DIMMER)
                    .withDescription(localization.getText("channel-type.velux.vanePosition.description"))
                    .withLabel(localization.getText("channel-type.velux.vanePosition.label"))
                    .build());
            // @formatter:on
        } else {
            // remove the vane channel
            channels.removeIf(vaneChannelPredicate);
        }

        // if we got this far, then update the thing
        Thing thing = editThing().withChannels(channels).build();
        scheduler.submit(() -> updateThing(thing));
    }
}
