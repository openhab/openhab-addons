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
package org.openhab.binding.ntfy.internal.models;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a message event returned by the ntfy service.
 * <p>
 * Contains fields such as the sequence id, expiration time, message body and priority.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class MessageEvent extends BaseEvent {
    @SerializedName("sequence_id")
    private String sequenceId = "";

    @SerializedName("expires")
    private Instant expires = Instant.MIN;

    @SerializedName("message")
    private String message = "";

    @SerializedName("priority")
    private int priority;

    /**
     * Returns the sequence identifier for this message.
     *
     * @return the sequence id
     */
    public String getSequenceId() {
        return sequenceId;
    }

    /**
     * Sets the sequence identifier for this message.
     *
     * @param sequenceId the sequence id
     */
    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    /**
     * Returns the expiry timestamp for the message, if provided.
     *
     * @return expiry as {@link Instant}
     */
    public Instant getExpires() {
        return expires;
    }

    /**
     * Sets the expiry timestamp for the message.
     *
     * @param expires the expiry instant
     */
    public void setExpires(Instant expires) {
        this.expires = expires;
    }

    /**
     * Returns the message body.
     *
     * @return the message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message body.
     *
     * @param message the message text
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the numeric priority of the message.
     *
     * @return priority as int
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the numeric priority of the message.
     *
     * @param priority the priority value
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
