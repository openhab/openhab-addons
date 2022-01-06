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
package org.openhab.binding.volvooncall.internal.dto;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PostResponse} is responsible for storing
 * elements given back after a post to VOC api
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PostResponse extends VocAnswer {

    public static enum Status {
        @SerializedName("Started")
        STARTED,
        @SerializedName("MessageDelivered")
        DELIVERED,
        @SerializedName("Failed")
        FAILED,
        @SerializedName("Successful")
        SUCCESSFULL
    }

    public static enum ServiceType {
        RHBLF, // Remote Honk and Blink Lights ?
        RDU, // Remote door unlock
        ERS, // Remote engine start
        TN // Theft notification
    }

    public @NonNullByDefault({}) Status status;
    public @NonNullByDefault({}) String vehicleId;
    @SerializedName("service")
    public @NonNullByDefault({}) String serviceURL;
    public @NonNullByDefault({}) ServiceType serviceType;
    public @NonNullByDefault({}) ZonedDateTime startTime;
    /*
     * Currently unused in the binding, maybe interesting in the future
     *
     * public static enum FailureReason {
     *
     * @SerializedName("TimeframePassed")
     * TIME_FRAME_PASSED
     * }
     *
     * private ZonedDateTime statusTimestamp;
     * 
     * private FailureReason failureReason;
     *
     * private Integer customerServiceId;
     */
}
