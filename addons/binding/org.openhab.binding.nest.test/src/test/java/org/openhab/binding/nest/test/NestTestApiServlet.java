/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.test;

import static org.openhab.binding.nest.NestBindingConstants.*;
import static org.openhab.binding.nest.internal.rest.NestStreamingRestClient.*;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link NestTestApiServlet} mocks the Nest API during tests.
 *
 * @author Wouter Born - Increase test coverage
 */
public class NestTestApiServlet extends HttpServlet {

    private static final long serialVersionUID = -5414910055159062745L;

    private static final String NEW_LINE = "\n";

    private static final String UPDATE_PATHS[] = { NEST_CAMERA_UPDATE_PATH, NEST_SMOKE_ALARM_UPDATE_PATH,
            NEST_STRUCTURE_UPDATE_PATH, NEST_THERMOSTAT_UPDATE_PATH };

    private final Logger logger = LoggerFactory.getLogger(NestTestApiServlet.class);

    private class SseEvent {
        private String name;
        private String data;

        public SseEvent(String name) {
            this.name = name;
        }

        public SseEvent(String name, String data) {
            this.name = name;
            this.data = data;
        }

        public String getData() {
            return data;
        }

        public String getName() {
            return name;
        }

        public boolean hasData() {
            return data != null && !data.isEmpty();
        }
    }

    private final Map<String, Map<String, String>> nestIdPropertiesMap = new ConcurrentHashMap<>();

    private final Map<Thread, Queue<SseEvent>> listenerQueues = new ConcurrentHashMap<>();

    private final ThreadLocal<PrintWriter> threadLocalWriter = new ThreadLocal<>();

    private final Gson gson = new GsonBuilder().create();

    public void closeConnections() {
        Set<Thread> threads = listenerQueues.keySet();
        listenerQueues.clear();
        threads.forEach(thread -> thread.interrupt());
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

        if (event.hasData()) {
            for (String dataLine : event.getData().split(NEW_LINE)) {
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

    private String getNestIdFromURI(String uri) {
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

    public String getNestIdPropertyState(String nestId, String propertyName) {
        Map<String, String> properties = nestIdPropertiesMap.get(nestId);
        return properties == null ? null : properties.get(propertyName);
    }

}
