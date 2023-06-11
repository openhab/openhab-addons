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

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class Program {

    private @Nullable Occurence occurence;
    private @Nullable String recurrence;
    private @Nullable String start;
    private @Nullable Boolean enabled;
    private @Nullable BigDecimal slot;

    public @Nullable Occurence getOccurence() {
        return occurence;
    }

    public @Nullable String getRecurrence() {
        return recurrence;
    }

    public @Nullable String getStart() {
        return start;
    }

    public @Nullable Boolean isEnabled() {
        return enabled;
    }

    public @Nullable BigDecimal getSlot() {
        return slot;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("occurence", occurence).append("recurrence", recurrence)
                .append("start", start).append("enabled", enabled).append("slot", slot).toString();
    }
}
