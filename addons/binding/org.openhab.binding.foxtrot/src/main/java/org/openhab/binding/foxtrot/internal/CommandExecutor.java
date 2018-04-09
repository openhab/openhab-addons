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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CommandExecutor.
 *
 * @author Radovan Sninsky
 * @since 2018-02-16 16:50
 */
public class CommandExecutor implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private final PlcComSClient plcClient;
    private final BlockingQueue<CommandEntry> writeQueue = new ArrayBlockingQueue<>(100);
    private final AtomicBoolean running = new AtomicBoolean(false);

    public CommandExecutor(PlcComSClient plcClient) {
        this.plcClient = plcClient;
    }

    public void start() {
        new Thread(this).start();
    }

    public void stop() {
        logger.debug("Canceling command executor ...");
        running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void run() {
        logger.debug("Starting polling for commands (write requests to Plc) ...");
        running.set(true);
        try {
            while (running.get()) {
                CommandEntry ce = writeQueue.poll(1, TimeUnit.SECONDS);

                try {
                    if (ce != null) {
                        logger.trace("Going to execute operation {} for variable {} w value {}", ce.op, ce.variable, ce.value);
                        if (CommandEntry.SET.equals(ce.op)) {
                            if (ce.value instanceof Boolean) {
                                plcClient.set(ce.variable, (Boolean) ce.value);
                            } else if (ce.value instanceof Integer) {
                                plcClient.set(ce.variable, (Integer) ce.value);
                            } else if (ce.value instanceof BigDecimal) {
                                plcClient.set(ce.variable, ((BigDecimal) ce.value).doubleValue());
                            } else {
                                plcClient.set(ce.variable, ce.value.toString());
                            }
                        } else if (CommandEntry.GET.equals(ce.op)) {
                            plcClient.doGet(ce.variable);
                        }
                    }
                } catch (ConnectException e) {
                    logger.error("Connection to PlcComS server is closed!");
                    break;
                } catch (IOException e) {
                    logger.error("Setting new value for '{}' failed w error: {}", ce.variable, e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Polling for command interuped");
        }
        running.set(false);
        logger.debug("Polling for commands finished");
    }

    public void execSet(String commandVariable, Object value) {
        if (!writeQueue.offer(new CommandEntry(CommandEntry.SET, commandVariable, value))) {
            logger.warn("Command queue for sending to Plc is full");
        }
    }

    public void execGet(String commandVariable) {
        if (!writeQueue.offer(new CommandEntry(CommandEntry.GET, commandVariable))) {
            logger.warn("Command queue for sending to Plc is full");
        }
    }

    class CommandEntry {
        static final String GET = "get";
        static final String SET = "set";

        String op;
        String variable;
        Object value;

        CommandEntry(String op, String commandVariable) {
            this(op, commandVariable, null);
        }

        CommandEntry(String op, String commandVariable, Object value) {
            this.op = op;
            this.variable = commandVariable;
            this.value = value;
        }
    }
}
