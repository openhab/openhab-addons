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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class AirConditioning {

    private @Nullable String failureCause;
    private @Nullable List<Program> programs = null;
    private @Nullable String status;
    private @Nullable ZonedDateTime updatedAt;

    public @Nullable String getFailureCause() {
        return failureCause;
    }

    public @Nullable List<Program> getPrograms() {
        return programs;
    }

    public @Nullable String getStatus() {
        return status;
    }

    public @Nullable ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("failureCause", failureCause).append("programs", programs)
                .append("status", status).append("updatedAt", updatedAt).toString();
    }
}
