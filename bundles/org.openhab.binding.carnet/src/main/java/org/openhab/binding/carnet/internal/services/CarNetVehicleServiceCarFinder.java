/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.services;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;
import static org.openhab.binding.carnet.internal.CarNetUtils.getString;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.CNAPI_SERVICE_CAR_FINDER;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApi;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetVehiclePosition;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;
import org.openhab.binding.carnet.internal.provider.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetVehicleServiceCarFinder} implements the carFinder service.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetVehicleServiceCarFinder extends CarNetVehicleBaseService {
    private final Logger logger = LoggerFactory.getLogger(CarNetVehicleServiceCarFinder.class);

    public CarNetVehicleServiceCarFinder(CarNetVehicleHandler thingHandler, CarNetApi api) {
        super(thingHandler, api);
        serviceId = CNAPI_SERVICE_CAR_FINDER;
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> ch) throws CarNetException {
        boolean ok = false;
        try {
            api.getVehiclePosition();
            ok = true;
        } catch (CarNetException e) {
            if (e.getApiResult().httpCode == HttpStatus.NO_CONTENT_204) {
                ok = true; // Ignore No Content = Info not available, but valid API result
            }
        }

        if (ok) {
            addChannel(ch, CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_GEO, ITEMT_LOCATION, null, false, true);
            addChannel(ch, CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_TIME, ITEMT_DATETIME, null, false, true);
            addChannel(ch, CHANNEL_GROUP_LOCATION, CHANNEL_PARK_LOCATION, ITEMT_LOCATION, null, false, true);
            addChannel(ch, CHANNEL_GROUP_LOCATION, CHANNEL_PARK_TIME, ITEMT_DATETIME, null, false, true);
        }
        return ok;
    }

    @Override
    public boolean serviceUpdate() throws CarNetException {
        try {
            logger.debug("{}: Get Vehicle Position", thingId);
            CarNetVehiclePosition position = updateLocation(api.getVehiclePosition(), CHANNEL_LOCATTION_GEO);
            if (position != null) {
                String time = position.getCarSentTime();
                updateChannel(CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_TIME, new DateTimeType(time));

                updateLocation(api.getStoredPosition(), CHANNEL_PARK_LOCATION);
                String parkingTime = getString(position.getParkingTime());
                updateChannel(CHANNEL_GROUP_LOCATION, CHANNEL_PARK_TIME,
                        !parkingTime.isEmpty() ? getDateTime(parkingTime) : UnDefType.NULL);
                return true;
            }
        } catch (CarNetException e) {
            updateChannel(CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_GEO, UnDefType.UNDEF);
            updateChannel(CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_TIME, UnDefType.UNDEF);
            if (e.getApiResult().httpCode != HttpStatus.NO_CONTENT_204) { // Ignore No Content = Info not available
                throw e;
            }
        }
        return false;
    }

    private @Nullable CarNetVehiclePosition updateLocation(@Nullable CarNetVehiclePosition position, String channel) {
        if (position != null) {
            PointType location = new PointType(new DecimalType(position.getLattitude()),
                    new DecimalType(position.getLongitude()));
            updateChannel(CHANNEL_GROUP_LOCATION, channel, location);
        }
        return position;
    }
}
