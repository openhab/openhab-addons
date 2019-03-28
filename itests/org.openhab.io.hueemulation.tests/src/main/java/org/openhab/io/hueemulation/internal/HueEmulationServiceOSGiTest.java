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
import static org.junit.Assert.assertThat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jupnp.UpnpService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.core.automation.RuleRegistry;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.EventAdmin;

/**
 * Integration tests for {@link HueEmulationService}.
 *
 * @author David Graeff - Initial contribution
 */
public class HueEmulationServiceOSGiTest extends JavaOSGiTest {
    private HueEmulationService hueService;
    VolatileStorageService volatileStorageService = new VolatileStorageService();

    private @Nullable RuleRegistry ruleRegistry;
    private @Nullable ItemRegistry itemRegistry;
    private @Nullable EventAdmin eventAdmin;
    private @Nullable UpnpService upnpService;

    @Mock
    ConfigurationAdmin configurationAdmin;

    @Mock
    EventPublisher eventPublisher;

    String host;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        registerService(volatileStorageService);
        registerService(configurationAdmin);

        eventAdmin = getService(EventAdmin.class, EventAdmin.class);
        assertThat(eventAdmin, notNullValue());

        itemRegistry = getService(ItemRegistry.class, ItemRegistry.class);
        assertThat(itemRegistry, notNullValue());

        ruleRegistry = getService(RuleRegistry.class, RuleRegistry.class);
        assertThat(ruleRegistry, notNullValue());

        upnpService = getService(UpnpService.class, UpnpService.class);
        assertThat(upnpService, notNullValue());

        hueService = getService(HueEmulationService.class, HueEmulationService.class);
        assertThat(hueService, notNullValue());

    }

    @After
    public void tearDown() {
        unregisterService(volatileStorageService);
    }

    @Test(timeout = 5000)
    public void UpnpServiceTest() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        waitFor(() -> !hueService.cs.ds.config.ipaddress.isEmpty(), 5000, 100);
        host = "http://" + hueService.cs.ds.config.ipaddress + ":"
                + String.valueOf(hueService.cs.config.discoveryHttpPort);

        HttpURLConnection c = (HttpURLConnection) new URL(host + "/description.xml").openConnection();
        assertThat(c.getResponseCode(), is(200));
        String body = read(c);
        assertThat(body, containsString(hueService.cs.config.uuid));

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

}
