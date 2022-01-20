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
package org.openhab.binding.silvercrestwifisocket.internal.handler;

import java.util.Set;

import org.openhab.binding.silvercrestwifisocket.internal.discovery.SilvercrestWifiSocketDiscoveryService;
import org.openhab.binding.silvercrestwifisocket.internal.entities.SilvercrestWifiSocketResponse;
import org.openhab.binding.silvercrestwifisocket.internal.runnable.SilvercrestWifiSocketUpdateReceiverRunnable;
import org.openhab.core.thing.Thing;

/**
 * The {@link SilvercrestWifiSocketMediator} is responsible for receiving all the UDP packets and route correctly to
 * each handler.
 *
 * @author Jaime Vaz - Initial contribution
 */
public interface SilvercrestWifiSocketMediator {

    /**
     * This method is called by the {@link SilvercrestWifiSocketUpdateReceiverRunnable}, when one new message has been
     * received.
     *
     * @param receivedMessage the {@link SilvercrestWifiSocketResponse} message.
     */
    void processReceivedPacket(final SilvercrestWifiSocketResponse receivedMessage);

    /**
     * Registers a new {@link Thing} and the corresponding {@link SilvercrestWifiSocketHandler}.
     *
     * @param thing the {@link Thing}.
     * @param handler the {@link SilvercrestWifiSocketHandler}.
     */
    void registerThingAndWifiSocketHandler(final Thing thing, final SilvercrestWifiSocketHandler handler);

    /**
     * Unregisters a {@link SilvercrestWifiSocketHandler} by the corresponding {@link Thing}.
     *
     * @param thing the {@link Thing}.
     */
    void unregisterWifiSocketHandlerByThing(final Thing thing);

    /**
     * Returns all the {@link Thing} registered.
     *
     * @returns all the {@link Thing}.
     */
    Set<Thing> getAllThingsRegistred();

    /**
     * Sets the discovery service to inform the when one new thing has been found.
     *
     * @param discoveryService the discovery service.
     */
    void setDiscoveryService(SilvercrestWifiSocketDiscoveryService discoveryService);
}
