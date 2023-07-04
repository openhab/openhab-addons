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
package org.openhab.binding.boschshc.internal.devices.motiondetector;

import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit Tests for {@link MotionDetectorHandler}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class MotionDetectorHandlerTest extends AbstractBatteryPoweredDeviceHandlerTest<MotionDetectorHandler> {

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

    @Test
    void testUpdateChannelsLatestMotionService() {
        JsonElement jsonObject = JsonParser.parseString("{\n" + "   \"@type\": \"latestMotionState\",\n"
                + "   \"latestMotionDetected\": \"2020-04-03T19:02:19.054Z\"\n" + " }");
        getFixture().processUpdate("LatestMotion", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_LATEST_MOTION),
                new DateTimeType("2020-04-03T19:02:19.054Z"));
    }
}
