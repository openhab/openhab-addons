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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Parser for the subset of SDP required for RTP talkback.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class SipSdpParser {

    private static final int DEFAULT_CLOCK_RATE = 8000;
    private static final int DEFAULT_PTIME_MS = 20;

    private static final String CODEC_PCMA = "PCMA";
    private static final String CODEC_PCMU = "PCMU";
    private static final int DEFAULT_VIDEO_PAYLOAD_TYPE = 96;
    private static final int DEFAULT_VIDEO_PORT = 30000;
    private static final String DEFAULT_VIDEO_FRAMERATE = "25.000000";

    public Optional<SipAudioOffer> parseAudioOffer(@Nullable String sdp) {
        if (sdp == null || sdp.isBlank()) {
            return Optional.empty();
        }

        String sessionHost = null;
        String mediaHost = null;
        int mediaPort = -1;
        List<Integer> offeredPayloadTypes = new ArrayList<>();
        int ptimeMs = DEFAULT_PTIME_MS;
        boolean inAudioSection = false;
        Map<Integer, String> payloadCodecs = new HashMap<>();
        Map<Integer, Integer> payloadClockRates = new HashMap<>();

        String[] lines = sdp.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("m=")) {
                inAudioSection = line.startsWith("m=audio ");
                if (inAudioSection) {
                    String[] parts = line.substring(2).split("\\s+");
                    if (parts.length >= 4 && "RTP/AVP".equalsIgnoreCase(parts[2])) {
                        mediaPort = parseInt(parts[1], -1);
                        offeredPayloadTypes.clear();
                        for (int i = 3; i < parts.length; i++) {
                            int candidatePt = parseInt(parts[i], -1);
                            if (candidatePt >= 0) {
                                offeredPayloadTypes.add(candidatePt);
                            }
                        }
                    }
                }
                continue;
            }

            if (line.startsWith("c=")) {
                @Nullable
                String host = parseConnectionHost(line);
                if (host != null) {
                    if (inAudioSection) {
                        mediaHost = host;
                    } else {
                        sessionHost = host;
                    }
                }
                continue;
            }

            if (!inAudioSection) {
                continue;
            }

            if (line.startsWith("a=ptime:")) {
                ptimeMs = Math.max(10, parseInt(line.substring("a=ptime:".length()), DEFAULT_PTIME_MS));
                continue;
            }

            if (line.startsWith("a=rtpmap:")) {
                String value = line.substring("a=rtpmap:".length()).trim();
                int sep = value.indexOf(' ');
                if (sep > 0 && sep < value.length() - 1) {
                    int mappedPayloadType = parseInt(value.substring(0, sep), -1);
                    String codecInfo = value.substring(sep + 1);
                    String[] codecParts = codecInfo.split("/");
                    if (codecParts.length >= 2 && mappedPayloadType >= 0) {
                        payloadCodecs.put(mappedPayloadType, codecParts[0].toUpperCase(Locale.ROOT));
                        int clockRate = parseInt(codecParts[1], DEFAULT_CLOCK_RATE);
                        payloadClockRates.put(mappedPayloadType, clockRate);
                    }
                }
            }
        }

        String remoteHost = mediaHost != null ? mediaHost : sessionHost;
        if (remoteHost == null || remoteHost.isBlank() || mediaPort <= 0 || offeredPayloadTypes.isEmpty()) {
            return Optional.empty();
        }

        int payloadType = -1;
        @Nullable
        String codecName = null;
        for (int offeredPt : offeredPayloadTypes) {
            String offeredCodec = resolveCodecName(offeredPt, payloadCodecs);
            if (isSupportedCodec(offeredCodec)) {
                payloadType = offeredPt;
                codecName = offeredCodec;
                break;
            }
        }

        if (payloadType < 0 || codecName == null) {
            return Optional.empty();
        }

        int clockRate = payloadClockRates.getOrDefault(payloadType, DEFAULT_CLOCK_RATE);
        return Optional.of(new SipAudioOffer(remoteHost, mediaPort, payloadType, codecName, clockRate, ptimeMs));
    }

    public Optional<String> buildAnswerSdp(@Nullable String inviteSdp, String localIp, int localAudioPort) {
        Optional<SipAudioOffer> parsedOffer = parseAudioOffer(inviteSdp);
        if (parsedOffer.isEmpty()) {
            return Optional.empty();
        }

        SipAudioOffer offer = parsedOffer.get();
        int advertisedAudioPort = localAudioPort > 0 ? localAudioPort : 0;
        int audioPayloadType = offer.getPayloadType();
        String audioCodecName = offer.getCodecName();
        int audioClockRate = offer.getClockRate();
        int videoPayloadType = parseVideoPayloadType(inviteSdp).orElse(DEFAULT_VIDEO_PAYLOAD_TYPE);
        String videoFramerate = DEFAULT_VIDEO_FRAMERATE;
        long sessionId = Math.max(1L, System.currentTimeMillis() / 1000L);
        StringBuilder answer = new StringBuilder(256);
        answer.append("v=0\r\n");
        answer.append("o=- ").append(sessionId).append(" 1 IN IP4 ").append(localIp).append("\r\n");
        answer.append("s=Dahua VT 1.5\r\n");
        answer.append("c=IN IP4 ").append(localIp).append("\r\n");
        answer.append("t=0 0\r\n");
        answer.append("m=audio ").append(advertisedAudioPort).append(" RTP/AVP ").append(audioPayloadType)
                .append("\r\n");
        answer.append("a=rtpmap:").append(audioPayloadType).append(" ").append(audioCodecName).append("/")
                .append(audioClockRate).append("\r\n");
        answer.append("a=sendrecv\r\n");
        answer.append("m=video ").append(DEFAULT_VIDEO_PORT).append(" RTP/AVP ").append(videoPayloadType)
                .append("\r\n");
        answer.append("a=framerate:").append(videoFramerate).append("\r\n");
        answer.append("a=rtpmap:").append(videoPayloadType).append(" H264/90000\r\n");
        answer.append("a=recvonly\r\n");

        return Optional.of(answer.toString());
    }

    private OptionalInt parseVideoPayloadType(@Nullable String sdp) {
        if (sdp == null || sdp.isBlank()) {
            return OptionalInt.empty();
        }

        boolean inVideoSection = false;
        for (String rawLine : sdp.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("m=")) {
                inVideoSection = line.startsWith("m=video ");
                if (inVideoSection) {
                    String[] parts = line.substring(2).split("\\s+");
                    if (parts.length >= 4 && "RTP/AVP".equalsIgnoreCase(parts[2])) {
                        return OptionalInt.of(parseInt(parts[3], DEFAULT_VIDEO_PAYLOAD_TYPE));
                    }
                }
                continue;
            }
            if (!inVideoSection) {
                continue;
            }
        }
        return OptionalInt.empty();
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String resolveCodecName(int payloadType, Map<Integer, String> payloadCodecs) {
        @Nullable
        String mapped = payloadCodecs.get(payloadType);
        if (mapped != null) {
            return mapped;
        }
        if (payloadType == 8) {
            return CODEC_PCMA;
        }
        if (payloadType == 0) {
            return CODEC_PCMU;
        }
        return "";
    }

    private static boolean isSupportedCodec(String codecName) {
        return CODEC_PCMA.equals(codecName) || CODEC_PCMU.equals(codecName);
    }

    private static @Nullable String parseConnectionHost(String connectionLine) {
        String[] parts = connectionLine.substring(2).trim().split("\\s+");
        if (parts.length < 3) {
            return null;
        }
        return parts[2];
    }
}
