/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toQuantityType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.core.types.State;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MeasureTest {

    @Test
    public void testMeasurePrecision() {
        State value = toQuantityType(25.0, MeasureClass.INSIDE_TEMPERATURE);
        assertEquals("25 °C", value.toString());
        value = toQuantityType(52.0, MeasureClass.INSIDE_TEMPERATURE);
        assertEquals("50 °C", value.toString());
        value = toQuantityType(-10.0, MeasureClass.INSIDE_TEMPERATURE);
        assertEquals("0 °C", value.toString());
    }
}
