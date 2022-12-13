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
package org.openhab.binding.boschshc.internal.devices.twinguard;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.AbstractSmokeDetectorHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Unit Tests for {@link TwinguardHandler}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class TwinguardHandlerTest extends AbstractSmokeDetectorHandlerTest<TwinguardHandler> {

    @Override
    protected TwinguardHandler createFixture() {
        return new TwinguardHandler(getThing());
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:000d6f0016d1a193";
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_TWINGUARD;
    }
}
