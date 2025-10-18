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
package org.openhab.binding.unifiprotect.internal.media;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builds a minimal go2rtc.yaml for a camera
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class Go2RtcConfigBuilder {

    private String listenHost = "127.0.0.1";
    private int listenPort = 1984;
    private final List<String> stunServers = new ArrayList<>(List.of("stun:stun.cloudflare.com:3478",
            "stun:stun.l.google.com:19302", "stun:global.stun.twilio.com:3478"));
    private final List<String> candidates = new ArrayList<>();
    private int webrtcListenPort = 8555;
    private int rtspListenPort = 8554;

    private final Map<String, List<String>> streams = new LinkedHashMap<>();
    private String ffmpegOptions = "-use_wallclock_as_timestamps 1 -re -fflags nobuffer -f alaw -ar 8000 -ac 1 -i - -vn -b:a 32k -application voip -frame_duration 20";
    private String ffmpegOutputOptions = "-acodec lib%s -ar %d -sample_fmt s%d  -f rtp %s#backchannel=1";

    public Go2RtcConfigBuilder(String host, int port) {
        this.listenHost = host;
        this.listenPort = port;
    }

    public Go2RtcConfigBuilder ffmpegOptions(String ffmpegOptions) {
        this.ffmpegOptions = ffmpegOptions;
        return this;
    }

    public Go2RtcConfigBuilder stun(String... servers) {
        this.stunServers.clear();
        this.stunServers.addAll(List.of(servers));
        return this;
    }

    public Go2RtcConfigBuilder candidates(String... candidates) {
        this.candidates.clear();
        this.candidates.addAll(List.of(candidates));
        return this;
    }

    public Go2RtcConfigBuilder webrtcListen(int port) {
        this.webrtcListenPort = port;
        return this;
    }

    public Go2RtcConfigBuilder rtspListen(int port) {
        this.rtspListenPort = port;
        return this;
    }

    public Go2RtcConfigBuilder addStreams(String streamId, List<URI> sources) {
        if (sources.size() > 1) {
            streams.put(streamId, List.of(formatSourceToRtspx(sources.get(0)), buildBackchannelExec(sources.get(1))));
        } else {
            streams.put(streamId, List.of(formatSourceToRtspx(sources.get(0))));
        }
        return this;
    }

    public Go2RtcConfigBuilder removeStream(String streamId) {
        streams.remove(streamId);
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("log:\n");
        sb.append("  format: \"color\"\n");
        sb.append("  level: \"debug\"\n");
        sb.append("  output: \"stdout\"\n");
        sb.append("  time: \"UNIXMS\"\n\n");
        // api
        sb.append("api:\n");
        sb.append("  listen: ").append(listenHost).append(":").append(listenPort).append("\n\n");

        // webrtc
        sb.append("webrtc:\n");
        sb.append("  listen: \"").append(":").append(webrtcListenPort).append("\"\n");
        sb.append("  ice_servers:\n");
        if (!stunServers.isEmpty()) {
            sb.append("    - urls: [ ");
            for (int i = 0; i < stunServers.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(quoteIfNeeded(stunServers.get(i)));
            }
            sb.append(" ]\n");
        }
        if (!candidates.isEmpty()) {
            sb.append("  candidates:\n");
            for (String c : candidates) {
                sb.append("    - ").append(quoteIfNeeded(c)).append("\n");
            }
        }
        sb.append("\n");

        // rtsp
        sb.append("rtsp:\n");
        sb.append("  listen: \"").append(":").append(rtspListenPort).append("\"\n\n");

        // streams
        sb.append("streams:\n");
        if (streams.isEmpty()) {
            sb.append("  {}\n");
            return sb.toString();
        }
        for (Map.Entry<String, List<String>> e : streams.entrySet()) {
            sb.append("  ").append(e.getKey()).append(":\n");
            for (String src : e.getValue()) {
                sb.append("    - ").append(quoteIfNeeded(src)).append("\n");
            }
        }
        return sb.toString();
    }

    // this handles the strange rtp vs rtps with unifi protect streams
    private String formatSourceToRtspx(URI u) {
        String host = u.getHost();
        if (host == null) {
            String auth = u.getAuthority(); // e.g., user@host:port
            if (auth != null) {
                int at = auth.lastIndexOf('@');
                String h = (at >= 0) ? auth.substring(at + 1) : auth;
                int colon = h.lastIndexOf(':');
                host = (colon >= 0) ? h.substring(0, colon) : h;
            } else {
                host = "";
            }
        }
        String outHost = host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
        String rawPath = u.getRawPath();
        String token;
        if (rawPath == null || rawPath.isEmpty() || "/".equals(rawPath)) {
            token = "";
        } else {
            int lastSlash = rawPath.lastIndexOf('/');
            token = rawPath.substring(lastSlash + 1);
        }

        return "rtspx://" + outHost + ":" + u.getPort() + "/" + token + "#backchannel=0";
    }

    // Quote only if YAML would break.
    private String quoteIfNeeded(String s) {
        if (s.isEmpty()) {
            return "''";
        }
        // (spaces, #, :, ?, [, ], {, }, & )
        if (s.matches("^[A-Za-z0-9._:/?&=+%-]+$")) {
            return s;
        }
        return "'" + s.replace("'", "''") + "'";
    }

    public String buildBackchannelExec(URI rtpUri) {
        // Exec command reads PCM s16be 8kHz from stdin, encodes Opus 24kHz mono @32k,
        return "exec:ffmpeg " + ffmpegOptions + " "
                + String.format(ffmpegOutputOptions, "opus", 24000, 16, rtpUri.toString());
    }
}
