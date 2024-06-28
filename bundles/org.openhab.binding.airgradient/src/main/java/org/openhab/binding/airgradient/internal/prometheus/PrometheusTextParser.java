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
package org.openhab.binding.airgradient.internal.prometheus;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Prometheus text format parser.
 *
 * Based on specification in
 * https://github.com/Showmax/prometheus-docs/blob/master/content/docs/instrumenting/exposition_formats.md
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class PrometheusTextParser {

    public static List<PrometheusMetric> parse(String text) {
        String[] lines = text.split("\\r?\\n");
        List<PrometheusMetric> metrics = new ArrayList<>(lines.length);
        for (String line : lines) {
            @Nullable
            PrometheusMetric metric = PrometheusMetric.parse(line);
            if (metric != null) {
                metrics.add(metric);
            }
        }
        return metrics;
    }
}
