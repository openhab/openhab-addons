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
package org.openhab.binding.modbus.discovery.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.discovery.ModbusDiscoveryListener;
import org.openhab.binding.modbus.discovery.ModbusDiscoveryParticipant;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A new instance of this class is created for each Modbus endpoint handler
 * that supports discovery.
 * This service gets called each time a discovery is requested, and it is
 * responsible to execute the discovery on the connected thing handler.
 * Actual discovery is done by the registered ModbusDiscoveryparticipants
 *
 * @author Nagy Attila Gabor - initial contribution
 *
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ModbusEndpointDiscoveryService.class)
@NonNullByDefault
public class ModbusEndpointDiscoveryService implements ModbusThingHandlerDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(ModbusEndpointDiscoveryService.class);

    // This is the handler we will do the discovery on
    private @Nullable ModbusEndpointThingHandler handler;

    // List of the registered participants
    // this only contains data when there is scan in progress
    private final List<ModbusDiscoveryParticipant> participants = new CopyOnWriteArrayList<>();

    // This is set true when we're waiting for a participant to finish discovery
    private boolean waitingForParticipant = false;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ModbusEndpointThingHandler thingHandler) {
            this.handler = thingHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return (ThingHandler) handler;
    }

    @Override
    public boolean startScan(ModbusDiscoveryService service) {
        ModbusEndpointThingHandler handler = this.handler;
        if (handler == null || !handler.isDiscoveryEnabled()) {
            return false;
        }
        logger.trace("Starting discovery on endpoint {}", handler.getUID().getAsString());

        participants.addAll(service.getDiscoveryParticipants());

        startNextParticipant(handler, service);

        return true;
    }

    @Override
    public boolean scanInProgress() {
        return !participants.isEmpty() || waitingForParticipant;
    }

    /**
     * Run the next participant's discovery process
     *
     * @param service reference to the ModbusDiscoveryService that will collect all the
     *            discovered items
     */
    private void startNextParticipant(final ModbusEndpointThingHandler handler, final ModbusDiscoveryService service) {
        if (participants.isEmpty()) {
            logger.trace("All participants has finished");
            service.scanFinished();
            return; // We're finished, this will exit the process
        }

        ModbusDiscoveryParticipant participant = participants.remove(0);

        waitingForParticipant = true;

        // Call startDiscovery on the next participant. The ModbusDiscoveryListener
        // callback will be notified each time a thing is discovered, and also when
        // the discovery is finished by this participant
        participant.startDiscovery(handler, new ModbusDiscoveryListener() {

            /**
             * Participant has found a thing
             */
            @Override
            public void thingDiscovered(DiscoveryResult result) {
                service.thingDiscovered(result);
            }

            /**
             * Participant finished discovery.
             * We can continue to the next participant
             */
            @Override
            public void discoveryFinished() {
                waitingForParticipant = false;
                startNextParticipant(handler, service);
            }
        });
    }
}
