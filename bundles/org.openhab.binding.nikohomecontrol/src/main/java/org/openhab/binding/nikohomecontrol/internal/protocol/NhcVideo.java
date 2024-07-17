/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcVideo} class represents a Niko Home Control 2 video door station device. It is used in conjunction
 * with NhcAccess2 to capture the bell signal on a video door station for access control.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public abstract class NhcVideo {
    private final Logger logger = LoggerFactory.getLogger(NhcVideo.class);

    protected NikoHomeControlCommunication nhcComm;

    private final String id;
    private String name;

    private @Nullable String macAddress;
    protected Map<Integer, String> callStatus = new ConcurrentHashMap<>();
    protected Map<Integer, NhcAccess> nhcAccessMap = new ConcurrentHashMap<>();

    private boolean supportsVideoStream;

    private @Nullable String ipAddress;
    private @Nullable String mjpegUri;
    private @Nullable String tnUri;

    protected NhcVideo(String id, String name, @Nullable String macAddress, @Nullable String ipAddress,
            @Nullable String mjpegUri, @Nullable String tnUri, boolean supportsVideoStream,
            NikoHomeControlCommunication nhcComm) {
        this.id = id;
        this.name = name;
        this.nhcComm = nhcComm;

        this.macAddress = macAddress;

        if (ipAddress != null) {
            try {
                // receiving IPv4 address as string with format L.00, convert to classic format for ip address
                this.ipAddress = InetAddress.getByName(ipAddress.split("\\.")[0]).getHostAddress();
            } catch (UnknownHostException e) {
                logger.debug("invalid ip address {} for video device {}", ipAddress, id);
            }
        }
        this.mjpegUri = mjpegUri;
        this.tnUri = tnUri;
        this.supportsVideoStream = supportsVideoStream;
    }

    @Nullable
    NhcAccess getNhcAccess(int buttonIndex) {
        return nhcAccessMap.get(buttonIndex);
    }

    /**
     * Create a link between a specific video device button and an access device
     *
     * @param buttonIndex
     * @param nhcAccess
     */
    public void setNhcAccess(int buttonIndex, NhcAccess nhcAccess) {
        nhcAccessMap.put(buttonIndex, nhcAccess);
    }

    /**
     * Remove the link to an access device for a specific video device button, to be called when an access device is
     * removed from the system.
     *
     * @param buttonIndex
     */
    public void removeNhcAccess(int buttonIndex) {
        nhcAccessMap.remove(buttonIndex);
    }

    /**
     * @return MAC address of the video device
     */
    public @Nullable String getMacAddress() {
        return macAddress;
    }

    /**
     * @return IP address of the video device
     */
    public @Nullable String getIpAddress() {
        return ipAddress;
    }

    /**
     * @return URI for the MJPEG stream on the video device
     */
    public @Nullable String getMjpegUri() {
        return mjpegUri;
    }

    /**
     * @return URI for static images on the video device
     */
    public @Nullable String getTnUri() {
        return tnUri;
    }

    /**
     * Get the id of the access control device.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Get name of the access control device.
     *
     * @return access control name
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of the access control device.
     *
     * @param name access control name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return true if the video device supports streaming video
     */
    public boolean supportsVideoStream() {
        return supportsVideoStream;
    }

    /**
     * Method called when video device is removed from the Niko Home Control Controller.
     */
    public void videoDeviceRemoved() {
        nhcAccessMap.forEach((buttonIndex, nhcAccess) -> {
            nhcAccess.setNhcVideo(null);
        });
        nhcAccessMap.clear();
        logger.debug("video device removed {}, {}", id, name);
    }

    /**
     * Ring the bell, send bell message to the controller
     *
     * @param buttonIndex
     */
    public void executeBell(int buttonIndex) {
        logger.debug("execute video bell {} for button {}", id, buttonIndex);
        nhcComm.executeVideoBell(id, buttonIndex);
    }

    /**
     * @param buttonIndex
     * @return state of button, values can be Idle, Ringing or Active
     */
    public @Nullable String getState(int buttonIndex) {
        return callStatus.get(buttonIndex);
    }

    /**
     * Update the call state with the call state received for a specific button
     *
     * @param buttonIndex
     * @param callStatus
     */
    public abstract void updateState(int buttonIndex, @Nullable String callStatus);

    /**
     * Update the call state with the call state received for a maximum of 4 buttons on the video device.
     *
     * @param callStatus01
     * @param callStatus02
     * @param callStatus03
     * @param callStatus04
     */
    public void updateState(@Nullable String callStatus01, @Nullable String callStatus02, @Nullable String callStatus03,
            @Nullable String callStatus04) {
        updateState(1, callStatus01);
        updateState(2, callStatus02);
        updateState(3, callStatus03);
        updateState(4, callStatus04);
    }
}
