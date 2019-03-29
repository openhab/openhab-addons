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
package org.openhab.io.hueemulation.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.io.hueemulation.internal.RESTApi.HttpMethod;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueDevice;
import org.openhab.io.hueemulation.internal.dto.HueStateColorBulb;
import org.openhab.io.hueemulation.internal.dto.HueStatePlug;
import org.openhab.io.hueemulation.internal.dto.HueUserAuth;

import com.google.gson.Gson;

/**
 * Tests for {@link RESTApi}.
 *
 * @author David Graeff - Initial contribution
 */
public class HueRestAPITest {

    private Gson gson;
    private HueDataStore ds;
    private RESTApi restAPI;
    private UserManagement userManagement;
    private ConfigManagement configManagement;
    @Mock
    private EventPublisher eventPublisher;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        gson = new Gson();
        ds = new HueDataStore();
        userManagement = spy(new UserManagement(ds));
        configManagement = spy(new ConfigManagement(ds));
        restAPI = spy(new RESTApi(ds, userManagement, configManagement, gson));
        restAPI.setEventPublisher(eventPublisher);

        // Add simulated lights
        ds.lights.put(1, new HueDevice(new SwitchItem("switch"), "switch", DeviceType.SwitchType));
        ds.lights.put(2, new HueDevice(new ColorItem("color"), "color", DeviceType.ColorType));
        ds.lights.put(3, new HueDevice(new ColorItem("white"), "white", DeviceType.WhiteTemperatureType));

        // Add group item
        ds.lights.put(10,
                new HueDevice(new GroupItem("white", new SwitchItem("switch")), "white", DeviceType.SwitchType));
    }

    @Test
    public void invalidUser() throws IOException {
        PrintWriter out = mock(PrintWriter.class);
        int result = restAPI.handleUser(HttpMethod.GET, "", out, "testuser", Paths.get(""), Paths.get(""), false);
        assertEquals(403, result);
    }

    @Test
    public void validUser() throws IOException {
        PrintWriter out = mock(PrintWriter.class);
        ds.config.whitelist.put("testuser", new HueUserAuth("testuser"));
        int result = restAPI.handleUser(HttpMethod.GET, "", out, "testuser", Paths.get("/"), Paths.get(""), false);
        assertEquals(200, result);
    }

    @Test
    public void addUser() throws IOException {
        PrintWriter out = mock(PrintWriter.class);
        HttpServletRequest req = mock(HttpServletRequest.class);

        // GET should fail
        int result = restAPI.handle(HttpMethod.GET, "", out, Paths.get("/api"), false);
        assertEquals(405, result);

        // Post should create a user, except: if linkbutton not enabled
        result = restAPI.handle(HttpMethod.POST, "", out, Paths.get("/api"), false);
        assertEquals(10403, result);

        // Post should create a user
        ds.config.linkbutton = true;
        when(req.getMethod()).thenReturn("POST");
        String body = "{'username':'testuser','devicetype':'user-label'}";
        result = restAPI.handle(HttpMethod.POST, body, out, Paths.get("/api"), false);
        assertEquals(result, 200);
        assertThat(ds.config.whitelist.get("testuser").name, is("user-label"));
    }

    @Test
    public void changeSwitchState() throws IOException {
        ds.config.whitelist.put("testuser", new HueUserAuth("testuser"));

        assertThat(((HueStatePlug) ds.lights.get(1).state).on, is(false));

        StringWriter out = new StringWriter();
        String body = "{'on':true}";
        int result = restAPI.handle(HttpMethod.PUT, body, out, Paths.get("/api/testuser/lights/1/state"), false);
        assertEquals(200, result);
        assertThat(out.toString(), containsString("success"));
        assertThat(((HueStatePlug) ds.lights.get(1).state).on, is(true));
        verify(eventPublisher).post(argThat((Event t) -> {
            assertThat(t.getPayload(), is("{\"type\":\"OnOff\",\"value\":\"ON\"}"));
            return true;
        }));
    }

    @Test
    public void changeGroupItemSwitchState() throws IOException {
        ds.config.whitelist.put("testuser", new HueUserAuth("testuser"));

        assertThat(((HueStatePlug) ds.lights.get(10).state).on, is(false));

        StringWriter out = new StringWriter();
        String body = "{'on':true}";
        int result = restAPI.handle(HttpMethod.PUT, body, out, Paths.get("/api/testuser/lights/10/state"), false);
        assertEquals(200, result);
        assertThat(out.toString(), containsString("success"));
        assertThat(((HueStatePlug) ds.lights.get(10).state).on, is(true));
        verify(eventPublisher).post(argThat((Event t) -> {
            assertThat(t.getPayload(), is("{\"type\":\"OnOff\",\"value\":\"ON\"}"));
            return true;
        }));
    }

    @Test
    public void changeOnAndBriValues() throws IOException {
        ds.config.whitelist.put("testuser", new HueUserAuth("testuser"));

        assertThat(((HueStateColorBulb) ds.lights.get(2).state).on, is(false));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).bri, is(0));

        String body = "{'on':true,'bri':200}";
        StringWriter out = new StringWriter();
        int result = restAPI.handle(HttpMethod.PUT, body, out, Paths.get("/api/testuser/lights/2/state"), false);
        assertEquals(200, result);
        assertThat(out.toString(), containsString("success"));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).on, is(true));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).bri, is(200));
    }

    @Test
    public void switchOnWithXY() throws IOException {
        ds.config.whitelist.put("testuser", new HueUserAuth("testuser"));

        assertThat(((HueStateColorBulb) ds.lights.get(2).state).on, is(false));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).bri, is(0));

        String body = "{'on':true,'bri':200,'xy':[0.5119,0.4147]}";
        StringWriter out = new StringWriter();
        int result = restAPI.handle(HttpMethod.PUT, body, out, Paths.get("/api/testuser/lights/2/state"), false);
        assertEquals(200, result);
        assertThat(out.toString(), containsString("success"));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).on, is(true));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).bri, is(200));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).xy[0], is(0.5119));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).xy[1], is(0.4147));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).colormode, is(HueStateColorBulb.ColorMode.xy));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).toHSBType().getHue().intValue(), is((int)27.47722590981918));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).toHSBType().getSaturation().intValue(), is(88));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).toHSBType().getBrightness().intValue(), is(78));
    }
}
