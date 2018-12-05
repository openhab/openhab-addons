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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.service.ReadyMarker;
import org.eclipse.smarthome.core.service.ReadyService;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.io.hueemulation.internal.dto.HueDataStore.UserAuth;
import org.openhab.io.hueemulation.internal.dto.HueDevice;
import org.openhab.io.hueemulation.internal.dto.HueUnauthorizedConfig;
import org.osgi.service.cm.ConfigurationAdmin;

import com.google.gson.Gson;

/**
 * Integration tests for {@link HueEmulationService}.
 *
 * @author David Graeff - Initial contribution
 */
public class HueEmulationServiceOSGiTest extends JavaOSGiTest {
    private HueEmulationService hueService;
    VolatileStorageService volatileStorageService = new VolatileStorageService();

    ItemRegistry itemRegistry;
    @Mock
    ConfigurationAdmin configurationAdmin;

    String host;

    @SuppressWarnings("null")
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        registerService(volatileStorageService);
        registerService(configurationAdmin);

        itemRegistry = getService(ItemRegistry.class, ItemRegistry.class);
        assertThat(itemRegistry, notNullValue());
        ReadyService readyService = getService(ReadyService.class, ReadyService.class);
        assertThat(readyService, notNullValue());
        hueService = getService(HueEmulationService.class, HueEmulationService.class);
        assertThat(hueService, notNullValue());

        readyService.markReady(new ReadyMarker("fake", "org.eclipse.smarthome.model.core"));
        waitFor(() -> hueService.discovery != null, 5000, 100);
        assertThat(hueService.started, is(true));

        InetAddress address = hueService.discovery.getAddress();
        host = "http://" + address.getHostAddress() + ":" + String.valueOf(hueService.discovery.getWebPort());
    }

    @After
    public void tearDown() {
        unregisterService(volatileStorageService);
    }

    @SuppressWarnings("null")
    private String read(HttpURLConnection urlConnection) throws IOException {
        String result = "";
        final InputStream _is;
        if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
            _is = urlConnection.getInputStream();
        } else {
            /* error from server */
            _is = urlConnection.getErrorStream();
        }
        try (InputStream in = new BufferedInputStream(_is)) {
            if (in != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
            }
        }
        return result;
    }

    @Test
    public void UnauthorizedAccessTest()
            throws InterruptedException, ExecutionException, TimeoutException, IOException {

        // upnp response
        HttpURLConnection c = (HttpURLConnection) new URL(host + "/description.xml").openConnection();
        assertThat(c.getResponseCode(), is(200));
        String body = read(c);
        assertThat(body, containsString(hueService.ds.config.uuid));

        // Unauthorized config access
        c = (HttpURLConnection) new URL(host + "/api/config").openConnection();
        assertThat(c.getResponseCode(), is(200));
        body = read(c);
        HueUnauthorizedConfig config = new Gson().fromJson(body, HueUnauthorizedConfig.class);
        assertThat(config.bridgeid, is(hueService.ds.config.bridgeid));
        assertThat(config.name, is(hueService.ds.config.name));

        // Invalid user name
        c = (HttpURLConnection) new URL(host + "/api/invalid/lights").openConnection();
        assertThat(c.getResponseCode(), is(403));
        body = read(c);
        assertThat(body, containsString("error"));

        // Add user name (no link button)
        body = "{'username':'testuser','devicetype':'label'}";
        c = (HttpURLConnection) new URL(host + "/api").openConnection();
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.getOutputStream().write(body.getBytes(), 0, body.getBytes().length);
        assertThat(c.getResponseCode(), is(403));

        // Add user name (link button)
        hueService.ds.config.linkbutton = true;
        c = (HttpURLConnection) new URL(host + "/api").openConnection();
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.getOutputStream().write(body.getBytes(), 0, body.getBytes().length);
        assertThat(c.getResponseCode(), is(200));
        body = read(c);
        assertThat(body, containsString("success"));
        assertThat(hueService.ds.config.whitelist.get("testuser").name, is("label"));
    }

    @Test
    public void LightsTest() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        HttpURLConnection c;
        String body;

        hueService.ds.config.whitelist.put("testuser", new UserAuth("testUserLabel"));

        c = (HttpURLConnection) new URL(host + "/api/testuser/lights").openConnection();
        assertThat(c.getResponseCode(), is(200));
        body = read(c);
        assertThat(body, containsString("{}"));

        Item item = mock(Item.class);
        hueService.ds.lights.put(1, new HueDevice(item, "switch", DeviceType.SwitchType));
        hueService.ds.lights.put(2, new HueDevice(item, "color", DeviceType.ColorType));
        hueService.ds.lights.put(3, new HueDevice(item, "white", DeviceType.WhiteTemperatureType));

        // Full access test
        c = (HttpURLConnection) new URL(host + "/api/testuser/lights").openConnection();
        assertThat(c.getResponseCode(), is(200));
        body = read(c);
        assertThat(body, containsString("switch"));
        assertThat(body, containsString("color"));
        assertThat(body, containsString("white"));

        // Single light access test
        c = (HttpURLConnection) new URL(host + "/api/testuser/lights/2").openConnection();
        assertThat(c.getResponseCode(), is(200));
        body = read(c);
        assertThat(body, containsString("color"));
    }
}
