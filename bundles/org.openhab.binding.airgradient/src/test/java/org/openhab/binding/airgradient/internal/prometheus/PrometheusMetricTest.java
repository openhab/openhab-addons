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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Jørgen Austvik - Initial contribution
 */
@SuppressWarnings({ "null" })
@NonNullByDefault
public class PrometheusMetricTest {

    @Test
    public void testParseEmpty() {
        var res = PrometheusMetric.parse("");
        assertThat(res, is(nullValue()));
    }

    @Test
    public void testParseComment() {
        var res = PrometheusMetric.parse("# Comment");
        assertThat(res, is(nullValue()));
    }

    @Test
    public void testParseAirGradient() {
        var res = PrometheusMetric.parse("atmp{id=\"Airgradient\"}31.6");
        assertThat(res.getMetricName(), is("atmp"));
        assertThat(res.getValue(), closeTo(31.6, 0.1));
        assertThat(res.getLabels().get("id"), is("Airgradient"));
    }

    @Test
    public void testParseNoLables() {
        var res = PrometheusMetric.parse("http_request_duration_seconds_count 144320");
        assertThat(res.getMetricName(), is("http_request_duration_seconds_count"));
        assertThat(res.getValue(), closeTo(144320, 0.1));
    }

    @Test
    public void testParseWithTimestamp() {
        var res = PrometheusMetric.parse("http_requests_total{method=\"post\",code=\"200\"} 1027    1395066363000");
        assertThat(res.getMetricName(), is("http_requests_total"));
        assertThat(res.getValue(), closeTo(1027, 0.1));
        assertThat(res.getTimeStamp(), is(Instant.ofEpochMilli(1395066363000L)));
        assertThat(res.getLabels().get("method"), is("post"));
        assertThat(res.getLabels().get("code"), is("200"));
    }

    @Test
    public void testParseNegativeEpoch() {
        var res = PrometheusMetric.parse("something_weird{problem=\"division by zero\"} 123 -3982045");
        assertThat(res.getMetricName(), is("something_weird"));
        assertThat(res.getTimeStamp(), is(Instant.ofEpochMilli(-3982045)));
        assertThat(res.getValue(), closeTo(123, 0.1));
        assertThat(res.getLabels().get("problem"), is("division by zero"));
    }
}
