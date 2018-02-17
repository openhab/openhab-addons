/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * CommandExecutor.
 *
 * @author Radovan Sninsky
 * @since 2018-02-16 16:50
 */
public class CommandExecutor {

    private static CommandExecutor instance;

    private final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private final PlcComSClient plcClient;
    private final BlockingQueue<CommandEntry> writeQueue = new ArrayBlockingQueue<>(100);
    private final Thread thread;

    private CommandExecutor(PlcComSClient plcClient) {
        this.plcClient = plcClient;

        thread = new Thread(() -> {
            logger.debug("Starting polling for commands (write requests to Plc) ...");
            try {
                while (plcClient.isOpen()) {
                    CommandEntry ce = writeQueue.poll(1, TimeUnit.SECONDS);

                    if (ce != null && ce.value != null) {
                        logger.trace("Setting Plc variable {} to {}", ce.commandVariable, ce.toString());
                        if (ce.value instanceof Boolean) {
                            plcClient.set(ce.commandVariable, (Boolean)ce.value);
                        } else if (ce.value instanceof Integer) {
                            plcClient.set(ce.commandVariable, (Integer) ce.value);
                        } else if (ce.value instanceof BigDecimal) {
                            plcClient.set(ce.commandVariable, ((BigDecimal) ce.value).doubleValue());
                        } else {
                            plcClient.set(ce.commandVariable, ce.value.toString());
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Sending data to Plc failed w error: {}", e.getMessage());
            } catch (InterruptedException e) {
                logger.warn("Polling for command interuped");
            }
            logger.debug("Polling for commands finished");
        });
    }

    public static CommandExecutor init(PlcComSClient client) throws IOException {
        instance = new CommandExecutor(client);
        instance.logger.debug("Initializing command executor ...");
        instance.plcClient.open();
        instance.thread.start();

        return instance;
    }

    public static CommandExecutor get() {
        return instance;
    }

    public void dispose() {
        logger.debug("Canceling command executor ...");
        if (plcClient != null) {
            plcClient.close();
        }
    }

    public void execCommand(String commandVariable, Object value) {
        if (!writeQueue.offer(new CommandEntry(commandVariable, value))) {
            logger.warn("Command queue for sending to Plc is full");
        }
    }

    class CommandEntry {
        String commandVariable;
        Object value;

        CommandEntry(String commandVariable, Object value) {
            this.commandVariable = commandVariable;
            this.value = value;
        }
    }
}
