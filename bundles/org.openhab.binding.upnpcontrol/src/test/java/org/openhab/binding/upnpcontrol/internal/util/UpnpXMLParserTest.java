/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

/**
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class UpnpXMLParserTest {

    @Test
    void externalDtdIsNotFetched() throws Exception {
        AtomicBoolean requested = new AtomicBoolean();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/external.dtd", exchange -> {
            requested.set(true);
            byte[] body = "<!ELEMENT Event EMPTY>".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            String xml = "<!DOCTYPE Event SYSTEM \"http://127.0.0.1:" + port + "/external.dtd\"><Event/>";
            UpnpXMLParser.getRenderingControlFromXML(xml);
            assertFalse(requested.get(), "External DTD must not be fetched");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void getAVTransportFromXMLRejectsXXE() throws IOException {
        Path canary = Files.createTempFile("upnp-xxe-canary", ".txt");
        try {
            Files.writeString(canary, "XXE_CANARY");
            String xxe = """
                    <?xml version="1.0"?>
                    <!DOCTYPE root [ <!ENTITY xxe SYSTEM "%s"> ]>
                    <Event><TransportState val="PLAYING"/>&xxe;</Event>
                    """.formatted(canary.toUri());
            assertTrue(UpnpXMLParser.getAVTransportFromXML(xxe).isEmpty());
        } finally {
            Files.deleteIfExists(canary);
        }
    }

    @Test
    void getEntriesFromXMLRejectsXXE() throws IOException {
        Path canary = Files.createTempFile("upnp-xxe-canary", ".txt");
        try {
            Files.writeString(canary, "XXE_CANARY");
            String xxe = """
                    <?xml version="1.0"?>
                    <!DOCTYPE root [ <!ENTITY xxe SYSTEM "%s"> ]>
                    <DIDL-Lite><item><dc:title>&xxe;</dc:title></item></DIDL-Lite>
                    """.formatted(canary.toUri());
            assertTrue(UpnpXMLParser.getEntriesFromXML(xxe).isEmpty());
        } finally {
            Files.deleteIfExists(canary);
        }
    }
}
