/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.handler;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.types.JellyfinExceptionHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ServerHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Miguel Álvarez - Initial contribution
 * @author Patrik Gfeller - Adjustments to work independently of the Android SDK
 *         and respective runtime
 * 
 */
@NonNullByDefault
public class ServerHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private final ExceptionHandler exceptionHandler;

    public static class TASKS {
        public static final String CONNECT = "Connect";
        public static final String REGISTER = "Registration";
        public static final String POLL = "Update";

        public static Map<String, Integer> delays = Map.ofEntries(Map.entry(TASKS.CONNECT, 0),
                Map.entry(TASKS.REGISTER, 5), Map.entry(TASKS.POLL, 10));
        public static Map<String, Integer> intervals = Map.ofEntries(Map.entry(TASKS.CONNECT, 10),
                Map.entry(TASKS.REGISTER, 1), Map.entry(TASKS.POLL, 10));
    }

    /**
     * Exception handler implementation
     * 
     * @author Patrik Gfeller - Initial contribution
     */
    private class ExceptionHandler implements JellyfinExceptionHandler {
        private final ServerHandler handler;

        private ExceptionHandler(ServerHandler handler) {
            this.handler = handler;
        }

        @Override
        public void handle(Exception exception) {
        }
    }

    public ServerHandler(Bridge bridge) {
        super(bridge);

        this.exceptionHandler = new ExceptionHandler(this);
    }

    @Override
    public void initialize() {
        try {
            scheduler.execute(initializeHandler());
        } catch (Exception e) {
            this.logger.warn("{}", e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private synchronized Runnable initializeHandler() {
        return () -> {
            this.stopTasks();
            this.startTasks();
        };
    }

    private synchronized void stopTasks() {
    }

    private synchronized void startTasks() {
    }

    private synchronized void stopTask(@Nullable ScheduledFuture<?> task) {
        if (task == null || task.isCancelled() || task.isDone()) {
            return;
        }

        task.cancel(true);
    }
}
