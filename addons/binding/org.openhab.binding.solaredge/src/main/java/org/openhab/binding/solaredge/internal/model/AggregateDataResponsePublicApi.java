/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this class is used to map the aggregate data response of the public API
 *
 * @author Alexander Friese - initial contribution
 */
public class AggregateDataResponsePublicApi implements DataResponse {

    private final Logger logger = LoggerFactory.getLogger(AggregateDataResponsePublicApi.class);

    private static final String METER_TYPE_PRODUCTION = "Production";
    private static final String METER_TYPE_CONSUMPTION = "Consumption";
    private static final String METER_TYPE_SELFCONSUMPTION = "SelfConsumption";
    private static final String METER_TYPE_IMPORT = "Purchased";
    private static final String METER_TYPE_EXPORT = "FeedIn";

    public static class MeterTelemetry {
        public String date;
        public Double value;
    }

    public static class MeterTelemetries {
        public String type;
        public List<MeterTelemetry> values;
    }

    public static class EnergyDetails {
        public AggregatePeriod timeUnit;
        public String unit;
        public List<MeterTelemetries> meters;
    }

    private EnergyDetails energyDetails;

    public EnergyDetails getEnergyDetails() {
        return energyDetails;
    }

    public void setEnergyDetails(EnergyDetails energyDetails) {
        this.energyDetails = energyDetails;
    }

    @Override
    public Map<Channel, State> getValues() {
        Map<Channel, State> valueMap = new HashMap<>();

        if (getEnergyDetails() != null && getEnergyDetails().timeUnit != null) {
            for (MeterTelemetries meter : getEnergyDetails().meters) {
                if (meter.type != null) {
                    if (meter.type.equals(METER_TYPE_PRODUCTION)) {
                        fillProductionData(meter, valueMap);
                    } else if (meter.type.equals(METER_TYPE_CONSUMPTION)) {
                        fillConsumptionData(meter, valueMap);
                    } else if (meter.type.equals(METER_TYPE_SELFCONSUMPTION)) {
                        fillSelfConsumptionData(meter, valueMap);
                    } else if (meter.type.equals(METER_TYPE_IMPORT)) {
                        fillImportData(meter, valueMap);
                    } else if (meter.type.equals(METER_TYPE_EXPORT)) {
                        fillExportData(meter, valueMap);
                    }
                }
            }
            fillSelfConsumptionCoverage(valueMap);
        }

        return valueMap;
    }

    /**
     * copies production data to the result map
     *
     * @param meter    - meter raw data
     * @param valueMap - target structure
     */
    private final void fillProductionData(MeterTelemetries meter, Map<Channel, State> valueMap) {
        switch (getEnergyDetails().timeUnit) {
            case DAY:
                assignValue(valueMap, AggregateDataChannels.DAY_PRODUCTION, meter.values.get(0));
                break;
            case WEEK:
                if (meter.values.size() == 1) {
                    assignValue(valueMap, AggregateDataChannels.WEEK_PRODUCTION, meter.values.get(0));
                } else if (meter.values.size() == 2) {
                    assignValue(valueMap, AggregateDataChannels.WEEK_PRODUCTION, meter.values.get(0),
                            meter.values.get(1));
                } else {
                    logger.warn("Response for weekly data has unexpected format, expected 2 entries got {}",
                            meter.values.size());
                }
                break;
            case MONTH:
                assignValue(valueMap, AggregateDataChannels.MONTH_PRODUCTION, meter.values.get(0));
                break;
            case YEAR:
                assignValue(valueMap, AggregateDataChannels.YEAR_PRODUCTION, meter.values.get(0));
                break;
        }
    }

    /**
     * copies consumption data to the result map
     *
     * @param meter    - meter raw data
     * @param valueMap - target structure
     */
    private final void fillConsumptionData(MeterTelemetries meter, Map<Channel, State> valueMap) {
        switch (getEnergyDetails().timeUnit) {
            case DAY:
                assignValue(valueMap, AggregateDataChannels.DAY_CONSUMPTION, meter.values.get(0));
                break;
            case WEEK:
                if (meter.values.size() == 1) {
                    assignValue(valueMap, AggregateDataChannels.WEEK_CONSUMPTION, meter.values.get(0));
                } else if (meter.values.size() == 2) {
                    assignValue(valueMap, AggregateDataChannels.WEEK_CONSUMPTION, meter.values.get(0),
                            meter.values.get(1));
                } else {
                    logger.warn("Response for weekly data has unexpected format, expected 2 entries got {}",
                            meter.values.size());
                }
                break;
            case MONTH:
                assignValue(valueMap, AggregateDataChannels.MONTH_CONSUMPTION, meter.values.get(0));
                break;
            case YEAR:
                assignValue(valueMap, AggregateDataChannels.YEAR_CONSUMPTION, meter.values.get(0));
                break;
        }
    }

