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
package org.openhab.binding.viessmann.internal.dto.error;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ExtendedPayload} provides the extended payload of a viessmann error message
 *
 * @author Ronny Grun - Initial contribution
 */
public class ExtendedPayload {
    private String reason;
    private String name;
    private Integer requestCountLimit;
    private String clientId;
    private String userId;
    private Long limitReset;
    public String code;
    public String details;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRequestCountLimit() {
        return requestCountLimit;
    }

    public void setRequestCountLimit(Integer requestCountLimit) {
        this.requestCountLimit = requestCountLimit;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getLimitReset() {
        return limitReset;
    }

    public String getLimitResetDateTime() {
        ZonedDateTime d = Instant.ofEpochMilli(limitReset).atZone(ZoneId.systemDefault());
        return d.toLocalDateTime().toString();
    }

    public void setLimitReset(Long limitReset) {
        this.limitReset = limitReset;
    }

    public String getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public @NonNullByDefault String toString() {
        return Objects.requireNonNullElse(reason, "") + " " + Objects.requireNonNullElse(details, "");
    }
}
