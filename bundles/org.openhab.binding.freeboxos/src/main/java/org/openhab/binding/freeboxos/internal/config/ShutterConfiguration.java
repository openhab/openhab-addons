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
package org.openhab.binding.freeboxos.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.home.HomeNode;
import org.openhab.binding.freeboxos.internal.api.home.HomeNodeEndpoint;
import org.openhab.binding.freeboxos.internal.api.home.HomeNodeEndpoint.EpType;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;

/**
 * The {@link ShutterConfiguration} is responsible for holding configuration informations associated to a Basic
 * Shutter thing type
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ShutterConfiguration extends ClientConfiguration {
    public static final String POSITION_SLOT_ID = "positionSlotId";
    public static final String STOP_SLOT_ID = "stopSlotId";
    public static final String TOGGLE_SLOT_ID = "toggleSlotId";
    public static final String POSITION_SIGNAL_ID = "positionSignalId";
    public static final String STATE_SIGNAL_ID = "stateSignalId";

    public int positionSlotId = 0;
    public int stopSlotId = 1;
    public int toggleSlotId = 2;
    public int positionSignalId = 3;
    public int stateSignalId = 4;

    public static void configure(DiscoveryResultBuilder discoveryResultBuilder, HomeNode homeNode)
            throws FreeboxException {
        for (HomeNodeEndpoint endPoint : homeNode.getShowEndpoints()) {
            String name = endPoint.getName();
            if (EpType.SLOT.equals(endPoint.getEpType()) && name != null) {
                switch (name) {
                    case "position_set":
                        discoveryResultBuilder.withProperty(POSITION_SLOT_ID, endPoint.getId());
                        break;
                    case "stop":
                        discoveryResultBuilder.withProperty(STOP_SLOT_ID, endPoint.getId());
                        break;
                    case "toggle":
                        discoveryResultBuilder.withProperty(TOGGLE_SLOT_ID, endPoint.getId());
                        break;
                    default:
                        throw new FreeboxException("Unexpected endpoint name :" + name);
                }
            } else if (EpType.SIGNAL.equals(endPoint.getEpType()) && name != null) {
                switch (name) {
                    case "position_set":
                        discoveryResultBuilder.withProperty(POSITION_SIGNAL_ID, endPoint.getId());
                        break;
                    case "state":
                        discoveryResultBuilder.withProperty(STATE_SIGNAL_ID, endPoint.getId());
                        break;
                    default:
                        throw new FreeboxException("Unexpected endpoint name :" + name);
                }
            }
        }
    }
}
