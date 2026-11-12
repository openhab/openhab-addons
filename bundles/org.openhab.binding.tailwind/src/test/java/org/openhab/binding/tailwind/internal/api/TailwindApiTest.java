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
package org.openhab.binding.tailwind.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tailwind.internal.FakeController;
import org.openhab.binding.tailwind.internal.FakeController.Received;
import org.openhab.binding.tailwind.internal.api.dto.TailwindDoor;
import org.openhab.binding.tailwind.internal.api.dto.TailwindStatus;

/**
 * Tests for {@link TailwindApi}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class TailwindApiTest {
    private final FakeController controller = new FakeController();
    private final TailwindApi api = new TailwindApi(controller.httpClient, "192.168.1.50", "123456");

    @Test
    public void statusRequestIsSentWithTheTokenAndParsed() throws TailwindException {
        TailwindStatus status = api.getStatus();

        Received request = controller.requests.get(0);
        assertEquals("http://192.168.1.50/json", request.url());
        assertEquals("123456", request.token());
        assertEquals("iQ3", request.body().get("product").getAsString());
        assertEquals("get", request.body().getAsJsonObject("data").get("type").getAsString());
        assertEquals("dev_st", request.name());

        assertTrue(status.isOk());
        assertEquals("_30_ae_a4_80_18_80_", status.deviceId);
        assertEquals("10.80", status.firmwareVersion);
        assertEquals(2, status.doorCount);
        assertEquals(0, status.nightModeEnabled);
        assertEquals(100, status.ledBrightness);
        assertEquals(-31, status.routerRssi);
        assertEquals(Boolean.TRUE, status.serverMonitor);
        TailwindDoor door2 = status.door(1);
        assertNotNull(door2);
        assertEquals("close", door2.status);
        assertEquals(1, door2.lockup);
        assertEquals(0, door2.disabled);
        assertNull(status.door(2));
    }

    @Test
    public void doorCommandsCarryIndexCommandAndPartialTime() throws TailwindException {
        api.openDoor(0);
        api.openDoorPartially(1, 2500);
        api.closeDoor(2);

        assertEquals("{\"door_idx\":0,\"cmd\":\"open\"}", controller.requests.get(0).value().toString());
        assertEquals("{\"door_idx\":1,\"cmd\":\"open\",\"partial_time\":2500}",
                controller.requests.get(1).value().toString());
        assertEquals("{\"door_idx\":2,\"cmd\":\"close\"}", controller.requests.get(2).value().toString());
        assertEquals("door_op", controller.requests.get(2).name());
    }

    @Test
    public void otherCommandsMatchTheApiDocument() throws TailwindException {
        api.setNotifyUrl("192.168.1.10:50904");
        api.setLedBrightness(40);
        api.setServerMonitor(false);
        api.identify();
        api.reboot();

        assertEquals("notify_url", controller.requests.get(0).name());
        assertEquals("{\"url\":\"192.168.1.10:50904\",\"proto\":\"udp\",\"enable\":1}",
                controller.requests.get(0).value().toString());
        assertEquals("status_led", controller.requests.get(1).name());
        assertEquals("{\"brightness\":40}", controller.requests.get(1).value().toString());
        assertEquals("server_monitor", controller.requests.get(2).name());
        assertEquals("{\"enable\":false}", controller.requests.get(2).value().toString());
        assertEquals("0.2", controller.requests.get(2).body().get("version").getAsString());
        assertEquals("identify", controller.requests.get(3).name());
        assertFalse(controller.requests.get(3).body().getAsJsonObject("data").has("value"));
        assertEquals("reboot", controller.requests.get(4).name());
    }

    @Test
    public void rejectedTokenIsUnauthorized() {
        controller.answerAllWith(400, FakeController.TOKEN_FAIL);

        assertThrows(TailwindUnauthorizedException.class, api::getStatus);
    }

    @Test
    public void unauthorizedHttpStatusIsUnauthorized() {
        controller.answerAllWith(401, "");

        assertThrows(TailwindUnauthorizedException.class, api::getStatus);
    }

    @Test
    public void refusedRequestIsNotACommunicationError() {
        controller.answerAllWith(200, "{\"result\":\"Fail\",\"info\":\"door_idx out of range\"}");

        TailwindException e = assertThrows(TailwindException.class, () -> api.openDoor(0));
        assertFalse(e instanceof TailwindCommunicationException);
        assertTrue(String.valueOf(e.getMessage()).contains("door_idx out of range"));
    }

    @Test
    public void answersThatCannotBeUnderstoodAreCommunicationErrors() {
        controller.answerAllWith(500, "<html>Internal Server Error</html>");
        assertThrows(TailwindCommunicationException.class, api::getStatus);

        controller.answerAllWith(200, "");
        assertThrows(TailwindCommunicationException.class, api::getStatus);

        controller.answerAllWith(200, "{\"result\":");
        assertThrows(TailwindCommunicationException.class, api::getStatus);
    }

    @Test
    public void noAnswerIsACommunicationError() {
        controller.setUnreachable(true);

        assertThrows(TailwindCommunicationException.class, api::getStatus);
    }
}
