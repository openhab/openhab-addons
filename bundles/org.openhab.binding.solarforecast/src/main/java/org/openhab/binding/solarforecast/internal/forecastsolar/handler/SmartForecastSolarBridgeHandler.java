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
package org.openhab.binding.solarforecast.internal.forecastsolar.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.CHANNEL_CORRECTION_FACTOR;

import java.util.Iterator;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarforecast.internal.SolarForecastException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartForecastSolarBridgeHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SmartForecastSolarBridgeHandler extends ForecastSolarBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SmartForecastSolarBridgeHandler.class);

    public SmartForecastSolarBridgeHandler(Bridge bridge, Optional<PointType> location) {
        super(bridge, location);
        logger.info("constructor SmartForecastSolarBridgeHandler called with location {}", location);
    }

    @Override
    public synchronized void forecastUpdate() {
        super.forecastUpdate();
        logger.info("forecastUpdate");
        double energyProductionSum = 0;
        double forecastProductionSum = 0;
        for (Iterator<ForecastSolarPlaneHandler> iterator = planes.iterator(); iterator.hasNext();) {
            try {
                SmartForecastSolarPlaneHandler sfph = (SmartForecastSolarPlaneHandler) iterator.next();
                energyProductionSum += sfph.getEnergyProduction();
                forecastProductionSum += sfph.getForecastProduction();
            } catch (SolarForecastException sfe) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "@text/solarforecast.site.status.exception [\"" + sfe.getMessage() + "\"]");
            }
        }
        double factor = 1;
        if (forecastProductionSum > 0) {
            factor = energyProductionSum / forecastProductionSum;
        }
        logger.info("forecastUpdate E3DC {}, Forecast {} factor {}", energyProductionSum, forecastProductionSum,
                factor);

        // calculate new correction factor out of each plane and their production values
        updateState(CHANNEL_CORRECTION_FACTOR, new DecimalType(factor));
    }
}
