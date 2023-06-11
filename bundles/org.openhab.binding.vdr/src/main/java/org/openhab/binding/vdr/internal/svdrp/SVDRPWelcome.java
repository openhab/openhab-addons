/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SVDRPWelcome} contains SVDRP Response Data that is sent after Connection has been established
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class SVDRPWelcome {

    private String version = "";
    private String charset = "";
    private String dateAndTime = "";

    private SVDRPWelcome() {
    }

    /**
     * Parse SVDRPResponse into SVDRPWelcome Object
     *
     * @param message SVDRP Client Response
     *            Example: VDRHOST SVDRP VideoDiskRecorder 2.4.5; Sat Jan 9 22:28:11 2021; UTF-8
     * @return Welcome Object
     * @throws SVDRPParseResponseException thrown if response data is not parseable
     */
    public static SVDRPWelcome parse(String message) throws SVDRPParseResponseException {
        SVDRPWelcome welcome = new SVDRPWelcome();
        StringTokenizer st = new StringTokenizer(message, ";");
        try {
            String hostAndVersion = st.nextToken();
            String dateAndTime = st.nextToken();
            String charset = st.nextToken();
            welcome.setCharset(charset.trim());
            welcome.setVersion(hostAndVersion.substring(hostAndVersion.lastIndexOf(" ")).trim());
            welcome.setDateAndTime(dateAndTime.trim());
        } catch (NoSuchElementException nex) {
            throw new SVDRPParseResponseException(nex.getMessage(), nex);
        }
        return welcome;
    }

    /**
     * Get VDR version
     *
     * @return VDR version String
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set VDR version
     *
     * @param version VDR version String
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get VDR Charset
     *
     * @return VDR charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Set VDR Charset
     *
     * @param charset VDR charset
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * Get VDR Date and Time String
     *
     * @return VDR Date and Time String
     */
    public String getDateAndTime() {
        return dateAndTime;
    }

    /**
     * Set VDR Date and Time String
     *
     * @param dateAndTime VDR Date and Time String
     */
    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }
}
