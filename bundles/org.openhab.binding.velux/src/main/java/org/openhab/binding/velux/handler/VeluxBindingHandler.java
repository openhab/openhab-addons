/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.velux.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.velux.internal.VeluxItemType;
import org.openhab.binding.velux.internal.utils.Localization;
import org.openhab.binding.velux.internal.utils.LoggerFulltrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * The class is responsible for representing the overall status of the Velux binding.
 * <P>
 * Beside the normal thing handling introduced by {@link BaseThingHandler}, it provides a method:
 * <ul>
 * <li>{@link #updateNoOfBridges} enable other classes to modify the number of activated Velux bridges.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBindingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(VeluxBindingHandler.class);
    private final LoggerFulltrace log = new LoggerFulltrace(logger, false);

    /*
     * ***************************
     * ***** Private Objects *****
     */
    private Integer currentNumberOfBridges = 0;

    /*
     * ************************
     * ***** Constructors *****
     */

    public VeluxBindingHandler(Thing thing) {
        super(thing);
        logger.trace("VeluxHandler(constructor) called.");
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
    @SuppressWarnings("deprecation")
    private ThingTypeUID thingTypeUIDOf(ChannelUID channelUID) {
        log.fulltrace("thingTypeUIDOf({}) called.", channelUID);
        return channelUID.getThingUID().getThingTypeUID();
    }

    /**
     * Returns a human-readable representation of the binding state. This should help especially unexperienced user to
     * blossom up the introduction of the Velux binding.
     *
     * @return bindingInformation of type {@link String}.
     */
    private String bridgeCountToString() {
        String information = Localization.getText("@text/runtime.multiple-bridges");
        switch (currentNumberOfBridges) {
            case 0:
                information = Localization.getText("@text/runtime.no-bridge");
                break;
            case 1:
                information = Localization.getText("@text/runtime.one-bridge");
                break;
        }
        return information;
    }

    /*
     * *******************************************************************
     * ***** Objects and Methods for abstract class BaseThingHandler *****
     */

    @Override
    public void initialize() {
        logger.trace("initialize() called.");
        updateNoOfBridges(0);
        logger.trace("initialize() done.");
    }

    @Override
    public void dispose() {
        logger.trace("dispose() called.");
        super.dispose();
    }

    /**
     * NOTE: It takes care by calling {@link #handleCommand} with the REFRESH command, that every used channel is
     * initialized.
     */
    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.trace("channelLinked({}) called.", channelUID.getAsString());
        handleCommand(channelUID, RefreshType.REFRESH);
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
            logger.trace("handleCommand(): found COMMAND {}.", command);

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
     * Modifies the number of activated Velux bridges, which is reflected in the Thing representing the overall status
     * of this binding.
     *
     * @param bridgeCount as Integer.
     */

    public void updateNoOfBridges(Integer bridgeCount) {
        logger.trace("updateNoOfBridges({}) called.", bridgeCount);
        this.currentNumberOfBridges = bridgeCount;
        if (bridgeCount < 1) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, bridgeCountToString());
        } else {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, bridgeCountToString());
        }
        logger.trace("updateNoOfBridges() done.");
    }

}
