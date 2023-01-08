/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.VeluxBindingProperties;
import org.openhab.binding.velux.internal.VeluxItemType;
import org.openhab.binding.velux.internal.handler.utils.ExtendedBaseThingHandler;
import org.openhab.binding.velux.internal.handler.utils.StateUtils;
import org.openhab.binding.velux.internal.handler.utils.ThingProperty;
import org.openhab.binding.velux.internal.utils.Localization;
import org.openhab.binding.velux.internal.utils.ManifestInformation;
import org.openhab.core.common.AbstractUID;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * The class is responsible for representing the overall status of the Velux binding.
 * <P>
 * Beside the normal thing handling introduced by {@link ExtendedBaseThingHandler}, it provides a method:
 * <ul>
 * <li>{@link #updateBindingState} to enable other classes to modify the number of activated Velux bridges and
 * Things.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBindingHandler extends ExtendedBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(VeluxBindingHandler.class);

    /*
     * ***************************
     * ***** Private Objects *****
     */
    private Thing thing;
    private Localization localization;
    private Integer currentNumberOfBridges = 0;
    private Integer currentNumberOfThings = 0;

    /*
     * ************************
     * ***** Constructors *****
     */

    public VeluxBindingHandler(Thing thing, final Localization localization) {
        super(thing);
        this.thing = thing;
        this.localization = localization;
        logger.trace("VeluxBindingHandler(constructor) called.");
    }

    /*
     * ***************************
     * ***** Private Methods *****
     */

    /**
     * Provide the ThingType for a given Channel.
     * <P>
     * Separated into this private method to deal with the deprecated method.
     * </P>
     *
     * @param channelUID for type {@link ChannelUID}.
     * @return thingTypeUID of type {@link ThingTypeUID}.
     */
    private ThingTypeUID thingTypeUIDOf(ChannelUID channelUID) {
        String[] segments = channelUID.getAsString().split(AbstractUID.SEPARATOR);
        if (segments.length > 1) {
            return new ThingTypeUID(segments[0], segments[1]);
        }
        logger.warn("thingTypeUIDOf({}) failed.", channelUID);
        return new ThingTypeUID(VeluxBindingConstants.BINDING_ID, VeluxBindingConstants.UNKNOWN_THING_TYPE_ID);
    }

    /**
     * Returns a human-readable representation of the binding state. This should help especially unexperienced user to
     * blossom up the introduction of the Velux binding.
     *
     * @return bindingInformation of type {@link String}.
     */
    private String bridgeCountToString() {
        String information;
        switch (currentNumberOfBridges) {
            case 0:
                information = localization.getText("runtime.no-bridge");
                break;
            case 1:
                information = localization.getText("runtime.one-bridge");
                break;
            default:
                information = localization.getText("runtime.multiple-bridges");
                break;
        }
        return information;
    }

    /**
     * Modifies all informations within openHAB to inform the user about the current state of this binding. That is:
     * <UL>
     * <LI>Update the properties about bundle version, number of bridges and things,</LI>
     * <LI>Usability of the binding in respect to defined bridges within the ThingStatus, and</LI>
     * <LI>Information of the binding state as channel value.</LI>
     * </UL>
     */
    private void updateVisibleInformation() {
        logger.trace("updateVisibleInformation(): updating properties.");
        ThingProperty.setValue(thing, VeluxBindingProperties.PROPERTY_BINDING_BUNDLEVERSION,
                ManifestInformation.getBundleVersion());
        ThingProperty.setValue(thing, VeluxBindingProperties.PROPERTY_BINDING_NOOFBRIDGES,
                currentNumberOfBridges.toString());
        ThingProperty.setValue(thing, VeluxBindingProperties.PROPERTY_BINDING_NOOFTHINGS,
                currentNumberOfThings.toString());

        // BaseThingHandler is sensitive during initialization phase. Therefore, to avoid (wrong) warnings about:
        // "tried updating the thing status although the handler was already disposed."
        if (this.isInitialized()) {
            logger.trace("updateVisibleInformation(): updating thing status.");
            if (currentNumberOfBridges < 1) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, bridgeCountToString());
            } else {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, bridgeCountToString());
            }
            logger.trace("updateVisibleInformation(): updating all channels.");
            for (Channel channel : thing.getChannels()) {
                handleCommand(channel.getUID(), RefreshType.REFRESH);
            }
        }
    }

    /*
     * *******************************************************************
     * ***** Objects and Methods for abstract class BaseThingHandler *****
     */

    @Override
    public void initialize() {
        logger.debug("initialize() called.");
        // The framework requires you to return from this method quickly.
        // Setting the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);
        // Take care of unusual situations...
        if (scheduler.isShutdown()) {
            logger.warn("initialize(): scheduler is shutdown, aborting the initialization of this bridge.");
            return;
        }
        logger.trace("initialize(): preparing background initialization task.");
        // Background initialization...
        scheduler.execute(() -> {
            logger.trace("initialize.scheduled(): Setting ThingStatus to ONLINE.");
            updateStatus(ThingStatus.ONLINE);
            updateVisibleInformation();
            logger.debug("Velux Binding Info Element '{}' is initialized.", getThing().getUID());
        });
        logger.trace("initialize() done.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand({},{}) called.", channelUID.getAsString(), command);
        /*
         * ===========================================================
         * Common part
         */
        String channelId = channelUID.getId();
        State newState = null;
        String itemName = channelUID.getAsString();
        VeluxItemType itemType = VeluxItemType.getByThingAndChannel(thingTypeUIDOf(channelUID), channelUID.getId());

        if (command instanceof RefreshType) {
            /*
             * ===========================================================
             * Refresh part
             */
            logger.trace("handleCommand(): refreshing item {}.", itemName);
            switch (itemType) {
                case BINDING_INFORMATION:
                    newState = StateUtils.createState(bridgeCountToString());
                    break;
                default:
                    logger.trace("handleCommand(): cannot handle REFRESH on channel {} as it is of type {}.", itemName,
                            channelId);
            }
            if (newState != null) {
                logger.debug("handleCommand(): updating {} ({}) to {}.", itemName, channelUID, newState);
                updateState(channelUID, newState);
            } else {
                logger.info("handleCommand({},{}): updating of item {} failed.", channelUID.getAsString(), command,
                        itemName);
            }
        } else {
            /*
             * ===========================================================
             * Modification part
             */
            switch (channelId) {
                default:
                    logger.warn("handleCommand() cannot handle command {} on channel {} (type {}).", command, itemName,
                            itemType);
            }
        }
        logger.trace("handleCommand() done.");
    }

    /*
     * **********************************
     * ***** (Other) Public Methods *****
     */

    /**
     * Update the information about bridges and things.
     * <P>
     * Provided for instrumentation of factory class to update this set of information.
     * </P>
     *
     * @param veluxBridgeCount describing the number of initialized bridges.
     * @param veluxThingCount describing the number of initialized things (in addition to Thing of type
     *            BindingInformation).
     */
    public void updateBindingState(Integer veluxBridgeCount, Integer veluxThingCount) {
        currentNumberOfBridges = veluxBridgeCount;
        currentNumberOfThings = veluxThingCount;
        updateVisibleInformation();
    }
}
