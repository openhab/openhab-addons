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
package org.openhab.binding.omnilink.internal.handler;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequest;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequests;
import org.openhab.binding.omnilink.internal.exceptions.BridgeOfflineException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.properties.AreaProperties;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;

/**
 * The {@link AbstractOmnilinkHandler} defines some methods that can be used across
 * the many different things exposed by the OmniLink protocol
 *
 * @author Brian O'Connell - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public abstract class AbstractOmnilinkHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(AbstractOmnilinkHandler.class);

    public AbstractOmnilinkHandler(Thing thing) {
        super(thing);
    }

    public @Nullable OmnilinkBridgeHandler getOmnilinkBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return (OmnilinkBridgeHandler) bridge.getHandler();
        } else {
            return null;
        }
    }

    protected void sendOmnilinkCommand(int message, int param1, int param2) {
        try {
            final OmnilinkBridgeHandler bridge = getOmnilinkBridgeHandler();
            if (bridge != null) {
                bridge.sendOmnilinkCommand(message, param1, param2);
            } else {
                logger.debug("Received null bridge while sending OmniLink command!");
            }
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Could not send command to OmniLink Controller: {}", e.getMessage());
        }
    }

    /**
     * Calculate the area filter the a supplied area
     *
     * @param area Area to calculate filter for.
     * @return Calculated Bit Filter for the supplied area. Bit 0 is area 1, bit 2 is area 2 and so on.
     */
    protected static int bitFilterForArea(AreaProperties areaProperties) {
        return BigInteger.ZERO.setBit(areaProperties.getNumber() - 1).intValue();
    }

    protected @Nullable List<AreaProperties> getAreaProperties() {
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
        List<AreaProperties> areas = new LinkedList<>();

        if (bridgeHandler != null) {
            ObjectPropertyRequest<AreaProperties> objectPropertyRequest = ObjectPropertyRequest
                    .builder(bridgeHandler, ObjectPropertyRequests.AREA, 0, 1).build();

            for (AreaProperties areaProperties : objectPropertyRequest) {
                String thingName = areaProperties.getName();
                if (areaProperties.getNumber() == 1 && "".equals(thingName)) {
                    areas.add(areaProperties);
                    break;
                } else if ("".equals(thingName)) {
                    break;
                } else {
                    areas.add(areaProperties);
                }
            }
        }
        return areas;
    }

    /**
     * Gets the configured number for a thing.
     *
     * @return Configured number for a thing.
     */
    protected int getThingNumber() {
        return ((Number) getThing().getConfiguration().get(THING_PROPERTIES_NUMBER)).intValue();
    }

    /**
     * Gets the configured area number for a thing.
     *
     * @return Configured area number for a thing.
     */
    protected int getAreaNumber() {
        String areaNumber = getThing().getProperties().get(THING_PROPERTIES_AREA);
        if (areaNumber != null) {
            return Integer.valueOf(areaNumber);
        } else {
            return -1;
        }
    }
}
