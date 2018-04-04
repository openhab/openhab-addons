/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal;

import org.openhab.binding.foxtrot.internal.plccoms.PlcComSClient;
import org.openhab.binding.foxtrot.internal.plccoms.PlcComSEception;
import org.openhab.binding.foxtrot.internal.plccoms.PlcComSReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ValuesReceiver.
 *
 * @author Radovan Sninsky
 * @since 2018-04-02 18:18
 */
public final class ValuesReceiver implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(ValuesReceiver.class);

    private PlcComSClient client;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Map<String, VarEntry> variables = new HashMap<>(1000);

    public ValuesReceiver(PlcComSClient client) {
        this.client = client;
    }

    public void start() {
        new Thread(this).start();
    }

    public void stop() {
        running.lazySet(false);
    }

    public boolean isRunning() {
        return running.get();
    }

    public void register(String var, Refreshable handler, BigDecimal delta) throws IOException {
        logger.debug("Registrating variable '{}' for listenig values (delta: {}) ...", var, delta);
        if (var != null) {
            String v = var.toLowerCase();
            variables.put(v, new VarEntry(v, handler, delta));
            client.enable(v, delta);
        }
    }

    public void unregister(String var) {
        try {
            if (var != null && variables.containsKey(var.toLowerCase())) {
                logger.debug("UnRegistrating variable '{}' for listenig values ...", var);
                client.disable(var.toLowerCase());
                variables.remove(var.toLowerCase());
            }
        } catch (IOException e) {
            logger.warn("Disabling diffs for '{}' failed, error: {}", var, e.getMessage());
        }
    }

    @Override
    public void run() {
        running.set(true);

        while (running.get()) {
            try {
                PlcComSReply reply = client.receive(1000);

                if (reply != null && reply.getName() != null) {
                    VarEntry ve = variables.get(reply.getName().toLowerCase());
                    if (ve != null) {
                        logger.trace("Refreshing variable {} w handler {}", reply.getName(), ve.handler);
                        ve.handler.refresh(reply);
                    }
                }
            } catch (ConnectException e) {
                logger.warn("Connection to PlcComS server is closed! New values are no more received.");
                running.set(false);
            } catch (PlcComSEception e) {
                logger.error("PlcComS returns error: {} {}: {}", e.getType(), e.getCode(), e.getMessage());
            } catch (IOException e) {
                logger.error("Communication with PlcComS failed", e);
            }
        }
        running.set(false);
        logger.debug("Listening value changes from PlcComS finished");
    }

    private class VarEntry {
        String name;
        Refreshable handler;
        BigDecimal delta;

        VarEntry(String name, Refreshable handler, BigDecimal delta) {
            this.name = name;
            this.handler = handler;
            this.delta = delta;
        }
    }
}
