/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.model;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this class is used to map the aggregate data response of the public API
 *
 * @author Alexander Friese - initial contribution
 */
public class AggregateDataResponsePublicApi implements DataResponse {

    private static final String UNIT_WH = "Wh";
    // private static final String UNIT_KWH = "KWh";
    private static final String UNIT_MWH = "MWh";

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
    public Map<String, String> getValues() {
        Map<String, String> valueMap = new HashMap<>();

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
     * @param meter - meter raw data
     * @param valueMap - target structure
     */
    private final void fillProductionData(MeterTelemetries meter, Map<String, String> valueMap) {
        switch (getEnergyDetails().timeUnit) {
            case DAY:
                valueMap.put(AggregateDataChannels.DAY_PRODUCTION.getFQName(), getValueAsKWh(meter.values.get(0)));
                break;
            case WEEK:
                // TODO: response data needs more clarification
                // valueMap.put(AggregateDataChannels.WEEK_PRODUCTION.getFQName(),
                // getValueAsKWh(meter.values.get(0)));
                break;
            case MONTH:
                valueMap.put(AggregateDataChannels.MONTH_PRODUCTION.getFQName(), getValueAsKWh(meter.values.get(0)));
                break;
            case YEAR:
                valueMap.put(AggregateDataChannels.YEAR_PRODUCTION.getFQName(), getValueAsKWh(meter.values.get(0)));
                break;
        }
    }

    /**
     * copies consumption data to the result map
     *
     * @param meter - meter raw data
     * @param valueMap - target structure
     */
    private final void fillConsumptionData(MeterTelemetries meter, Map<String, String> valueMap) {
        switch (getEnergyDetails().timeUnit) {
            case DAY:
                valueMap.put(AggregateDataChannels.DAY_CONSUMPTION.getFQName(), getValueAsKWh(meter.values.get(0)));
                break;
            case WEEK:
                // TODO: response data needs more clarification
                // valueMap.put(AggregateDataChannels.WEEK_CONSUMPTION.getFQName(),
                // getValueAsKWh(meter.values.get(0)));
                break;
            case MONTH:
                valueMap.put(AggregateDataChannels.MONTH_CONSUMPTION.getFQName(), getValueAsKWh(meter.values.get(0)));
                break;
            case YEAR:
                valueMap.put(AggregateDataChannels.YEAR_CONSUMPTION.getFQName(), getValueAsKWh(meter.values.get(0)));
                break;
        }
    }

    /**
     * copies self-consumption data to the result map
     *
     * @param meter - meter raw data
     * @param valueMap - target structure
     */
    private final void fillSelfConsumptionData(MeterTelemetries meter, Map<String, String> valueMap) {
        switch (getEnergyDetails().timeUnit) {
            case DAY:
                valueMap.put(AggregateDataChannels.DAY_SELFCONSUMPTIONFORCONSUMPTION.getFQName(),
                        getValueAsKWh(meter.values.get(0)));
                break;
            case WEEK:
                // TODO: response data needs more clarification
                // valueMap.put(AggregateDataChannels.WEEK_SELFCONSUMPTIONFORCONSUMPTION.getFQName(),
                // getValueAsKWh(meter.values.get(0)));
                break;
            case MONTH:
                valueMap.put(AggregateDataChannels.MONTH_SELFCONSUMPTIONFORCONSUMPTION.getFQName(),
                        getValueAsKWh(meter.values.get(0)));
                break;
            case YEAR:
                valueMap.put(AggregateDataChannels.YEAR_SELFCONSUMPTIONFORCONSUMPTION.getFQName(),
                        getValueAsKWh(meter.values.get(0)));
                break;
        }
    }

    /**
     * copies import data to the result map
     *
     * @param meter - meter raw data
     * @param valueMap - target structure
     */
    private final void fillImportData(MeterTelemetries meter, Map<String, String> valueMap) {
        switch (getEnergyDetails().timeUnit) {
            case DAY:
                valueMap.put(AggregateDataChannels.DAY_IMPORT.getFQName(), getValueAsKWh(meter.values.get(0)));
                break;
            case WEEK:
                // TODO: response data needs more clarification
                // valueMap.put(AggregateDataChannels.WEEK_IMPORT.getFQName(),
                // getValueAsKWh(meter.values.get(0)));
                break;
            case MONTH:
                valueMap.put(AggregateDataChannels.MONTH_IMPORT.getFQName(), getValueAsKWh(meter.values.get(0)));
                break;
            case YEAR:
                valueMap.put(AggregateDataChannels.YEAR_IMPORT.getFQName(), getValueAsKWh(meter.values.get(0)));
                break;
        }
    }

    /**
     * copies export data to the result map
     *
     * @param meter - meter raw data
     * @param valueMap - target structure
     */
    private final void fillExportData(MeterTelemetries meter, Map<String, String> valueMap) {
        switch (getEnergyDetails().timeUnit) {
            case DAY:
                valueMap.put(AggregateDataChannels.DAY_EXPORT.getFQName(), getValueAsKWh(meter.values.get(0)));
                break;
            case WEEK:
                // TODO: response data needs more clarification
                // valueMap.put(AggregateDataChannels.WEEK_EXPORT.getFQName(),
                // getValueAsKWh(meter.values.get(0)));
                break;
            case MONTH:
                valueMap.put(AggregateDataChannels.MONTH_EXPORT.getFQName(), getValueAsKWh(meter.values.get(0)));
                break;
            case YEAR:
                valueMap.put(AggregateDataChannels.YEAR_EXPORT.getFQName(), getValueAsKWh(meter.values.get(0)));
                break;
        }
    }

    /**
     * calculates the self consumption coverage
     *
     * @param valueMap - target structure
     */
    private final void fillSelfConsumptionCoverage(Map<String, String> valueMap) {
        String selfConsumption = null;
        String consumption = null;
        switch (getEnergyDetails().timeUnit) {
            case DAY:
                selfConsumption = valueMap.get(AggregateDataChannels.DAY_SELFCONSUMPTIONFORCONSUMPTION.getFQName());
                consumption = valueMap.get(AggregateDataChannels.DAY_CONSUMPTION.getFQName());
                valueMap.put(AggregateDataChannels.DAY_SELFCONSUMPTIONCOVERAGE.getFQName(),
                        calculatePercent(selfConsumption, consumption));
                break;
            case WEEK:
                // TODO: response data needs more clarification
                break;
            case MONTH:
                selfConsumption = valueMap.get(AggregateDataChannels.MONTH_SELFCONSUMPTIONFORCONSUMPTION.getFQName());
                consumption = valueMap.get(AggregateDataChannels.MONTH_CONSUMPTION.getFQName());
                valueMap.put(AggregateDataChannels.MONTH_SELFCONSUMPTIONCOVERAGE.getFQName(),
                        calculatePercent(selfConsumption, consumption));
                break;
            case YEAR:
                selfConsumption = valueMap.get(AggregateDataChannels.YEAR_SELFCONSUMPTIONFORCONSUMPTION.getFQName());
                consumption = valueMap.get(AggregateDataChannels.YEAR_CONSUMPTION.getFQName());
                valueMap.put(AggregateDataChannels.YEAR_SELFCONSUMPTIONCOVERAGE.getFQName(),
                        calculatePercent(selfConsumption, consumption));
                break;
        }
    }

    /**
     * converts the meter value to kWh
     *
     * @param value
     * @return
     */
    protected final String getValueAsKWh(MeterTelemetry value) {
        Double convertedValue = value.value;

        if (energyDetails != null && energyDetails.unit != null && energyDetails.unit.equals(UNIT_WH)) {
            convertedValue = convertedValue / 1000;
        } else if (energyDetails != null && energyDetails.unit != null && energyDetails.unit.equals(UNIT_MWH)) {
            convertedValue = convertedValue * 1000;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(convertedValue);
    }

    /**
     * calculates percentage
     *
     * @param value
     * @return
     */
    protected final String calculatePercent(String dividendString, String divisorString) {
        if (dividendString != null && divisorString != null) {
            Double dividend = Double.valueOf(dividendString);
            Double divisor = Double.valueOf(divisorString);

            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df.format(dividend / divisor);
        }
        return null;
    }

}
