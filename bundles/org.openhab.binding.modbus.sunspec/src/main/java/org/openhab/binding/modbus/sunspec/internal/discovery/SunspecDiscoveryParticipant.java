/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sunspec.internal.discovery;

import static org.openhab.binding.modbus.sunspec.internal.SunSpecConstants.SUPPORTED_THING_TYPES_UIDS;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.discovery.ModbusDiscoveryListener;
import org.openhab.binding.modbus.discovery.ModbusDiscoveryParticipant;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.core.thing.ThingTypeUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for sunspec
 *
 * @author Nagy Attila Gabor - initial contribution
 *
 */
@Component
@NonNullByDefault
public class SunspecDiscoveryParticipant implements ModbusDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(SunspecDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return new HashSet<ThingTypeUID>(SUPPORTED_THING_TYPES_UIDS.values());
    }

    @Override
    public void startDiscovery(ModbusEndpointThingHandler handler, ModbusDiscoveryListener listener) {
        logger.trace("Starting sunspec discovery");
        try {
            new SunspecDiscoveryProcess(handler, listener).detectModel();
        } catch (EndpointNotInitializedException ex) {
            logger.debug("Could not start discovery process");
            listener.discoveryFinished();
        }
    }
}
