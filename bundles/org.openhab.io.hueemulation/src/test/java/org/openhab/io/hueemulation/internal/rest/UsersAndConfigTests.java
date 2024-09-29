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
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;

import org.eclipse.jetty.client.api.ContentResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.HueEmulationConfig;
import org.openhab.io.hueemulation.internal.dto.HueUnauthorizedConfig;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse;
import org.openhab.io.hueemulation.internal.dto.response.HueSuccessResponseCreateUser;
import org.openhab.io.hueemulation.internal.rest.mocks.ConfigStoreWithoutMetadata;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Tests for various user management API endpoints.
 *
 * @author David Graeff - Initial contribution
 */
public class UsersAndConfigTests {

    ConfigurationAccess configurationAccess = new ConfigurationAccess();

    CommonSetup commonSetup;

    @BeforeEach
    public void setUp() throws IOException {
        commonSetup = new CommonSetup(false);

        configurationAccess.cs = commonSetup.cs;
        configurationAccess.userManagement = commonSetup.userManagement;
        configurationAccess.configAdmin = commonSetup.configAdmin;

        commonSetup.start(new ResourceConfig().registerInstances(configurationAccess));
    }

    @AfterEach
    public void tearDown() throws Exception {
        commonSetup.dispose();
    }

    @Test
    public void invalidUser() {
        assertFalse(commonSetup.userManagement.authorizeUser("blub"));
    }

    @Test
    public void validUser() {
        assertTrue(commonSetup.userManagement.authorizeUser("testuser"));
    }

    @Test
    public void configStoreRestartOnNoUUID() {
        ConfigStore configStore = new ConfigStoreWithoutMetadata(commonSetup.networkAddressService,
                commonSetup.configAdmin, commonSetup.scheduler);

        // No uuid known yet
        assertThat(configStore.ds.config.uuid, is(""));
        configStore.activate(Collections.emptyMap());

        assertThat(configStore.getConfig().uuid, not(is("")));
        // The config admin service was requested for the service config
        Mockito.verify(commonSetup.configAdminConfig).getProperties();
        Dictionary<String, Object> p = commonSetup.configAdminConfig.getProperties();
        // And the service config was updated
        assertThat(p.get(HueEmulationConfig.CONFIG_UUID), is(configStore.getConfig().uuid));
    }

    @Test
    public void addUser() throws Exception {
        // GET should fail
        assertEquals(405, commonSetup.sendGet().getStatus());

        String body = "{'username':'testuser','devicetype':'app#device'}";

        // Post should create a user, except: if linkbutton not enabled
        ContentResponse response = commonSetup.sendPost(body);
        assertThat(response.getStatus(), is(200));
        HueResponse[] r = commonSetup.cs.gson.fromJson(response.getContentAsString(), HueResponse[].class);
        assertNotNull(r[0].error);

        // Post should create a user
        commonSetup.cs.ds.config.linkbutton = true;
        response = commonSetup.sendPost(body);
        assertThat(response.getStatus(), is(200));

        JsonElement e = JsonParser.parseString(response.getContentAsString()).getAsJsonArray().get(0);
        e = e.getAsJsonObject().get("success");
        HueSuccessResponseCreateUser rc = commonSetup.cs.gson.fromJson(e, HueSuccessResponseCreateUser.class);
        assertNotNull(rc);
        assertThat(commonSetup.cs.ds.config.whitelist.get(rc.username).name, is("app#device"));
    }

    @Test
    public void unauthorizedAccessTest() throws Exception {
        // Unauthorized config
        ContentResponse response = commonSetup.sendGet("/config");
        assertThat(response.getStatus(), is(200));
        HueUnauthorizedConfig config = new Gson().fromJson(response.getContentAsString(), HueUnauthorizedConfig.class);
        assertThat(config.bridgeid, is(commonSetup.cs.ds.config.bridgeid));
        assertThat(config.name, is(commonSetup.cs.ds.config.name));

        // Invalid user name
        response = commonSetup.sendGet("/invalid/config");
        assertThat(response.getStatus(), is(403));
        assertThat(response.getContentAsString(), containsString("error"));
    }
}
