/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.controls;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import org.openhab.binding.loxone.internal.LxBindingConfiguration;
import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.types.LxConfig;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Dummy implementation of thing handler and its API towards controls.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxServerHandlerDummy implements LxServerHandlerApi {

    Gson gson;
    LxConfig config;
    LxBindingConfiguration bindingConfig = new LxBindingConfiguration();

    Queue<String> actionQueue = new LinkedList<>();

    Map<LxUuid, LxControl> controls;
    Map<LxUuid, LxControl> extraControls = new HashMap<>();
    Map<ChannelUID, StateDescription> stateDescriptions = new HashMap<>();

    public LxServerHandlerDummy() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LxUuid.class, LxUuid.DESERIALIZER);
        builder.registerTypeAdapter(LxControl.class, LxControl.DESERIALIZER);
        gson = builder.create();
    }

    void loadConfiguration() {
        InputStream stream = LxServerHandlerDummy.class.getResourceAsStream("LoxAPP3.json");
        assertNotNull(stream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        assertNotNull(reader);
        String msg = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        assertNotNull(msg);

        stateDescriptions.clear();

        LxConfig config = gson.fromJson(msg, LxConfig.class);
        config.finalize(this);
        controls = config.controls;
        assertNotNull(controls);
    }

    @Override
    public void sendAction(LxUuid id, String operation) throws IOException {
        actionQueue.add(id + "/" + operation);
    }

    @Override
    public void addControl(LxControl control) {
        extraControls.put(control.getUuid(), control);
    }

    @Override
    public void removeControl(LxControl control) {
        LxControl ctrl = extraControls.remove(control.getUuid());
        assertNotNull(ctrl);
    }

    @Override
    public void setChannelState(ChannelUID channelId, State state) {
    }

    @Override
    public void setChannelStateDescription(ChannelUID channelId, StateDescription description) {
        assertNotNull(channelId);
        assertNotNull(description);
        stateDescriptions.put(channelId, description);
    }

    @Override
    public String getSetting(String name) {
        return null;
    }

    @Override
    public void setSettings(Map<String, String> properties) {
    }

    @Override
    public Gson getGson() {
        return gson;
    }

    @Override
    public ThingUID getThingId() {
        return new ThingUID("loxone:miniserver:12345678");
    }
}
