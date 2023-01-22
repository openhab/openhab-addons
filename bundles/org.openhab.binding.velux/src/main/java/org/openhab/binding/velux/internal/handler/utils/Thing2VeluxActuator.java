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
package org.openhab.binding.velux.internal.handler.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.VeluxBindingProperties;
import org.openhab.binding.velux.internal.handler.VeluxBridgeHandler;
import org.openhab.binding.velux.internal.things.VeluxExistingProducts;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.openhab.binding.velux.internal.things.VeluxProductSerialNo;
import org.openhab.core.thing.ChannelUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * The class {@link Thing2VeluxActuator} provides simplified access to Velux device behind the Velux bridge by
 * evaluating the
 * Thing property belonging to a channel and comparing them with the bridge registered objects. To put it in a nutshell,
 * the methods provide a cache for faster access,
 * <ul>
 * <li>{@link #Thing2VeluxActuator} Constructor,</LI>
 * <li>{@link #isKnown} returns whether actuator is well-known,</LI>
 * <li>{@link #getProductBridgeIndex} returns the Velux bridge index for access,</LI>
 * <li>{@link #isInverted} returns a flag about value inversion.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class Thing2VeluxActuator {
    private final Logger logger = LoggerFactory.getLogger(Thing2VeluxActuator.class);

    // Class internal

    private VeluxBridgeHandler bridgeHandler;
    private ChannelUID channelUID;
    private boolean isInverted = false;
    private VeluxProduct thisProduct = VeluxProduct.UNKNOWN;

    // Private

    private void mapThing2Velux() {
        // only process real actuator things
        if (!VeluxBindingConstants.ACTUATOR_THINGS.contains(bridgeHandler.thingTypeUIDOf(channelUID))) {
            logger.trace("mapThing2Velux(): channel {} is not an Actuator Thing, exiting.", channelUID);
            return;
        }

        // the uniqueIndex is the serial number (if valid)
        String uniqueIndex = null;
        boolean invert = false;
        if (ThingConfiguration.exists(bridgeHandler, channelUID, VeluxBindingProperties.CONFIG_ACTUATOR_SERIALNUMBER)) {
            String serial = (String) ThingConfiguration.getValue(bridgeHandler, channelUID,
                    VeluxBindingProperties.CONFIG_ACTUATOR_SERIALNUMBER);
            invert = VeluxProductSerialNo.indicatesRevertedValues(serial);
            serial = VeluxProductSerialNo.cleaned(serial);
            uniqueIndex = ("".equals(serial) || serial.equals(VeluxProductSerialNo.UNKNOWN)) ? null : serial;
        }
        logger.trace("mapThing2Velux(): in {} serialNumber={}.", channelUID, uniqueIndex);

        // if serial number not valid, the uniqueIndex is name (if valid)
        if (uniqueIndex == null) {
            if (ThingConfiguration.exists(bridgeHandler, channelUID, VeluxBindingProperties.PROPERTY_ACTUATOR_NAME)) {
                String name = (String) ThingConfiguration.getValue(bridgeHandler, channelUID,
                        VeluxBindingProperties.PROPERTY_ACTUATOR_NAME);
                uniqueIndex = ("".equals(name) || name.equals(VeluxBindingConstants.UNKNOWN)) ? null : name;
            }
            logger.trace("mapThing2Velux(): in {} name={}.", channelUID, uniqueIndex);
        }

        if (uniqueIndex == null) {
            logger.warn("mapThing2Velux(): in {} cannot find a uniqueIndex, aborting.", channelUID);
            return;
        } else {
            logger.trace("mapThing2Velux(): in {} uniqueIndex={}, proceeding.", channelUID, uniqueIndex);
        }

        // handle value inversion
        if (!invert) {
            if (ThingConfiguration.exists(bridgeHandler, channelUID,
                    VeluxBindingProperties.PROPERTY_ACTUATOR_INVERTED)) {
                invert = (boolean) ThingConfiguration.getValue(bridgeHandler, channelUID,
                        VeluxBindingProperties.PROPERTY_ACTUATOR_INVERTED);
            }
        }
        isInverted = invert;
        logger.trace("mapThing2Velux(): in {} isInverted={}.", channelUID, isInverted);

        VeluxExistingProducts existing = bridgeHandler.bridgeParameters.actuators.getChannel().existingProducts;

        if (!existing.isRegistered(uniqueIndex)) {
            logger.warn("mapThing2Velux(): actuator with uniqueIndex={} is not registered", uniqueIndex);
            return;
        }

        logger.trace("mapThing2Velux(): fetching actuator for {}.", uniqueIndex);
        thisProduct = existing.get(uniqueIndex);
        logger.debug("mapThing2Velux(): found actuator {}.", thisProduct);
        return;
    }

    // Constructor

    /**
     * Constructor.
     * <P>
     *
     * @param thisBridgeHandler The Velux bridge handler with a specific communication protocol which provides
     *            information for this channel.
     * @param thisChannelUID The item passed as type {@link ChannelUID} for which a refresh is intended.
     */
    public Thing2VeluxActuator(VeluxBridgeHandler thisBridgeHandler, ChannelUID thisChannelUID) {
        bridgeHandler = thisBridgeHandler;
        channelUID = thisChannelUID;
    }

    // Public methods

    /**
     * Returns the Velux gateway index for accessing a Velux device based on the Thing configuration which belongs to
     * the channel passed during constructor.
     * <p>
     *
     * @return <b>bridgeProductIndex</B> for accessing the Velux device (or ProductBridgeIndex.UNKNOWN if not found).
     */
    public ProductBridgeIndex getProductBridgeIndex() {
        if (VeluxProduct.UNKNOWN.equals(thisProduct)) {
            mapThing2Velux();
        }
        if (VeluxProduct.UNKNOWN.equals(thisProduct)) {
            return ProductBridgeIndex.UNKNOWN;
        }
        return thisProduct.getBridgeProductIndex();
    }

    /**
     * Returns true, if the actuator is known within the bridge.
     * <p>
     *
     * @return <b>isKnown</B> as boolean.
     */
    public boolean isKnown() {
        return (!(ProductBridgeIndex.UNKNOWN.equals(getProductBridgeIndex())));
    }

    /**
     * Returns the flag whether a value inversion in intended for the Velux device based on the Thing configuration
     * which belongs to the channel passed during constructor.
     * <p>
     *
     * @return <b>isInverted</B> for handling of values of the Velux device (or false if not found)..
     */
    public boolean isInverted() {
        if (VeluxProduct.UNKNOWN.equals(thisProduct)) {
            mapThing2Velux();
        }
        if (VeluxProduct.UNKNOWN.equals(thisProduct)) {
            logger.warn("isInverted(): Thing not found in Velux Bridge.");
        }
        return isInverted;
    }
}
