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
package org.openhab.binding.connectedcar.internal.api.carnet;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.CarUtils.getString;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiConstants.CNAPI_SERVICE_CAR_FINDER;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.GeoPosition;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.handler.CarNetVehicleHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetServiceCarFinder} implements the carFinder service.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetServiceCarFinder extends ApiBaseService {
    private final Logger logger = LoggerFactory.getLogger(CarNetServiceCarFinder.class);

    public CarNetServiceCarFinder(CarNetVehicleHandler thingHandler, CarNetApi api) {
        super(CNAPI_SERVICE_CAR_FINDER, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> ch) throws ApiException {
        addChannels(ch, true, CHANNEL_LOCATTION_GEO, CHANNEL_LOCATTION_TIME, CHANNEL_LOCATTION_ADDRESS,
                CHANNEL_PARK_LOCATION, CHANNEL_PARK_ADDRESS, CHANNEL_PARK_TIME);
        return true;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        boolean updated = false;
        try {
            logger.debug("{}: Get Vehicle Position", thingId);
            GeoPosition position = api.getVehiclePosition();
            updated |= updateChannel(CHANNEL_LOCATTION_GEO, position.getAsPointType());

            String time = position.getCarSentTime();
            updated |= updateChannel(CHANNEL_LOCATTION_TIME, new DateTimeType(time));
            updated |= updateAddress(position, CHANNEL_LOCATTION_ADDRESS);

            position = api.getStoredPosition();
            updated |= updateChannel(CHANNEL_PARK_LOCATION, position.getAsPointType());
            updated |= updateChannel(CHANNEL_LOCATTION_TIME, new DateTimeType(time));
            updated |= updateAddress(position, CHANNEL_PARK_ADDRESS);
            String parkingTime = getString(position.getParkingTime());
            updated |= updateChannel(CHANNEL_PARK_TIME,
                    !parkingTime.isEmpty() ? getDateTime(parkingTime) : UnDefType.UNDEF);
            updated |= updateChannel(CHANNEL_CAR_MOVING, OnOffType.OFF);
        } catch (ApiException e) {
            updateChannel(CHANNEL_LOCATTION_GEO, UnDefType.UNDEF);
            updateChannel(CHANNEL_LOCATTION_TIME, UnDefType.UNDEF);
            if (e.getApiResult().httpCode == HttpStatus.NO_CONTENT_204) {
                updated |= updateChannel(CHANNEL_CAR_MOVING, OnOffType.ON);
            } else {
                throw e;
            }
        }
        return updated;
    }

    private boolean updateAddress(GeoPosition position, String channel) {
        return updateLocationAddress(position.getAsPointType(), channel);
    }
}
