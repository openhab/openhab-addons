/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.luftdateninfo.internal;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.luftdateninfo.internal.dto.SensorDataValue;
import org.openhab.binding.luftdateninfo.internal.handler.HTTPHandler;
import org.openhab.binding.luftdateninfo.internal.util.FileReader;

/**
 * The {@link HTTPHandlerEvalTest} test all evaluations on SensorDataValues
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class HTTPHandlerEvalTest {

    private @Nullable List<SensorDataValue> conditions;
    private @Nullable List<SensorDataValue> particulate;
    private @Nullable List<SensorDataValue> noise;
    private HTTPHandler http = new HTTPHandler();

    @Before
    public void setUp() {
        String conditionsStr = FileReader.readFileInString("src/test/resources/condition-result-no-pressure.json");
        assertNotNull(conditionsStr);
        conditions = http.getLatestValues(conditionsStr);

        String particulateStr = FileReader.readFileInString("src/test/resources/pm-result.json");
        assertNotNull(particulateStr);
        particulate = http.getLatestValues(particulateStr);

        String noiseStr = FileReader.readFileInString("src/test/resources/noise-result.json");
        assertNotNull(noiseStr);
        noise = http.getLatestValues(noiseStr);
    }

    @Test
    public void testIsCondition() {
        assertTrue(http.isCondition(conditions));
        assertFalse(http.isCondition(particulate));
        assertFalse(http.isCondition(noise));
        assertFalse(http.isCondition(null));
    }

    @Test
    public void testIsParticulate() {
        assertFalse(http.isParticulate(conditions));
        assertTrue(http.isParticulate(particulate));
        assertFalse(http.isParticulate(noise));
        assertFalse(http.isParticulate(null));
    }

    @Test
    public void testIsNoise() {
        assertFalse(http.isNoise(conditions));
        assertFalse(http.isNoise(particulate));
        assertTrue(http.isNoise(noise));
        assertFalse(http.isNoise(null));
    }
}
