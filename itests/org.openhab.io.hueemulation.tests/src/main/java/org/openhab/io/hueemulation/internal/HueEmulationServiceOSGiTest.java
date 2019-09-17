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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openhab.core.automation.RuleRegistry;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.EventAdmin;

/**
 * Integration tests for {@link HueEmulationService}.
 *
 * @author David Graeff - Initial contribution
 */
public class HueEmulationServiceOSGiTest extends JavaOSGiTest {

    private static final String JUPNP_PID = "org.jupnp";

    private HueEmulationService hueService;
    private VolatileStorageService volatileStorageService = new VolatileStorageService();

    @Before
    public void setUp() throws IOException, InvalidSyntaxException {
        registerService(volatileStorageService);

        EventAdmin eventAdmin = getService(EventAdmin.class, EventAdmin.class);
        assertThat(eventAdmin, notNullValue());

        ItemRegistry itemRegistry = getService(ItemRegistry.class, ItemRegistry.class);
        assertThat(itemRegistry, notNullValue());

        RuleRegistry ruleRegistry = getService(RuleRegistry.class, RuleRegistry.class);
        assertThat(ruleRegistry, notNullValue());

        ConfigurationAdmin configurationAdmin = getService(ConfigurationAdmin.class, ConfigurationAdmin.class);
        assertThat(configurationAdmin, notNullValue());

        Dictionary<String, Object> jupnpConfig = new Hashtable<>();
        jupnpConfig.put("threadPoolSize", 5);
        Configuration configuration = configurationAdmin.getConfiguration(JUPNP_PID, null);
        configuration.update(jupnpConfig);

        waitForAssert(() -> {
            hueService = getService(HueEmulationService.class, HueEmulationService.class);
            assertThat(hueService, notNullValue());
        }, 3000, 200);
    }

    @After
    public void tearDown() {
        unregisterService(volatileStorageService);
    }

    @Test(timeout = 10000)
    public void UpnpServiceTest() throws IOException {
        waitFor(() -> !hueService.cs.ds.config.ipaddress.isEmpty(), 5000, 100);

        String ipAddress = hueService.cs.ds.config.ipaddress;
        int port = hueService.cs.config.discoveryHttpPort == 0 ? Integer.getInteger("org.osgi.service.http.port", 8080)
                : hueService.cs.config.discoveryHttpPort;
        String url = "http://" + ipAddress + ":" + port + "/description.xml";

        waitForAssert(() -> {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                assertThat(c.getResponseCode(), is(200));
                String body = read(c);
                assertThat(body, containsString(hueService.cs.config.uuid));
            } catch (IOException e) {
                fail(e.getMessage());
            }
        });
    }

    private String read(HttpURLConnection urlConnection) throws IOException {
        String result = "";
        final InputStream is;
        if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
            is = urlConnection.getInputStream();
        } else {
            /* error from server */
            is = urlConnection.getErrorStream();
        }
        try (InputStream in = new BufferedInputStream(is)) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
        }
        return result;
    }

}
