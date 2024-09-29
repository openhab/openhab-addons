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
package org.openhab.binding.velux.internal.handler.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * The class {@link ThingConfiguration} provides methods for dealing with
 * properties.
 * <ul>
 * <li>{@link #exists} Check existence of a property,</LI>
 * <li>{@link #getValue} Returns a property value.</LI>
 * </UL>
 * <P>
 * Noninstantiable utility class
 * </P>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class ThingConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThingConfiguration.class);

    /*
     * ************************
     * ***** Constructors *****
     */

    // Suppress default constructor for non-Instantiability

    private ThingConfiguration() {
        throw new AssertionError();
    }

    /*
     * **************************
     * ***** Public Methods *****
     */

    /**
     * Check existence of the configuration value for the given channel and
     * desired configName which are defined within VeluxBindingProperties.
     * <p>
     *
     * @param bridge which handles the mentioned Things,
     * @param channelUID describes the channel to by scrutinized,
     * @param configName defines the configuration entry which is to be evaluated.
     * @return <b>exists</B> of type boolean.
     */
    public static boolean exists(BaseBridgeHandler bridge, ChannelUID channelUID, String configName) {
        ThingUID channelTUID = channelUID.getThingUID();
        Thing thingOfChannel = bridge.getThing().getThing(channelTUID);
        boolean exists = false;
        if (thingOfChannel == null) {
            LOGGER.debug("exists(): Channel {} does not belong to a thing.", channelUID);
        } else {
            if (thingOfChannel.getConfiguration().get(configName) != null) {
                exists = true;
            }
        }
        LOGGER.trace("exists({},{}) returns {}.", channelUID, configName, exists);
        return exists;
    }

    /**
     * Return the property value of type Object for the given channel and
     * desired propertyName which are defined within VeluxBindingProperties.
     * <p>
     *
     * @param bridge which handles the mentioned Things,
     * @param channelUID describes the channel to by scrutinized,
     * @param configName defines the configuration entry which is to be evaluated.
     * @return <b>configurationValue</B> of type {@link Object}. Will return {@code null}, if not found, or if value
     *         itself
     *         is {@code null}.
     */
    public static Object getValue(BaseBridgeHandler bridge, ChannelUID channelUID, String configName) {
        ThingUID channelTUID = channelUID.getThingUID();
        Thing thingOfChannel = bridge.getThing().getThing(channelTUID);
        if (thingOfChannel == null) {
            LOGGER.warn("getValue(): Channel {} does not belong to a thing.", channelUID);
            return true;
        }
        Object configurationValue = thingOfChannel.getConfiguration().get(configName);
        LOGGER.trace("getValue({},{}) returns {}.", channelUID, configName, configurationValue);
        return configurationValue;
    }
}
