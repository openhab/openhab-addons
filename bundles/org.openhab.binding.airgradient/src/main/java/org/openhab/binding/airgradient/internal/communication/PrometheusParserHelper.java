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
package org.openhab.binding.airgradient.internal.communication;

import java.util.Arrays;
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
            if (metric.getMetricName().equals("pm01")) {
                measure.pm01 = metric.getValue();
            } else if (metric.getMetricName().equals("pm02")) {
                measure.pm02 = metric.getValue();
            } else if (metric.getMetricName().equals("pm10")) {
                measure.pm10 = metric.getValue();
            } else if (metric.getMetricName().equals("rco2")) {
                measure.rco2 = metric.getValue();
            } else if (metric.getMetricName().equals("atmp")) {
                measure.atmp = metric.getValue();
            } else if (metric.getMetricName().equals("rhum")) {
                measure.rhum = metric.getValue();
            } else if (metric.getMetricName().equals("tvoc")) {
                measure.tvoc = metric.getValue();
            } else if (metric.getMetricName().equals("nox")) {
                measure.noxIndex = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_wifi_rssi_dbm")) {
                measure.wifi = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_co2_ppm")) {
                measure.rco2 = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_pm1_ugm3")) {
                measure.pm01 = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_pm2d5_ugm3")) {
                measure.pm02 = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_pm10_ugm3")) {
                measure.pm10 = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_pm0d3_p100ml")) {
                measure.pm003Count = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_tvoc_index")) {
                measure.tvoc = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_tvoc_raw_index")) {
                measure.tvocIndex = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_nox_index")) {
                measure.noxIndex = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_temperature_degc")) {
                measure.atmp = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_humidity_percent")) {
                measure.rhum = metric.getValue();
            }

            if (metric.getLabels().containsKey("id")) {
                String id = metric.getLabels().get("id");
                measure.serialno = id;
                measure.locationId = id;
                measure.locationName = id;
            }

            if (metric.getLabels().containsKey("airgradient_serial_number")) {
                String id = metric.getLabels().get("airgradient_serial_number");
                measure.serialno = id;
                measure.locationId = id;
                measure.locationName = id;
            }
        }

        return Arrays.asList(measure);
    }
}
