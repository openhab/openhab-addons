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
package org.openhab.binding.wiz.internal.handler;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wiz.internal.discovery.WizDiscoveryService;
import org.openhab.binding.wiz.internal.entities.RegistrationRequestParam;
import org.openhab.binding.wiz.internal.entities.WizResponse;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;

/**
 * The {@link WizMediator} is responsible for receiving all the sync
 * packets and route correctly to each handler.
 *
 * @author Sriram Balakrishnan - Initial contribution
 * @author Joshua Freeman - pass through NetworkAddressService
 */
@NonNullByDefault
public interface WizMediator {

    /**
     * This method is called by the {@link WizUpdateReceiverRunnable}, when
     * one new message has been received.
     *
     * @param receivedMessage the {@link WizResponse} message.
     */
    void processReceivedPacket(final WizResponse receivedMessage);

    /**
     * Returns a {@link RegistrationRequestParam} based on the current OpenHAB
     * connection.
     *
     */
    RegistrationRequestParam getRegistrationParams() throws IllegalStateException;

    /**
     * Registers a new {@link Thing} and the corresponding
     * {@link WizHandler}.
     *
     * @param thing the {@link Thing}.
     * @param handler the {@link WizHandler}.
     */
    void registerThingAndWizBulbHandler(final Thing thing, final WizHandler handler);

    /**
     * Unregisters a {@link WizHandler} by the corresponding {@link Thing}.
     *
     * @param thing the {@link Thing}.
     */
    void unregisterWizBulbHandlerByThing(final Thing thing);

    /**
     * Returns all the {@link Thing} registered.
     *
     * @returns all the {@link Thing}.
     */
    Set<Thing> getAllThingsRegistered();

    /**
     * Sets the discovery service to inform the user when one new thing has been
     * found.
     *
     * @param discoveryService the discovery service.
     */
    void setDiscoveryService(final @Nullable WizDiscoveryService discoveryService);

    /**
     * Gets the NetworkAddressService used to configure the mediator instance.
     * 
     * @return networkAddressService
     */
    NetworkAddressService getNetworkAddressService();
}
