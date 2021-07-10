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
package org.openhab.binding.octopusenergy.internal.handler;

import static org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;

import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyApiHelper;
import org.openhab.binding.octopusenergy.internal.dto.Consumption;
import org.openhab.binding.octopusenergy.internal.dto.ElectricityMeterPoint;
import org.openhab.binding.octopusenergy.internal.exception.RecordNotFoundException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OctopusEnergyElectricityMeterPointHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class OctopusEnergyElectricityMeterPointHandler extends OctopusEnergyBaseMeterPointHandler {

    private final Logger logger = LoggerFactory.getLogger(OctopusEnergyElectricityMeterPointHandler.class);

    public OctopusEnergyElectricityMeterPointHandler(Thing thing, OctopusEnergyApiHelper apiHelper) {
        super(thing, apiHelper);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(OctopusEnergyElectricityMeterPointActions.class);
    }

    public ElectricityMeterPoint getMeterPoint() throws RecordNotFoundException {
        return apiHelper.getAccounts().getElectricityMeterPoint(thing.getUID().getId());
    }

    @Override
    protected void updateThing() {
        try {
            ElectricityMeterPoint meterPoint = getMeterPoint();
            logger.debug("Updating all channels for meter point : {}", meterPoint);
            updateState(CHANNEL_METERPOINT_MPAN, new StringType(meterPoint.mpan));
            try {
                updateState(CHANNEL_METERPOINT_CURRENT_TARIFF,
                        new StringType(meterPoint.getAgreementAsOf(ZonedDateTime.now()).tariffCode));
            } catch (RecordNotFoundException e) {
                updateState(CHANNEL_METERPOINT_CURRENT_TARIFF, UnDefType.UNDEF);
            }
            try {
                Consumption consumption = meterPoint.getMostRecentConsumption();
                updateState(CHANNEL_METERPOINT_MOST_RECENT_CONSUPTION_AMOUNT,
                        new QuantityType<Energy>(consumption.consumption, Units.KILOWATT_HOUR));
                updateState(CHANNEL_METERPOINT_MOST_RECENT_CONSUPTION_START_TIME,
                        new DateTimeType(consumption.intervalStart));
                updateState(CHANNEL_METERPOINT_MOST_RECENT_CONSUPTION_END_TIME,
                        new DateTimeType(consumption.intervalEnd));
            } catch (RecordNotFoundException e) {
                updateState(CHANNEL_METERPOINT_MOST_RECENT_CONSUPTION_AMOUNT, UnDefType.UNDEF);
                updateState(CHANNEL_METERPOINT_MOST_RECENT_CONSUPTION_START_TIME, UnDefType.UNDEF);
                updateState(CHANNEL_METERPOINT_MOST_RECENT_CONSUPTION_END_TIME, UnDefType.UNDEF);
            }

            if (meterPoint.priceList.isEmpty()) {
                updateState(CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_START_TIME, UnDefType.UNDEF);
                updateState(CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_END_TIME, UnDefType.UNDEF);
                updateState(CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_MIN_AMOUNT, UnDefType.UNDEF);
                updateState(CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_MAX_AMOUNT, UnDefType.UNDEF);
            } else {
                updateState(CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_START_TIME,
                        new DateTimeType(meterPoint.priceList.get(0).validFrom));
                updateState(CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_END_TIME,
                        new DateTimeType(meterPoint.priceList.get(meterPoint.priceList.size() - 1).validTo));
                try {
                    updateState(CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_MIN_AMOUNT,
                            new DecimalType(meterPoint.getMinPrice(true)));
                    updateState(CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_MAX_AMOUNT,
                            new DecimalType(meterPoint.getMaxPrice(true)));
                } catch (RecordNotFoundException e) {
                    updateState(CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_MIN_AMOUNT, UnDefType.UNDEF);
                    updateState(CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_MAX_AMOUNT, UnDefType.UNDEF);
                }
            }
        } catch (RecordNotFoundException e) {
            logger.debug("Trying to update unknown meter point: {}", thing.getUID().getId());
        }
    }
}
