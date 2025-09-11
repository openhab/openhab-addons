/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.smokedetector;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Unit Tests for {@link SmokeDetectorHandler}.
 *
 * @author Gerd Zanker - Initial contribution
 *
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class SmokeDetectorHandlerTest extends AbstractSmokeDetectorHandlerWithAlarmServiceTest<SmokeDetectorHandler> {

    @Override
    protected SmokeDetectorHandler createFixture() {
        return new SmokeDetectorHandler(getThing());
    }

    @Override
    protected String getDeviceID() {
        return "hdm:HomeMaticIP:3014F711A00004DBB85C1234";
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_SMOKE_DETECTOR;
    }
}
