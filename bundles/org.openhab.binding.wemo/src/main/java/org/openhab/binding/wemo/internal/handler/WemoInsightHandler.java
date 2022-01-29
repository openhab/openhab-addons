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
package org.openhab.binding.wemo.internal.handler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoInsightHandler} is responsible for handling commands for
 * a WeMo Insight Switch.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class WemoInsightHandler extends WemoHandler {

    private final Logger logger = LoggerFactory.getLogger(WemoInsightHandler.class);
    private final Map<String, String> stateMap = new ConcurrentHashMap<String, String>();

    public WemoInsightHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpCaller) {
        super(thing, upnpIOService, wemoHttpCaller);
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        logger.debug("Received pair '{}':'{}' (service '{}') for thing '{}'",
                new Object[] { variable, value, service, this.getThing().getUID() });

        updateStatus(ThingStatus.ONLINE);

        if (!"BinaryState".equals(variable) && !"InsightParams".equals(variable)) {
            return;
        }

        if (variable != null && value != null) {
            this.stateMap.put(variable, value);
        }

        if (value != null && value.length() > 1) {
            String insightParams = stateMap.get(variable);

            if (insightParams != null) {
                String[] splitInsightParams = insightParams.split("\\|");

                if (splitInsightParams[0] != null) {
                    OnOffType binaryState = "0".equals(splitInsightParams[0]) ? OnOffType.OFF : OnOffType.ON;
                    logger.trace("New InsightParam binaryState '{}' for device '{}' received", binaryState,
                            getThing().getUID());
                    updateState(WemoBindingConstants.CHANNEL_STATE, binaryState);
                }

                long lastChangedAt = 0;
                try {
                    lastChangedAt = Long.parseLong(splitInsightParams[1]) * 1000; // convert s to ms
                } catch (NumberFormatException e) {
                    logger.warn("Unable to parse lastChangedAt value '{}' for device '{}'; expected long",
                            splitInsightParams[1], getThing().getUID());
                }
                ZonedDateTime zoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastChangedAt),
                        TimeZone.getDefault().toZoneId());

                State lastChangedAtState = new DateTimeType(zoned);
                if (lastChangedAt != 0) {
                    logger.trace("New InsightParam lastChangedAt '{}' for device '{}' received", lastChangedAtState,
                            getThing().getUID());
                    updateState(WemoBindingConstants.CHANNEL_LASTCHANGEDAT, lastChangedAtState);
                }

                State lastOnFor = DecimalType.valueOf(splitInsightParams[2]);
                logger.trace("New InsightParam lastOnFor '{}' for device '{}' received", lastOnFor,
                        getThing().getUID());
                updateState(WemoBindingConstants.CHANNEL_LASTONFOR, lastOnFor);

                State onToday = DecimalType.valueOf(splitInsightParams[3]);
                logger.trace("New InsightParam onToday '{}' for device '{}' received", onToday, getThing().getUID());
                updateState(WemoBindingConstants.CHANNEL_ONTODAY, onToday);

                State onTotal = DecimalType.valueOf(splitInsightParams[4]);
                logger.trace("New InsightParam onTotal '{}' for device '{}' received", onTotal, getThing().getUID());
                updateState(WemoBindingConstants.CHANNEL_ONTOTAL, onTotal);

                State timespan = DecimalType.valueOf(splitInsightParams[5]);
                logger.trace("New InsightParam timespan '{}' for device '{}' received", timespan, getThing().getUID());
                updateState(WemoBindingConstants.CHANNEL_TIMESPAN, timespan);

                State averagePower = new QuantityType<>(DecimalType.valueOf(splitInsightParams[6]), Units.WATT); // natively
                                                                                                                 // given
                                                                                                                 // in W
                logger.trace("New InsightParam averagePower '{}' for device '{}' received", averagePower,
                        getThing().getUID());
                updateState(WemoBindingConstants.CHANNEL_AVERAGEPOWER, averagePower);

                BigDecimal currentMW = new BigDecimal(splitInsightParams[7]);
                State currentPower = new QuantityType<>(currentMW.divide(new BigDecimal(1000), 0, RoundingMode.HALF_UP),
                        Units.WATT); // recalculate
                // mW to W
                logger.trace("New InsightParam currentPower '{}' for device '{}' received", currentPower,
                        getThing().getUID());
                updateState(WemoBindingConstants.CHANNEL_CURRENTPOWER, currentPower);

                BigDecimal energyTodayMWMin = new BigDecimal(splitInsightParams[8]);
                // recalculate mW-mins to Wh
                State energyToday = new QuantityType<>(
                        energyTodayMWMin.divide(new BigDecimal(60000), 0, RoundingMode.HALF_UP), Units.WATT_HOUR);
                logger.trace("New InsightParam energyToday '{}' for device '{}' received", energyToday,
                        getThing().getUID());
                updateState(WemoBindingConstants.CHANNEL_ENERGYTODAY, energyToday);

                BigDecimal energyTotalMWMin = new BigDecimal(splitInsightParams[9]);
                // recalculate mW-mins to Wh
                State energyTotal = new QuantityType<>(
                        energyTotalMWMin.divide(new BigDecimal(60000), 0, RoundingMode.HALF_UP), Units.WATT_HOUR);
                logger.trace("New InsightParam energyTotal '{}' for device '{}' received", energyTotal,
                        getThing().getUID());
                updateState(WemoBindingConstants.CHANNEL_ENERGYTOTAL, energyTotal);

                if (splitInsightParams.length > 10 && splitInsightParams[10] != null) {
                    BigDecimal standByLimitMW = new BigDecimal(splitInsightParams[10]);
                    State standByLimit = new QuantityType<>(
                            standByLimitMW.divide(new BigDecimal(1000), 0, RoundingMode.HALF_UP), Units.WATT); // recalculate
                    // mW to W
                    logger.trace("New InsightParam standByLimit '{}' for device '{}' received", standByLimit,
                            getThing().getUID());
                    updateState(WemoBindingConstants.CHANNEL_STANDBYLIMIT, standByLimit);

                    if (currentMW.divide(new BigDecimal(1000), 0, RoundingMode.HALF_UP).intValue() > standByLimitMW
                            .divide(new BigDecimal(1000), 0, RoundingMode.HALF_UP).intValue()) {
                        updateState(WemoBindingConstants.CHANNEL_ONSTANDBY, OnOffType.OFF);
                    } else {
                        updateState(WemoBindingConstants.CHANNEL_ONSTANDBY, OnOffType.ON);
                    }
                }
            }
        }
    }
}
