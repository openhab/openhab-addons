/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.servletservices.models;

import org.apache.commons.lang.StringUtils;
import org.openhab.io.neeo.internal.models.NeeoDevice;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannel;

/**
 * The class that encapsulates a result of a NEEO Brain call. THe result can be successful or not, include a message and
 * a device
 *
 * @author Tim Roberts - Initial contribution
 */
public class ReturnStatus {

    /** The static success helper */
    public static final ReturnStatus SUCCESS = new ReturnStatus(true);

    /** True if the call was successful, false otherwise */
    private final boolean success;

    /** The optional message if not successful */
    private final String message;

    /** The optional device if successful */
    private final NeeoDevice device;

    /** The optional channel if successful */
    private final NeeoDeviceChannel channel;

    /**
     * Creates a return status of true or not (with no message or device)
     *
     * @param success whether the call was successful
     */
    public ReturnStatus(boolean success) {
        this(success, null, null, null);
    }

    /**
     * Creates a return status of SUCCESS with a device. If device is null, same result as calling 'constructor(true)'
     *
     * @param device the possibly null device
     */
    public ReturnStatus(NeeoDevice device) {
        this(true, null, device, null);
    }

    /**
     * Creates a return status of SUCCESS with a channel. If channel is null, same result as calling 'constructor(true)'
     *
     * @param channel the possibly null channel
     */
    public ReturnStatus(NeeoDeviceChannel channel) {
        this(true, null, null, channel);
    }

    /**
     * Creates the return status with the specified message. If the message is empty, same result as calling
     * 'constructor(true)'. If not empty, success is set to false
     *
     * @param message the possibly null, possibly empty message
     */
    public ReturnStatus(String message) {
        this(StringUtils.isEmpty(message), message, null, null);
    }

    /**
     * Creates the return status with the specified message and success indicator
     *
     * @param success true if successful, false otherwise
     * @param message the possibly null, possibly empty message
     */
    public ReturnStatus(boolean success, String message) {
        this(success, message, null, null);
    }

    /**
     * Creates the returns status for the success, message and optional device or channel.
     *
     * @param success whether the call was successful
     * @param message the possibly null, possibly empty message
     * @param device the possibly null device
     * @param channel the possibly null channel
     */
    private ReturnStatus(boolean success, String message, NeeoDevice device, NeeoDeviceChannel channel) {
        this.success = success;
        this.message = message;
        this.device = device;
        this.channel = channel;
    }

    /**
     * Checks if the call was successful
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the related message
     *
     * @return the possibly empty, possibly null message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the related device
     *
     * @return the possibly null device
     */
    public NeeoDevice getDevice() {
        return device;
    }

    /**
     * Gets the related channel
     *
     * @return the possibly null channel
     */
    public NeeoDeviceChannel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "ReturnStatus [success=" + success + ", message=" + message + ", device=" + device + ", channel="
                + channel + "]";
    }
}
