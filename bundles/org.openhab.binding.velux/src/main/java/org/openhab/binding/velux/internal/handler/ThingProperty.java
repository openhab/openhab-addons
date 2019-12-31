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
package org.openhab.binding.velux.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * The class {@link ThingProperty} provides methods for dealing with
 * properties.
 * <ul>
 * <li>{@link #exists} Check existence of a property,</LI>
 * <li>{@link #getValue} Returns a property value,</LI>
 * <li>{@link #setValue} Modifies a property value.</LI>
 * </UL>
 * <P>
 * Noninstantiable utility class
 * </P>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class ThingProperty {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThingProperty.class);

    /*
     * ************************
     * ***** Constructors *****
     */

    // Suppress default constructor for non-Instantiability

    private ThingProperty() {
        throw new AssertionError();
    }

    /*
     * **************************
     * ***** Public Methods *****
     */

    /**
     * Check existence of the property value for the given channel and
     * desired propertyName which are defined within VeluxBindingProperties.
     * <p>
     *
     * @param bridge which handles the mentioned Things,
     * @param channelUID describes the channel to by scrutinized,
     * @param propertyName defines the property which is to be evaluated.
     * @return <b>exists</B> of type boolean.
     */
    static boolean exists(BaseBridgeHandler bridge, ChannelUID channelUID, String propertyName) {
        ThingUID channelTUID = channelUID.getThingUID();
        Thing thingOfChannel = bridge.getThingByUID(channelTUID);
        boolean exists = false;
        if (thingOfChannel == null) {
            LOGGER.warn("exists(): Channel {} does not belong to a thing.", channelUID);
        } else {
            if (thingOfChannel.getConfiguration().get(propertyName) != null) {
                exists = true;
            }
        }
        LOGGER.trace("exists({},{}) returns {}.", channelUID, propertyName, exists);
        return exists;
    }

    /**
     * Return the property value of type Object for the given channel and
     * desired propertyName which are defined within VeluxBindingProperties.
     * <p>
     *
     * @param bridge which handles the mentioned Things,
     * @param channelUID describes the channel to by scrutinized,
     * @param propertyName defines the property which is to be evaluated.
     * @return <b>propertyValue</B> of type {@link Object}. Will return {@code null}, if not found, or if value itself
     *         is {@code null}.
     */
    static Object getValue(BaseBridgeHandler bridge, ChannelUID channelUID, String propertyName) {
        ThingUID channelTUID = channelUID.getThingUID();
        Thing thingOfChannel = bridge.getThingByUID(channelTUID);
        if (thingOfChannel == null) {
            LOGGER.warn("getValue(): Channel {} does not belong to a thing.", channelUID);
            return true;
        }
        Object propertyValue = thingOfChannel.getConfiguration().get(propertyName);
        LOGGER.trace("getValue({},{}) returns {}.", channelUID, propertyName, propertyValue);
        return propertyValue;
    }

    /**
     * Modifies the property value for the givenpropertyName which are defined within
     * VeluxBindingProperties.
     * <p>
     *
     * @param thing with property will be modified,
     * @param propertyName defines the property which is to be modified.
     * @param propertyValue defines the new property value.
     */
    public static void setValue(Thing thing, String propertyName, Object propertyValue) {
        thing.setProperty(propertyName, propertyValue.toString());
        LOGGER.trace("setValue() {} set to {}.", propertyName, propertyValue);
        return;
    }

    /**
     * Modifies the property value for the given bridge and desired propertyName which are defined within
     * VeluxBindingProperties.
     * <p>
     *
     * @param bridgeHandler which contains the properties,
     * @param channelUID describes the channel to by scrutinized,
     * @param propertyName defines the property which is to be modified.
     * @param propertyValue defines the new property value.
     */
    static void setValue(ExtendedBaseBridgeHandler bridgeHandler, ChannelUID channelUID, String propertyName,
            Object propertyValue) {
        ThingUID channelTUID = channelUID.getThingUID();
        Thing thingOfChannel = bridgeHandler.getThingByUID(channelTUID);
        if (thingOfChannel == null) {
            LOGGER.warn("setValue(): Channel {} does not belong to a thing.", channelUID);
            return;
        }
        thingOfChannel.setProperty(propertyName, propertyValue.toString());
        LOGGER.trace("setValue() {} set to {}.", propertyName, propertyValue);
        return;
    }

    /**
     * Modifies the property value for the given bridge and desired propertyName which are defined within
     * VeluxBindingProperties.
     * <p>
     *
     * @param bridgeHandler which contains the properties,
     * @param propertyName defines the property which is to be modified.
     * @param propertyValue defines the new property value.
     */
    static void setValue(ExtendedBaseBridgeHandler bridgeHandler, String propertyName, Object propertyValue) {
        Map<String, String> properties = bridgeHandler.editProperties();
        properties.put(propertyName, propertyValue.toString());
        bridgeHandler.updateProperties(properties);
        LOGGER.trace("setValue() {} set to {}.", propertyName, propertyValue);
        return;
    }

}
