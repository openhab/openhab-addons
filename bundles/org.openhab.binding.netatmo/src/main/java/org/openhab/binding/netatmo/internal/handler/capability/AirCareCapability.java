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
package org.openhab.binding.netatmo.internal.handler.capability;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.AircareApi;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.handler.NACommonInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AirCareCapability} give the ability to read weather station api
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class AirCareCapability extends RestCapability<AircareApi> {
    private final Logger logger = LoggerFactory.getLogger(AirCareCapability.class);

    public AirCareCapability(NACommonInterface handler, ApiBridge apiBridge) {
        super(handler, apiBridge.getRestManager(AircareApi.class));
    }

    @Override
    public List<NAObject> updateReadings() {
        List<NAObject> result = new ArrayList<>();
        try {
            result.add(api.getHomeCoach(handlerId));
        } catch (NetatmoException e) {
            logger.warn("Error retrieving NHC data '{}' : {}", handler.getId(), e.getMessage());
        }
        return result;
    }
}
