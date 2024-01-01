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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.Commands.SYSTEM_STATUS_CONFIG_CMD;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.Commands.ZONE_BASIC_STATUS_CMD;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.openhab.binding.yamahareceiver.internal.ResponseLoader;

/**
 * Testing context for a selected Yamaha model.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class ModelContext {

    private final ResponseLoader rl = new ResponseLoader();
    private final XMLConnection connection;
    private String model;

    public XMLConnection getConnection() {
        return connection;
    }

    public ModelContext(XMLConnection connection) {
        this.connection = connection;
    }

    public void prepareForModel(String model) throws IOException {
        this.model = model;

        String descFile = String.format("/desc_%s.xml", model);
        String desc = rl.load(descFile, model);
        if (desc == null) {
            throw new FileNotFoundException("Could not load " + descFile);
        }

        when(connection.getResponse(eq("/YamahaRemoteControl/desc.xml"))).thenReturn(desc);
        when(connection.getHost()).thenReturn("localhost");

        respondWith(SYSTEM_STATUS_CONFIG_CMD, "System_Config.xml");
        respondWith(String.format("<Main_Zone>%s</Main_Zone>", ZONE_BASIC_STATUS_CMD), "Main_Zone_Basic_Status.xml");
    }

    public void respondWith(String command, String path) {
        try {
            String response = rl.load(path, model);
            if (response != null) {
                when(connection.sendReceive(eq(command))).thenReturn(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
