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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.handler.utils.ExtendedBaseBridgeHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * The class {@link BridgeChannels} provides methods for dealing with
 * properties.
 * <ul>
 * <li>{@link #getAllChannelUIDs}</LI>
 * <li>{@link #getAllLinkedChannelUIDs}</LI>
 * </UL>
 * <P>
 * Noninstantiable utility class
 * </P>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
final class BridgeChannels {
    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeChannels.class);

    /*
     * ************************
     * ***** Constructors *****
     */

    // Suppress default constructor for non-instantiability

    private BridgeChannels() {
        throw new AssertionError();
    }

    /*
     * **************************
     * ***** Public Methods *****
     */

    /**
     * Return the Channel identifiers of all child things and the bridge things.
     * <p>
     *
     * @param bridge which will be scrutinized for things.
     * @return <b>channelUIDs</B> of type {@link Set} of {@link ChannelUID}s.
     */
    static Set<ChannelUID> getAllChannelUIDs(ExtendedBaseBridgeHandler bridge) {
        Set<ChannelUID> channelUIDs = new HashSet<>();
        Set<Thing> things = new HashSet<>(bridge.getThing().getThings());
        things.add(bridge.getThing());
        for (Thing thing : things) {
            for (Channel channel : thing.getChannels()) {
                channelUIDs.add(channel.getUID());
            }
        }
        LOGGER.trace("getAllChannelUIDs() returns {}.", channelUIDs);
        return channelUIDs;
    }

    /**
     * Return the Channel identifiers of all child things and the bridge things,
     * which are linked.
     * <p>
     *
     * @param bridge which will be scrutinized for things.
     * @return <b>channelUIDs</B> of type {@link Set} of {@link ChannelUID}s.
     */
    static Set<ChannelUID> getAllLinkedChannelUIDs(ExtendedBaseBridgeHandler bridge) {
        Set<ChannelUID> channelUIDs = new HashSet<>();
        Set<Thing> things = new HashSet<>(bridge.getThing().getThings());
        things.add(bridge.getThing());
        for (Thing thing : things) {
            for (Channel channel : thing.getChannels()) {
                if (bridge.isLinked(channel.getUID())) {
                    channelUIDs.add(channel.getUID());
                }
            }
        }
        LOGGER.trace("getAllLinkedChannelUIDs() returns {}.", channelUIDs);
        return channelUIDs;
    }
}
