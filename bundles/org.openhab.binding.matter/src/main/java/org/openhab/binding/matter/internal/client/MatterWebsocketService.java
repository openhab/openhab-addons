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
package org.openhab.binding.matter.internal.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service for managing the Matter Node.js process
 * 
 * @author Dan Cunningham - Initial contribution
 */
@Component(service = MatterWebsocketService.class, scope = ServiceScope.SINGLETON)
@NonNullByDefault
public class MatterWebsocketService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Pattern LOG_PATTERN = Pattern
            .compile("^\\S+\\s+\\S+\\s+(TRACE|DEBUG|INFO|WARN|ERROR)\\s+(\\S+)\\s+(.*)$");
    private static final String MATTER_JS_PATH = "/matter-server/matter.js";
    // Delay before restarting the node process after it exits as well as notifying listeners when it's ready
    private static final int STARTUP_DELAY_SECONDS = 5;
    // Timeout for shutting down the node process
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 3;
    // Delay before retrying to start the node process if it fails
    private static final int STARTUP_RETRY_DELAY_SECONDS = 30;
    private final List<NodeProcessListener> processListeners = new ArrayList<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool("matter.MatterWebsocketService");
    private final NodeJSRuntimeManager nodeManager;
    private @Nullable ScheduledFuture<?> notifyFuture;
    private @Nullable ScheduledFuture<?> restartFuture;
    // The Node.js process running the matter.js script
    private @Nullable Process nodeProcess;
    // The state of the service, STARTING, READY, SHUTTING_DOWN
    private ServiceState state = ServiceState.STARTING;
    // the port the node process is listening on
    private int port;
    private AtomicBoolean restarting = new AtomicBoolean(false);

    @Activate
    public MatterWebsocketService(final @Reference HttpClientFactory httpClientFactory) throws IOException {
        nodeManager = new NodeJSRuntimeManager(httpClientFactory.getCommonHttpClient());
        scheduledStart(0);
    }

    @Deactivate
    public void deactivate() {
        stopNode();
        executorService.shutdown();
    }

    public void restart() {
        stopNode();
        scheduledStart(STARTUP_DELAY_SECONDS);
    }

    public void addProcessListener(NodeProcessListener listener) {
        processListeners.add(listener);
        if (state == ServiceState.READY) {
            listener.onNodeReady(port);
        }
    }

    public void removeProcessListener(NodeProcessListener listener) {
        processListeners.remove(listener);
    }

    public void stopNode() {
        logger.debug("stopNode");
        state = ServiceState.SHUTTING_DOWN;
        cancelFutures();
        Process nodeProcess = this.nodeProcess;
        if (nodeProcess != null && nodeProcess.isAlive()) {
            nodeProcess.destroy();
            try {
                // Wait for the process to terminate
                if (!nodeProcess.waitFor(SHUTDOWN_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)) {
                    nodeProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Interrupted while waiting for Node process to stop", e);
            }
        }
    }

    public int getPort() {
        return port;
    }

    public boolean isReady() {
        return state == ServiceState.READY;
    }

    private void cancelFutures() {
        ScheduledFuture<?> notifyFuture = this.notifyFuture;
        if (notifyFuture != null) {
            notifyFuture.cancel(true);
        }
        ScheduledFuture<?> restartFuture = this.restartFuture;
        if (restartFuture != null) {
            restartFuture.cancel(true);
        }
    }

    private synchronized void scheduledStart(int delay) {
        if (!restarting.compareAndSet(false, true)) {
            logger.debug("Restart already scheduled, skipping");
            return;
        }
        logger.debug("Scheduling restart in {} seconds", delay);
        restartFuture = scheduler.schedule(() -> {
            try {
                port = runNodeWithResource(MATTER_JS_PATH);
                restarting.set(false);
            } catch (IOException e) {
                restarting.set(false);
                if (state != ServiceState.SHUTTING_DOWN) {
                    logger.warn("Failed to start the Matter Node process, retrying in {} seconds",
                            STARTUP_RETRY_DELAY_SECONDS);
                    scheduledStart(STARTUP_RETRY_DELAY_SECONDS);
                }
            }
        }, delay, TimeUnit.SECONDS);
    }

    private int runNodeWithResource(String resourcePath, String... additionalArgs) throws IOException {
        state = ServiceState.STARTING;
        String nodePath = nodeManager.getNodePath();
        Path scriptPath = extractResourceToTempFile(resourcePath);

        port = findAvailablePort();
        List<String> command = new ArrayList<>();
        command.add(nodePath);
        command.add(scriptPath.toString());
        command.add("--host");
        command.add("localhost");
        command.add("--port");
        command.add(String.valueOf(port));
        command.addAll(List.of(additionalArgs));

        ProcessBuilder pb = new ProcessBuilder(command);
        nodeProcess = pb.start();

        // Start output and error stream readers
        executorService.submit(this::readOutputStream);
        executorService.submit(this::readErrorStream);

        // Wait for the process to exit in a separate thread
        executorService.submit(() -> {
            int exitCode = -1;
            try {
                Process nodeProcess = this.nodeProcess;
                if (nodeProcess != null) {
                    exitCode = nodeProcess.waitFor();
                    logger.debug("Node process exited with code: {}", exitCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Interrupted while waiting for Node process to exit", e);
            } finally {
                try {
                    Files.deleteIfExists(scriptPath);
                    notifyExitListeners(exitCode);
                } catch (IOException e) {
                    logger.debug("Failed to delete temporary script file", e);
                }

                if (state != ServiceState.SHUTTING_DOWN) {
                    logger.debug("trying to restart, state: {}", state);
                    scheduledStart(STARTUP_DELAY_SECONDS);
                }
            }
        });
        return port;
    }

    private void readOutputStream() {
        Process nodeProcess = this.nodeProcess;
        if (nodeProcess != null) {
            processStream(nodeProcess.getInputStream(), "Error reading Node process output", true);
        }
    }

    private void readErrorStream() {
        Process nodeProcess = this.nodeProcess;
        if (nodeProcess != null) {
            processStream(nodeProcess.getErrorStream(), "Error reading Node process error stream", false);
        }
    }

    private void processStream(InputStream inputStream, String errorMessage, boolean triggerNotify) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // we only want to do this once!
                if (state == ServiceState.STARTING && triggerNotify && notifyFuture == null) {
                    notifyFuture = scheduler.schedule(() -> {
                        state = ServiceState.READY;
                        this.notifyFuture = null;
                        notifyReadyListeners();
                    }, STARTUP_DELAY_SECONDS, TimeUnit.SECONDS);
                }
                if (logger.isTraceEnabled()) {
                    Matcher matcher = LOG_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String component = matcher.group(2);
                        String message = matcher.group(3);
                        logger.trace("{}: {}", component, message);
                    } else {
                        logger.trace(line);
                    }
                }
            }
        } catch (IOException e) {
            if (!state.equals(ServiceState.SHUTTING_DOWN)) {
                logger.debug("{}", errorMessage, e);
            }
        }
    }

    private void notifyExitListeners(int exitCode) {
        for (NodeProcessListener listener : processListeners) {
            listener.onNodeExit(exitCode);
        }
    }

    private void notifyReadyListeners() {
        for (NodeProcessListener listener : processListeners) {
            listener.onNodeReady(port);
        }
    }

    private Path extractResourceToTempFile(String resourcePath) throws IOException {
        Path tempFile = Files.createTempFile("node-script-", ".js");
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        tempFile.toFile().deleteOnExit(); // Ensure the temp file is deleted on JVM exit
        return tempFile;
    }

    private int findAvailablePort() throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
            return serverSocket.getLocalPort();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logger.debug("Failed to close ServerSocket", e);
                }
            }
        }
    }

    public interface NodeProcessListener {
        void onNodeExit(int exitCode);

        void onNodeReady(int port);
    }

    public enum ServiceState {
        /**
         * The service is up and ready.
         */
        READY,

        /**
         * The service is in the process of starting but not yet ready.
         */
        STARTING,

        /**
         * The service is in the process of shutting down, so it shouldn't be restarted.
         */
        SHUTTING_DOWN
    }
}
