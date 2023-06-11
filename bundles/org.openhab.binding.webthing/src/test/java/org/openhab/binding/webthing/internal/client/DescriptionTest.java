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
package org.openhab.binding.webthing.internal.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 *
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class DescriptionTest {

    @Test
    public void testDescriptionEventStreamUri() throws Exception {
        var httpClient = mock(org.eclipse.jetty.client.HttpClient.class);
        var request = Mocks.mockRequest(null, load("/awning_response.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090"))).thenReturn(request);

        var loader = new DescriptionLoader(httpClient);
        var description = loader.loadWebthingDescription(URI.create("http://example.org:8090"), Duration.ofSeconds(2));
        assertEquals("ws://192.168.4.12:9040/0", description.getEventStreamUri().get().toString());
    }

    @Test
    public void testDescriptionEventStreamUriServerlaAlternateParts() throws Exception {
        var httpClient = mock(org.eclipse.jetty.client.HttpClient.class);
        var request = Mocks.mockRequest(null, load("/virtual-things_response.json"));
        when(httpClient.newRequest(URI.create("http://example.org:8090"))).thenReturn(request);

        var loader = new DescriptionLoader(httpClient);
        var description = loader.loadWebthingDescription(URI.create("http://example.org:8090"), Duration.ofSeconds(2));
        assertEquals("ws://webthings/things/virtual-things-7", description.getEventStreamUri().get().toString());
    }

    public static String load(String name) throws Exception {
        return new String(Files.readAllBytes(Paths.get(WebthingTest.class.getResource(name).toURI())));
    }
}
