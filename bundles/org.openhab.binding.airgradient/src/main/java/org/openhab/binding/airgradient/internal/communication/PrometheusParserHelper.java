/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.airgradient.internal.communication;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.binding.airgradient.internal.prometheus.PrometheusMetric;
import org.openhab.binding.airgradient.internal.prometheus.PrometheusTextParser;

/**
 * Helper for parsing Prometheus data.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class PrometheusParserHelper {

    public static List<Measure> parsePrometheus(String stringResponse) {
        List<PrometheusMetric> metrics = PrometheusTextParser.parse(stringResponse);
        Measure measure = new Measure();

        for (PrometheusMetric metric : metrics) {
            switch (metric.getMetricName()) {
                case "pm01", "airgradient_pm1_ugm3" -> measure.pm01 = metric.getValue();
                case "pm02", "airgradient_pm2d5_ugm3" -> measure.pm02 = metric.getValue();
                case "pm10", "airgradient_pm10_ugm3" -> measure.pm10 = metric.getValue();
                case "rco2", "airgradient_co2_ppm" -> measure.rco2 = metric.getValue();
                case "atmp", "airgradient_temperature_degc" -> measure.atmp = metric.getValue();
                case "rhum", "airgradient_humidity_percent" -> measure.rhum = metric.getValue();
                case "tvoc", "airgradient_tvoc_index" -> measure.tvoc = metric.getValue();
                case "airgradient_tvoc_raw_index" -> measure.tvocIndex = metric.getValue();
                case "nox", "airgradient_nox_index" -> measure.noxIndex = metric.getValue();
                case "airgradient_pm0d3_p100ml" -> measure.pm003Count = metric.getValue();
                case "airgradient_wifi_rssi_dbm" -> measure.wifi = metric.getValue();
            }

            String id = metric.getLabels().get("id");
            if (id != null) {
                measure.serialno = id;
                measure.locationId = id;
                measure.locationName = id;
            }

            String serialNumber = metric.getLabels().get("airgradient_serial_number");
            if (serialNumber != null) {
                measure.serialno = serialNumber;
                measure.locationId = serialNumber;
                measure.locationName = serialNumber;
            }
        }

        return List.of(measure);
    }
}
