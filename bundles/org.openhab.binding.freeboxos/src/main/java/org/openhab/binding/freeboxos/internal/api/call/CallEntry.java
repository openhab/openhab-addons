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
package org.openhab.binding.freeboxos.internal.api.call;

import java.time.ZonedDateTime;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.CallType;

/**
 * The {@link CallEntry} is the Java class used to map the "CallEntry" structure used by the call API
 *
 * https://dev.freebox.fr/sdk/os/call/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CallEntry {
    private CallType type = CallType.UNKNOWN;
    private @Nullable ZonedDateTime datetime; // Call creation timestamp.
    private @Nullable String number; // Calling or called number
    private @Nullable String name;
    private int duration; // Call duration in seconds.

    public CallType getType() {
        return (type == CallType.MISSED && duration == 0) ? CallType.INCOMING : type;
    }

    public ZonedDateTime getDatetime() {
        return Objects.requireNonNull(datetime);
    }

    public String getPhoneNumber() {
        return Objects.requireNonNull(number);
    }

    public @Nullable String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }
}