    /**
     * copies self-consumption data to the result map
     *
     * @param meter    - meter raw data
     * @param valueMap - target structure
     */
    private final void fillSelfConsumptionData(MeterTelemetries meter, Map<Channel, State> valueMap) {
        switch (getEnergyDetails().timeUnit) {
            case DAY:
                assignValue(valueMap, AggregateDataChannels.DAY_SELFCONSUMPTIONFORCONSUMPTION, meter.values.get(0));
                break;
            case WEEK:
                if (meter.values.size() == 1) {
                    assignValue(valueMap, AggregateDataChannels.WEEK_SELFCONSUMPTIONFORCONSUMPTION,
                            meter.values.get(0));
                } else if (meter.values.size() == 2) {
                    assignValue(valueMap, AggregateDataChannels.WEEK_SELFCONSUMPTIONFORCONSUMPTION, meter.values.get(0),
                            meter.values.get(1));
                } else {
                    logger.warn("Response for weekly data has unexpected format, expected 2 entries got {}",
                            meter.values.size());
                }

                break;
            case MONTH:
                assignValue(valueMap, AggregateDataChannels.MONTH_SELFCONSUMPTIONFORCONSUMPTION, meter.values.get(0));
                break;
            case YEAR:
                assignValue(valueMap, AggregateDataChannels.YEAR_SELFCONSUMPTIONFORCONSUMPTION, meter.values.get(0));
                break;
        }
    }

    /**
     * copies import data to the result map
     *
     * @param meter    - meter raw data
     * @param valueMap - target structure
     */
    private final void fillImportData(MeterTelemetries meter, Map<Channel, State> valueMap) {
        switch (getEnergyDetails().timeUnit) {
            case DAY:
                assignValue(valueMap, AggregateDataChannels.DAY_IMPORT, meter.values.get(0));
                break;
            case WEEK:
                if (meter.values.size() == 1) {
                    assignValue(valueMap, AggregateDataChannels.WEEK_IMPORT, meter.values.get(0));
                } else if (meter.values.size() == 2) {
                    assignValue(valueMap, AggregateDataChannels.WEEK_IMPORT, meter.values.get(0), meter.values.get(1));
                } else {
                    logger.warn("Response for weekly data has unexpected format, expected 2 entries got {}",
                            meter.values.size());
                }

                break;
            case MONTH:
                assignValue(valueMap, AggregateDataChannels.MONTH_IMPORT, meter.values.get(0));
                break;
            case YEAR:
                assignValue(valueMap, AggregateDataChannels.YEAR_IMPORT, meter.values.get(0));
                break;
        }
    }

    /**
     * copies export data to the result map
     *
     * @param meter    - meter raw data
     * @param valueMap - target structure
     */
    private final void fillExportData(MeterTelemetries meter, Map<Channel, State> valueMap) {
        switch (getEnergyDetails().timeUnit) {
            case DAY:
                assignValue(valueMap, AggregateDataChannels.DAY_EXPORT, meter.values.get(0));
                break;
            case WEEK:
                if (meter.values.size() == 1) {
                    assignValue(valueMap, AggregateDataChannels.WEEK_EXPORT, meter.values.get(0));
                } else if (meter.values.size() == 2) {
                    assignValue(valueMap, AggregateDataChannels.WEEK_EXPORT, meter.values.get(0), meter.values.get(1));
                } else {
                    logger.warn("Response for weekly data has unexpected format, expected 2 entries got {}",
                            meter.values.size());
                }

                break;
            case MONTH:
                assignValue(valueMap, AggregateDataChannels.MONTH_EXPORT, meter.values.get(0));
                break;
            case YEAR:
                assignValue(valueMap, AggregateDataChannels.YEAR_EXPORT, meter.values.get(0));
                break;
        }
    }

