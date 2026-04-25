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
package org.openhab.binding.dahuadoor.internal.media;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Go2RtcManager} manages the lifecycle of a go2rtc sidecar process for a single
 * Dahua door station. It writes a per-thing YAML configuration, starts the go2rtc binary, and
 * terminates it on disposal.
 *
 * <p>
 * go2rtc v1.9.x is required. The binary must be provided by the user via the {@code go2rtcPath}
 * configuration parameter.
 * </p>
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class Go2RtcManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(Go2RtcManager.class);

    /** Health-check timeout in milliseconds. */
    private static final int HEALTH_CHECK_TIMEOUT_MS = 2000;
    /** Maximum number of health-check polls after process start. */
    private static final int HEALTH_CHECK_MAX_POLLS = 10;
    /** Delay between health-check polls in milliseconds. */
    private static final long HEALTH_CHECK_POLL_DELAY_MS = 500;
    /** Maximum wait for go2rtc to exit after SIGTERM. */
    private static final long STOP_TIMEOUT_SECONDS = 5;

    private final String go2rtcBinary;
    private final int apiPort;
    private final int webRtcPort;
    private final String stunServer;
    private final String hostname;
    private final String username;
    private final String password;
    private final int rtspChannel;
    private final int rtspSubtype;
    private final int backchannelRtpPort;
    private final String streamName;

    private @Nullable Process process;
    private @Nullable File configFile;
    private @Nullable Thread logThread;

    /**
     * Creates a new Go2RtcManager.
     *
     * @param go2rtcBinary absolute path to the go2rtc binary
     * @param apiPort HTTP API port for go2rtc (typically 1984)
     * @param webRtcPort WebRTC data port for go2rtc (typically 8555)
     * @param stunServer STUN server in host:port format (e.g. stun.l.google.com:19302)
     * @param streamName go2rtc stream name for this device (e.g. dahua_vto2202_living)
     * @param hostname device hostname or IP
     * @param username device username
     * @param password device password
     * @param rtspChannel RTSP channel index (typically 1)
     * @param rtspSubtype RTSP sub-type index (0 = main, 1 = sub)
     */
    public Go2RtcManager(String go2rtcBinary, int apiPort, int webRtcPort, String stunServer, String streamName,
            String hostname, String username, String password, int rtspChannel, int rtspSubtype) {
        this.go2rtcBinary = go2rtcBinary;
        this.apiPort = apiPort;
        this.webRtcPort = webRtcPort;
        this.stunServer = stunServer;
        this.streamName = streamName;
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.rtspChannel = rtspChannel;
        this.rtspSubtype = rtspSubtype;
        this.backchannelRtpPort = apiPort + 20000;
    }

    /**
     * Returns the go2rtc stream name for this device.
     *
     * @return stream name string
     */
    public String getStreamName() {
        return streamName;
    }

    /**
     * Returns the go2rtc HTTP API port.
     *
     * @return API port
     */
    public int getApiPort() {
        return apiPort;
    }

    /**
     * Starts the go2rtc sidecar process.
     * Writes a YAML config file, spawns the process, and waits until the HTTP API is healthy.
     *
     * @throws IOException if the binary does not exist, is not executable, config cannot be written,
     *             or the process fails to start
     * @throws InterruptedException if the thread is interrupted while waiting for startup
     */
    public void start() throws IOException, InterruptedException {
        File binary = new File(go2rtcBinary);
        if (!binary.exists() || !binary.isFile()) {
            throw new IOException(
                    "go2rtc binary not found: " + go2rtcBinary + " — set go2rtcPath in thing configuration");
        }
        if (!binary.canExecute()) {
            try {
                Files.setPosixFilePermissions(binary.toPath(), PosixFilePermissions.fromString("rwxr-xr-x"));
            } catch (UnsupportedOperationException | IOException e) {
                throw new IOException("go2rtc binary is not executable and could not chmod: " + go2rtcBinary, e);
            }
        }

        File localConfigFile = writeConfig();
        configFile = localConfigFile;

        ProcessBuilder pb = new ProcessBuilder(go2rtcBinary, "-config", localConfigFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process startedProcess = pb.start();
        process = startedProcess;
        LOGGER.info("go2rtc started (stream={}, apiPort={}, PID={})", streamName, apiPort, startedProcess.pid());

        // Pipe go2rtc stdout+stderr into the openHAB log at DEBUG level so problems are visible.
        Thread logTh = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(startedProcess.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LOGGER.debug("go2rtc [{}]: {}", streamName, line);
                }
            } catch (IOException e) {
                LOGGER.trace("go2rtc [{}] output reader closed: {}", streamName, e.getMessage());
            }
        }, "go2rtc-log-" + streamName);
        logTh.setDaemon(true);
        logTh.start();
        logThread = logTh;

        waitForHealthy();
    }

    /**
     * Stops the go2rtc process and removes the temporary config file.
     */
    public void stop() {
        Process localProcess = process;
        if (localProcess != null) {
            LOGGER.info("Stopping go2rtc (stream={}, PID={})", streamName, localProcess.pid());
            localProcess.destroy();
            try {
                if (!localProcess.waitFor(STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    LOGGER.warn("go2rtc did not terminate in {}s, forcing shutdown (stream={})", STOP_TIMEOUT_SECONDS,
                            streamName);
                    localProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            process = null;
        }
        Thread localLogThread = logThread;
        if (localLogThread != null) {
            localLogThread.interrupt();
            logThread = null;
        }
        File localConfig = configFile;
        if (localConfig != null && localConfig.exists()) {
            if (!localConfig.delete()) {
                LOGGER.debug("Could not delete go2rtc config file {}", localConfig.getAbsolutePath());
            }
            configFile = null;
        }
    }

    /**
     * Returns {@code true} if the go2rtc process is currently running.
     *
     * @return process alive status
     */
    public boolean isRunning() {
        Process localProcess = process;
        return localProcess != null && localProcess.isAlive();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Builds the YAML configuration string for go2rtc, using the device RTSP URL constructed
     * from this instance's parameters (explicit port 554, no {@code proto=Onvif}).
     * <p>
     * An explicit ICE host candidate for the local IPv4 address is auto-detected by opening a
     * temporary socket towards the Dahua device. This ensures that browsers on the same LAN
     * receive a reachable candidate even when go2rtc does not enumerate the IPv4 interface
     * as a host candidate on its own.
     */
    private String buildYaml() {
        String userInfo = URLEncoder.encode(username, StandardCharsets.UTF_8) + ":"
                + URLEncoder.encode(password, StandardCharsets.UTF_8);
        String rtspUrl = "rtsp://" + userInfo + "@" + hostname + ":554/cam/realmonitor?channel=" + rtspChannel
                + "&subtype=" + rtspSubtype;
        String backchannelExec = "exec:ffmpeg -use_wallclock_as_timestamps 1 -re -fflags nobuffer -f alaw -ar 8000 "
                + "-ac 1 -i - -vn -c:a pcm_s16le -ar 16000 -ac 1 -payload_type 97 -f rtp " + "rtp://127.0.0.1:"
                + backchannelRtpPort + "#backchannel=1#audio=alaw/8000";

        String safeRtspUrl = rtspUrl.replace("'", "''");
        String safeBackchannelExec = backchannelExec.replace("'", "''");

        StringBuilder candidates = new StringBuilder();
        String localIp = detectLocalIp();
        if (localIp != null) {
            candidates.append("    - ").append(localIp).append(":").append(webRtcPort).append("\n");
            LOGGER.debug("go2rtc config: adding local IPv4 ICE candidate {}:{}", localIp, webRtcPort);
        }
        candidates.append("    - stun:").append(stunServer).append("\n");

        return "log:\n" + "  level: debug\n" + "streams:\n" + "  " + streamName + ":\n" + "    - '" + safeRtspUrl
                + "'\n" + "    - '" + safeBackchannelExec + "'\n" + "api:\n" + "  origin: \"*\"\n"
                + "  listen: \"127.0.0.1:" + apiPort + "\"\n" + "webrtc:\n" + "  listen: \":" + webRtcPort + "\"\n"
                + "  candidates:\n" + candidates;
    }

    /**
     * Detects the local IPv4 address used to reach the Dahua device by opening a temporary
     * TCP socket towards {@code hostname:554}.
     *
     * @return local IPv4 address string (e.g. {@code "172.18.0.2"}), or {@code null} if detection fails
     */
    private @Nullable String detectLocalIp() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostname, 554), 2000);
            String addr = socket.getLocalAddress().getHostAddress();
            // Ignore loopback and IPv6 addresses — only use routable IPv4
            if (!addr.startsWith("127.") && !addr.contains(":")) {
                return addr;
            }
        } catch (IOException e) {
            LOGGER.debug("Could not detect local IP by connecting to {}:554 — skipping explicit ICE candidate: {}",
                    hostname, e.getMessage());
        }
        return null;
    }

    /**
     * Writes the YAML config to a temporary file and returns that file.
     */
    private File writeConfig() throws IOException {
        File cfg = File.createTempFile("go2rtc_" + streamName + "_", ".yaml");
        cfg.deleteOnExit();
        try (FileWriter fw = new FileWriter(cfg, StandardCharsets.UTF_8)) {
            fw.write(buildYaml());
        }
        LOGGER.debug("go2rtc config written to {}", cfg.getAbsolutePath());
        return cfg;
    }

    /**
     * Polls the go2rtc {@code /api/config} endpoint until it returns HTTP 200 or the maximum
     * number of polls is reached.
     *
     * @throws InterruptedException if interrupted while polling
     * @throws IOException if go2rtc does not become healthy in time
     */
    private void waitForHealthy() throws InterruptedException, IOException {
        String healthUrl = "http://127.0.0.1:" + apiPort + "/api/config";
        for (int i = 0; i < HEALTH_CHECK_MAX_POLLS; i++) {
            if (!isRunning()) {
                throw new IOException("go2rtc process exited unexpectedly during startup");
            }
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(healthUrl).toURL().openConnection();
                conn.setConnectTimeout(HEALTH_CHECK_TIMEOUT_MS);
                conn.setReadTimeout(HEALTH_CHECK_TIMEOUT_MS);
                conn.setRequestMethod("GET");
                int status = conn.getResponseCode();
                conn.disconnect();
                if (status == HttpURLConnection.HTTP_OK) {
                    LOGGER.debug("go2rtc API healthy after {} poll(s)", i + 1);
                    return;
                }
            } catch (IOException e) {
                LOGGER.trace("go2rtc health check attempt {}/{} failed: {}", i + 1, HEALTH_CHECK_MAX_POLLS,
                        e.getMessage());
            }
            Thread.sleep(HEALTH_CHECK_POLL_DELAY_MS);
        }
        throw new IOException(
                "go2rtc did not become healthy after " + HEALTH_CHECK_MAX_POLLS + " polls on port " + apiPort);
    }
}
