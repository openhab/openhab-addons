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
package org.openhab.binding.nest.internal.wwn.test;

import static org.openhab.binding.nest.internal.wwn.WWNBindingConstants.*;
import static org.openhab.binding.nest.internal.wwn.rest.WWNStreamingRestClient.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link WWNTestApiServlet} mocks the Nest API during tests.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class WWNTestApiServlet extends HttpServlet {

    private static final long serialVersionUID = -5414910055159062745L;

    private static final String NEW_LINE = "\n";

    private static final String UPDATE_PATHS[] = { NEST_CAMERA_UPDATE_PATH, NEST_SMOKE_ALARM_UPDATE_PATH,
            NEST_STRUCTURE_UPDATE_PATH, NEST_THERMOSTAT_UPDATE_PATH };

    private final Logger logger = LoggerFactory.getLogger(WWNTestApiServlet.class);

    private static class SseEvent {
        private String name;
        private @Nullable String data;

        public SseEvent(String name) {
            this.name = name;
        }

        public SseEvent(String name, String data) {
            this.name = name;
            this.data = data;
        }

        public @Nullable String getData() {
            return data;
        }

        public String getName() {
            return name;
        }
    }

    private final Map<String, Map<String, String>> nestIdPropertiesMap = new ConcurrentHashMap<>();

    private final Map<Thread, Queue<SseEvent>> listenerQueues = new ConcurrentHashMap<>();

    private final ThreadLocal<PrintWriter> threadLocalWriter = new ThreadLocal<>();

    private final Gson gson = new GsonBuilder().create();

    public void closeConnections() {
        Set<Thread> threads = listenerQueues.keySet();
        listenerQueues.clear();
        threads.forEach(Thread::interrupt);
    }

    public void reset() {
        nestIdPropertiesMap.clear();
    }

    public void queueEvent(String eventName) {
        SseEvent event = new SseEvent(eventName);
        listenerQueues.forEach((thread, queue) -> queue.add(event));
    }

    public void queueEvent(String eventName, String data) {
        SseEvent event = new SseEvent(eventName, data);
        listenerQueues.forEach((thread, queue) -> queue.add(event));
    }

    @SuppressWarnings("resource")
    private void writeEvent(SseEvent event) {
        logger.debug("Writing {} event", event.getName());

        PrintWriter writer = threadLocalWriter.get();

        writer.write("event: ");
        writer.write(event.getName());
        writer.write(NEW_LINE);

        String eventData = event.getData();
        if (eventData != null) {
            for (String dataLine : eventData.split(NEW_LINE)) {
                writer.write("data: ");
                writer.write(dataLine);
                writer.write(NEW_LINE);
            }
        }

        writer.write(NEW_LINE);
        writer.flush();
    }

    private void writeEvent(String eventName) {
        writeEvent(new SseEvent(eventName));
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ArrayBlockingQueue<SseEvent> queue = new ArrayBlockingQueue<>(10);
        listenerQueues.put(Thread.currentThread(), queue);

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.flushBuffer();

        logger.debug("Opened event stream to {}:{}", request.getRemoteHost(), request.getRemotePort());

        PrintWriter writer = response.getWriter();
        threadLocalWriter.set(writer);
        writeEvent(OPEN);

        while (listenerQueues.containsKey(Thread.currentThread()) && !writer.checkError()) {
            try {
                SseEvent event = queue.poll(KEEP_ALIVE_MILLIS, TimeUnit.MILLISECONDS);
                if (event != null) {
                    writeEvent(event);
                } else {
                    writeEvent(KEEP_ALIVE);
                }
            } catch (InterruptedException e) {
                logger.debug("Evaluating loop conditions after interrupt");
            }
        }

        listenerQueues.remove(Thread.currentThread());
        threadLocalWriter.remove();
        writer.close();

        logger.debug("Closed event stream to {}:{}", request.getRemoteHost(), request.getRemotePort());
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.debug("Received put request: {}", request);

        String uri = request.getRequestURI();
        String nestId = getNestIdFromURI(uri);

        if (nestId == null) {
            logger.error("Unsupported URI: {}", uri);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        InputStreamReader reader = new InputStreamReader(request.getInputStream());
        Map<String, String> propertiesUpdate = gson.fromJson(reader, new TypeToken<Map<String, String>>() {
        }.getType());

        Map<String, String> properties = getOrCreateProperties(nestId);
        properties.putAll(propertiesUpdate);

        gson.toJson(propertiesUpdate, response.getWriter());

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private @Nullable String getNestIdFromURI(@Nullable String uri) {
        if (uri == null) {
            return null;
        }
        for (String updatePath : UPDATE_PATHS) {
            if (uri.startsWith(updatePath)) {
                return uri.replaceAll(updatePath, "");
            }
        }
        return null;
    }

    private Map<String, String> getOrCreateProperties(String nestId) {
        Map<String, String> properties = nestIdPropertiesMap.get(nestId);
        if (properties == null) {
            properties = new HashMap<>();
            nestIdPropertiesMap.put(nestId, properties);
        }
        return properties;
    }

    public @Nullable String getNestIdPropertyState(String nestId, String propertyName) {
        Map<String, String> properties = nestIdPropertiesMap.get(nestId);
        return properties == null ? null : properties.get(propertyName);
    }
}