    /**
     * calculates the self consumption coverage
     *
     * @param valueMap - target structure
     */
    private final void fillSelfConsumptionCoverage(Map<Channel, State> valueMap) {
        State selfConsumption = null;
        State consumption = null;
        switch (getEnergyDetails().timeUnit) {
            case DAY:
                selfConsumption = valueMap.get(AggregateDataChannels.DAY_SELFCONSUMPTIONFORCONSUMPTION);
                consumption = valueMap.get(AggregateDataChannels.DAY_CONSUMPTION);
                assignPercentage(valueMap, AggregateDataChannels.DAY_SELFCONSUMPTIONCOVERAGE, selfConsumption,
                        consumption);
                break;
            case WEEK:
                selfConsumption = valueMap.get(AggregateDataChannels.WEEK_SELFCONSUMPTIONFORCONSUMPTION);
                consumption = valueMap.get(AggregateDataChannels.WEEK_CONSUMPTION);
                assignPercentage(valueMap, AggregateDataChannels.WEEK_SELFCONSUMPTIONCOVERAGE, selfConsumption,
                        consumption);
                break;
            case MONTH:
                selfConsumption = valueMap.get(AggregateDataChannels.MONTH_SELFCONSUMPTIONFORCONSUMPTION);
                consumption = valueMap.get(AggregateDataChannels.MONTH_CONSUMPTION);
                assignPercentage(valueMap, AggregateDataChannels.MONTH_SELFCONSUMPTIONCOVERAGE, selfConsumption,
                        consumption);
                break;
            case YEAR:
                selfConsumption = valueMap.get(AggregateDataChannels.YEAR_SELFCONSUMPTIONFORCONSUMPTION);
                consumption = valueMap.get(AggregateDataChannels.YEAR_CONSUMPTION);
                assignPercentage(valueMap, AggregateDataChannels.YEAR_SELFCONSUMPTIONCOVERAGE, selfConsumption,
                        consumption);
                break;
        }
    }

    /**
     * converts the meter value to QuantityType. If multiple meter value are provided a sum will be calculated. If no
     * unit can be determined UnDefType.UNDEF will be used
     *
     * @param targetMap result will be put into this map
     * @param channel   channel to assign the value
     * @param values    one or more meter values
     */
    protected final void assignValue(Map<Channel, State> targetMap, Channel channel, MeterTelemetry... values) {
        double sum = 0.0;
        State result = UnDefType.UNDEF;

        for (MeterTelemetry value : values) {
            if (value.value != null) {
                sum += value.value;
            }
        }

        if (energyDetails != null && energyDetails.unit != null) {
            Unit<Energy> unit = determineEnergyUnit(energyDetails.unit);
            if (unit != null) {
                result = new QuantityType<Energy>(sum, unit);
            } else {
                logger.debug("Channel {}: Could not determine unit: '{}'", channel.getFQName(), energyDetails.unit);
            }
        } else {
            logger.debug("Channel {}: Value has no unit.", channel.getFQName());
        }

        targetMap.put(channel, result);
    }

    /**
     * calculates percentage and assigns it to the corresponding channel and puts it into the targetmap
     *
     * @param targetMap      result will be put into this map
     * @param channel        channel to assign the value
     * @param dividendString
     * @param divisorString
     */
    protected final void assignPercentage(Map<Channel, State> targetMap, Channel channel, State dividendAsState,
            State divisorAsState) {
        double percent = -1;
        State result = UnDefType.UNDEF;

        if (dividendAsState != null && divisorAsState != null) {
            DecimalType dividendAsDecimalType = dividendAsState.as(DecimalType.class);
            DecimalType divisorAsDecimalType = divisorAsState.as(DecimalType.class);

            // null check is necessary although eclipse states it is not!
            if (dividendAsDecimalType != null && divisorAsDecimalType != null) {
                double dividend = dividendAsDecimalType.doubleValue();
                double divisor = divisorAsDecimalType.doubleValue();
                if (dividend >= 0.0 && divisor > 0.0) {
                    percent = dividend / divisor * 100;
                }
            }
        }

        if (percent >= 0.0) {
            result = new QuantityType<Dimensionless>(percent, SmartHomeUnits.PERCENT);
        } else {
            logger.debug("Channel {}: Could not calculate percent.", channel.getFQName());
        }

        targetMap.put(channel, result);
    }

}
