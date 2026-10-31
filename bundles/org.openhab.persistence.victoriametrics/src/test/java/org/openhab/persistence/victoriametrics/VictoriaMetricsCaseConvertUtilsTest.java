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
package org.openhab.persistence.victoriametrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsCaseConvertUtils;

/**
 * Unit tests for {@link VictoriaMetricsCaseConvertUtils}.
 *
 * @author Franz - Initial contribution
 * @author Michael Weger - Continuation of initial contribution
 */
@NonNullByDefault
public class VictoriaMetricsCaseConvertUtilsTest {

    @Test
    public void testCamelToSnake() {
        assertEquals("open_hab", VictoriaMetricsCaseConvertUtils.camelToSnake("OpenHAB"));
        assertEquals("my_variable_name", VictoriaMetricsCaseConvertUtils.camelToSnake("myVariableName"));
        assertEquals("simple", VictoriaMetricsCaseConvertUtils.camelToSnake("simple"));
        assertEquals("ip_address", VictoriaMetricsCaseConvertUtils.camelToSnake("IPAddress"));
        assertEquals("log_id", VictoriaMetricsCaseConvertUtils.camelToSnake("LogID"));
        assertEquals("mqtt_topic_id", VictoriaMetricsCaseConvertUtils.camelToSnake("MQTTTopicID"));
        assertEquals("abc", VictoriaMetricsCaseConvertUtils.camelToSnake("ABC"));
    }
}
