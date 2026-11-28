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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builds the go2rtc YAML configuration.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public final class Go2RtcConfig {

    /**
     * go2rtc's management API has no authentication, so it listens only on loopback. WebRTC media uses
     * {@link Address#host()}, which must be reachable by the browser.
     */
    public static final String API_HOST = "127.0.0.1";

    /**
     * The LAN address used for WebRTC media, and the ports go2rtc listens on. The management API does not use
     * {@link #host()}; it listens on {@link #API_HOST}.
     */
    public record Address(String host, int apiPort, int webRtcPort, int rtspPort) {
    }

    private static final int MAX_PORT_ATTEMPTS = 50;

    private Go2RtcConfig() {
    }

    /**
     * Returns the go2rtc YAML configuration. The listen and talk streams are declared without sources; the
     * data pump publishes media to them over RTSP.
     *
     * @param address the WebRTC address and the ports to listen on. The API listens on {@link #API_HOST}
     * @param listenStream the name of the stream the browser listens to
     * @param talkStream the name of the stream the browser talks to
     */
    public static String toYaml(Address address, String listenStream, String talkStream) {
        StringBuilder yaml = new StringBuilder();
        yaml.append("api:\n");
        yaml.append("  listen: \"").append(API_HOST).append(":").append(address.apiPort()).append("\"\n");
        yaml.append("webrtc:\n");
        yaml.append("  listen: \"").append(address.host()).append(":").append(address.webRtcPort()).append("\"\n");
        yaml.append("rtsp:\n");
        yaml.append("  listen: \"").append(API_HOST).append(":").append(address.rtspPort()).append("\"\n");
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
     * <p>
     * UDP is bound first. On Windows, a TCP ephemeral port is often inside a UDP excluded range, so binding UDP to a
     * port chosen for TCP fails most of a short retry loop.
     */
    public static int freeTcpUdpPort() throws IOException {
        InetAddress wildcard = InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 });
        IOException lastFailure = null;
        for (int attempt = 0; attempt < MAX_PORT_ATTEMPTS; attempt++) {
            try (DatagramSocket udp = new DatagramSocket(0, wildcard)) {
                int port = udp.getLocalPort();
                try (ServerSocket tcp = new ServerSocket()) {
                    tcp.bind(new InetSocketAddress(wildcard, port));
                    return port;
                }
            } catch (IOException e) {
                lastFailure = e;
            }
        }
        throw new IOException("No port free for both TCP and UDP after " + MAX_PORT_ATTEMPTS + " attempts",
                lastFailure);
    }
}
