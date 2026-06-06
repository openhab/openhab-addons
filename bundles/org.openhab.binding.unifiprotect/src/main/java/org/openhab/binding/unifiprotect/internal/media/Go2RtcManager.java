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
package org.openhab.binding.unifiprotect.internal.media;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Go2RtcManager.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class Go2RtcManager {
    private final Logger logger = LoggerFactory.getLogger(Go2RtcManager.class);
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "OH-binding-unifiprotect-go2rtc-supervisor");
        t.setDaemon(true);
        return t;
    });
    private final Path binDir;
    private final Path configDir;
    private final Path configFile;
    private final Supplier<Path> go2rtcPathSupplier;
    private final Supplier<Path> ffmpegPathSupplier;
    private final String listenHost;
    private final int listenPort;
    private final Duration healthTimeout = Duration.ofSeconds(2);
    private final Duration startupGrace = Duration.ofSeconds(5);
    private int consecutiveUnhealthyChecks = 0;
    private final int maxConsecutiveUnhealthyChecks = 3;
    private boolean stopping = false;
    @Nullable
    private ScheduledFuture<?> tickFuture;
    @Nullable
    private Process process;
    @Nullable
    private Long lastStartNanos;

    public Go2RtcManager(Path binDir, Path configDir, Supplier<Path> go2rtcPathSupplier,
            Supplier<Path> ffmpegPathSupplier, String listenHost, int listenPort, String configFileName) {
        this.binDir = binDir;
        this.configDir = configDir;
        this.go2rtcPathSupplier = go2rtcPathSupplier;
        this.ffmpegPathSupplier = ffmpegPathSupplier;
        this.listenHost = listenHost;
        this.listenPort = listenPort;
        this.configFile = configDir.resolve(configFileName);
    }

    private static long hashConfigContent(String content) {
        CRC32 crc = new CRC32();
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        crc.update(bytes, 0, bytes.length);
        return crc.getValue();
    }

    public synchronized void applyConfig(String yamlContent) throws IOException {
        logger.debug("Applying config: {}", configFile);
        Files.createDirectories(configDir);
        Files.createDirectories(binDir);
        long newHash = hashConfigContent(yamlContent);
        long existingHash = -1L;
        boolean hasExisting = Files.exists(configFile);
        if (hasExisting) {
            try {
                String existingContent = Files.readString(configFile, StandardCharsets.UTF_8);
                existingHash = hashConfigContent(existingContent);
            } catch (IOException e) {
                logger.debug("Failed reading existing config for hash compare, will overwrite", e);
            }
        }

        if (hasExisting && existingHash == newHash) {
            logger.debug("Config unchanged (CRC32={}), skipping restart", Long.toHexString(newHash));
            return;
        }

        Files.writeString(configFile, yamlContent, StandardCharsets.UTF_8);
        logger.debug("Config changed (old CRC32={}, new CRC32={}), wrote config and restarting",
                existingHash == -1L ? "n/a" : Long.toHexString(existingHash), Long.toHexString(newHash));
        restart();
    }

    public synchronized void startIfNeeded() throws IOException {
        Process p1 = this.process;
        if (p1 != null && p1.isAlive()) {
            return;
        }
        logger.debug("Starting go2rtc with config: {}", configFile);
        Path workDir = binDir.getParent();
        Path bin = go2rtcPathSupplier.get(); // ensures download if needed
        List<String> cmd = new ArrayList<>();
        cmd.add(bin.toString());
        cmd.add("-config");
        cmd.add(configFile.toString());
        cmd.add(listenHost + ":" + listenPort);
        ProcessBuilder pb = new ProcessBuilder(cmd).directory(workDir.toFile()).redirectErrorStream(true);
        // Ensure ffmpeg is on PATH so go2rtc can invoke it directly
        Path ffmpeg = ffmpegPathSupplier.get();
        if (ffmpeg.getParent() != null) {
            String ffmpegDir = ffmpeg.getParent().toString();
            Map<String, String> env = pb.environment();
            String pathKey = env.containsKey("Path") ? "Path" : (env.containsKey("PATH") ? "PATH" : "PATH");
            String currentPath = env.getOrDefault(pathKey, "");
            if (!currentPath.contains(ffmpegDir)) {
                String updatedPath = ffmpegDir + File.pathSeparator + currentPath;
                env.put(pathKey, updatedPath);
                logger.debug("Prepended FFmpeg dir to {}: {}", pathKey, ffmpegDir);
            }
        }
        logger.debug("Starting go2rtc with in dir {} cmd: {} and args: {}", workDir.toFile(), bin,
                cmd.stream().collect(Collectors.joining(" ")));
        Process process = pb.start();
        this.process = process;
        this.lastStartNanos = System.nanoTime();
        this.consecutiveUnhealthyChecks = 0;
        // Ensure we don't lose track of exit and clear reference deterministically
        process.onExit().thenAccept(p -> {
            synchronized (Go2RtcManager.this) {
                Process p2 = this.process;
                if (p.equals(p2)) {
                    logger.debug("go2rtc process exited with code {}", p.exitValue());
                    this.process = null;
                }
            }
        });
        attachLogger(process);
        ScheduledFuture<?> tickFuture = this.tickFuture;
        if (tickFuture != null) {
            tickFuture.cancel(true);
        }
        this.tickFuture = exec.scheduleWithFixedDelay(this::tick, 1, 5, TimeUnit.SECONDS);
        logger.debug("Scheduled tick for go2rtc with config: {}", configFile);
    }

    private void attachLogger(Process process) {
        Thread t = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("[{}] {}", "go2rtc", line);
                    }
                }
            } catch (IOException ignored) {
            }
        }, "go2rtc-logger");
        t.setDaemon(true);
        t.start();
    }

    public synchronized void destroy() {
        stopping = true;
        Process process = this.process;
        if (process != null && process.isAlive()) {
            process.destroy();
            try {
                if (!process.waitFor(3, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException ignored) {
            }
        }
        exec.shutdownNow();
    }

    public void restart() throws IOException {
        stop();
        startIfNeeded();
    }

    public synchronized void stop() {
        logger.debug("Stopping go2rtc");
        ScheduledFuture<?> tickFuture = this.tickFuture;
        if (tickFuture != null) {
            tickFuture.cancel(true);
        }
        this.lastStartNanos = null;
        this.consecutiveUnhealthyChecks = 0;
        Process process = this.process;
        if (process != null) {
            try {
                if (process.isAlive()) {
                    process.destroy();
                    if (!process.waitFor(2, TimeUnit.SECONDS)) {
                        process.destroyForcibly();
                    }
                }
            } catch (Exception ignored) {
            }
            this.process = null;
        }
    }

    private void tick() {
        synchronized (this) {
            if (stopping) {
                return;
            }
            Process process = this.process;
            if (process == null || !process.isAlive()) {
                try {
                    startIfNeeded();
                } catch (IOException e) {
                    logger.debug("Failed to start go2rtc with config: {}", configFile, e);
                }
                return;
            }
            // Allow a short grace period after starting before enforcing health checks
            Long startedAt = this.lastStartNanos;
            if (startedAt != null) {
                Duration sinceStart = Duration.ofNanos(System.nanoTime() - startedAt);
                if (sinceStart.compareTo(startupGrace) < 0) {
                    return;
                }
            }
            if (!isHealthy()) {
                consecutiveUnhealthyChecks++;
                if (consecutiveUnhealthyChecks >= maxConsecutiveUnhealthyChecks) {
                    try {
                        process.destroy();
                        if (!process.waitFor(2, TimeUnit.SECONDS)) {
                            process.destroyForcibly();
                        }
                        startIfNeeded();
                    } catch (Exception ignored) {
                    } finally {
                        consecutiveUnhealthyChecks = 0;
                        this.lastStartNanos = System.nanoTime();
                    }
                }
            } else {
                consecutiveUnhealthyChecks = 0;
            }
        }
    }

    public boolean isHealthy() {
        try {
            HttpClient http = HttpClient.newBuilder().connectTimeout(healthTimeout).build();
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://" + listenHost + ":" + listenPort + "/"))
                    .timeout(healthTimeout).GET().build();
            HttpResponse<Void> res = http.send(req, HttpResponse.BodyHandlers.discarding());
            return res.statusCode() >= 200 && res.statusCode() < 500; // UI or 404 ok
        } catch (Exception e) {
            return false;
        }
    }

    public String getBaseUrl() {
        return "http://" + listenHost + ":" + listenPort;
    }

    public void deleteConfigFile() {
        try {
            Files.deleteIfExists(configFile);
        } catch (IOException ignored) {
        }
    }
}
