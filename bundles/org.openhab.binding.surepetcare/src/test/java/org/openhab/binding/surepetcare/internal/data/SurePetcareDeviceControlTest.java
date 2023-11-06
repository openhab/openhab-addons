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
package org.openhab.binding.surepetcare.internal.data;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.time.LocalTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDeviceControl;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDeviceCurfew;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDeviceCurfewList;

/**
 * The {@link SurePetcareDeviceControlTest} class implements unit test case for {@link SurePetcareDeviceControl}
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareDeviceControlTest {

    @Test
    public void testJsonDeserializeCurfewArray() throws ParseException {
        String testResponse = "{\"curfew\":[{\"enabled\":true,\"lock_time\":\"19:30\",\"unlock_time\":\"07:00\"}],\"locking\":0,\"fast_polling\":false}";
        SurePetcareDeviceControl response = SurePetcareConstants.GSON.fromJson(testResponse,
                SurePetcareDeviceControl.class);
        if (response != null) {
            assertEquals(1, response.curfewList.size());
            assertEquals(Integer.valueOf(0), response.lockingModeId);
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testJsonDeserializeSingleCurfew() throws ParseException {
        String testResponse = "{\"curfew\":{\"enabled\":true,\"lock_time\":\"19:00\",\"unlock_time\":\"08:00\"},\"fast_polling\":true}";

        SurePetcareDeviceControl response = SurePetcareConstants.GSON.fromJson(testResponse,
                SurePetcareDeviceControl.class);
        if (response != null) {
            assertEquals(1, response.curfewList.size());
            assertEquals(true, response.fastPolling);
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testJsonSerializeLockingMode() throws ParseException {
        SurePetcareDeviceControl control = new SurePetcareDeviceControl();
        control.lockingModeId = Integer.valueOf(4);

        String json = SurePetcareConstants.GSON.toJson(control);
        assertEquals("{\"locking\":4}", json);
    }

    @Test
    public void testJsonSerializeCurfew() throws ParseException {
        SurePetcareDeviceControl control = new SurePetcareDeviceControl();
        SurePetcareDeviceCurfewList curfews = new SurePetcareDeviceCurfewList();
        curfews.add(new SurePetcareDeviceCurfew(true, LocalTime.of(19, 30, 00), LocalTime.of(07, 00, 00)));
        control.curfewList = curfews;

        String json = SurePetcareConstants.GSON.toJson(control);
        assertEquals("{\"curfew\":[{\"enabled\":true,\"lock_time\":\"19:30\",\"unlock_time\":\"07:00\"}]}", json);
    }
}
