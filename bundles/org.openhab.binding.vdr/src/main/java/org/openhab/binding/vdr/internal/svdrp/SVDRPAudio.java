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
package org.openhab.binding.vdr.internal.svdrp;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SVDRPAudio} contains SVDRP Response Data for Audio Tracks for Current Channel
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class SVDRPAudio {

    private static final Pattern PATTERN_AUDIO = Pattern.compile("([0-9]{1,2}) (\\S+) ([*]?)(.*)");

    private List<SVDRPAudioTrack> tracks = new ArrayList<SVDRPAudioTrack>();

    /**
     * parse object from SVDRP Client Response
     *
     * @param message SVDRP Client Response
     * @return Audio Object
     * @throws SVDRPParseResponseException thrown if response data is not parseable
     */
    public static SVDRPAudio parse(String message) throws SVDRPParseResponseException {
        SVDRPAudio audio = new SVDRPAudio();
        StringTokenizer st = new StringTokenizer(message, System.lineSeparator());
        while (st.hasMoreTokens()) {
            Matcher matcher = PATTERN_AUDIO.matcher(st.nextToken());
            if (matcher.find() && matcher.groupCount() == 4) {
                boolean active = false;
                if ("*".equals(matcher.group(3))) {
                    active = true;
                }
                SVDRPAudioTrack track = new SVDRPAudioTrack(Integer.parseInt(matcher.group(1)), matcher.group(2),
                        matcher.group(4), active);
                audio.getAudioTracks().add(track);
            }
        }
        if (audio.getAudioTracks().isEmpty()) {
            throw new SVDRPParseResponseException("Audio could not be parsed correctly: " + message);
        }

        return audio;
    }

    /**
     *
     * @return
     */
    public List<SVDRPAudioTrack> getAudioTracks() {
        return tracks;
    }

    /**
     *
     * @return
     */
    public int getActiveTrackNumber() throws SVDRPParseResponseException {
        for (SVDRPAudioTrack t : tracks) {
            if (t.isActive()) {
                return t.getId();
            }
        }
        throw new SVDRPParseResponseException("No active audio track found");
    }
}
