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
package org.openhab.binding.miio.internal.transport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.MiIoMessageListener;
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.binding.miio.internal.cloud.MiCloudException;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests how requests via the Xiaomi cloud affect the status reported for the device.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class MiIoAsyncCommunicationCloudTest {

    private static final String DEVICE_ID = "AABBCCDD";

    private @Mock @NonNullByDefault({}) CloudConnector cloudConnector;
    private @Mock @NonNullByDefault({}) MiIoMessageListener listener;
    private @NonNullByDefault({}) MiIoAsyncCommunication communication;

    @BeforeEach
    public void setUp() {
        communication = new MiIoAsyncCommunication("127.0.0.1", new byte[16], DEVICE_ID, 1, 1000, cloudConnector);
        communication.getListeners().add(listener);
    }

    private MiIoSendCommand command(String method) {
        JsonObject json = new JsonObject();
        json.addProperty("id", 1);
        json.addProperty("method", method);
        json.add("params", JsonParser.parseString("{\"model\":\"test.model\"}"));
        return new MiIoSendCommand(1, MiIoCommand.getCommand(method), json, "de", "");
    }

    @Test
    public void failingCustomCloudRequestDoesNotChangeDeviceStatus() throws MiCloudException {
        when(cloudConnector.sendCloudCommand(anyString(), anyString(), anyString()))
                .thenThrow(new MiCloudException("Cannot execute request. Cloud service not available"));

        MiIoSendCommand result = communication.sendMiIoSendCommand(command("/v2/recipes/query"));

        assertTrue(result.isError());
        assertEquals("Cannot execute request. Cloud service not available",
                result.getResponse().get("error").getAsString());
        verify(listener, never()).onStatusUpdated(any(), any());
    }

    @Test
    public void successfulCustomCloudRequestReturnsResponse() throws MiCloudException {
        when(cloudConnector.sendCloudCommand("/v2/recipes/query", "de", "{\"model\":\"test.model\"}"))
                .thenReturn("{\"code\":0,\"result\":{\"recipes\":[]}}");

        MiIoSendCommand result = communication.sendMiIoSendCommand(command("/v2/recipes/query"));

        assertFalse(result.isError());
        assertEquals(JsonParser.parseString("{\"recipes\":[]}"), result.getResult());
        verify(listener, never()).onStatusUpdated(any(), any());
    }

    @Test
    public void emptyCustomCloudResponseGivesErrorWithoutStatusChange() throws MiCloudException {
        when(cloudConnector.sendCloudCommand(anyString(), anyString(), anyString())).thenReturn("");

        MiIoSendCommand result = communication.sendMiIoSendCommand(command("/v2/recipes/query"));

        assertTrue(result.isError());
        assertEquals("Received message is not a JSON object", result.getResponse().get("error").getAsString());
        verify(listener, never()).onStatusUpdated(any(), any());
    }

    @Test
    public void nonObjectCustomCloudResponseGivesErrorWithoutStatusChange() throws MiCloudException {
        when(cloudConnector.sendCloudCommand(anyString(), anyString(), anyString())).thenReturn("[1,2]");

        MiIoSendCommand result = communication.sendMiIoSendCommand(command("/v2/recipes/query"));

        assertTrue(result.isError());
        assertEquals("Received message is not a JSON object", result.getResponse().get("error").getAsString());
        verify(listener, never()).onStatusUpdated(any(), any());
    }

    @Test
    public void failingCloudRpcCommandStillSetsDeviceOffline() throws MiCloudException {
        when(cloudConnector.sendRPCCommand(anyString(), anyString(), any(MiIoSendCommand.class)))
                .thenThrow(new MiCloudException("Cannot execute request. Cloud service not available"));

        MiIoSendCommand result = communication.sendMiIoSendCommand(command("get_prop"));

        assertTrue(result.isError());
        verify(listener).onStatusUpdated(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }
}
