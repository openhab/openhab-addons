/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SVDRPEpgEvent} contains SVDRP Response Data for an EPG Event
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class SVDRPEpgEvent {

    public enum TYPE {
        NOW,
        NEXT
    }

    private String title = "";
    private String subtitle = "";
    private Instant begin = Instant.now();
    private Instant end = Instant.now();
    private int duration;

    private SVDRPEpgEvent() {
    }

    /**
     * parse object from SVDRP Client Response
     *
     * @param message SVDRP Client Response
     * @return SVDRPEpgEvent Object
     * @throws SVDRPParseResponseException thrown if response data is not parseable
     */
    public static SVDRPEpgEvent parse(String message) throws SVDRPParseResponseException {
        SVDRPEpgEvent entry = new SVDRPEpgEvent();
        StringTokenizer st = new StringTokenizer(message, System.lineSeparator());

        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            if (line.length() >= 1 && !line.startsWith("End")) {
                switch (line.charAt(0)) {
                    case 'T':
                        entry.setTitle(line.substring(1).trim());
                        break;
                    case 'S':
                        entry.setSubtitle(line.substring(1).trim());
                        break;
                    case 'E':
                        StringTokenizer lt = new StringTokenizer(line.substring(1).trim(), " ");
                        lt.nextToken(); // event id
                        try {
                            long begin = Long.parseLong(lt.nextToken());
                            entry.setBegin(Instant.ofEpochSecond(begin));
                        } catch (NumberFormatException | NoSuchElementException e) {
                            throw new SVDRPParseResponseException("Begin: " + e.getMessage(), e);
                        }
                        try {
                            entry.setDuration(Integer.parseInt(lt.nextToken()) / 60);
                        } catch (NumberFormatException | NoSuchElementException e) {
                            throw new SVDRPParseResponseException("Duration: " + e.getMessage(), e);
                        }
                        entry.setEnd(entry.getBegin().plus(entry.getDuration(), ChronoUnit.MINUTES));
                    default:
                        break;
                }
            } else if (!line.startsWith("End")) {
                throw new SVDRPParseResponseException("EPG Event Line corrupt: " + line);
            }
        }

        return entry;
    }

    /**
     * Get Title of EPG Event
     *
     * @return Event Title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set Title of EPG Event
     *
     * @param title Event Title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get Subtitle of EPG Event
     *
     * @return Event Subtitle
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * Set Subtitle of EPG Event
     *
     * @param subtitle Event Subtitle
     */
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * Get Begin of EPG Event
     *
     * @return Event Begin
     */
    public Instant getBegin() {
        return begin;
    }

    /**
     * Set Begin of EPG Event
     *
     * @param begin Event Begin
     */
    public void setBegin(Instant begin) {
        this.begin = begin;
    }

    /**
     * Get End of EPG Event
     *
     * @return Event End
     */
    public Instant getEnd() {
        return end;
    }

    /**
     * Set End of EPG Event
     *
     * @param end Event End
     */
    public void setEnd(Instant end) {
        this.end = end;
    }

    /**
     * Get Duration of EPG Event in Minutes
     *
     * @return Event Duration in Minutes
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Set Duration of EPG Event in Minutes
     *
     * @param duration Event Duration in Minutes
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * String Representation of SVDRPDiskStatus Object
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Title: ");
        sb.append(title);
        sb.append(System.lineSeparator());

        sb.append("Subtitle: ");
        sb.append(subtitle);
        sb.append(System.lineSeparator());

        sb.append("Begin: ");
        sb.append(begin);
        sb.append(System.lineSeparator());

        sb.append("End: ");
        sb.append(end);
        sb.append(System.lineSeparator());

        if (duration > -1) {
            sb.append("Duration: ");
            sb.append(duration);
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
