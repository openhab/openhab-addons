/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueDataStore.UserAuth;
import org.openhab.io.hueemulation.internal.dto.HueDevice;
import org.openhab.io.hueemulation.internal.dto.HueStateColorBulb;
import org.openhab.io.hueemulation.internal.dto.HueStatePlug;

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
    }

    @Test
    public void invalidUser() throws IOException {
        PrintWriter out = mock(PrintWriter.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getMethod()).thenReturn("GET");
        int result = restAPI.handleUser(req, out, "testuser", Paths.get(""));
        assertEquals(403, result);
    }

    @Test
    public void validUser() throws IOException {
        PrintWriter out = mock(PrintWriter.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getMethod()).thenReturn("GET");
        ds.config.whitelist.put("testuser", new UserAuth("testuser"));
        int result = restAPI.handleUser(req, out, "testuser", Paths.get("/"));
        assertEquals(200, result);
    }

    @Test
    public void addUser() throws IOException {
        PrintWriter out = mock(PrintWriter.class);
        HttpServletRequest req = mock(HttpServletRequest.class);

        // GET should fail
        when(req.getMethod()).thenReturn("GET");
        int result = restAPI.handle(req, out, Paths.get("/api"));
        assertEquals(405, result);

        // Post should create a user, except: if linkbutton not enabled
        when(req.getMethod()).thenReturn("POST");
        result = restAPI.handle(req, out, Paths.get("/api"));
        assertEquals(10403, result);

        // Post should create a user
        ds.config.linkbutton = true;
        when(req.getMethod()).thenReturn("POST");
        BufferedReader r = new BufferedReader(new StringReader("{'username':'testuser','devicetype':'user-label'}"));
        when(req.getReader()).thenReturn(r);
        when(req.getContentType()).thenReturn("application/json");
        result = restAPI.handle(req, out, Paths.get("/api"));
        assertEquals(result, 200);
        assertThat(ds.config.whitelist.get("testuser").name, is("user-label"));
    }

    @Test
    public void changeLightState() throws IOException {
        StringWriter out = new StringWriter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        // Prepare request mock to POST a json
        when(req.getMethod()).thenReturn("PUT");
        when(req.getContentType()).thenReturn("application/json");

        // Add simulated lights
        ds.lights.put(1, new HueDevice(new SwitchItem("switch"), "switch", DeviceType.SwitchType));
        ds.lights.put(2, new HueDevice(new ColorItem("color"), "color", DeviceType.ColorType));
        ds.lights.put(3, new HueDevice(new ColorItem("white"), "white", DeviceType.WhiteTemperatureType));

        // Add simulated api-key
        ds.config.whitelist.put("testuser", new UserAuth("testuser"));

        int result;

        // Post new state to a switch
        assertThat(((HueStatePlug) ds.lights.get(1).state).on, is(false));
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{'on':true}")));
        when(req.getRequestURI()).thenReturn("/api/testuser/lights/1/state");
        result = restAPI.handle(req, out, Paths.get(req.getRequestURI()));
        assertEquals(200, result);
        assertThat(out.toString(), containsString("success"));
        assertThat(((HueStatePlug) ds.lights.get(1).state).on, is(true));
        verify(eventPublisher).post(argThat((Event t) -> {
            assertThat(t.getPayload(), is("{\"type\":\"OnOff\",\"value\":\"ON\"}"));
            return true;
        }));

        // Post new state to a light
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).on, is(false));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).bri, is(0));
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{'on':true,'bri':200}")));
        when(req.getRequestURI()).thenReturn("/api/testuser/lights/2/state");
        result = restAPI.handle(req, out, Paths.get(req.getRequestURI()));
        assertEquals(200, result);
        assertThat(out.toString(), containsString("success"));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).on, is(true));
        assertThat(((HueStateColorBulb) ds.lights.get(2).state).bri, is(200));

    }

}
