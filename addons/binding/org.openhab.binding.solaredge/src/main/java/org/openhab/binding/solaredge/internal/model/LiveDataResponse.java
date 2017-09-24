package org.openhab.binding.solaredge.internal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Copyright (c) 2017. Schenker AG
 * All rights reserved.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveDataResponse {
    private static final String GRID = "GRID";
    private static final String LOAD = "LOAD";
    private static final String PV = "PV";
    private static final String STORAGE = "STORAGE";
    private static final String ZERO_POWER = "0.0";

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(LiveDataResponse.class);

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        public String status;
        public String currentPower;
    }

    public static class BatteryValue {
        public String status;
        public String currentPower;
        public String chargeLevel;
        public String critical;
    }

    public static class Connection {
        public String from;
        public String to;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SiteCurrentPowerFlow {
        @JsonProperty(GRID)
        public Value grid;

        @JsonProperty(LOAD)
        public Value load;

        @JsonProperty(PV)
        public Value pv;

        @JsonProperty(STORAGE)
        public BatteryValue storage;

        public List<Connection> connections;

    }

    private SiteCurrentPowerFlow siteCurrentPowerFlow;

    public Map<String, String> getValues() {
        Map<String, String> valueMap = new HashMap<>();

        valueMap.put(LiveDataChannels.PRODUCTION.getFQName(), siteCurrentPowerFlow.pv.currentPower);
        valueMap.put(LiveDataChannels.CONSUMPTION.getFQName(), siteCurrentPowerFlow.load.currentPower);

        valueMap.put(LiveDataChannels.PV_STATUS.getFQName(), siteCurrentPowerFlow.pv.status);
        valueMap.put(LiveDataChannels.BATTERY_STATUS.getFQName(), siteCurrentPowerFlow.storage.status);
        valueMap.put(LiveDataChannels.BATTERY_CRITICAL.getFQName(), siteCurrentPowerFlow.storage.critical);
        valueMap.put(LiveDataChannels.BATTERY_LEVEL.getFQName(), siteCurrentPowerFlow.storage.chargeLevel);
        valueMap.put(LiveDataChannels.LOAD_STATUS.getFQName(), siteCurrentPowerFlow.load.status);
        valueMap.put(LiveDataChannels.GRID_STATUS.getFQName(), siteCurrentPowerFlow.grid.status);

        // determine power flow from connection list
        for (Connection con : siteCurrentPowerFlow.connections) {
            if (con.from.equalsIgnoreCase(GRID)) {
                valueMap.put(LiveDataChannels.IMPORT.getFQName(), siteCurrentPowerFlow.grid.currentPower);
                valueMap.put(LiveDataChannels.EXPORT.getFQName(), ZERO_POWER);
            } else {
                valueMap.put(LiveDataChannels.EXPORT.getFQName(), siteCurrentPowerFlow.grid.currentPower);
                valueMap.put(LiveDataChannels.IMPORT.getFQName(), ZERO_POWER);

            }
            if (con.from.equalsIgnoreCase(STORAGE)) {
                valueMap.put(LiveDataChannels.BATTERY_CHARGE.getFQName(),
                        "-" + siteCurrentPowerFlow.storage.currentPower);
            } else {
                valueMap.put(LiveDataChannels.BATTERY_CHARGE.getFQName(), siteCurrentPowerFlow.storage.currentPower);
            }
        }

        return valueMap;
    }

    public final SiteCurrentPowerFlow getSiteCurrentPowerFlow() {
        return siteCurrentPowerFlow;
    }

    public final void setSiteCurrentPowerFlow(SiteCurrentPowerFlow siteCurrentPowerFlow) {
        this.siteCurrentPowerFlow = siteCurrentPowerFlow;
    }

}
