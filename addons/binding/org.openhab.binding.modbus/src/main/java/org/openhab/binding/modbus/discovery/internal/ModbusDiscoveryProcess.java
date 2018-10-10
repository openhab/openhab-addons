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
package org.openhab.binding.modbus.discovery.internal;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.openhab.binding.modbus.discovery.ModbusDiscoveryListener;
import org.openhab.binding.modbus.discovery.ModbusDiscoveryParticipant;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Each instance of this class is responsible the handle a discovery
 * process of a Modbus endpoint.
 *
 * It should call each participant in series to get all the
 * available things available at the endpoint.
 *
 * @author Nagy Attila Gabor - initial contribution
 *
 */
public class ModbusDiscoveryProcess {
    private final Logger logger = LoggerFactory.getLogger(ModbusDiscoveryProcess.class);

    // Set of the registered participants
    private final List<ModbusDiscoveryParticipant> participants;

    // The handler that should be tested for known things
    private final ModbusEndpointThingHandler handler;

    // The main service that started us. This gets called when a thing has
    // been discovered
    private final ModbusDiscoveryService service;

    public ModbusDiscoveryProcess(ModbusDiscoveryService service, Set<ModbusDiscoveryParticipant> participants,
            ModbusEndpointThingHandler handler) {
        this.participants = new CopyOnWriteArrayList<ModbusDiscoveryParticipant>(participants);
        this.handler = handler;
        this.service = service;
    }

    /**
     * Start the discovery process
     */
    public void start() {
        startNextParticipant();
    }

    /**
     * Run the next participant's discovery process
     */
    private void startNextParticipant() {
        if (participants.size() == 0) {
            logger.trace("All participants has finished");
            service.scanFinished();
            return; // We're finished, this will exit the process
        }

        ModbusDiscoveryParticipant participant = participants.remove(0);

        logger.debug("Starting discovery of participant {}", participant);

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
                startNextParticipant();
            }
        });
    }
}
