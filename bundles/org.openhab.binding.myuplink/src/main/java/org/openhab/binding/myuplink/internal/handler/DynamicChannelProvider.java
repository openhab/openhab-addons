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
package org.openhab.binding.myuplink.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myuplink.internal.provider.ChannelFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

/**
 * this interface provides all methods which deal with channels
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface DynamicChannelProvider {

    /**
     * registers a channel with the thing.
     *
     * @param channel
     *            the channel to be registered.
     */
    void registerChannel(Channel channel);

    /**
     * Simple Getter to retrieve the Channelfactory of this thing.
     *
     * @return
     *         the ChannelFactory
     */
    ChannelFactory getChannelFactory();

    /**
     * Simple Getter to retrieve the ThingUid
     *
     * @return
     *         the ThingUid
     */
    ThingUID getThingUid();
}
