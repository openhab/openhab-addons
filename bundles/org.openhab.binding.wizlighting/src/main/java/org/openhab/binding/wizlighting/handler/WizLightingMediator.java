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
package org.openhab.binding.wizlighting.handler;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.wizlighting.internal.discovery.WizLightingDiscoveryService;
import org.openhab.binding.wizlighting.internal.entities.WizLightingResponse;
import org.openhab.binding.wizlighting.internal.entities.WizLightingSyncResponse;
import org.openhab.binding.wizlighting.internal.runnable.WizLightingUpdateReceiverRunnable;

/**
 * The {@link WizLightingMediator} is responsible for receiving all the sync packets and route correctly to
 * each handler.
 *
 * @author Sriram Balakrishnan - Initial contribution
 */
public interface WizLightingMediator {

    /**
     * This method is called by the {@link WizLightingUpdateReceiverRunnable}, when one new message has been
     * received.
     *
     * @param receivedMessage the {@link WizLightingResponse} message.
     */
    void processReceivedPacket(final WizLightingSyncResponse receivedMessage);

    /**
     * Registers a new {@link Thing} and the corresponding {@link WizLightingHandler}.
     *
     * @param thing the {@link Thing}.
     * @param handler the {@link WizLightingHandler}.
     */
    void registerThingAndWizBulbHandler(final Thing thing, final WizLightingHandler handler);

    /**
     * Unregisters a {@link WizLightingHandler} by the corresponding {@link Thing}.
     *
     * @param thing the {@link Thing}.
     */
    void unregisterWizBulbHandlerByThing(final Thing thing);

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
    void setDiscoveryService(WizLightingDiscoveryService discoveryService);
}
