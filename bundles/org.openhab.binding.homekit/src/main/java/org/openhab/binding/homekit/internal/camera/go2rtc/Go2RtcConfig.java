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
package org.openhab.binding.homekit.internal.camera.go2rtc;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builds the go2rtc YAML configuration.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public final class Go2RtcConfig {

    /**
     * The address and ports that go2rtc listens on.
     */
    public record Address(String host, int apiPort, int webRtcPort, int rtspPort) {
    }

    private static final int MAX_PORT_ATTEMPTS = 10;

    private Go2RtcConfig() {
    }

    /**
     * Returns the go2rtc YAML configuration. The listen and talk streams are declared without sources; the
     * data pump publishes media to them over RTSP.
     *
     * @param address the host address and ports to listen on
     * @param listenStream the name of the stream the browser listens to
     * @param talkStream the name of the stream the browser talks to
     */
    public static String toYaml(Address address, String listenStream, String talkStream) {
        StringBuilder yaml = new StringBuilder();
        yaml.append("api:\n");
        yaml.append("  listen: \"").append(address.host()).append(":").append(address.apiPort()).append("\"\n");
        yaml.append("webrtc:\n");
        yaml.append("  listen: \"").append(address.host()).append(":").append(address.webRtcPort()).append("\"\n");
        yaml.append("rtsp:\n");
        yaml.append("  listen: \"127.0.0.1:").append(address.rtspPort()).append("\"\n");
        yaml.append("streams:\n");
        yaml.append("  ").append(listenStream).append(":\n");
        yaml.append("  ").append(talkStream).append(":\n");
        return yaml.toString();
    }

    /**
     * Returns a free TCP port selected by the OS.
     * There is a small window between closing the probe socket and go2rtc binding the port.
     */
    public static int freeTcpPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    /**
     * Returns a port that is free for both TCP and UDP, as go2rtc listens on both for WebRTC.
     * Retries when the UDP side of a TCP-free port is already in use.
     */
    public static int freeTcpUdpPort() throws IOException {
        for (int attempt = 0; attempt < MAX_PORT_ATTEMPTS; attempt++) {
            try (ServerSocket tcp = new ServerSocket(0)) {
                int port = tcp.getLocalPort();
                try (DatagramSocket udp = new DatagramSocket(port)) {
                    return port;
                } catch (SocketException e) {
                    // UDP port in use; try another
                }
            }
        }
        throw new IOException("No port free for both TCP and UDP after " + MAX_PORT_ATTEMPTS + " attempts");
    }
}
