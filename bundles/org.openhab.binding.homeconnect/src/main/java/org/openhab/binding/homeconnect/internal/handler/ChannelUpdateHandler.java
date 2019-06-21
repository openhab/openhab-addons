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
package org.openhab.binding.homeconnect.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;

/**
 * The {@link ChannelUpdateHandler} is responsible for handling events, which where send via Server-Sent event
 * interface.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public interface ChannelUpdateHandler {
    void handle(ChannelUID channelUID, ExpiringCacheMap<ChannelUID, State> cache)
            throws CommunicationException, AuthorizationException;
}
