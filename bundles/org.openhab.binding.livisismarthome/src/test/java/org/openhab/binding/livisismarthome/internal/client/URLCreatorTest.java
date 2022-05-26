/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.livisismarthome.internal.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class URLCreatorTest {

    @Test
    public void testCreateEventsURL() {
        String url = URLCreator.createEventsURL("localhost", "token+123/456", false);
        // The token should be URL encoded
        assertEquals("ws://localhost:9090/events?token=token%2B123%2F456", url);
    }

    @Test
    public void testCreateEventsURL_ClassicController() {
        String url = URLCreator.createEventsURL("localhost", "token123", true);
        assertEquals("ws://localhost:8080/events?token=token123", url);
    }

    @Test
    public void testCreateEventsURL_Gen2Controller() {
        String url = URLCreator.createEventsURL("localhost", "token123", false);
        assertEquals("ws://localhost:9090/events?token=token123", url);
    }
}
