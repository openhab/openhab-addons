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

    @Before
    public void setUp() {
        String conditionsStr = FileReader.readFileInString("src/test/resources/condition-result-no-pressure.json");
        assertNotNull(conditionsStr);
        conditions = HTTPHandler.getLatestValues(conditionsStr);

        String particulateStr = FileReader.readFileInString("src/test/resources/pm-result.json");
        assertNotNull(particulateStr);
        particulate = HTTPHandler.getLatestValues(particulateStr);

        String noiseStr = FileReader.readFileInString("src/test/resources/noise-result.json");
        assertNotNull(noiseStr);
        noise = HTTPHandler.getLatestValues(noiseStr);
    }

    @Test
    public void testIsCondition() {
        assertTrue(HTTPHandler.isCondition(conditions));
        assertFalse(HTTPHandler.isCondition(particulate));
        assertFalse(HTTPHandler.isCondition(noise));
        assertFalse(HTTPHandler.isCondition(null));
    }

    @Test
    public void testIsParticulate() {
        assertFalse(HTTPHandler.isParticulate(conditions));
        assertTrue(HTTPHandler.isParticulate(particulate));
        assertFalse(HTTPHandler.isParticulate(noise));
        assertFalse(HTTPHandler.isParticulate(null));
    }

    @Test
    public void testIsNoise() {
        assertFalse(HTTPHandler.isNoise(conditions));
        assertFalse(HTTPHandler.isNoise(particulate));
        assertTrue(HTTPHandler.isNoise(noise));
        assertFalse(HTTPHandler.isNoise(null));
    }

}
