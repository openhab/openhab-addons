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
package org.openhab.binding.airgradient.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.thing.Thing;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class MeasureHelperTest {

    @Test
    public void testCreatePropertiesUsesFirmwareFallback() {
        Measure measure = new Measure();
        measure.firmware = "3.1.21";

        Map<String, String> properties = MeasureHelper.createProperties(measure);

        assertThat(properties, hasEntry(Thing.PROPERTY_FIRMWARE_VERSION, "3.1.21"));
    }

    @Test
    public void testCreatePropertiesSkipsEmptyFirmwareVersion() {
        Measure measure = new Measure();

        Map<String, String> properties = MeasureHelper.createProperties(measure);

        assertThat(properties, not(hasEntry(Thing.PROPERTY_FIRMWARE_VERSION, "")));
    }
}
