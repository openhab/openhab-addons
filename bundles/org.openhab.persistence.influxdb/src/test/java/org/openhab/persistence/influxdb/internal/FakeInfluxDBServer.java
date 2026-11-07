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
package org.openhab.persistence.influxdb.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Minimal HTTP server on localhost that answers pings and write requests the way an InfluxDB server would, to test
 * how the repositories handle write responses.
 *
 * @author Eugen Freiter - Initial contribution
 */
@NonNullByDefault
public class FakeInfluxDBServer implements AutoCloseable {
    private final HttpServer server;
    private final List<String> writeBodies = new CopyOnWriteArrayList<>();
    private final List<String> writeQueries = new CopyOnWriteArrayList<>();
    private volatile int writeStatus = 204;
    private volatile String writeResponse = "";
    private boolean closed;

    /**
     * @param writePath the path of the write endpoint, e.g. <code>/write</code> for InfluxDB 1
     */
    public FakeInfluxDBServer(String writePath) throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/ping", exchange -> {
            exchange.getResponseHeaders().add("X-Influxdb-Version", "1.8.10");
            respond(exchange, 204, "");
        });
        server.createContext(writePath, exchange -> {
            writeQueries.add(String.valueOf(exchange.getRequestURI().getRawQuery()));
            writeBodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, writeStatus, writeResponse);
        });
        server.start();
    }

    public String getUrl() {
        return "http://" + server.getAddress().getHostString() + ":" + server.getAddress().getPort();
    }

    public void setWriteResponse(int status, String body) {
        writeStatus = status;
        writeResponse = body;
    }

    public List<String> getWriteBodies() {
        return writeBodies;
    }

    public List<String> getWriteQueries() {
        return writeQueries;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            server.stop(0);
        }
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        if (bytes.length == 0) {
            exchange.sendResponseHeaders(status, -1);
        } else {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        exchange.close();
    }
}
