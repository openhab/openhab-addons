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
package org.openhab.binding.freeboxos.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.Endpoint;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EpType;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.HomeNode;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;

/**
 * The {@link ShutterConfiguration} is responsible for holding configuration informations associated to a Basic
 * Shutter thing type
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ShutterConfiguration extends ClientConfiguration {
    private static final String POSITION_SLOT_ID = "positionSlotId";
    private static final String STOP_SLOT_ID = "stopSlotId";
    private static final String TOGGLE_SLOT_ID = "toggleSlotId";
    private static final String POSITION_SIGNAL_ID = "positionSignalId";
    private static final String STATE_SIGNAL_ID = "stateSignalId";

    public int positionSlotId = 0;
    public int stopSlotId = 1;
    public int toggleSlotId = 2;
    public int positionSignalId = 3;
    public int stateSignalId = 4;

    public static void configure(DiscoveryResultBuilder discoveryResultBuilder, HomeNode homeNode)
            throws FreeboxException {
        for (Endpoint endPoint : homeNode.showEndpoints()) {
            String name = endPoint.name();
            if (EpType.SLOT.equals(endPoint.epType()) && name != null) {
                switch (name) {
                    case "position_set":
                        discoveryResultBuilder.withProperty(POSITION_SLOT_ID, endPoint.id());
                        break;
                    case "stop":
                        discoveryResultBuilder.withProperty(STOP_SLOT_ID, endPoint.id());
                        break;
                    case "toggle":
                        discoveryResultBuilder.withProperty(TOGGLE_SLOT_ID, endPoint.id());
                        break;
                    default:
                        throw new FreeboxException("Unexpected endpoint name :" + name);
                }
            } else if (EpType.SIGNAL.equals(endPoint.epType()) && name != null) {
                switch (name) {
                    case "position_set":
                        discoveryResultBuilder.withProperty(POSITION_SIGNAL_ID, endPoint.id());
                        break;
                    case "state":
                        discoveryResultBuilder.withProperty(STATE_SIGNAL_ID, endPoint.id());
                        break;
                    default:
                        throw new FreeboxException("Unexpected endpoint name :" + name);
                }
            }
        }
    }
}
