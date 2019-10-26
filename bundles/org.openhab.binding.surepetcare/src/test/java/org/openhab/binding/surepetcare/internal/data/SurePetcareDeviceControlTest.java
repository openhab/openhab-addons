/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;

/**
 * The {@link SurePetcareDeviceControlTest} class implements unit test case for {@link SurePetcareDeviceControl}
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareDeviceControlTest {

    @Test
    public void testJsonDeserialize() throws ParseException {
        String testResponse = "{\"curfew\":[{\"enabled\":true,\"lock_time\":\"19:30\",\"unlock_time\":\"07:00\"}],\"locking\":0,\"fast_polling\":false}";
        SurePetcareDeviceControl response = SurePetcareConstants.GSON.fromJson(testResponse,
                SurePetcareDeviceControl.class);

        assertEquals(1, response.getCurfewList().size());
        assertEquals(new Integer(0), response.getLockingModeId());
    }

    @Test
    public void testJsonSerializeLockingMode() throws ParseException {

        SurePetcareDeviceControl control = new SurePetcareDeviceControl();
        control.setLockingModeId(new Integer(4));

        String json = SurePetcareConstants.GSON.toJson(control);
        assertEquals("{\"locking\":4}", json);
    }

    @Test
    public void testJsonSerializeCurfew() throws ParseException {

        SurePetcareDeviceControl control = new SurePetcareDeviceControl();
        SurePetcareDeviceCurfewList curfews = new SurePetcareDeviceCurfewList();
        curfews.add(new SurePetcareDeviceCurfew(true, "19:30", "07:00"));
        control.setCurfewList(curfews);

        String json = SurePetcareConstants.GSON.toJson(control);
        assertEquals("{\"curfew\":[{\"enabled\":true,\"lock_time\":\"19:30\",\"unlock_time\":\"07:00\"}]}", json);
    }

}
