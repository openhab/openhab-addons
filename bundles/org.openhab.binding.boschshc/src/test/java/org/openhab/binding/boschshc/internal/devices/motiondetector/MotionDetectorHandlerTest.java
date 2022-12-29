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
package org.openhab.binding.boschshc.internal.devices.motiondetector;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Unit Tests for {@link MotionDetectorHandler}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class MotionDetectorHandlerTest extends AbstractBatteryPoweredDeviceHandlerTest<MotionDetectorHandler> {

    @Override
    protected MotionDetectorHandler createFixture() {
        return new MotionDetectorHandler(getThing());
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:000d6f0012fd2571";
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_MOTION_DETECTOR;
    }
}
