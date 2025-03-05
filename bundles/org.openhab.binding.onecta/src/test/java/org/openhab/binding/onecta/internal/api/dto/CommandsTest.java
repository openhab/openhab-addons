/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.onecta.internal.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.onecta.internal.api.Enums;
import org.openhab.binding.onecta.internal.api.dto.commands.*;
import org.openhab.binding.onecta.internal.exception.DaikinCommunicationException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Alexander Drent - Initial contribution
 */
public class CommandsTest {

    final String UNITID = "1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6";
    final Enums.ManagementPoint MANAGEMENTPOINTTYPE = Enums.ManagementPoint.CLIMATECONTROL;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void startScanTest() throws DaikinCommunicationException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("{\"value\":25.5,\"path\":null}", mapper.writeValueAsString(new CommandFloat(25.5f)));
        assertEquals("{\"value\":25.5,\"path\":\"Sample context\"}",
                mapper.writeValueAsString(new CommandFloat(25.5f, "Sample context")));

        assertEquals("{\"value\":25,\"path\":null}", mapper.writeValueAsString(new CommandInteger(25)));
        assertEquals("{\"value\":25,\"path\":\"Sample context\"}",
                mapper.writeValueAsString(new CommandInteger(25, "Sample context")));

        assertEquals("{\"value\":\"test\",\"path\":null}", mapper.writeValueAsString(new CommandString("test")));
        assertEquals("{\"value\":\"test\",\"path\":\"Sample context\"}",
                mapper.writeValueAsString(new CommandString("test", "Sample context")));

        assertEquals("{\"value\":\"off\",\"path\":null}", mapper.writeValueAsString(new CommandOnOf(Enums.OnOff.OFF)));
        assertEquals("{\"value\":\"on\",\"path\":\"Sample context\"}",
                mapper.writeValueAsString(new CommandOnOf(Enums.OnOff.ON, "Sample context")));

        assertEquals("{\"value\":false}", mapper.writeValueAsString(new CommandTrueFalse(Enums.OnOff.OFF)));
        assertEquals("{\"value\":true}", mapper.writeValueAsString(new CommandTrueFalse(Enums.OnOff.ON)));
    }
}
