/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sleepiq.api.test.AbstractTest;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationPresetRequest;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationActuatorSpeed;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationPreset;
import org.openhab.binding.sleepiq.internal.api.enums.Side;
import org.openhab.binding.sleepiq.internal.api.impl.GsonGenerator;

import com.google.gson.Gson;

/**
 * The {@link SetFoundationPresetTest} tests serialization of the foundation preset request.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class SetFoundationPresetTest extends AbstractTest {
    private static Gson gson = GsonGenerator.create(true);

    @Test
    public void testSerializeAllFields() throws Exception {
        FoundationPresetRequest preset = new FoundationPresetRequest().withSide(Side.RIGHT)
                .withFoundationPreset(FoundationPreset.WATCH_TV)
                .withFoundationActuatorSpeed(FoundationActuatorSpeed.FAST);
        assertEquals(readJson("set-foundation-preset.json"), gson.toJson(preset));
    }
}
