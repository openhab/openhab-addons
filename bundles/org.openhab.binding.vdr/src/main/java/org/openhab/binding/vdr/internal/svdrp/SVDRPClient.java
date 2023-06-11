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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SVDRPClient} encapsulates all calls to the SVDRP interface of a VDR
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public interface SVDRPClient {

    /**
     *
     * Open VDR Socket Connection
     *
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    public void openConnection() throws SVDRPConnectionException, SVDRPParseResponseException;

    /**
     * Close VDR Socket Connection
     *
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    public void closeConnection() throws SVDRPConnectionException, SVDRPParseResponseException;

    /**
     * Retrieve Disk Status from SVDRP Client
     *
     * @return SVDRP Disk Status
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    public SVDRPDiskStatus getDiskStatus() throws SVDRPConnectionException, SVDRPParseResponseException;

    /**
     * Retrieve EPG Event from SVDRPClient
     *
     * @param type Type of EPG Event (now, next)
     * @return SVDRP EPG Event
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    public SVDRPEpgEvent getEpgEvent(SVDRPEpgEvent.TYPE type)
            throws SVDRPConnectionException, SVDRPParseResponseException;

    /**
     * Retrieve current volume from SVDRP Client
     *
     * @return SVDRP Volume Object
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    public SVDRPVolume getSVDRPVolume() throws SVDRPConnectionException, SVDRPParseResponseException;

    /**
     * Set volume on SVDRP Client
     *
     * @param newVolume Volume in Percent
     * @return SVDRP Volume Object
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    public SVDRPVolume setSVDRPVolume(int newVolume) throws SVDRPConnectionException, SVDRPParseResponseException;

    /**
     * Send Key command to SVDRP Client
     *
     * @param key Key Command to send
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    public void sendSVDRPKey(String key) throws SVDRPConnectionException, SVDRPParseResponseException;

    /**
     * Send Message to SVDRP Client
     *
     * @param message Message to send
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    public void sendSVDRPMessage(String message) throws SVDRPConnectionException, SVDRPParseResponseException;

    /**
     * Retrieve current Channel from SVDRP Client
     *
     * @return SVDRPChannel object
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    public SVDRPChannel getCurrentSVDRPChannel() throws SVDRPConnectionException, SVDRPParseResponseException;

    /**
     * Change current Channel on SVDRP Client
     *
     * @param number Channel to be set
     * @return SVDRPChannel object
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    public SVDRPChannel setSVDRPChannel(int number) throws SVDRPConnectionException, SVDRPParseResponseException;

    /**
     * Retrieve from SVDRP Client if a recording is currently active
     *
     * @return is currently a recording active
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    public boolean isRecordingActive() throws SVDRPConnectionException, SVDRPParseResponseException;

    /**
     * Retrieve VDR Version from SVDRP Client
     *
     * @return VDR Version
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    public String getSVDRPVersion();
}
