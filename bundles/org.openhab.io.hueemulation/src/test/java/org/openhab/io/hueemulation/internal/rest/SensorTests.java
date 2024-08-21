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
package org.openhab.io.hueemulation.internal.rest;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.rest.mocks.DummyItemRegistry;

/**
 * Tests for {@link Sensors}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SensorTests {
    protected @NonNullByDefault({}) CommonSetup commonSetup;
    protected @NonNullByDefault({}) ItemRegistry itemRegistry;
    protected @NonNullByDefault({}) ConfigStore cs;

    private @NonNullByDefault({}) HttpClient httpClient;

    Sensors subject = new Sensors();

    private void addItemToReg(GenericItem item, State state, String label) {
        item.setState(state);
        item.setLabel(label);
        itemRegistry.add(item);
    }

    @BeforeEach
    public void setUp() throws Exception {
        commonSetup = new CommonSetup(false);
        itemRegistry = new DummyItemRegistry();

        this.cs = commonSetup.cs;

        subject.cs = cs;
        subject.userManagement = commonSetup.userManagement;
        subject.itemRegistry = itemRegistry;
        subject.activate();

        httpClient = new HttpClient();
        httpClient.start();

        // Add simulated sensor items
        addItemToReg(new SwitchItem("switch1"), OnOffType.ON, "name1");
        addItemToReg(new ContactItem("contact1"), OpenClosedType.OPEN, "");
        addItemToReg(new ColorItem("color1"), HSBType.BLUE, "");
        addItemToReg(new DimmerItem("white1"), new PercentType(12), "");
        addItemToReg(new RollershutterItem("roller1"), new PercentType(12), "");
        addItemToReg(new NumberItem("number1"), new DecimalType(12), "");

        commonSetup.start(new ResourceConfig().registerInstances(subject));
    }

    @AfterEach
    public void tearDown() throws Exception {
        commonSetup.dispose();
    }

    @Test
    public void renameSensor() throws Exception {
        assertThat(cs.ds.sensors.get("switch1").name, is("name1"));

        String body = "{'name':'name2'}";
        ContentResponse response = commonSetup.sendPut("/testuser/sensors/switch1", body);
        assertEquals(200, response.getStatus());

        body = response.getContentAsString();

        assertThat(body, containsString("success"));
        assertThat(body, containsString("name"));
        assertThat(cs.ds.sensors.get("switch1").name, is("name2"));
    }

    @Test
    public void allAndSingleSensor() throws Exception {
        ContentResponse response = commonSetup.sendGet("/testuser/sensors");
        assertEquals(200, response.getStatus());

        String body = response.getContentAsString();

        assertThat(body, containsString("switch1"));
        assertThat(body, containsString("color1"));
        assertThat(body, containsString("white1"));

        // Single light access test
        response = commonSetup.sendGet("/testuser/sensors/switch1");
        assertEquals(200, response.getStatus());
        body = response.getContentAsString();
        assertThat(body, containsString("CLIPGenericFlag"));
    }
}
