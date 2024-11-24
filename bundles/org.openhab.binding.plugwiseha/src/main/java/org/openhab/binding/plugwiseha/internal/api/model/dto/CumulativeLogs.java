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
package org.openhab.binding.plugwiseha.internal.api.model.dto;

import java.util.Map;
import java.util.Optional;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The {@link CumulativeLogs} class is an object model class that
 * mirrors the XML structure provided by the Plugwise Home Automation
 * controller for the collection of logs.
 * It extends the {@link PlugwiseHACollection} class.
 * 
 * @author L. Siepel - Initial contribution
 */
@XStreamAlias("logs")
public class CumulativeLogs extends PlugwiseHACollection<CumulativeLog> {

    private static final String FAILED_BURNER_STARTS = "failed_burner_starts";
    private static final String BURNER_STARTS = "burner_starts";
    private static final String BURNER_OP_TIME = "burner_operation_time";
    private static final String DHW_BURNER_OP_TIME = "domestic_hot_water_burner_operation_time";
    private static final String FAILED_BURNER_IGNITIONS = "failed_burner_flame_ignitions";

    public Optional<Double> getBurnerFailedStarts() {
        return this.getLog(FAILED_BURNER_STARTS).map(logEntry -> logEntry.getMeasurementAsDouble())
                .orElse(Optional.empty());
    }

    public Optional<Double> getBurnerStarts() {
        return this.getLog(BURNER_STARTS).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Double> getBurnerOpTime() {
        return this.getLog(BURNER_OP_TIME).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Double> getBurnerDHWOPTime() {
        return this.getLog(DHW_BURNER_OP_TIME).map(logEntry -> logEntry.getMeasurementAsDouble())
                .orElse(Optional.empty());
    }

    public Optional<Double> getBurnerFailedIgnitions() {
        return this.getLog(FAILED_BURNER_IGNITIONS).map(logEntry -> logEntry.getMeasurementAsDouble())
                .orElse(Optional.empty());
    }

    public Optional<CumulativeLog> getLog(String logItem) {
        return Optional.ofNullable(this.get(logItem));
    }

    @Override
    public void merge(Map<String, CumulativeLog> logsToMerge) {
        if (logsToMerge != null) {
            for (CumulativeLog logToMerge : logsToMerge.values()) {
                String type = logToMerge.getType();
                CumulativeLog originalLog = this.get(type);

                if (originalLog == null || originalLog.isOlderThan(logToMerge)) {
                    this.put(type, logToMerge);
                } else {
                    this.put(type, originalLog);
                }
            }
        }
    }
}
