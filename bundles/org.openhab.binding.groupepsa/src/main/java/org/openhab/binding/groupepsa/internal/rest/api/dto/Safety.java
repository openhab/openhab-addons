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
package org.openhab.binding.groupepsa.internal.rest.api.dto;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class Safety {

    private @Nullable ZonedDateTime createdAt;
    private @Nullable String beltWarning;
    private @Nullable String eCallTriggeringRequest;

    public @Nullable ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public @Nullable String getBeltWarning() {
        return beltWarning;
    }

    public @Nullable String getECallTriggeringRequest() {
        return eCallTriggeringRequest;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("createdAt", createdAt).append("beltWarning", beltWarning)
                .append("eCallTriggeringRequest", eCallTriggeringRequest).toString();
    }
}
